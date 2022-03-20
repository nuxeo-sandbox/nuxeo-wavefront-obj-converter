package org.nuxeo.labs.threed.obj;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy("nuxeo-wavefront-obj-converter-core")
public class TestGltfConverter {

    @Inject
    protected CommandLineExecutorService commandLineExecutorService;

    @Inject
    protected ConversionService conversionService;

    @Test
    public void commandLineIsAvailable() {
        Assert.assertTrue(commandLineExecutorService.getCommandAvailability("gltf2glb").isAvailable());
    }

    @Test
    public void converterIsAvailable() {
        Assert.assertTrue(conversionService.isConverterAvailable("gltf2glb").isAvailable());
    }

    @Test
    public void testConversion() {
        File file = new File(getClass().getResource("/files/crown_victoria_taxi.zip").getPath());
        Blob blob = new FileBlob(file,"application/zip");
        BlobHolder bh = new SimpleBlobHolder(blob);
        HashMap<String, Serializable> params = new HashMap<>();
        params.put("targetFileName","taxi.glb");
        BlobHolder conversionResult = conversionService.convert("gltf2glb",bh,params);
        Blob glbBlob = conversionResult.getBlob();
        Assert.assertNotNull(glbBlob);
        Assert.assertEquals("model/gltf-binary",glbBlob.getMimeType());
        Assert.assertEquals("taxi.glb",glbBlob.getFilename());
        Assert.assertTrue(glbBlob.getLength()>0);
    }
}
