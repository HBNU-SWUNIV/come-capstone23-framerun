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

# 코드 설명: 파이어베이스에 사진을 업로드 -> 어플에 FCM 알림 전송


# 파이어베이스
"""Project"""

# PROJECT_ID = "프로젝트 ID"
# # my project id

# cred = credentials.Certificate(
#     "firebase-adminsdk json파일 경로"
# )  # (키 이름 ) 부분에 본인의 키이름을 적어주세요.
# default_app = firebase_admin.initialize_app(
#     cred, {"storageBucket": f"{PROJECT_ID}.appspot.com"}
# )

def fcm_run(file, registration_token):

    # FCM
    print("FCM 시작")  # 확인용

    message = messaging.Message(
        data={
            'title': '도어락 외부인 접근 알림',
            # jpg 파일 이름을 어플로 넘김. -> 파이어베이스에서 사진 가져올 것(어플)
            'body': name_to_datetime(file),
            #'body': "ㅎㅇ",
            'image': file,
        },
        token=registration_token,
    )

    response = messaging.send(message)
    print('Successfully sent message:', response)

# token = '토큰 값'  # 토큰 값

# fcm_run(filename, token)