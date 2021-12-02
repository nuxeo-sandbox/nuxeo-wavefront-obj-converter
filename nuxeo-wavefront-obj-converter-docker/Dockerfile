# vim:set ft=dockerfile:
ARG BASE_IMAGE

FROM ${BASE_IMAGE}

ARG VERSION
ARG SCM_REF
ARG BUILD_TAG
ARG NUXEO_CLID
ARG PACKAGES

LABEL com.customer.sample.version=${VERSION}
LABEL com.customer.sample.scm-ref=${SCM_REF}
LABEL com.customer.sample.build-tag=${BUILD_TAG}

# install node.js and screenshot-glb
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash && \
    source ~/.bash_profile && nvm install 16.13.0 && npm install -g obj2gltf

# workaround to make the obj2gltf command available in the default system PATH used by Java
USER 0
RUN ln -s /home/nuxeo/.nvm/versions/node/v16.13.0/bin/node /usr/local/bin/node
RUN ln -s /home/nuxeo/.nvm/versions/node/v16.13.0/bin/obj2gltf /usr/local/bin/obj2gltf
USER 900

COPY --chown=nuxeo:0 target/nuxeo-wavefront-obj-converter-package-*.zip /home/nuxeo/local-packages/nuxeo-wavefront-obj-converter-package.zip

RUN /install-packages.sh --offline /home/nuxeo/local-packages/nuxeo-wavefront-obj-converter-package.zip

RUN /install-packages.sh --clid ${NUXEO_CLID} ${PACKAGES}