/*
 * (C) Copyright 2023 Nuxeo (http://nuxeo.com/) and others.
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

package org.nuxeo.labs.threed.obj.adapter;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.runtime.api.Framework;

import java.util.HashMap;

public class Blob2GlbResourceAdapter {

    protected DocumentModel doc;

    public Blob2GlbResourceAdapter(DocumentModel doc) {
        this.doc = doc;
    }

    public Blob getBlob() {
        BlobHolder blobholder = doc.getAdapter(BlobHolder.class);
        if (blobholder != null && blobholder.getBlob() != null) {
            Blob blob = blobholder.getBlob();
            ConversionService conversionService = Framework.getService(ConversionService.class);
            if (conversionService.isSourceMimeTypeSupported("image2glbResource", blob.getMimeType())) {
                BlobHolder conversionResult = conversionService.convert("image2glbResource", new SimpleBlobHolder(blob), new HashMap<>());
                return conversionResult.getBlob();
            } else {
                return blob;
            }
        }
        return null;
    }
}
