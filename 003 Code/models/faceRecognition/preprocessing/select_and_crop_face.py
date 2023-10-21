import os
import cv2
import glob
import shutil
import zipfile
from PIL import Image
from tqdm import tqdm

# 액세서리는 비착용 1종에 대해서만 수행
# 인물.zip > 액세서리 S (6가지) > 조명 세기 및 방향 L (30가지) > 표정 E (3가지) > 각도 C (20가지)
zip_path = glob.glob("./Middle_Resolution/*.zip") # KFACE zip 파일 경로 작성

person = []
for z in zip_path:
    person.append(os.path.basename(z).split('.')[0]) # 인물 고유 식별 번호 저장
    
    # 1 : 기본, 2, 3 : 안경, 4: 선글라스, 5 : 모자, 6 : 모자 + 안경
    accessory = ["S001", "S002", "S003", "S005", "S006"]
    
    # 가림 없는 L1, L3, L6, L7의 경우 바운딩 박스 제공 / L6, L7은 너무 어두움, L4, L5는 약간 어두움, L1 : 아주 밝음 / L3 : 보통 밝음
    lux = ["L1", "L3", "L4", "L5"] 
    
    # 모든 표정에 대한 이미지 사용
    emotion = ["E01", "E02", "E03"] 

    # 필요한 각도만 설정
    angle = ["C6", "C7", "C8", "C9", "C19", "C20"]
    
    img_path = [] # 이미지 경로 저장
    txt_path = []
    
    for a in accessory:
        for l in lux:
            for e in emotion:
                for c in angle:
                    img_path.append(a + '/' + l + '/' + e + '/' + c + '.jpg')
                    txt_path.append(a + '/' + l + '/' + e + '/' + c + '.txt')

    for z, c in tqdm(zip(zip_path, person)):
        for i, t in zip(img_path, txt_path):
            zipfile.ZipFile(z).extract("S001/" + i, "./Middle/" + c)
            zipfile.ZipFile(z).extract("S001/" + t, "./Middle/" + c)
    
    # crop
    for j, c in enumerate(person):
        imgs = glob.glob("AI_HUB/" + c + "/*/*/*/*.jpg")
        txts = glob.glob("AI_HUB/" + c + "/*/*/*/*.txt")

        for i, (img, txt) in enumerate(zip(imgs, txts)):
            name = str(i)
            with open(txt, 'r') as f:
                bbox = f.read().split('\n')[7].split()
                bbox = list(map(int, bbox))
                (x, y, w, h) = bbox

                img = cv2.imread(img)
                img = img[y: y + h, x: x + w]
                img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

                if j >= 390:
                    base_val = "face_valid/" + str(j - 390)
                    if not os.path.exists(base_val):
                        os.makedirs(base_val)
                    Image.fromarray(img).save(os.path.join(base_val, str(j-390) + '_' + name) + '.jpg')
                else:
                    base = "face_train/" + str(j)

                    if not os.path.exists(base):
                        os.makedirs(base)
                    Image.fromarray(img).save(os.path.join(base, str(j) + '_' + name) + '.jpg')