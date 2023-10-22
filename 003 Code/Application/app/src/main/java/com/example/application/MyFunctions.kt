package com.example.application

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class MyGlobals {
    companion object {
        val BASE_URL = "http://192.168.200.151:5000/"
    }
}

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
            val endpoint = "userjoin"
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