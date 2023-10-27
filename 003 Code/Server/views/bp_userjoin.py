# from server import *
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

getvectorURL = 'IP주소/userjoin/getvector'

# '''model (사전 학습 가중치 + 모델 호출)'''
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
weight = 'kface.r34.arc.unpg.wisk1.0.pt'
ckpt = torch.load(weight, map_location=device)  # load checkpoint

model = ckpt['backbone'].to(device)
model = DataParallel(model)

# 'main': 이름, __name__: 모듈명, url_prefix='/': URL 프리픽스
bp = Blueprint('userjoin', __name__, url_prefix='/')


# 1-1 얼굴 등록
# 어플에서 파이어베이스로 사진 전송이 완료됐음을 인지하고, 파이어베이스에서 사진을 가져오는 코드
@bp.route('/join_face', methods=['POST'])  # ★ POST 변경
def join_face():

    # 어플에서 /userjoin?user_name = 사용자이름 URL 접속한 후
    user_name = request.args.get('user_name')
    print('User Join >>', user_name)

    # 새 폴더를 만들 경로 지정 (사용자 이름으로 폴더 생성할 것)
    new_folder_path = 'C:/Framerun/user/' + user_name

    # 폴더가 이미 존재하는지 확인
    if not os.path.exists(new_folder_path):
        # 폴더를 생성합니다.
        os.makedirs(new_folder_path)
        print("Folder created successfully.")
    else:
        print("The folder already exists.")

    # folder_path = user_name.encode(
    #     'iso-8859-1').decode('utf-8') + '/'  # 파이어베이스 폴더
    folder_path = user_name+'/'
    print("user_name:", user_name)

    bucket = storage.bucket()
    blobs = bucket.list_blobs(prefix=folder_path)

    for blob in blobs:
        filename = blob.name.split('/')[-1]
        # name print 찍으면 공백 하나 발생함 -> 이거때매 blob.download_to_filename(filename) 실행이 안 됨
        if filename == "":
            continue

        # Download the file from Firebase Storage
        blob.download_to_filename(filename=new_folder_path + '/' + filename)
        print(filename)

    print(user_name + "'s images downloaded successfully.")

    # /userjoin/getvertor로 이동
    response = requests.post(getvectorURL + '?user_name=' + user_name)

    return 'successful'  # 클라이언트에게 응답 전송


# 1-2. 받은 사용자 사진으로 벡터값 저장
@bp.route('/join_face/getvector', methods=['POST'])  # ★ POST 변경
def vector():
    print("<<Get Vector>>")
    t1 = datetime.now()
    user_name = request.args.get('user_name')
    folder_path = './user/' + user_name  # 폴더 경로
    pic = []

    for root, dirs, files in os.walk(folder_path):
        for file in files:
            file_path = posixpath.join(root, file)
            pic.append(file_path)

    '''얼굴 특징 벡터 추출'''
    def getFeature(model, image):
        # image = cv2.imread(img_path)
        image = cv2.resize(image, (112, 112))
        image = image.transpose((2, 0, 1))
        image = image[np.newaxis, :, :, :]
        image = image.astype(np.float32, copy=False)
        image -= 127.5
        image /= 127.5

        data = torch.from_numpy(image)
        data = data.to(torch.device('cuda'))
        feature = model(data)
        feature = feature.data.cpu().numpy()[0]

        return feature

    # 벡터값 계산
    v = {}
    index = 1  # 벡터값 인덱스
    cnt = 0  # 벡터값 저장 성공 카운트

    for p in pic:
        image = cv2.imread(p)
        obj = RetinaFace.detect_faces(p)

        try:
            for key in obj.keys():
                identity = obj[key]
                facial_area = identity["facial_area"]
                crop_image = image[facial_area[1]
                    :facial_area[3], facial_area[0]:facial_area[2]]

            feature = getFeature(model, crop_image)  # 얼굴의 특징 벡터 추출
            v[p] = feature

            # npy 파일로 저장하기
            np.save('./user/' + user_name + str(index) + '.npy', feature)

            print(index, "벡터값 저장 성공")
            index += 1
            cnt += 1
        except AttributeError:
            print(index, "벡터값 저장 실패")
            index += 1
            continue
    t2 = datetime.now()
    print('벡터값 계산 및 저장 소요 시간:', t2-t1)
    print('%d장 사진 중, %d개 벡터값 저장' % (len(pic), cnt))

    return 'done!'


# 2-1. 음성 등록
@bp.route('/join_voice', methods=['GET', 'POST']) # ★ POST 변경
def join_voice():



    return "1"