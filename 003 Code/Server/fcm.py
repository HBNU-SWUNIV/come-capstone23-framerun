import datetime
import numpy as np
from datetime import datetime
from time import sleep
import datetime
import os
import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
from firebase_admin import messaging
from uuid import uuid4
from name_to_datetime import name_to_datetime
from firebase_img_upload import firebase_Upload
import json

with open(r"C:\Framerun\webserver\token.json", "r") as file:
        global token 
        token = json.load(file)['token']


def fcm_data(file_name):
    print("FCM 시작: 도어락 외부인 접근 알림")

    try:
        body = name_to_datetime(file_name)
        message = messaging.Message(
                data={
                    'title': '도어락 외부인 접근 알림',
                    # jpg 파일 이름을 어플로 넘김. -> 파이어베이스에서 사진 가져올 것(어플)
                    'body': body,

                    'image': file_name,
                },
                token=token,
            )

        response = messaging.send(message)
        print('Successfully sent data:', response)
        
    except Exception as e:
        print("fcm_data 실패")
    
    
def fcm_noti(title, body):
    try:
        print("FCM 시작: ", title)

        message = messaging.Message(
            notification=messaging.Notification(
            title=title,
            body=body,
            ),
            token=token,
        )

        response = messaging.send(message)
        print('Successfully sent notification:', response)

    except Exception as e:
        print("fcm_noti 실패")

