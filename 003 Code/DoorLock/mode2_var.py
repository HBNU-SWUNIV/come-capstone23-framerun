import requests
import json

def mode2_var(key, value):

    # 업데이트할 JSON 데이터
    client_data = {key: value}

    # 서버에 POST 요청 보내기
    response = requests.post("http://ip 주소/mode2_edit", json=client_data)

    # 업데이트된 JSON 데이터 확인
    if response.status_code == 200:
        updated_data = response.json()
        print("mode2 업데이트 성공")
    else:
        print("mode2 업데이트 실패")
