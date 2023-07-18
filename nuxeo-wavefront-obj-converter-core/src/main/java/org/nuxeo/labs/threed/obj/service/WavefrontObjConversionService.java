package org.nuxeo.labs.threed.obj.service;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.io.IOException;

public interface WavefrontObjConversionService {

    Blob convert(DocumentModel doc);

}
