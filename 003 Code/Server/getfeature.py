import cv2
import numpy as np
import torch


def getFeature(model, image):
    # image = cv2.imread(img_path)
    image = cv2.resize(image, (112, 112))
    image = image.transpose((2, 0, 1))
    image = image[np.newaxis, :, :, :]
    image = image.astype(np.float32, copy=False)
    image -= 127.5
    image /= 127.5

    data = torch.from_numpy(image)
    data = data.to(torch.device('cuda'))
    feature = model(data)
    feature = feature.data.cpu().numpy()[0]

    return feature
