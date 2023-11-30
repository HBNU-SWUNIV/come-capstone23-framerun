package com.example.framerunappfinal.CheckVisitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.framerunappfinal.MainActivity
import com.example.framerunappfinal.R
import com.example.framerunappfinal.postToken


import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FirebaseService"


    //Token 생성 메서드(FirebaseInstanceIdService 사라짐)
    override fun onNewToken(token: String) {
        Log.d(TAG, "new Token: $token")

        // 토큰 값을 따로 저장
        val pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("token", token).apply()
        editor.commit()
        Log.i(TAG, "성공적으로 토큰을 저장함")
    }

    // 메시지 수신 메서드(포그라운드)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage!!.from)


        //받은 remoteMessage의 값 출력해보기. 데이터메세지 / 알림메세지
        Log.d(TAG, "Message data : ${remoteMessage.data}")
        Log.d(TAG, "Message noti : ${remoteMessage.notification}")


        // 데이터 저장
        val image = remoteMessage.data["image"]
        val title = remoteMessage.data["body"]
        if (image != null && title != null) {
            IntruderDataRepository.addIntruder(Intruder(image = image, title = title))
        }

        if(remoteMessage.data.isNotEmpty()){
            handleDataMessage(remoteMessage)
        }
        remoteMessage.notification?.let{
            handleNotificationMessage(it)
        }

    }

    private fun handleDataMessage(remoteMessage: RemoteMessage) {
        val image = remoteMessage.data["image"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        // 데이터 메시지에 대한 추가 처리
        // 예: 알림 생성, 데이터 저장 등
        sendNotification(title, body)
    }

    private fun handleNotificationMessage(notification: RemoteMessage.Notification) {
        val title = notification.title
        val body = notification.body

        sendNotification(title, body)
    }

    //알림 생성 메서드
//    private fun sendNotification(remoteMessage: RemoteMessage) {
    private fun sendNotification(title: String?, body: String?) {
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        val intent = if (title == "도어락 외부인 접근 알림") {
            Intent(this, CheckVisitorFragment::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE)


        val channelId = "my_channel"
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 알림 내용 설정
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title) // 수정됨
            .setContentText(body)  // 수정됨
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 이후에는 채널이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notice", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        notificationManager.notify(uniId, notificationBuilder.build())

    }

    // Token 가져오기 -> 서버로 토큰 번호 전송하기
    fun getFirebaseToken() {
        //비동기 방식
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.d(TAG, "token=${it}")
            postToken(it)
        }


    }
}
