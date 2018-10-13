package com.angad.omnify

import android.app.Application
import com.angad.omnify.helpers.AppUtils
import com.angad.omnify.networks.HackerNewsService
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp
import io.realm.Realm
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppController: Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        FirebaseApp.initializeApp(this)
        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(this)
    }

    companion object {
        private var service: HackerNewsService? = null

        fun getService(): HackerNewsService? {
            if(service == null) {
                initRetrofit()
            }
            return service
        }

        fun initRetrofit() {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            val retrofit = Retrofit.Builder()
                    .baseUrl(AppUtils.HACKERNEWS_API_ENDPOINT)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            service = retrofit.create(HackerNewsService::class.java)
        }
    }
}