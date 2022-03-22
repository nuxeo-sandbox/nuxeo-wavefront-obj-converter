package org.nuxeo.labs.threed.obj.service;

import org.apache.commons.io.FilenameUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.io.fsexporter.FSExporterService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class WavefrontObjConversionServiceImpl extends DefaultComponent implements WavefrontObjConversionService {

    public Blob convert(DocumentModel doc) {
        try {
            return doc.isFolder() ? convertFolderish(doc) : convertSingleDocument(doc);
        } catch (IOException | CommandNotAvailable e) {
           throw new NuxeoException(e);
        }
    }

    public Blob convertFolderish(DocumentModel doc) throws IOException, CommandNotAvailable {
        Path tmpFolder = Framework.createTempDirectory("obj_conversion");
        FSExporterService fsExporter = Framework.getService(FSExporterService.class);
        fsExporter.export(doc.getCoreSession(), doc.getPathAsString(), tmpFolder.toString(), null);

        //fs exporters creates a container, need to get its path
        File[] tmpFolderFiles = tmpFolder.toFile().listFiles();
        if (tmpFolderFiles == null || tmpFolderFiles.length != 1 || !tmpFolderFiles[0].isDirectory()) {
            throw new NuxeoException("unknown exported folderish structure");
        }
        Path tmpDocFolder = tmpFolderFiles[0].toPath();

        //get obj file
        Optional<File> objFile = Arrays.stream(tmpDocFolder.toFile().listFiles()).filter(file -> FilenameUtils.isExtension(file.getName(), "obj")).findFirst();
        if (objFile.isEmpty()) {
            throw new NuxeoException("No OBJ file found in structure");
        }

        CommandLineExecutorService cs = Framework.getService(CommandLineExecutorService.class);
        CmdParameters parameters = new CmdParameters();
        parameters.addNamedParameter("sourceFilePath", objFile.get().getAbsolutePath());
        String targetPath = tmpFolder.toFile().getAbsolutePath() + "/preview.glb";
        parameters.addNamedParameter("targetFilePath", targetPath);
        ExecResult result = cs.execCommand("obj2gltf", parameters);
        if (result.isSuccessful()) {
            File glbFile = new File(targetPath);
            if (glbFile.exists()) {
                return new FileBlob(new File(targetPath), "model/gltf-binary");
            }
        }

        throw new NuxeoException("OBJ to GLB conversion failed");
    }

    public Blob convertSingleDocument(DocumentModel doc) {
        Blob blob = (Blob) doc.getPropertyValue("file:content");
        BlobHolder input = new SimpleBlobHolder(blob);
        ConversionService cs = Framework.getService(ConversionService.class);
        HashMap<String, Serializable> params = new HashMap<>();
        params.put("targetFileName", "preview.glb");
        BlobHolder output = cs.convert("obj2gltf", input, params);
        return output.getBlob();
    }

}
