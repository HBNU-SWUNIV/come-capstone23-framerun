from keypad import keypad
import time
from datetime import datetime, timedelta
import RPi._GPIO as GPIO
from time import sleep
from led import led

# 도어락 키패드 용
def input_password():
    ## led setting
    led_blue = 12
    led_red = 1

    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    GPIO.setup(led_blue, GPIO.OUT, initial=GPIO.LOW)
    GPIO.setup(led_red, GPIO.OUT, initial=GPIO.LOW)

    # pwm_led = GPIO.PWM(led, 255)

    kp = keypad()

    print("패스워드를 입력하세요. 키 입력 마지막은 '*' ")

    input_array = []

    while True:
        t1 = datetime.now()
        kp = keypad()
        digit = None

        while digit == None:
            t2 = datetime.now()
            if t2 - t1 >= timedelta(seconds=10):
                return "Timeout"

            digit = kp.getKey()

        if digit == "*":
            if len(input_array) < 4:
                # GPIO.output(led_red, 1)
                # sleep(0.3)
                # GPIO.output(led_red, 0)
                led("red")
                print("패스워드 길이가 짧습니다. 다시 입력하세요")

                input_array = []
                continue
            else:
                break
        # print("led")
        # GPIO.output(led_blue, 1)
        # sleep(0.3)
        # GPIO.output(led_blue, 0)
        led("blue")

        input_array.append(digit)

        if len(input_array) > 10:
            # GPIO.output(led_red, 1)
            # time.sleep(0.3)
            # GPIO.output(led_red, 0)
            led("red")
            print("패스워드 길이가 10을 초과했습니다. 다시 입력하세요")
            input_array = []

    # str로 변환
    input_array = "".join(map(str, input_array))
    return input_array

# ####### 키패드 대체용
# def input_password():
#     print("패스워드를 입력하세요. 키 입력 마지막은 '*' ")

#     while True:
#         p = input()
#         password = p.split("*")[0]
#         if 4 <= len(password) <= 10:
#             # print(type(password))///
#             return str(password)
#         elif len(password) < 4:
#             print("패스워드 길이가 짧습니다. 다시 입력하세요")
#         else:
#             print("패스워드 길이가 10을 초과했습니다. 다시 입력하세요")

if __name__ == "__main__":
    a = input_password()
    print(a)
