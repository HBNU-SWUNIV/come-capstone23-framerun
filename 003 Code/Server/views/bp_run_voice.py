# from server import *
import json
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
from flask import Blueprint
from voice_test import voice_test

mode2_edit_URL = 'http://IP 주소/mode2_edit'
test_path = 'c:/FRAMERUN/voice_run/data/test/'


# 'main': 이름, __name__: 모듈명, url_prefix='/': URL 프리픽스
bp = Blueprint('run_voice', __name__, url_prefix='/')

# 어플에서 음성을 받은 후 voice_test()
@bp.route('/run_voice', methods=['POST', 'GET'])
def run_voice():

    with open("c:/Framerun/webserver/mode.json", "r") as file:
        mode_data = json.load(file)

    if mode_data['mode'] == 'mode_2':
    
        data = request.data
        voice_name = request.args.get('name')
        voice_path = os.path.join(test_path, voice_name)
        
        # 음성 파일 받기
        with open(voice_path, 'wb') as file:
            file.write(data)
        print('done')
        
        # 화자 인식
        if voice_test(voice_path) == "success":
            if os.path.isfile(voice_path):
                os.remove(voice_path)
                
            # mode2 업데이트할 JSON 데이터
            client_data = {"voice": "1"}

            # 서버에 POST 요청 보내기
            response = requests.post(mode2_edit_URL, json=client_data)

            if response.status_code == 200:
                print("mode2 업데이트 성공")
                return "success", 200

            else:
                print("mode2 업데이트 실패")
                return "fail", 500
        
    
    return 500

