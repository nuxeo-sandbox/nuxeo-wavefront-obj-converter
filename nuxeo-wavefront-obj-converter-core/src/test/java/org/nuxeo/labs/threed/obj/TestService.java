package org.nuxeo.labs.threed.obj;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.threed.obj.service.WavefrontObjConversionService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy({"nuxeo-wavefront-obj-converter-core","nuxeo-fsexporter"})
public class TestService {

    @Inject
    protected CoreSession session;

    @Inject
    protected WavefrontObjConversionService wavefrontobjconversionservice;

    @Inject
    protected TransactionalFeature transactionalFeature;

    @Test
    public void testService() {
        assertNotNull(wavefrontobjconversionservice);
    }

    @Test
    public void testFolderishConversion() throws IOException {
        DocumentModel folder = session.createDocumentModel(session.getRootDocument().getPathAsString(),"Folder","Folder");
        folder = session.createDocument(folder);

        File file = new File(getClass().getResource("/files/MMSEV.zip").getPath());
        ZipFile zipFile = new ZipFile(file);
        Iterator<ZipEntry> entries = (Iterator<ZipEntry>) zipFile.entries();
        while (entries.hasNext()) {
            ZipEntry entry = entries.next();
            Blob blob = new FileBlob(zipFile.getInputStream(entry));
            blob.setFilename(entry.getName());
            DocumentModel doc = session.createDocumentModel(folder.getPathAsString(), entry.getName(), "File");
            doc.setPropertyValue("file:content", (Serializable) blob);
            session.createDocument(doc);
        }

        transactionalFeature.nextTransaction();

        Blob glbBlob = wavefrontobjconversionservice.convert(folder);
        Assert.assertNotNull(glbBlob);
        Assert.assertEquals("model/gltf-binary",glbBlob.getMimeType());
        //Assert.assertEquals("suzanne.glb",glbBlob.getFilename());
        Assert.assertTrue(glbBlob.getLength()>0);
    }

    @Test
    public void testNonFolderishConversion() throws IOException {
        Blob blob = new FileBlob(new File(getClass().getResource("/files/suzanne.obj").getPath()),"model/obj");
        DocumentModel file = session.createDocumentModel(session.getRootDocument().getPathAsString(),"File","File");
        file.setPropertyValue("file:content", (Serializable) blob);
        file = session.createDocument(file);
        Blob glbBlob = wavefrontobjconversionservice.convert(file);
        Assert.assertNotNull(glbBlob);
        Assert.assertEquals("model/gltf-binary",glbBlob.getMimeType());
        //Assert.assertEquals("suzanne.glb",glbBlob.getFilename());
        Assert.assertTrue(glbBlob.getLength()>0);
    }


}
