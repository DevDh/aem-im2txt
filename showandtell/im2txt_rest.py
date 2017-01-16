from flask import Flask, jsonify, render_template, request
import os
import uuid
import json

from im2txt import my_run_inference
from im2txt import do_train
from im2txt import prepare_train_data

from os.path import expanduser

app = Flask(__name__)

HOME = expanduser("~")

@app.route('/im2txt', methods=['POST'])
def upload():

    if request.method == 'POST':
        file = request.files['file']
        extension = os.path.splitext(file.filename)[1]
        f_name = str(uuid.uuid4()) + extension
        image_path = os.path.join("/var/tmp/uploads/", f_name)
        file.save(image_path)

    print("Generating captions for image at path : ", image_path)

    sentences = my_run_inference.generate(image_path)

    return jsonify(sentences), 200


OUTPUT_DIR = HOME + "/im2txt/data/mscoco"

@app.route('/train', methods=['POST'])
def train():

    if request.method == 'POST':
        # check if the post request has the file part
        if 'file' not in request.files:
            print('No file found')
            return

        file = request.files['file']
        extension = os.path.splitext(file.filename)[1]
        f_name = str(uuid.uuid4()) + extension
        image_path = os.path.join("/var/tmp/uploads/", f_name)
        file.save(image_path)

        captions = request.form['captions']

    print("Train the model with image %s, and captions '%s' : ", image_path, captions)

    # Prepare the Training data
    prepare_train_data.prepare(image_path, captions)

    #Do the Trainining
    do_train.train(OUTPUT_DIR)

    print("returning now after completion....")
    return jsonify({'status': "200"})


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)