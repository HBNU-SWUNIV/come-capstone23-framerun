import subprocess
from datetime import datetime

import requests
from firebase_img_upload import firebase_Upload


def execute_camera(file_path):
    t1 = datetime.now()

    subprocess.run(
        [
            "libcamera-still",
            "--width",
            "640",
            "--height",
            "480",
            "-o",
            file_path,
            "-t",
            "1",
        ]
    )

    t2 = datetime.now()

    print(t2 - t1)

    print("사진 촬영 완료")

