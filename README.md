AEM IM2TXT
==========

This AEM project provides the capability
 - to generate the Image Captions using the AEM workflows.
 - to train the Im2txt inception model
 
The following steps provide the details:
 - Setup the AEM instance to interact with im2txt REST api
 - Expose the REST api using Flask - Python Web Framework

PREREQUISITES
==============

1. Using the link below, make sure to install and train the im2txt model

    https://github.com/tensorflow/models/tree/master/im2txt
    
    After the training, you should be able to run and generate the captions (using the below link)
    https://github.com/tensorflow/models/tree/master/im2txt#generating-captions

2. The AEM instance is up and running


REST API
========

To setup the im2txt's model, refer the [im2txt README.md](showandtell/README.md). 
    
BUILD
======

    mvn clean install -PautoInstallPackage
    mvn clean install -PautoInstallBundle


CONFIGURE IM2TXT SERVICE
========================
   
In the AEM Configuration console, http://localhost:4502/system/console/configMgr

Find the service name 'Im2txt Configuration Service'

- Configure the im2txt.service.endpoint = http://0.0.0.0:5000/im2txt
   
*NOTE: Endpoint consists of <PROTOCOL://HOST:PORT>

- Configure the Im2txt Training URI (im2txt.train.uri) = /train

- Configure the Im2txt Process Image URI (im2txt.process.image.uri) = /im2txt

** Refer : showandtell/README.md - REST API, for more details where the REST api is running.

![Alt text](https://github.com/DevDh/aem-im2txt/blob/master/screenshots/im2txt-config-service.png?raw=true "Im2txt Configuration Service")
Refer 


WORKFLOW
=========

Configure any existing or new workflows with the additional Process step, either for im2txt caption generate, or Train the model.

1. Train the model:
For any asset image (any jpeg) 

Example:
http://localhost:4502/mnt/overlay/dam/gui/content/assets/metadataeditor.html/content/dam/geometrixx-outdoors/articles/12-travel-tips.jpg

Click edit, and add/update the Description.
Trigger the workflow with 'Im2txt Train Inception Model Step' included, because by default this step is fetching the caption details from 'Description' field only.


2. Generate The Image Captions

Configure the 'Im2txt Image Caption Generate Step' the workflow step with any existing or new workflows for any DAM image operations, and the new workflow instances is already triggered.

For any DAM's image, this workflow step will add the generated captions (property name = 'captions') to the following path:
 
* Location of added captions
    
      ASSET-NODE-PATH
                |
            jcr:content
                    |
                  metadata

For example, in my case, using the below link shows the newly added property with the value = generated captions.

http://localhost:4502/crx/de/index.jsp#/content/dam/geometrixx-outdoors/activities/surfing/PDP_4_c15.jpg/jcr%3Acontent/metadata

In the asset details:

http://localhost:4502/mnt/overlay/dam/gui/content/assets/metadataeditor.html/content/dam/geometrixx-outdoors/articles/12-travel-tips.jpg

You should see the 'Im2txt Generated Captions' in the asset metadata.

Example:
![Alt text](https://github.com/DevDh/aem-im2txt/blob/master/screenshots/gencap1.png?raw=true "Optional Title"){:height="36px" width="36px"}.



INFO
=====

For any questions, send an email at pd@headwire.com.
