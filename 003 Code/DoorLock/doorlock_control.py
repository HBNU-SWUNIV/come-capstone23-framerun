import time
import RPi.GPIO as GPIO


def doorlock_control():
    GPIO.setwarnings(False)

    # 도어락 모터 GPIO
    GPIO.setmode(GPIO.BCM)
    pin_door = 6

    GPIO.setup(pin_door, GPIO.OUT, initial=GPIO.LOW)

    GPIO.output(pin_door, GPIO.HIGH)
    time.sleep(0.1)
    GPIO.output(pin_door, GPIO.LOW)  # 다시 LOW로, 안하면 모터 오류남
    time.sleep(0.1)

    time.sleep(2)

if __name__ == "__main__":
    doorlock_control()
