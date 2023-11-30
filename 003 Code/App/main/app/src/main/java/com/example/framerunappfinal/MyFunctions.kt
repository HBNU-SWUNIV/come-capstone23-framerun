package com.example.framerunappfinal

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MyGlobals {
    companion object {
        val BASE_URL = "url입력"
    }
}

//public suspend fun convertPcmToWav(
//    pcmFile: File,
//    wavFile: File,
//    headerSize: Int,
//    audioFormat: Int,
//    sampleRate: Int,
//    channels: Int,
//) : Boolean {
//    return withContext(Dispatchers.IO) {
//        try {
//            val dataSize = pcmFile.length().toInt()
//            val totalSize = dataSize + headerSize
//            val byteRate = sampleRate * channels * (audioFormat / 8)
//
//            val wavBuffer = ByteBuffer.allocate(headerSize + dataSize)
//            wavBuffer.order(ByteOrder.LITTLE_ENDIAN)
//
//            // WAV 파일 헤더 작성
//            wavBuffer.put("RIFF".toByteArray(Charsets.US_ASCII)) // ChunkID
//            wavBuffer.putInt(totalSize - 8) // ChunkSize
//            wavBuffer.put("WAVE".toByteArray(Charsets.US_ASCII)) // Format
//            wavBuffer.put("fmt ".toByteArray(Charsets.US_ASCII)) // Subchunk1ID
//            wavBuffer.putInt(16) // Subchunk1Size
//            wavBuffer.putShort(1) // AudioFormat (PCM)
//            wavBuffer.putShort(1.toShort()) // NumChannels (모노)
//            wavBuffer.putInt(sampleRate) // SampleRate
//            wavBuffer.putInt(sampleRate * 2) // ByteRate (SampleRate * NumChannels * BitsPerSample/8)
//            wavBuffer.putShort(2.toShort()) // BlockAlign (NumChannels * BitsPerSample/8)
//            wavBuffer.putShort(16.toShort()) // BitsPerSample (16비트)
//
//            wavBuffer.put("data".toByteArray(Charsets.US_ASCII)) // Subchunk2ID
//            wavBuffer.putInt(dataSize) // Subchunk2Size
//
//            // PCM 데이터 추가
//            val inputStream = FileInputStream(pcmFile).channel
//            inputStream.read(wavBuffer)
//            inputStream.close()
//
//            wavBuffer.flip()
//
//            // WAV 파일에 데이터 쓰기
//            val fileChannel = FileOutputStream(wavFile).channel
//            fileChannel.write(wavBuffer)
//            fileChannel.close()
//            Log.d("MainActivity", "PCM to WAV conversion successful")
//            true
//
//        } catch (e: Exception) {
//            Log.e("MainActivity", "Error converting PCM to WAV: ${e.message}", e)
//            false
//        }
//    }
//}

public fun putPasswordMode(mode:String, hashed_pass : String? = null, curr: String? = null) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val endpoint = "mode_set"
            val url = "${MyGlobals.BASE_URL}${endpoint}"


            // JSON 객체 생성
            val jsonObject = JSONObject()
            jsonObject.put("mode", mode)
            jsonObject.put("password",hashed_pass)
            jsonObject.put("time", curr)

            // OkHttp 클라이언트 인스턴스 생성
            val client = OkHttpClient()

            // JSON 객체를 문자열로 변환
            val requestBody = jsonObject.toString()

            // 요청 객체 생성
            val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody(MEDIA_TYPE_JSON))
                .build()

            // 요청을 동기적으로 실행하고 응답을 받음
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseBody = response.body?.string()
                println(responseBody)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}

//서버에 post로 request하기
public fun postUsername(username: String) {
    //i/o작업을 수행하기 위한 코루틴 스코프 생성(백그라운드 실행)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            //서버의 엔드포인트(경로)설정
            val endpoint = "join_user"
            //요청에 포함될 사용자 이름
            val paramName = "user_name"

            //서버로 보낼 요청의 url 생성, 사용자 이름 utf-8로 인코딩
            val url = "${MyGlobals.BASE_URL}${endpoint}?${paramName}=${URLEncoder.encode(username, "UTF-8")}"
            //빈 JSONObject를 생성
            val jsonBody = JSONObject()
            //OkHttp 클라이언트 인스턴스를 생성
            val client = OkHttpClient()
            //요청의 미디어 타입을 설정
            val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

            //JSONObject에 "user_name" 키와 사용자 이름 값을 설정
            jsonBody.put("user_name", username)

            //JSONObject를 문자열 형태로 변환
            val requestBody = jsonBody.toString()

            //요청 객체를 생성
            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody(MEDIA_TYPE_JSON))
                .build()

            //요청을 동기적으로 실행하고 응답을 받음
            //use 함수를 사용하여 응답 처리가 끝난 후에 자원을 해제
            client.newCall(request).execute().use { response ->

                Log.d(ContentValues.TAG, "서버신호보냄 ")

                //응답이 성공적이지 않은 경우 예외를 발생
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseBody = response.body?.string()

                //응답 바디의 내용을 출력
                println(response.body!!.string())
                Log.d(ContentValues.TAG, "서버신호보냄 ")

            }
            //예외가 발생한 경우 해당 예외를 처리
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

//서버에 post로 request하기
public fun postToken(token: String) {
    //i/o작업을 수행하기 위한 코루틴 스코프 생성(백그라운드 실행)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            //서버의 엔드포인트(경로)설정
            val endpoint = "token"


            //서버로 보낼 요청의 url 생성, 사용자 이름 utf-8로 인코딩
            val url = "${MyGlobals.BASE_URL}${endpoint}"
            //빈 JSONObject를 생성
            val jsonBody = JSONObject()
            //OkHttp 클라이언트 인스턴스를 생성
            val client = OkHttpClient()
            //요청의 미디어 타입을 설정
            val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

            //JSONObject에 "user_name" 키와 사용자 이름 값을 설정
            jsonBody.put("token", token)

            //JSONObject를 문자열 형태로 변환
            val requestBody = jsonBody.toString()

            //요청 객체를 생성
            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody(MEDIA_TYPE_JSON))
                .build()

            //요청을 동기적으로 실행하고 응답을 받음
            //use 함수를 사용하여 응답 처리가 끝난 후에 자원을 해제
            client.newCall(request).execute().use { response ->
                //응답이 성공적이지 않은 경우 예외를 발생
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseBody = response.body?.string()

                //응답 바디의 내용을 출력
                println(response.body!!.string())

            }
            //예외가 발생한 경우 해당 예외를 처리
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

public fun uploadAudioFile(context: Context,file : File){
    CoroutineScope(Dispatchers.IO).launch {
        try {
            //서버의 엔드포인트(경로)설정
            val endpoint = "run_voice"
            val paramName = "name"
            val filename = file.name

            //서버로 보낼 요청의 url 생성, 사용자 이름 utf-8로 인코딩
            val url = "${MyGlobals.BASE_URL}${endpoint}?${paramName}=${URLEncoder.encode(filename, "UTF-8")}"

            Log.d(TAG,"url: $url")

            //서버로 보낼 요청의 url 생성, 사용자 이름 utf-8로 인코딩
            val client = OkHttpClient()

//            //빈 JSONObject를 생성
//            val jsonBody = JSONObject()
//            //OkHttp 클라이언트 인스턴스를 생성
//            //요청의 미디어 타입을 설정
//            val MEDIA_TYPE_JSON = "audio/json; charset=utf-8".toMediaType()
//
//
//            //JSONObject를 문자열 형태로 변환
//            val requestBody = jsonBody.toString()

//
//            val MEDIA_TYPE = "audio/wav".toMediaType()
//
//            // 파일을 포함하는 multipart/form-data 요청 본문 생성
//            val requestBody = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("file", file.name, file.asRequestBody(MEDIA_TYPE))
//                .build()


            val fileRequestBody = file.asRequestBody("audio/wav".toMediaType())

            Log.d(ContentValues.TAG, "fileRequestBody : $fileRequestBody")

            val request = Request.Builder()
                .url(url) // 서버의 URL
                .post(fileRequestBody)
                .build()



            // 요청 실행
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseBody = response.body?.string()
                println(responseBody)
                if(responseBody == "fail"){
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "화자인식을 다시하세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                if(responseBody=="success"){
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "화자인식을 성공했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
