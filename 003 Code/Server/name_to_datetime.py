from datetime import datetime

# 코드 설명: 외부인 사진 파일 이름을 토대로 시간, 날짜 파싱


def name_to_datetime(str):

    # 날짜와 시간을 추출
    date_str = str[:8]
    time_str = str[9:15]

    # 날짜를 datetime 객체로 변환
    date = datetime.strptime(date_str, "%Y%m%d").date()

    # 시간을 datetime 객체로 변환
    time = datetime.strptime(time_str, "%H%M%S").time()

    # 포맷에 맞게 출력
    formatted_date = date.strftime("%Y년 %m월 %d일 ")
    formatted_time = time.strftime("%H시 %M분 %S초")

    return formatted_date + formatted_time
