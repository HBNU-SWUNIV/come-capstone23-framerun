from views import bp_userjoin, bp_run_face, bp_mode, bp_temp_pw
from flask import Flask
from flask import Flask, request
from werkzeug.utils import secure_filename
import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
import os
import requests
import torch
import numpy as np
import matplotlib.pyplot as plt
import time
import cv2
from torch.nn import DataParallel
from datetime import datetime
from retinaface import RetinaFace
from itertools import combinations
import posixpath

app = Flask(__name__)

app.register_blueprint(bp_userjoin.bp)
app.register_blueprint(bp_run_face.bp)
app.register_blueprint(bp_mode.bp)
app.register_blueprint(bp_temp_pw.bp)

# Firebase 앱 초기화(initialize_app) 함수 호출
PROJECT_ID = "프로젝트 ID"
# (키 이름 ) 부분에 본인의 키이름을 적어주세요.
cred = credentials.Certificate(
    "firebase-adminsdk json파일 경로")
default_app = firebase_admin.initialize_app(
    cred, {'storageBucket': f"{PROJECT_ID}.appspot.com"})

@app.route('/')
def home_screen():
    return "Framerun"

@app.route('/token', methods=['POST'])
def get_token():
    response = request.get_json()
    with open(r"C:\Framerun\webserver\token.txt", "w") as file:
        file.write(response['data'])  # 받은 token 값을 파일에 저장하기
    return "save token done"

if __name__ == '__main__':
    app.run(host="0.0.0.0", port="5000", debug=True)

if __name__ == '__userjoin__':
    app.run('0.0.0.0', port=5000, debug=True)
if __name__ == '__run_face__':
    app.run('0.0.0.0', port=5000, debug=True)
if __name__ == '__mode__':
    app.run('0.0.0.0', port=5000, debug=True)
if __name__ == '__temp_pw__':
    app.run('0.0.0.0', port=5000, debug=True)
