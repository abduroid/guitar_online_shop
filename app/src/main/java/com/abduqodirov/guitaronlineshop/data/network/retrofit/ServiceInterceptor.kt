package com.abduqodirov.guitaronlineshop.data.network.retrofit

import com.abduqodirov.guitaronlineshop.data.local_chaching.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

class ServiceInterceptor @Inject constructor(private val tokenManager: TokenManager) : Interceptor {

    var sessionToken: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {

        val token = tokenManager.getToken()
        sessionToken = token

        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $sessionToken")
            .addHeader("Cookie", "refreshToken=$sessionToken")
            .build()
        Timber.d("interceptor yasaldi $sessionToken")

        return chain.proceed(request)
    }
}
