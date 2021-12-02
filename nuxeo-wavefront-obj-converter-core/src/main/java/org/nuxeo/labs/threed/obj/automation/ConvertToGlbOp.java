/*
 * (C) Copyright 2021 Nuxeo (http://nuxeo.com/) and others.
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

package org.nuxeo.labs.threed.obj.automation;

import java.io.IOException;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.labs.threed.obj.service.WavefrontObjConversionService;

@Operation(id = ConvertToGlbOp.ID, category = Constants.CAT_DOCUMENT, label = "Document.ConvertOBJ2GLB", description = "Convert WaveFront OBJ to GLB")
public class ConvertToGlbOp {

    public static final String ID = "Document.ConvertOBJ2GLB";

    @Context
    protected WavefrontObjConversionService wavefrontObjConversionService;

    @OperationMethod
    public Blob run(DocumentModel doc) throws IOException {
        return wavefrontObjConversionService.convert(doc);
    }
}
