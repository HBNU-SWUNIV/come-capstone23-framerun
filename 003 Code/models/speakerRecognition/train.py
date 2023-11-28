import os
import glob
import time
import librosa
import numpy as np
import tensorflow as tf
import tensorflow.compat.v1 as tf
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split

tf.random.set_random_seed(777)
tf.compat.v1.disable_eager_execution()
tf.compat.v1.reset_default_graph()

# 설정 변수 정의
DATA_TRAIN_PATH = "./data/train"
learning_rate = 0.005
training_epochs = 25 # 한 사람당 epoch
batch_size = 2
n_mfcc = 128
train_status = True

def preprocessing(path):
    global X_train, X_test, Y_train, Y_test, tf_classes, train_status
    X_train, Y_train = [], []
    X_test, Y_test = [], []

    X_data = []
    Y_label = []

    folders = sorted(list(set(os.listdir(path)) - set(['.ipynb_checkpoints'])))
    print("Speaker id", folders)

    tf_classes = 0
    for folder in folders:
        if not os.path.isdir(path): continue
        files = os.listdir(path + "/" + folder)
        print(f"\nFolder name : {folder}- ({len(files)} files)")
        for wav in files:
            if not wav.endswith(".wav"): continue
            else:
                print("\tFile name :", wav)
                y, sr = librosa.load(path + "/" + folder + "/" + wav)
                mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=128, hop_length=int(sr * 0.01), n_fft=int(sr * 0.02)).T

                # 데이터를 frame 단위(by hop_length)로 나누면 ROW만큼의 데이터셋이 구축
                X_data.extend(mfcc)

                # one-hot encoding
                label = [0 for i in range(len(folders))]
                label[tf_classes] = 1

                for i in range(len(mfcc)):
                    Y_label.append(label)
                print(f"\t\t{label}")
                
        tf_classes = tf_classes + 1
        if tf_classes > 9:
            # print("Maximum capacity: 10 users.")
            train_status = False
            break

    print()
    
    if train_status:
        print("X_data:", np.shape(X_data))
        print("Y_label:", np.shape(Y_label))
        X_train, X_test, Y_train, Y_test = train_test_split(np.array(X_data), np.array(Y_label), test_size=.2)
    
        xy = (X_train, X_test, Y_train, Y_test)
        np.save("./data.npy", xy)

class NeuralNetwork:
    def __init__(self):
        self.X_train = X_train
        self.Y_train = Y_train
        self.X_test = X_test
        self.Y_test = Y_test
        self.tf_classes = tf_classes
        self.learning_rate = learning_rate
        self.training_epochs = training_epochs
        self.batch_size = batch_size

        self.X = tf.compat.v1.placeholder(tf.float32, [None, 128])
        self.Y = tf.compat.v1.placeholder(tf.float32, [None, self.tf_classes])
        self.keep_prob = tf.compat.v1.placeholder(tf.float32)
        sd = 1 / np.sqrt(128)

        # Hidden layers
        layers = [128, 256, 256, 256, 128, 128, 128, self.tf_classes]
        A = self.X
        for i in range(len(layers) - 1):
            with tf.name_scope('layer{}'.format(i + 1)):
                W = tf.Variable(tf.random_normal([layers[i], layers[i + 1]], mean=0, stddev=sd), name='W{}'.format(i + 1))
                b = tf.Variable(tf.random_normal([layers[i + 1]], mean=0, stddev=sd), name='b{}'.format(i + 1))
                Z = tf.add(tf.matmul(A, W), b)
                if i == len(layers) - 2:
                    self.hypothesis = Z                    
                elif i == 1:
                    A = tf.nn.tanh(Z)
                    A = tf.nn.dropout(A, keep_prob=self.keep_prob)                   
                else:
                    A = tf.nn.relu(Z)
                    A = tf.nn.dropout(A, keep_prob=self.keep_prob)

        self.cost = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=self.hypothesis, labels=self.Y))
        self.optimizer = tf.train.AdamOptimizer(learning_rate=self.learning_rate).minimize(self.cost)
        self.is_correct = tf.equal(tf.argmax(self.hypothesis, 1), tf.argmax(self.Y, 1))

    def train(self):
        split_X = np.split(self.X_train, self.batch_size)
        split_Y = np.split(self.Y_train, self.batch_size)

        saver = tf.train.Saver()        
        sess = tf.Session()
        sess.run(tf.global_variables_initializer())

        best_acc = -1
        start = time.time()

        for epoch in range(self.training_epochs * self.tf_classes):
            avg_cost = 0

            for i in range(self.batch_size):
                batch_xs = split_X[i]
                batch_ys = split_Y[i]
                feed_dict = {self.X: batch_xs, self.Y: batch_ys, self.keep_prob: 0.7}
                c, _ = sess.run([self.cost, self.optimizer], feed_dict=feed_dict)
                avg_cost += c / self.batch_size

            # Compute training accuracy
            train_correct_prediction = sess.run(self.is_correct, feed_dict={self.X: self.X_train, self.Y: self.Y_train, self.keep_prob: 1.0})
            train_accuracy = np.mean(train_correct_prediction)

            # Compute test accuracy
            test_correct_prediction = sess.run(self.is_correct, feed_dict={self.X: self.X_test, self.Y: self.Y_test, self.keep_prob: 1.0})
            test_accuracy = np.mean(test_correct_prediction)

            if test_accuracy > best_acc:
                best_acc = test_accuracy
                saver.save(sess, f"./checkpoint/best")

            print('Epoch:', '%04d' % (epoch), '| loss : ', '{:.9f}'.format(avg_cost), '| train Acc:', train_accuracy, '| test Acc:', test_accuracy)

        correct_prediction = tf.equal(tf.argmax(self.hypothesis, 1), tf.argmax(self.Y, 1))
        accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
        print("Accuracy: ", sess.run(accuracy, feed_dict={self.X: self.X_test, self.Y: self.Y_test, self.keep_prob: 1}))

        end = time.time()
        print("ETA", end - start)
        print('Learning Finished!')
        
if __name__ == '__main__':
    preprocessing(DATA_TRAIN_PATH)

    if train_status:
        X_train, X_test, Y_train, Y_test = np.load("./data.npy", allow_pickle=True)
        X_train = X_train.astype("float32")
        X_test = X_test.astype("float32")
    
        model = NeuralNetwork()
        
        model.train()
        
    else: 
        print("Maximum capacity: 10 users.")