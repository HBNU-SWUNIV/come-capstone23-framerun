from pydub import AudioSegment
import glob
import os

def m4a_to_wav(input_file, output_file):
    sound = AudioSegment.from_file(input_file, format="m4a")
    sound.export(output_file, format="wav")

if __name__ == "__main__":
    curr = os.getcwd()
    name = os.path.basename(curr)
    path = glob.glob("./*.m4a")
    for i in range(len(path)):
        input_file = path[i]  # 입력 파일 이름
        output_file = f"./{name}_{i}.wav"  # 출력 파일 이름
        m4a_to_wav(input_file, output_file)
        os.remove(input_file)
