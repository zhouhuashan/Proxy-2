package com.shareyourproxy

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.support.multidex.MultiDex
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.facebook.FacebookSdk
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp.OkHttpImagePipelineConfigFactory
import com.shareyourproxy.Constants.MASTER_KEY
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.RxAppDataManager
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber

/**
 * Proxy application that handles syncing the current user and handling BaseCommands.
 */
class ProxyApplication : Application() {
    var currentUser: User? = null
    var sharedPreferences: SharedPreferences = getSharedPreferences(MASTER_KEY, Context.MODE_PRIVATE)

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    fun initialize() {
        FacebookSdk.sdkInitialize(this)
        MultiDex.install(this)
        if (BuildConfig.USE_LEAK_CANARY) {
            _refWatcher = LeakCanary.install(this)
        }
        RxAppDataManager.newInstance(this, sharedPreferences, getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        initializeBuildConfig()
        initializeRealm()
        initializeFresco()
    }

    fun initializeFresco() {
        val config = OkHttpImagePipelineConfigFactory.newBuilder(this, RestClient.client).build()
        Fresco.initialize(this, config)
    }

    fun initializeRealm() {
        val config = RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().schemaVersion(BuildConfig.VERSION_CODE.toLong()).build()
        Realm.setDefaultConfiguration(config)
    }

    fun initializeBuildConfig() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        val analytics = RxGoogleAnalytics(this)
        if (BuildConfig.USE_GOOGLE_ANALYTICS) {
            analytics.analytics.enableAdvertisingIdCollection(true)
            analytics.analytics.enableAutoActivityReports(this)
        } else {
            analytics.analytics.appOptOut = true
        }
        //Twitter and Crashlytics
        val authConfig = TwitterAuthConfig(BuildConfig.TWITTER_KEY, BuildConfig.TWITTER_SECRET)
        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, Twitter(authConfig), Crashlytics(), Answers())
        } else {
            Fabric.with(this, Twitter(authConfig), Answers())
        }
    }

    companion object {
        private var _refWatcher: RefWatcher? = null
        fun watchForLeak(obj: Any) {
            _refWatcher?.watch(obj)
        }
    }

}