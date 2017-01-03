from flask import Flask, jsonify, render_template, request
import os
import uuid
import json

from im2txt import my_run_inference

app = Flask(__name__)

@app.route('/im2txt', methods=['GET', 'POST'])
def upload():
    if request.method == 'GET':
        print("GET request method not supported, exit now...")
        return

    if request.method == 'POST':
        file = request.files['file']
        extension = os.path.splitext(file.filename)[1]
        f_name = str(uuid.uuid4()) + extension
        image_path = os.path.join("/var/tmp/uploads/", f_name)
        file.save(image_path)

    print("Generating captions for image at path : ", image_path)

    sentences = my_run_inference.generate(image_path)

    return jsonify(sentences), 200


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)