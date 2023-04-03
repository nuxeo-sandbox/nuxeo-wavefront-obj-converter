package org.nuxeo.labs.threed.obj.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.labs.threed.obj.adapter.Blob2GlbResourceAdapter;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        //get children sorted by path
        PageProviderService pageProviderService = Framework.getService(PageProviderService.class);
        Map<String, Serializable> props = new HashMap<>();
        props.put(CoreQueryDocumentPageProvider.CORE_SESSION_PROPERTY, (Serializable) doc.getCoreSession());

        @SuppressWarnings("unchecked")
        PageProvider<DocumentModel> pp = (PageProvider<DocumentModel>) pageProviderService.getPageProvider(
                "obj_components", null, null, null, null,
                props, new Object[] { doc.getId() });

        Map<String,Path> folderPaths = new HashMap<>();
        folderPaths.put(doc.getPathAsString(),tmpFolder);

        // loop and create tree
        do {
            List<DocumentModel> children = pp.getCurrentPage();
            for (DocumentModel current : children) {
                Path parent = folderPaths.get(current.getPath().removeLastSegments(1).toString());
                if (current.isFolder()) {
                    File folder = new File(Path.of(parent.toString(), (String) current.getPropertyValue("dc:title")).toString());
                    if(!folder.createNewFile()) {
                        throw new IOException("Could not create tmp folder "+folder.getPath());
                    }
                } else {
                    Blob blob = current.getAdapter(Blob2GlbResourceAdapter.class).getBlob();
                    if (blob != null) {
                        String name = (String) current.getPropertyValue("dc:title");
                        if (name == null) {
                            name = current.getName();
                        }
                        File file = new File(Path.of(parent.toString(), name).toString());
                        if(file.createNewFile()) {
                            try (InputStream in = blob.getStream();OutputStream out = new FileOutputStream(file)) {
                                IOUtils.copy(in, out);
                            }
                        } else {
                            throw new IOException("Could not create tmp file "+file.getPath());
                        }
                    }
                }
            }
            pp.nextPage();
        } while (pp.isNextEntryAvailable());

        //get obj file
        File[] files = tmpFolder.toFile().listFiles();
        if (files == null) {
            throw new NuxeoException("Cannot list tmp file structure");
        }
        Optional<File> objFile = Arrays.stream(files)
                .filter(file -> FilenameUtils.isExtension(file.getName(), "obj")).findFirst();
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
