ShowAndTell's IM2TXT
====================

The following provides the details to expose the Rest api to generate the provided image's captions.

PREREQUISITES
==============

1. Using the link below, make sure to install and train the im2txt model

    https://github.com/tensorflow/models/tree/master/im2txt
    
    After the training, you should be able to run and generate the captions (using the below link)
    https://github.com/tensorflow/models/tree/master/im2txt#generating-captions

REST API
========

From the showandtell directory, add the following files accordingly:

1. Copy all the files from showandtell directory to the im2txt directory(where you installed and trained the model)
 
    On your trained model machine, goto the directory at the location: <PATH-TO-model-im2txt>

2. Run the following command:

    python image_generator_rest.py
    
The above command will results:
         * Running on http://0.0.0.0:5000/ (Press CTRL+C to quit)
         * Restarting with stat
         * Debugger is active!
         * Debugger pin code: 164-368-744
     
CONFIGURE IM2TXT SERVICE
========================
   
In the AEM Configuration console, http://localhost:4502/system/console/configMgr
Find the service name 'Im2txt Configuration Service'
Edit the im2txt.service.endpoint = http://0.0.0.0:5000/im2txt   

*NOTE: Endpoint consists of <PROTOCOL://HOST:PORT/PATH>


Make sure to match this property with the machine details where the im2txt model is trained.


INFO
=====

For any questions, send an email at pd@headwire.com.
