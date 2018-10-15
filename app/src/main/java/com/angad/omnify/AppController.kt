package com.angad.omnify

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.angad.omnify.helpers.AppUtils
import com.angad.omnify.networks.HackerNewsService
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp
import com.uphyca.stetho_realm.RealmInspectorModulesProvider
import io.realm.Realm
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * application class for app omnify
 */
class AppController: Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        FirebaseApp.initializeApp(this)
        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(this)

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build()).build())
    }

    /**
     * multidex support for app
     */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    /**
     * companion object for service object
     */
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