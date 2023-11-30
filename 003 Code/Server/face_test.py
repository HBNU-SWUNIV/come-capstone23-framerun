import pdb
import subprocess
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

with open("./webserver/face_path.txt", "r") as file:
    filepath = file.read().strip()

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
weight = weight.pt 파일
ckpt = torch.load(weight, map_location=device)  # load checkpoint

model = ckpt['backbone'].to(device)
model = DataParallel(model)

# filepath = 'C:/Framerun/sensor_img/20231129_045538.jpg'
image = cv2.imread(filepath)
obj = RetinaFace.detect_faces(filepath)

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

try:
    for key in obj.keys():

        identity = obj[key]
        facial_area = identity["facial_area"]
        crop_image = image[facial_area[1]:facial_area[3], facial_area[0]:facial_area[2]]
            
    feature = getFeature(model, crop_image)  # 얼굴의 특징 벡터 추출
    print('벡터 추출 done')

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
                s2 = datetime.now()
                print("벡터값 비교", s2-s1)
                print('얼굴 인식 성공', v1)

                # 센서 사진 삭제
                if os.path.isfile(filepath):
                    os.remove(filepath)

                with open(r"C:\Framerun\webserver\mode.json", "r") as file:
                    mode_data = json.load(file)

                if mode_data['mode'] == "mode_2":
                    mode_data["face"] = "success"

                with open(r"C:\Framerun\webserver\mode.json", "w") as file:
                    json.dump(mode_data, file)
                
                break

    except UnboundLocalError:
        print("에러 발생")

except AttributeError:
    print("벡터 추출 fail")
