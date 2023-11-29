""" #Image Upload"""

# 서버에 사진 전송
import datetime
import os

import requests


def imgUpload(folder_name, img_send_URL):
    t1 = datetime.datetime.now()  # 시간 확인용 t1

    path = "/home/framerun/Desktop/Camera/" + folder_name
    url = img_send_URL + "?folder_name=" + folder_name
    for filename in os.listdir(path):
        filepath = os.path.join(path, filename)
        if os.path.isfile(filepath):
            files = {"file": open(filepath, "rb")}
            response = requests.post(url, files=files)
            print(response.json)

    # files = {"file": open(folder_path, "rb")}  # 파일의 절대 경로
    # files = {'file': open('./A1.jpg', 'rb')}  # 파일의 상대 경로

    t2 = datetime.datetime.now()  # 시간 확인용 t2

    if response.text == "save successfully":
        print("웹서버 전송 성공 >> %s" % (t2 - t1))
        return "successfully"
    else:
        return "retry"
