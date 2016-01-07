package com.shareyourproxy.app

import android.os.Bundle
import com.shareyourproxy.IntentLauncher.launchEmailIntent
import com.shareyourproxy.IntentLauncher.launchIntroductionActivity
import com.shareyourproxy.IntentLauncher.launchInviteFriendIntent
import com.shareyourproxy.IntentLauncher.launchUserProfileActivity
import com.shareyourproxy.R
import com.shareyourproxy.api.rx.JustObserver
import com.shareyourproxy.api.rx.RxBusRelay
import com.shareyourproxy.api.rx.RxGoogleAnalytics
import com.shareyourproxy.api.rx.event.SelectDrawerItemEvent
import com.shareyourproxy.app.adapter.DrawerAdapter
import com.shareyourproxy.app.dialog.ShareLinkDialog
import com.shareyourproxy.app.fragment.AggregateFeedFragment
import rx.subscriptions.CompositeSubscription
import timber.log.Timber


/**
 * The main landing point after loggin in. This is tabbed activity with [Contact]s and [Group]s.
 */
private final class AggregateFeedActivity : BaseActivity() {
    private val analytics = RxGoogleAnalytics(this)
    private val subscriptions: CompositeSubscription = CompositeSubscription()
    private val busObserver: JustObserver<Any> get() = object : JustObserver<Any>() {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override fun next(event: Any) {
            if (event is SelectDrawerItemEvent) {
                onDrawerItemSelected(event)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val aggregateFeedFragment = AggregateFeedFragment()
            supportFragmentManager.beginTransaction().replace(android.R.id.content, aggregateFeedFragment).commit()
        }
    }

    /**
     * [SelectDrawerItemEvent]. When a drawer item is selected, call a proper event flow.
     * @param event data
     */
    fun onDrawerItemSelected(event: SelectDrawerItemEvent) {
        when (event.drawerItem) {
            DrawerAdapter.DrawerItem.PROFILE -> {
                val user = loggedInUser
                analytics.userProfileViewed(user)
                launchUserProfileActivity(this, user, user.id)
            }
            DrawerAdapter.DrawerItem.SHARE_PROFILE -> ShareLinkDialog(loggedInUser.groups).show(supportFragmentManager)
            DrawerAdapter.DrawerItem.INVITE_FRIEND -> launchInviteFriendIntent(this)
            DrawerAdapter.DrawerItem.TOUR -> launchIntroductionActivity(this)
            DrawerAdapter.DrawerItem.REPORT_ISSUE -> launchEmailIntent(this, getString(R.string.contact_proxy))
            DrawerAdapter.DrawerItem.HEADER -> {
            }
            else -> Timber.e("Invalid drawer item")
        }
    }

    override fun onResume() {
        super.onResume()
        subscriptions.add(RxBusRelay.rxBusObservable().subscribe(busObserver))
    }

    override fun onPause() {
        super.onPause()
        subscriptions.unsubscribe()
    }

}