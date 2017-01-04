AEM IM2TXT
==========

This AEM project is to provide the capability to generate the Image Captions using the AEM workflows.
The following provides the details:
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

mvn clean install -PautoInstallBundle


CONFIGURE IM2TXT SERVICE
========================
   
In the AEM Configuration console, http://localhost:4502/system/console/configMgr
Find the service name 'Im2txt Configuration Service'
Edit the im2txt.service.endpoint = http://0.0.0.0:5000/im2txt   

*NOTE: Endpoint consists of <PROTOCOL://HOST:PORT/PATH>

** Refer : showandtell/README.md - REST API, for more details where the REST api is running.

REVIEW THE WORKFLOW RESULTS
============================

Assuming you configured this workflow step with any existing or new workflows for any DAM image operations, and the new workflow instances is already triggered.

For any DAM's image, this workflow step will add the generated captions (property name = 'captions') to the following path:
 
* Location of added captions
    
      ASSET-NODE-PATH
                |
            jcr:content
                    |
                  metadata

For example, in my case, using the below link shows the newly added property with the value = generated captions.

http://localhost:4502/crx/de/index.jsp#/content/dam/geometrixx-outdoors/activities/surfing/PDP_4_c15.jpg/jcr%3Acontent/metadata


INFO
=====

For any questions, send an email at pd@headwire.com.
