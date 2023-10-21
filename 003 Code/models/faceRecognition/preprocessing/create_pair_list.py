import random

f= open("../data/pair.txt","w+")

# 동일인물에 대한 데이터
for person in range(10):
    for i in range(20):
        num1, num2 = random.sample(range(24), 2) 
        # d1 d2 동일인물여부(0/1)
        f.write(f"/workspace/faceRecognition/data/valid/{person}/{person}_{num1}.jpg /workspace/faceRecognition/data/valid/{person}/{person}_{num2}.jpg 1\n")

f.write("\n")

# 서로 다른 인물에 대한 데이터
for i in range(200):
        person1, person2 = random.sample(range(10), 2) 
        num1, num2 = random.sample(range(24), 2) 
        f.write(f"/workspace/faceRecognition/data/valid/{person1}/{person1}_{num1}.jpg /workspace/faceRecognition/data/valid/{person2}/{person2}_{num2}.jpg 0\n")