import json
from views import bp_join_user, bp_run_voice, bp_run_face, bp_mode, bp_temp_pw
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
from fcm import fcm_data, fcm_noti
import posixpath
from voice_test import voice_test

app = Flask(__name__)


# Firebase 앱 초기화(initialize_app) 함수 호출
PROJECT_ID = "framerun-cloud"
# (키 이름 ) 부분에 본인의 키이름을 적어주세요.
cred = credentials.Certificate(
    키 값 json 파일 경로)
default_app = firebase_admin.initialize_app(
    cred, {'storageBucket': f"{PROJECT_ID}.appspot.com"})

app.register_blueprint(bp_join_user.bp)
app.register_blueprint(bp_run_voice.bp)
app.register_blueprint(bp_run_face.bp)
app.register_blueprint(bp_mode.bp)
app.register_blueprint(bp_temp_pw.bp)

@app.route('/')
def home_screen():
    return "Framerun"

# 어플이 새로 켜질때마다 보내는 token 값을 받는 서버 코드
@app.route('/token', methods=['POST'])
def get_token():
    token = request.get_json()
    with open("c:/Framerun/webserver/token.json", "w") as file:
        json.dump(token, file)  # 받은 token 값을 파일에 저장하기
        print("save token done")
    return "save token done"

@app.route('/fcm_send', methods=['POST', 'GET'])
def fcm_send():
    
    filename = request.args.get('file_name')
    
    fcm_data(filename)  # fcm.py
    return ('disapproval')                       


if __name__ == '__main__':
    app.run(host="0.0.0.0", port="5000", debug=True)

if __name__ == '__join_user__':
    app.run('0.0.0.0', port=5000, debug=True)
if __name__ == '__run_voice__':
    app.run('0.0.0.0', port=5000, debug=True)
if __name__ == '__run_face__':
    app.run('0.0.0.0', port=5000, debug=True)
if __name__ == '__mode__':
    app.run('0.0.0.0', port=5000, debug=True)
if __name__ == '__temp_pw__':
    app.run('0.0.0.0', port=5000, debug=True)
