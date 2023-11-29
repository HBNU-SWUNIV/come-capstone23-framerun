import subprocess
from flask import Flask
from flask import Flask, request
import urllib3
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
from getfeature import getFeature
from voice_train import voice_train
from fcm import fcm_noti
import posixpath
import asyncio
from split_audio_file import split_audio_file
# from face_vector import face_vector

from flask import Blueprint

vector_URL = 'http://Ip주소/join_user/getvector'
def face_vvector():
    # num: 0-> 얼굴 인식 1-> 사용자 등록

    # '''model (사전 학습 가중치 + 모델 호출)'''
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    weight = 'kface.r34.arc.unpg.wisk1.0.pt'
    ckpt = torch.load(weight, map_location=device)  # load checkpoint

    model = ckpt['backbone'].to(device)
    model = DataParallel(model)

    print(img_folder_path)
    print(img_folder_path == os.path.join('C:/Framerun/user/', "GAEUN"))
    print(user_name)
    print(user_name == "song")

    print("##")
    img_folder_path = 'C:/Framerun/user/song'
    user_name = "song"
    option = 1
    

    t1 = datetime.now()

    pic = []

    for root, dirs, files in os.walk(img_folder_path):
            for file in files:
                file_path = posixpath.join(root, file)
                pic.append(file_path)

    '''얼굴 특징 벡터 추출'''

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

                feature = getFeature(model, crop_image)  # 얼굴의 특징 벡터 추출 getfeature.py
                v[p] = feature

                if option == 1:
                    print(index, "벡터값 계산 성공")
                    # npy 파일로 저장하기
                    np.save('./user/face_npy/' + user_name + str(index) + '.npy', feature)
                    index += 1
                    cnt += 1
                else:
                    print('벡터값 계산 성공')
                    return feature
            except AttributeError:
                if option == 1:
                    print(index, "벡터값 계산 실패")
                    index += 1
                    continue
                else:
                    print("벡터값 계산 실패")
                    return "fail"

    t2 = datetime.now()


    print('벡터값 계산 소요 시간:', t2-t1)
    if option == 1:
        print('%d장 사진 중, %d개 벡터값 저장' % (len(pic), cnt))
# '''

#####
# 변수
global voice_folder_path
voice_folder_path = "C:/Framerun/voice_run/data/train"
# 'main': 이름, __name__: 모듈명, url_prefix='/': URL 프리픽스
bp = Blueprint('join_user', __name__, url_prefix='/')


# 1. 어플에서 보낸 사용자 등록 사진 및 음성 파일 받기 
@bp.route('/join_user', methods=['POST','GET'])
def join_user():
    
    # 어플에서 /userjoin?user_name = 사용자이름 URL 접속한 후
    global user_name
    user_name = request.args.get('user_name')
    print('사용자 등록 시도 >>', user_name)

    # 유저 데이터를 받을 폴더 지정
    
    # image: 사용자 이름 폴더
    global img_folder_path
    img_folder_path = os.path.join('C:/Framerun/user/', user_name)

    # audio: 음성 인식 train 폴더
    # 새로운 폴더 생성 (생성할 폴더 이름 : train 폴더 안에 있는 폴더들의 개수 (인덱스))
    voice_folder = os.path.join(voice_folder_path, str(len(os.listdir(voice_folder_path))))
    os.mkdir(voice_folder)

    # 폴더가 이미 존재하는지 확인
    if not os.path.exists(img_folder_path):
        # 폴더를 생성합니다.
        os.makedirs(img_folder_path)
        print("Folder created successfully.")
    else:
        print("The folder already exists.")
        # return "already exist"

    bucket = storage.bucket()
    blobs = bucket.list_blobs(prefix=user_name+'/')

    try:
        for blob in blobs:
            filename = os.path.basename(blob.name)

            # audio 형식 사용자 등록
            if blob.content_type.startswith("audio/wav"):
                blob.download_to_filename("C:/Framerun/practice/" + filename)

                try:
                    print("ss")
                    # split_audio_file(input_file, output_path, name) -> minji_0.wav , minji_1.wav
                    split_audio_file(voice_folder +'/' + filename, voice_folder, user_name) # split_audio_file.py
                except Exception as e:
                    fcm_noti("사용자 등록 실패@", "")
                    return 'fail', 500
                else:
                    if os.path.isfile("C:/Framerun/practice" +'/' + filename):
                        os.remove(voice_folder +'/' + filename)

            # image 형식 사용자 등록
            elif blob.content_type.startswith("image/"):
                blob.download_to_filename(img_folder_path + '/' + filename)
            else:
                pass

            print(f"Downloaded {filename} from Cloud Storage.")

            print(user_name + "'s files downloaded successfully.")

            print(img_folder_path)
            print(user_name)
            
            # response = requests.post(vector_URL)
            
            # /join_user/get_vector로 이동
            # if response.status_code == 200:
            #     fcm_noti("사용자 등록 완료1","")
            #     return 'successful' , 200
            # else:
            #     fcm_noti("사용자 등록 실패1","")
            #     return 'fail' , 500
            # requests.post('http://192.168.0.5:5000/join_face/getvector' + '?user_name=' + user_name)

    except Exception as e:
        print(e)
        fcm_noti("사용자 등록 실패 ss", "")
        return 'fail', 500
    
    else:
        print(user_name + "'s files downloaded successfully.")
        
        response = requests.post(vector_URL)

        if response.status_code == 200:
            fcm_noti("사용자 등록 완료","")
            return 'successful' , 200
        else:
            fcm_noti("사용자 등록 실패","")
            return 'fail' , 500

# 2. 사진 및 음성 파일 벡터값 추출하기
@bp.route('/join_user/get_vector', methods=['GET', 'POST']) # ★ POST 변경
def get_vector():

    try:
        print("<<Get Face Vector>>")
    
        face_vector(img_folder_path, user_name, 1)

    except Exception as e:
        print(e)
        fcm_noti("사용자 등록 실패", "얼굴 등록에 실패하였습니다. ")
        print('Join Face Failed!')
        return "0", 500
    else:
        fcm_noti("사용자 얼굴 등록 완료","")
        print("Join Face Done!")

    try:
        print("<<Get Voice Vector>>")
        # 음성 train
        voice_train()
    except Exception as e:
        print(e)
        fcm_noti("사용자 등록 실패", "음성 등록에 실패하였습니다. ")
        print('Join Voice Failed!')
        return "0", 500
    else:
        fcm_noti("사용자 음성 등록 완료","")
        print("Join Voice Done!")
        return "1", 200
