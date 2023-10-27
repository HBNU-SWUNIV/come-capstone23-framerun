import socket


def check_network_connection():
    try:
        # 구글의 DNS 서버에 연결 시도
        socket.create_connection(("8.8.8.8", 53), timeout=3)
        print
        return True
    except OSError:
        pass
    return False


if check_network_connection():
    print("네트워크에 연결되어 있습니다.")
else:
    print("네트워크에 연결되어 있지 않습니다.")
