from datetime import datetime
import json
import bcrypt
from flask import Blueprint, request

# 'main': 이름, __name__: 모듈명, url_prefix='/': URL 프리픽스
bp = Blueprint('mode', __name__, url_prefix='/')

# 잠금 모드 설정 서버

@bp.route('/mode_set', methods=['POST'])
def mode_setting():

    data = request.get_json()  # 클라이언트(어플)로부터 JSON 데이터 받아오기

    with open(r"C:\Framerun\webserver\mode.json", "w") as file:
        json.dump(data, file)  # JSON 데이터를 "mode.json" 파일에 쓰기
    
    print("mode set >>> ", data['mode'])
    return "done"

# 잠금 모드 확인 서버
# 기본 비밀번호:1 | 다중 잠금:2 | 임시 비밀번호:3

@bp.route('/mode_check', methods=['POST'])
def mode_check():
    with open(r"C:\Framerun\webserver\mode.json", "r") as file:
        mode_data = json.load(file)

    # json에 임시 비번 모드로 저장되어 있을 때
    if mode_data['mode'] == 'mode_3':

        # 임시비밀번호 유효시간 check
        # 현재 시간 얻기
        current_time = datetime.now()
        time_format = "%Y/%m/%d %H:%M:%S"

        mode_time = datetime.strptime(mode_data['time'], time_format)

        # 유효시간 경과되었을 시 mode data 변경
        if current_time > mode_time:

            # 값 변경
            mode_data['mode'] = 'mode_1'
            del mode_data['password']
            del mode_data['time']

            # mode.json 파일에 저장
            with open('C:\Framerun\webserver\mode.json', 'w') as file:
                json.dump(mode_data, file)

    mode_type = mode_data['mode']

    print("mode check >>> " + mode_type)

    return mode_type

# mode2 관리
@bp.route('/mode2_edit', methods=['POST', 'GET'])
def mode2_edit():
    with open(r"C:\Framerun\webserver\mode.json", "r") as file:
            mode_data = json.load(file)

    if mode_data['mode'] == "mode_2":
        client_data = request.json
        mode_data.update(client_data)

        with open(r"C:\Framerun\webserver\mode.json", "w") as file:
            json.dump(mode_data, file)

        return "1"
    
    else:
        return "0", 500

# mode2 체크
@bp.route('/mode2_check', methods=['POST', 'GET'])
def mode2_check():
    with open(r"C:\Framerun\webserver\mode.json", "r") as file:
            mode_data = json.load(file)
    if all(key in mode_data and mode_data[key] == "success" for key in ["face", "voice", "password"]):
        return "success"
    else:
        return "fail", 500
    

# mode3 관리
@bp.route('/mode3', methods=['POST'])
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
        return "fail", 500
