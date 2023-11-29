import RPi._GPIO as GPIO
from time import sleep


def led(color):
    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)

    ## led setting
    if color == "blue" or color == "red":
        if color == "blue":
            led_pin = 12
        else:
            led_pin = 1

        GPIO.setup(led_pin, GPIO.OUT, initial=GPIO.LOW)

        # 밝기 조절
        pwm_led = GPIO.PWM(led_pin, 255)

        # led 실행
        pwm_led.start(0)
        pwm_led.ChangeDutyCycle(10)  # 밝기 : 1~100
        sleep(0.3)  # 키패드 입력 sleep까지 됨
        pwm_led.stop()

    elif color == "magenta":
        blue_pin = 12 #12
        red_pin = 1
        # led_pin = [12, 1]

        GPIO.setup(blue_pin, GPIO.OUT, initial=GPIO.LOW)
        GPIO.setup(red_pin, GPIO.OUT, initial=GPIO.LOW)

        # 밝기 조절
        pwm_led = GPIO.PWM(blue_pin, 255)
        pwm_led2 = GPIO.PWM(red_pin, 255)

        pwm_led.start(0)
        pwm_led2.start(0)
        pwm_led.ChangeDutyCycle(10)
        pwm_led2.ChangeDutyCycle(10)

        sleep(0.3)
        
        pwm_led.stop()
        pwm_led2.stop()

if __name__ == "__main__":
    color = "magenta"
    led(color)
