package org.nuxeo.labs.threed.obj;

import java.io.File;
import java.io.Serializable;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.labs.threed.obj.automation.ConvertToGlbOp;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "nuxeo-wavefront-obj-converter-core" })
public class TestOperation {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Test
    public void testOperation() throws OperationException {
        Blob blob = new FileBlob(new File(getClass().getResource("/files/suzanne.obj").getPath()), "model/obj");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "File", "File");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);

        OperationContext ctx = new OperationContext();
        ctx.setInput(doc);
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestOperation");
        chain.add(ConvertToGlbOp.ID);

        Blob glbBlob = (Blob) automationService.run(ctx, chain);
        Assert.assertNotNull(glbBlob);
        Assert.assertEquals("model/gltf-binary", glbBlob.getMimeType());
        // Assert.assertEquals("suzanne.glb",glbBlob.getFilename());
        Assert.assertTrue(glbBlob.getLength() > 0);
    }

}
