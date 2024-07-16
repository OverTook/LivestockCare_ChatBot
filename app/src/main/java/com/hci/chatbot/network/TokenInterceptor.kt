package com.hci.chatbot.network

import okhttp3.Interceptor
import okhttp3.Response


class TokenInterceptor(private val firebaseToken: String, private val userId: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Authorization", "Bearer $firebaseToken") //Bearer는 OAuth2.0 표준을 따르기 위해 추가하였음
            .header("User-ID", userId) //서버단에서 토큰 정보로 조회한 유저 아이디와 이 헤더의 유저 아이디를 비교하여 검증
            .method(original.method, original.body)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}