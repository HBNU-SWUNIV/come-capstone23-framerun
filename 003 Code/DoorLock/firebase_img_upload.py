import datetime
import numpy as np
from datetime import datetime
from time import sleep
import datetime
import os
import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
from uuid import uuid4

# 코드 설명: 파이어베이스에 사진을 업로드


# 파이어베이스
"""Project"""
PROJECT_ID = "framerun-cloud"
# my project id

cred = credentials.Certificate(
    "/home/framerun/Desktop/키 파일.json"
)  # (키 이름 ) 부분에 본인의 키이름을 적어주세요.
default_app = firebase_admin.initialize_app(
    cred, {"storageBucket": f"{PROJECT_ID}.appspot.com"}
)


def firebase_Upload(file):
    # 버킷은 바이너리 객체의 상위 컨테이너이다. 버킷은 Storage에서 데이터를 보관하는 기본 컨테이너이다.
    bucket = storage.bucket()  # 기본 버킷 사용

    t1 = datetime.datetime.now()  # 시간 확인용 t1

    blob = bucket.blob(
        "Outsider_Img/" + file
    )  # 저장한 사진을 파이어베이스 storage의 'Outsider_Img' 디렉토리에 저장

    # new token and metadata 설정
    new_token = uuid4()
    # access token이 필요하다.
    metadata = {"firebaseStorageDownloadTokens": new_token}
    blob.metadata = metadata

    # upload file
    blob.upload_from_filename(
        filename="/home/framerun/Desktop/Camera/" + file, content_type="image/jpg"
    )  # 파일이 저장된 주소와 이미지 형식(png는 원본, jpg는 압축됨)
    # debugging

    t2 = datetime.datetime.now()  # 시간 확인용 t2

    file_size = os.path.getsize("/home/framerun/Desktop/Camera/" + file)
    kb_size = file_size / 1024

    print("Firebase 업로드 성공 %f KB >> %s" % (kb_size, t2 - t1))
    print(blob.public_url)  # 파이어베이스 저장 위치 url
