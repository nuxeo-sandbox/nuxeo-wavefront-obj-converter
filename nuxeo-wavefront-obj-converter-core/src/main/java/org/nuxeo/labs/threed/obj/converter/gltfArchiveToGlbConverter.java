/*
 * (C) Copyright 2022 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Michael Vachette
 */

package org.nuxeo.labs.threed.obj.converter;

import org.apache.commons.io.FilenameUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.platform.convert.plugins.CommandLineConverter;
import org.nuxeo.runtime.api.Framework;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class gltfArchiveToGlbConverter extends CommandLineConverter {

    @Override
    public BlobHolder convert(BlobHolder blobHolder, Map<String, Serializable> parameters) throws ConversionException {

        Blob blob = blobHolder.getBlob();

        Blob gltfBlob = null;

        try {
            Path tmpDirectory = Framework.createTempDirectory("conversion-glb");
            try (ZipInputStream zipIn = new ZipInputStream(blob.getStream())) {
                for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null; ) {
                    Path resolvedPath = tmpDirectory.resolve(ze.getName()).normalize();
                    if (!resolvedPath.startsWith(tmpDirectory)) {
                        // see: https://snyk.io/research/zip-slip-vulnerability
                        throw new RuntimeException("Entry with an illegal path: " + ze.getName());
                    }
                    if (ze.isDirectory()) {
                        Files.createDirectories(resolvedPath);
                    } else {
                        Files.createDirectories(resolvedPath.getParent());
                        Files.copy(zipIn, resolvedPath);
                        if (FilenameUtils.isExtension(resolvedPath.getFileName().toString(),"gltf")) {
                            gltfBlob = new FileBlob(resolvedPath.toFile(),"model/gltf");
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new NuxeoException(e);
        }

        if (gltfBlob!=null) {
            return super.convert(new SimpleBlobHolder(gltfBlob),parameters);
        } else {
            throw new NuxeoException("couldn't find gltf file");
        }
    }
}
