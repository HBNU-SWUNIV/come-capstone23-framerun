import librosa
import pandas as pd
import tensorflow.compat.v1 as tf
import time

def voice_test(filepath):
    t1 = time.time() 

    with tf.Session() as sess:
        'c:/FRAMERUN/voice_run/data/test/'
        saver = tf.train.import_meta_graph('c:/FRAMERUN/voice_run/checkpoint/best.meta')  # 모델 메타데이터 불러오기
        saver.restore(sess, tf.train.latest_checkpoint('c:/FRAMERUN/voice_run/checkpoint'))  # 체크포인트 복원

        hypothesis = tf.get_default_graph().get_tensor_by_name('layer7/Add:0')

        X = tf.compat.v1.get_default_graph().get_tensor_by_name('Placeholder:0')
        Y = tf.compat.v1.get_default_graph().get_tensor_by_name('Placeholder_1:0')
        keep_prob = tf.compat.v1.get_default_graph().get_tensor_by_name('Placeholder_2:0')
        
        y, sr = librosa.load(filepath)
        X_test = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=128,
                                      hop_length=int(sr*0.01),n_fft=int(sr*0.02)).T
        

        correct_prediction = tf.equal(tf.argmax(hypothesis, 1), tf.argmax(Y, 1))


        predict = pd.value_counts(pd.Series(sess.run(tf.argmax(hypothesis, 1),
                                                feed_dict={X: X_test, keep_prob:1})))
        predict_id = predict.index[0]
        print(f"Predict\/n{predict}")
        # print("========")
        
        if predict_id in [0,1,2,3,4]:
            print("\nunknown : 등록되어있지 않습니다.")
        else:
            acc = predict[predict_id]/len(X_test)
            
            # # 나머지 사용자로 인식할 확률
            # acc_list = predict / len(X_test)
            # print(acc_list)
    
            if acc > 0.80:
                print("open")
                return "success"
            else:
                print("\nunknown : 등록되어있지 않습니다.")
                return "fail"
        print(acc)
        t2 = time.time()

        print(t2-t1)
        

if __name__ == '__main__':

    path = 'c:/FRAMERUN/voice_run/data/test/' + 'so2 (1).wav'
    voice_test(path)
    print("ss")
