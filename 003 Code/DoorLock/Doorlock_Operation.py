import RPi.GPIO as GPIO
import time
from datetime import datetime
from time import sleep
import numpy as np
import cv2
import sys, os
import requests
from werkzeug.datastructures import MultiDict
from requests.exceptions import Timeout, HTTPError, RequestException
from excute_camera import execute_camera
from imgUpload import imgUpload
from check_network_connection import check_network_connection
from compare_passwords import compare_passwords

"""Var Setting"""
img_type = "jpg"
img_cnt = 0
lock_cnt = 0

""" #Sensor Setting"""
triggerPin = 20
echoPin = 21

GPIO.setmode(GPIO.BCM)
GPIO.setup(triggerPin, GPIO.OUT)  # 트리거핀을 출력으로 사용
GPIO.setup(echoPin, GPIO.IN)  # 에코핀을 입력으로 사용

"""  Running

1. 초음파 센서 인식
2. 라즈베리파이 네트워크 연결 여부 확인
    2-1. 연결 없으면 -> 기본 비밀번호 모드
    2-2. 연결 있으면 웹서버 모드 요청 겸 웹서버 네트워크 확인 (잠금 모드 확인 서버)

        3-1. 서버로부터 모드 전송 받으면
            (1) mode_1 기본 비밀번호 모드
            사용자가 입력한 비밀번호 해시값 <-비교-> 라즈베리파이에 저장된 비밀번호 해시값

            (2) mode_2 다중 잠금 모드

            (3) mode_3 임시 비밀번호 모드
            1. 라즈베리파이에서 input으로 비밀번호를 입력
            2. 입력한 값을 서버로 보내서 비밀번호 일치 여부 check (request)
            3. 결과를 라즈베리파이로 전송 (response)

        3-2. 서버로부터 정해진 시간 안에 반응 안 오면 웹서버-오프라인으로 인식 -> 기본 비밀번호 모드
"""
try:
    while True:
        # 구형파 발생
        GPIO.output(triggerPin, GPIO.LOW)  # 10m/s 동안 초음파를 쏴야함
        sleep(0.000005)  # 기초단위가 1초라서 10us는 10의 마이너스 5승으로 처리
        GPIO.output(triggerPin, GPIO.HIGH)

        # 시간측정
        while GPIO.input(echoPin) == 0:  # 펄스 발생(초음파 전송이 끝나는 시간을 start에 저장)
            start = time.time()
        while GPIO.input(echoPin) == 1:  # 펄스 돌아옴(초음파 수신이 완료될때까지의 시간을 stop에 저장)
            stop = time.time()

        rtTotime = stop - start  # 리턴 투 타임 = (end시간 - start시간)

        # 거리 = 시간 * 속력
        # 이때 소리의  속력은 340m/s인데 cm로 단위를 바꿔줘야함=> 34000 cm/s
        # 그리고 340m/s 는 왕복속도라서 편도로 봐야하니 나누기 2를 해줘야함
        distance = rtTotime * (34000 / 2)

        # 1. 초음파 센서 인식
        if distance < 30 or distance > 3000:
            t1 = datetime.now()
            print("센서 감지 됨 >> %.2f cm" % distance)
            time_stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = ".".join([time_stamp, img_type])
            filepath = "/home/framerun/Desktop/Camera/" + filename

            # 2. 네트워크 연결 여부 확인

            # 2-1. 네트워크 연결 x -> 기본 비밀번호 모드
            if check_network_connection() == False:
                print("네트워크에 연결되어 있지 않습니다. mode_1으로 작동됩니다.")

                if compare_passwords() == True:
                    t2 = datetime.now()
                    print("{ mode_1 도어락 잠금 해제 성공 }")
                    print("소요 시간 : ", t2 - t1)

                    # 도어락 제어
                    time.sleep(5)
                else:
                    t2 = datetime.now()
                    print("{ mode_1 도어락 잠금 해제 실패 }")
                    print("소요 시간 : ", t2 - t1)

                    lock_cnt += 1
                    if lock_cnt == 5:
                        # 5번 시도했으나 모두 실패하면 도어락 멈춤(1분)
                        print("Locked")
                        time.sleep(60)
                        lock_cnt = 0

            # 2-2. 네트워크 연결 o -> 웹서버에 모드 확인 요청
            elif check_network_connection() == True:
                try:
                    response = requests.post(
                        "mode check 서버 주소", timeout=2  # <<-- URL 수정
                    )

                    response.raise_for_status()  # 웹서버와 통신 확인

                    mode_type = response.text
                    print("Mode: " + mode_type)

                    # 3-1. 서버로부터 모드 전송 받으면

                    # ● mode_2 다중 잠금 모드
                    if mode_type == "mode_2":
                        print("mode_2")

                    # ● mode_3 임시 비밀번호 모드
                    elif mode_type == "mode_3":
                        print("mode_3")

                        inputPW = input("비밀번호를 입력하세요: ")

                        response = requests.post(
                            "임시 비밀번호 서버 주소", data=inputPW
                        )  # <<<--- URL 수정

                        result = response.text
                        # 서버의 응답 확인
                        if response.status_code == 200:
                            if result == "success":
                                print("{ mode_3 도어락 잠금 해제 성공 }")
                            else:
                                print("{ mode_3 도어락 잠금 해제 실패 }")
                        else:
                            """
                            오류 처리
                            """
                            print("errorr")

                    # ● 그 외 1번 모드일 때 or 이상한 값으로 모드가 저장되어 있을 때 : (1) 기본 비밀번호 모드
                    else:
                        print("mode_1")

                        if compare_passwords() == True:
                            print("{ mode_1 도어락 잠금 해제 성공 }")

                            # 도어락 제어
                            time.sleep(5)
                        else:
                            print("{ mode_1 도어락 잠금 해제 실패 }")

                            lock_cnt += 1
                            if lock_cnt == 5:
                                # 5번 시도했으나 모두 실패하면 도어락 멈춤(1분)
                                print("5번 잠금 해제 실패. 1분 정지")
                                time.sleep(60)
                                lock_cnt = 0

                except (
                    Timeout,
                    HTTPError,
                    RequestException,
                ):  # Timeout or 웹서버와의 통신 불가 -> (1) 기본 비밀번호 모드
                    print("웹서버의 응답이 없습니다.")
                    print("mode_1")

                    if compare_passwords() == True:
                        print("{ mode_1 도어락 잠금 해제 성공 }")

                        # 도어락 제어
                        time.sleep(5)
                    else:
                        print("{ mode_1 도어락 잠금 해제 실패 }")

                        lock_cnt += 1
                        if lock_cnt == 5:
                            # 5번 시도했으나 모두 실패하면 도어락 멈춤(1분)
                            print("Locked")
                            time.sleep(60)
                            lock_cnt = 0

            else:
                print("ERROR 발생")

            # /////////////////////////////////////////////////////
            

        # 센서 인식X
        else:
            print("센서 감지 안 됨 ")

        time.sleep(0.5)


except KeyboardInterrupt:
    GPIO.cleanup()
