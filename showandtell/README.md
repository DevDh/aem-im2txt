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

        python image_rest.py
    
    In my case, the above command resulted like:
         
         * Running on http://0.0.0.0:5000/ (Press CTRL+C to quit)
         * Restarting with stat
         * Debugger is active!
         * Debugger pin code: 164-368-744
         
   It means the Rest api can be accessed using http://0.0.0.0:5000/. This is what we will configure in AEM Config Service.   
       
       
REST CLIENT  
============

1. To Process the image to generate the captions and send it to the following URI:

http://0.0.0.0:5000/im2txt

POST Request with the form containing image + text(captions)

Add to the request headers

    enctype : multipart/form-data
    

2. To train the Im2txt Inception model with the image + captions and send it to the following URI:
 
http://0.0.0.0:5000/train
 
 POST request with the form containing the image + text
 
 Add to the request headers
 
     enctype : multipart/form-data
     
INFO
=====

For any questions, send an email at pd@headwire.com.
