package com.saitejajanjirala.weather_tracker.di

import android.content.Context
import com.saitejajanjirala.weather_tracker.BuildConfig
import com.saitejajanjirala.weather_tracker.data.local.DataStoreManager
import com.saitejajanjirala.weather_tracker.data.remote.ApiService
import com.saitejajanjirala.weather_tracker.util.Util
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesOkHttpClient(@ApplicationContext context: Context):OkHttpClient{
        val interceptor = Interceptor { chain ->
            val request = chain.request()
            val url = request.url
            val newUrl = url.newBuilder()
                .addQueryParameter("key",BuildConfig.API_KEY)
                .build()
            val newRequest = request.newBuilder()
                .url(newUrl)
                .build()
            chain.proceed(newRequest)
        }
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(interceptor)
            .addInterceptor(InternetInterceptor(context))
            .build()
    }


    @Provides
    @Singleton
    fun providesApiService(okHttpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl(Util.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                MoshiConverterFactory.create(
                Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
            ))
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun providesDataStoreManager(@ApplicationContext context: Context) :DataStoreManager{
        return DataStoreManager(context)
    }
}