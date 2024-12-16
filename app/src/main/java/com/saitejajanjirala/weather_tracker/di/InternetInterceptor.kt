package com.saitejajanjirala.weather_tracker.di

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import com.saitejajanjirala.weather_tracker.util.Util.isInternetAvailable
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class InternetInterceptor @Inject constructor(@ApplicationContext private val context: Context):
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val available = (context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).isInternetAvailable()
        if(!available){
            throw NoInternetException(msg = "No Internet Connection Available")
        }

        return chain.proceed(chain.request())
    }


}

class NoInternetException ( msg : String) : IOException(msg)