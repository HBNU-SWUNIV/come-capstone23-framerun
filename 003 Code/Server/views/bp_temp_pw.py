import json
import bcrypt
from flask import Blueprint, request
from datetime import datetime

# 'main': 이름, __name__: 모듈명, url_prefix='/': URL 프리픽스
bp = Blueprint('temp_pw', __name__, url_prefix='/')


# 임시 비밀번호 모드 서버
"""
1. 도어락 비밀번호 입력
2. 입력한 값을 서버로 보내서 비밀번호 일치 여부 check (request)
3. 결과를 라즈베리파이로 전송 (response)
"""


@bp.route('/temp_pw', methods=['POST'])
def temp():
    comparePW = request.get_data()  # 도어락에서 입력한 비밀번호
    with open(r"C:\Framerun\webserver\mode.json", "r") as file:
        mode_data = json.load(file)

    # 입력한 비밀번호와 임시 비밀번호 비교
    result = bcrypt.checkpw(
        comparePW, mode_data['password'].encode("utf-8")
    )

    if result == True:
        print(datetime.now(), "임시 비밀번호 잠금 해제 성공")
        return "success"

    # 입력한 비밀번호가 임시 비밀번호 값과 틀릴 때 , password="null"일 때 (mode가 다를 때)
    else:
        print(datetime.now(), "임시 비밀번호 잠금 해제 실패")
        return "fail"
