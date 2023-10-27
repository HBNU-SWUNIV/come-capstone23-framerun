import bcrypt
from compare_passwords import compare_passwords


def setting_password():
    lock_cnt = 0
    with open(r"/home/framerun/Desktop/도어락/password.txt", "r") as file:
        check = file.read()

    # 비밀번호 설정하기 전, 사용자 인증
    if check:
        print("비밀번호 재설정을 하려면 기존의 비밀번호를 입력해야 합니다.")

        while True:
            if compare_passwords() == False:
                lock_cnt += 1
                if lock_cnt != 5:
                    print("재시도 하세요. 남은 횟수는 ", 5 - lock_cnt)
                else:
                    print("Locked")
                    return "Locked"
            else:
                break

    while True:
        user_input = input("설정할 비밀번호 6자리를 입력하세요: ")

        if len(user_input) == 6 and user_input.isdigit():
            # 입력한 비밀번호를 bcrypt 알고리즘을 이용하여 해시화
            hashed_password = bcrypt.hashpw(
                user_input.encode("utf-8"), bcrypt.gensalt()
            )
            save_password = hashed_password.decode("utf-8")

            with open(r"/home/framerun/Desktop/도어락/password.txt", "w") as file:
                file.write(save_password)

            print("비밀번호 설정 완료")
            return "done"
        else:
            print("비밀번호 설정 실패")


# setting_password()