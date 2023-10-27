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
from torch.nn import DataParallel
from datetime import datetime
from retinaface import RetinaFace
import itertools
from flask import Blueprint
from getfeature import getFeature
from firebase_img_upload import firebase_Upload
from name_to_datetime import name_to_datetime
from fcm_run import fcm_run


# '''model (사전 학습 가중치 + 모델 호출)'''
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
weight = 'kface.r34.arc.unpg.wisk1.0.pt'
ckpt = torch.load(weight, map_location=device)  # load checkpoint

model = ckpt['backbone'].to(device)
model = DataParallel(model)

# 'main': 이름, __name__: 모듈명, url_prefix='/': URL 프리픽스
bp = Blueprint('run_face', __name__, url_prefix='/')

# 3. 카메라 모듈로 찍힌 사진 1장의 벡터값과 유저의 벡터값과 비교 (얼굴 인식)
# 사진 촬영→ 웹서버에서 벡터값 비교(얼굴인식)→ if 1 : return 열림 else:  어플 알림 보내는 웹서버 요청


# /face_multiple: 사진 여러장 받아서 얼굴 인식
@bp.route('/face_multiple', methods=['POST'])  # 수정 ★ POST
def face_multiple():
    s1 = datetime.now()
    # 유저의 벡터값 가져오기
    npy_path = 'c:/FRAMERUN/user/'
    npy_list = [f for f in os.listdir(
        npy_path) if f.endswith('.npy')]  # ndarray 파일 이름 list

    vector_list = {}  # ndarray dictionary
    for file_name in npy_list:
        user_vector = np.load(npy_path + file_name)
        vector_list[file_name] = user_vector
    print('유저의 벡터값 가져오기 done')
    s2 = datetime.now()

    print("유저 벡터값 가져오는 시간 :", s2-s1)

    # 3-1. 벡터값 추출
    # (1) 사진 가져오기
    s1 = datetime.now()
    folder_name = request.args.get('folder_name')  # 수정 ★ POST
    sensorImg_path = 'c:/FRAMERUN/sensor_img/' + folder_name + '/'
    img_list = [f for f in os.listdir(sensorImg_path)]
    print('사진 가져오기 done')
    s2 = datetime.now()

    print("사진 불러오는 시간 :", s2-s1)

    # (2) 벡터 추출
    t1 = datetime.now()
    v = {}
    for p in img_list:
        image = cv2.imread(sensorImg_path + p)
        obj = RetinaFace.detect_faces(sensorImg_path + p)
        try:
            for key in obj.keys():

                identity = obj[key]
                facial_area = identity["facial_area"]
                crop_image = image[facial_area[1]                                   :facial_area[3], facial_area[0]:facial_area[2]]

            feature = getFeature(model, crop_image)  # 얼굴의 특징 벡터 추출
            v[p] = feature
            print('벡터 추출 done')
        except AttributeError:
            print("벡터 추출 fail")
            continue
    t2 = datetime.now()
    print('벡터 추출 소요시간: ', t2-t1)

    # 3-2. 벡터값 비교
    # (1) 비교하기

    def cosine_metric(x1, x2):
        return np.dot(x1, x2) / (np.linalg.norm(x1) * np.linalg.norm(x2))

    combi = list(itertools.product(vector_list.keys(), v))
    try:
        for v1, v2 in combi:
            sim = cosine_metric(vector_list[v1], v[v2])
            print(v1 + " & " + v2 + '=', sim)
            if sim >= 0.85:
                t3 = datetime.now()
                print('얼굴 인식 성공', v1)
                print(t3-t1)
                return ('approval')
    except UnboundLocalError:
        print("에러 발생")

    return ('disapproval')


# /face_one: 사진 한 장을 받아서 얼굴 인식
@bp.route('/face_one', methods=['POST'])
def face_one():
    # t1 = datetime.now()

    # 유저의 벡터값 가져오기
    s1 = datetime.now()
    npy_path = 'c:/FRAMERUN/user/'
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
    filename = file.filename
    filepath = 'c:/FRAMERUN/sensor_img/'
    file.save(filepath + secure_filename(filename))
    s2 = datetime.now()
    print('사진 가져오기 done')

    print("사진 가져오는 시간 :", s2-s1)

    # (2) 벡터 추출
    s1 = datetime.now()
    image = cv2.imread(filepath + filename)
    obj = RetinaFace.detect_faces(filepath + filename)

    try:
        for key in obj.keys():

            identity = obj[key]
            facial_area = identity["facial_area"]
            crop_image = image[facial_area[1]:facial_area[3], facial_area[0]:facial_area[2]]

        feature = getFeature(model, crop_image)  # 얼굴의 특징 벡터 추출
        print('벡터 추출 done')

    except AttributeError:
        print("벡터 추출 fail")

    # t2 = datetime.now()
    # print(t2-t1)
    s2 = datetime.now()
    print("벡터 추출 시간:", s2-s1)

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
                t3 = datetime.now()
                print('얼굴 인식 성공', v1)
                # print(t3-t1)
                return ('approval')

    except UnboundLocalError:
        print("에러 발생")
        return ('error')

    s2 = datetime.now()
    print("벡터값 비교", s2-s1)

    # case2. 얼굴 인식 실패 -> 방문자 사진 알림 FCM (방문자면 빨리빨리 안해도 되자나..?^^ 일단 비동기식x)

    # Firebase Image Upload - 특정 기기에 메시지 전송
    firebase_Upload(filename)  # firebase_img_upload.py

    print("Firebase Image Upload Done.")  # try catch 문 작성 보충해야댐

    # 파일 업로드 이후 파일 제거
    '''
    if os.path.isfile(filepath+filename):
        os.remove(filepath+filename)
    '''

    # FCM
    registration_token = '토큰 값'  # 토큰 값
    fcm_run(filename, registration_token)  # fcm_run.py

    return ('disapproval')
