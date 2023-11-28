import pandas as pd
import tensorflow.compat.v1 as tf

if __name__ == '__main__':

    with tf.Session() as sess:
        path = "./data/train"
        user_num = len(sorted(list(set(os.listdir(path)) - set(['.ipynb_checkpoints']))))

        if 7 < user_num <= 10:
            th = 0.75
        elif 4 < user_num <=7:
            th = 0.80
        elif 2 < user_num <= 4:
            th = 0.95
        elif user_num <= 2:
            th = 0.97
            
        saver = tf.train.import_meta_graph('./checkpoint/best.meta')  # 모델 메타데이터 불러오기
        saver.restore(sess, tf.train.latest_checkpoint('./checkpoint'))  # 체크포인트 복원

        hypothesis = tf.get_default_graph().get_tensor_by_name('layer7/Add:0')

        X = tf.compat.v1.get_default_graph().get_tensor_by_name('Placeholder:0')
        Y = tf.compat.v1.get_default_graph().get_tensor_by_name('Placeholder_1:0')
        keep_prob = tf.compat.v1.get_default_graph().get_tensor_by_name('Placeholder_2:0')
        
        y, sr = librosa.load("./data/test/6/6_19.wav") # load your test data
        X_test = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=128,
                                      hop_length=int(sr*0.01),n_fft=int(sr*0.02)).T
        

        correct_prediction = tf.equal(tf.argmax(hypothesis, 1), tf.argmax(Y, 1))


        predict = pd.value_counts(pd.Series(sess.run(tf.argmax(hypothesis, 1),
                                                feed_dict={X: X_test, keep_prob:1})))
        predict_id = predict.index[0]
        print(f"Predict\n{predict}")
        print("========")
        
        acc = predict[predict_id]/len(X_test)
        print(acc)
        print(th)
        if acc > th:
            print(f"\n{predict_id} 님, 인증되었습니다!")
        else:
            print("\nunknown : 등록되어있지 않습니다.")
        
        # 나머지 사용자로 인식할 확률
        acc_list = predict / len(X_test)
        print(acc_list)
