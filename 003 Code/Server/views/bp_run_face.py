import pdb
from flask import Flask
from flask import Flask, request
from werkzeug.utils import secure_filename
import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
from firebase_admin import messaging
import os
import requests
import torch
import numpy as np
import matplotlib.pyplot as plt
import cv2
import json
from torch.nn import DataParallel
from datetime import datetime
from retinaface import RetinaFace
import itertools
from flask import Blueprint
from getfeature import getFeature
from firebase_img_upload import firebase_Upload
from name_to_datetime import name_to_datetime
from fcm import fcm_data, fcm_noti
from face_vector import face_vector

mode2_edit_URL = 'http://Ip 주소/mode2_edit'

# '''model (사전 학습 가중치 + 모델 호출)'''
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
weight = weight.pt 
ckpt = torch.load(weight, map_location=device)  # load checkpoint

model = ckpt['backbone'].to(device)
model = DataParallel(model)

# 'main': 이름, __name__: 모듈명, url_prefix='/': URL 프리픽스
bp = Blueprint('run_face', __name__, url_prefix='/')

# 3. 카메라 모듈로 찍힌 사진 1장의 벡터값과 유저의 벡터값과 비교 (얼굴 인식)
# 사진 촬영→ 웹서버에서 벡터값 비교(얼굴인식)→ if 1 : return 열림 else:  어플 알림 보내는 웹서버 요청

@bp.route('/run_face', methods=['POST', 'GET'])
def face_one():

    # 유저의 벡터값 가져오기
    s1 = datetime.now()
    npy_path = 'c:/FRAMERUN/user/face_npy/'
    npy_list = [f for f in os.listdir(
        npy_path) if f.endswith('.npy')]  # ndarray 파일 이름 list

    vector_list = {}  # ndarray dictionary
    for file_name in npy_list:
        user_vector = np.load(npy_path + file_name)
        vector_list[file_name] = user_vector
    s2 = datetime.now()
    print('유저의 벡터값 가져오기 done')
    print("유저 벡터값 가져오는 시간 :", s2-s1)

    # 3-1. 벡터값 추출
    # (1) 사진 가져오기
    s1 = datetime.now()
    file = request.files['file']
    filename = secure_filename(file.filename)
    # filename = 'minji.jpg'
    filepath = os.join.path('c:/FRAMERUN/sensor_img/' , filename)

    file.save(filepath)

    s2 = datetime.now()
    print('사진 가져오기 done')

    print("사진 가져오는 시간 :", s2-s1)

    # (2) 벡터 추출
    s1 = datetime.now()

    image = cv2.imread(filepath)
    obj = RetinaFace.detect_faces(filepath)

    try:
        for key in obj.keys():

            identity = obj[key]
            facial_area = identity["facial_area"]
            crop_image = image[facial_area[1]:facial_area[3], facial_area[0]:facial_area[2]]
        
        feature = getFeature(model, crop_image)  # 얼굴의 특징 벡터 추출
        print('벡터 추출 done')

        s2 = datetime.now()
        print("벡터 추출 시간:", s2-s1)

    except AttributeError:
        print("벡터 추출 fail")
        

    # 3-2. 벡터값 비교
    s1 = datetime.now()
    def cosine_metric(x1, x2):
        return np.dot(x1, x2) / (np.linalg.norm(x1) * np.linalg.norm(x2))
    try:
        for v1 in vector_list:
            sim = cosine_metric(vector_list[v1], feature)
            print(v1 + '과 비교: ', sim)

            # case1. 얼굴 인식 성공
            if sim >= 0.85:
                s2 = datetime.now()
                print("벡터값 비교", s2-s1)

                print('얼굴 인식 성공', v1)
                if os.path.isfile(filepath):
                    os.remove(filepath)
                
                # mode2 업데이트할 JSON 데이터
                client_data = {"face": "1"}

                # 서버에 POST 요청 보내기
                response = requests.post(mode2_edit_URL, json=client_data)

                if response.status_code == 200:
                    print("mode2 업데이트 성공")
                    return "success", 200
                else:
                    print("mode2 업데이트 실패")
                    return "fail", 500

    except UnboundLocalError:
        print("에러 발생"), 500

    # case2. 얼굴 인식 실패 -> 방문자 사진 알림 FCM 

    firebase_Upload(filename)  # firebase_img_upload.py  특정 기기에 메시지 전송

    print("Firebase Image Upload Done.")  
    
    if os.path.isfile(filepath+filename):
        os.remove(filepath+filename)

    # FCM
    fcm_data(filepath+filename)  # fcm.py
    return ('disapproval'), 500
