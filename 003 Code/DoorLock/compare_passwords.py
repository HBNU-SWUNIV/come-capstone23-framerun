import bcrypt


def compare_passwords():
    try:
        user_input = input("비밀번호를 입력하세요: ")
        with open("/home/framerun/Desktop/도어락/password.txt", "r") as file:
            save_password = file.read()
        result = bcrypt.checkpw(
            user_input.encode("utf-8"), save_password.encode("utf-8")
        )
        print(result)
        return result
    except FileNotFoundError:
        print("error1")
        return "error"
    except IOError:
        print("error2")
        return "error"


# compare_passwords()
