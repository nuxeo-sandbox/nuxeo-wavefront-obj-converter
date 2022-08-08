nuxeo-wavefront-obj-converter
===================

A plugin that adds wavefront OBJ to GLB file format conversion capabilities

## List of Features (details below)

- OBJ to GLB converter using [obj2gltf](https://github.com/CesiumGS/obj2gltf)
- GLTF to GLB converter using [gltf-pipeline](https://github.com/CesiumGS/gltf-pipeline)
- an automation operation to convert a single document holding a simple OBJ file or a folder containing the OBJ file and other resources as individual documents

## Build

Assuming maven is correctly setup on your computer:

```
git clone https://github.com/nuxeo-sandbox/nuxeo-wavefront-obj-converter
cd nuxeo-wavefront-obj-converter
mvn clean install
```

To build the plugin without building the Docker image, use:

```
mvn -DskipDocker=true clean install
```

## Install

Install the package on your instance.

This plugin relies on [obj2gltf](https://github.com/CesiumGS/obj2gltf) and [gltf-pipeline](https://github.com/CesiumGS/gltf-pipeline) which must be installed on your nuxeo server performing the conversion. 
Have a look at the repository [Dockerfile](https://github.com/nuxeo-sandbox/nuxeo-wavefront-obj-converter/blob/master/nuxeo-wavefront-obj-converter-docker/Dockerfile) to find more details about the installation steps.

# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo

Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/), and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com).
