# opencv 사진 촬영
import cv2


def execute_camera(folder_name, time_stamp, img_type):
    # 사진찍기
    # 중복없는 파일명 만들기

    filename = ".".join([time_stamp, img_type])

    cap = cv2.VideoCapture(0)  # 노트북 웹캠을 카메라로 사용
    cap.set(3, 640)  # 너비
    cap.set(4, 480)  # 높이

    ret, frame = cap.read()  # 사진 촬영
    # 필요한가?? frame = cv2.flip(frame, 1) # 좌우 대칭

    ########### 추가 ##################
    # [출처] OpenCV 카메라(CAM)에 텍스트(Text) 표시하기 파이썬 예제|작성자 천동이
    # frame이라는 이미지에 글씨 넣는 함수
    # frame : 카메라 이미지
    # str : 문자열 변수 ---> time_stamp
    # (0, 100) : 문자열이 표시될 좌표 x = 0, y = 100
    # cv2.FONT_HERSHEY_SCRIPT_SIMPLEX : 폰트 형태
    # 1 : 문자열 크기(scale) 소수점 사용가능
    # (0, 255, 0) : 문자열 색상 (r,g,b)
    cv2.putText(
        frame, time_stamp, (0, 100), cv2.FONT_HERSHEY_SCRIPT_SIMPLEX, 1, (0, 255, 0)
    )

    cv2.imwrite(
        "/home/framerun/Desktop/Camera/" + folder_name + "/" + filename, frame
    )  # 사진 저장

    cap.release()
    cv2.destroyAllWindows()

    print("사진 촬영 완료")
