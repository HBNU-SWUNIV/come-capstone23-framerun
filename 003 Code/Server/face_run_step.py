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


def face_run_step(filepath):
    print(filepath)

    # 경로 저장
    with open("./webserver/face_path.txt", "w") as file:
        file.write(filepath)
    
    face_run_py = './webserver/face_test.py'
    
    subprocess.run(['python', face_run_py])

    # 경로 삭제
    with open('./webserver/face_path.txt', 'w') as file:
        file.truncate(0)

if __name__ == '__main__':

    # filepath = 'C:/Framerun/sensor_img/20231129_045538.jpg'
    # HA(filepath)
            with open(r"C:\Framerun\webserver\mode.json", "r") as file:
                mode_data = json.load(file)

            if mode_data['mode'] == "mode_2":
                mode_data["face"] = "success"

            with open(r"C:\Framerun\webserver\mode.json", "w") as file:
                json.dump(mode_data, file)