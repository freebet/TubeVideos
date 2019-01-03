/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.util.Log
import android.view.*
import com.amazon.android.webkit.AmazonWebKitFactories
import com.amazon.android.webkit.AmazonWebKitFactory
import kotlinx.android.synthetic.main.activity_start.*
import org.mozilla.focus.architecture.NonNullObserver
import org.mozilla.focus.browser.BrowserFragment
import org.mozilla.focus.browser.BrowserFragment.Companion.APP_URL_HOME
import org.mozilla.focus.browser.InfoActivity
import org.mozilla.focus.ext.toSafeIntent
import org.mozilla.focus.home.pocket.Pocket
import org.mozilla.focus.home.pocket.PocketOnboardingActivity
import org.mozilla.focus.iwebview.IWebView
import org.mozilla.focus.iwebview.WebViewProvider
import org.mozilla.focus.locale.LocaleAwareAppCompatActivity
import org.mozilla.focus.session.Session
import org.mozilla.focus.session.SessionManager
import org.mozilla.focus.session.Source
//import org.mozilla.focus.telemetry.SentryWrapper
//import org.mozilla.focus.telemetry.TelemetryWrapper
//import org.mozilla.focus.telemetry.UrlTextInputLocation
import org.mozilla.focus.utils.OnUrlEnteredListener
import org.mozilla.focus.utils.SafeIntent
import org.mozilla.focus.utils.Settings
import org.mozilla.focus.utils.ViewUtils
import org.mozilla.focus.utils.publicsuffix.PublicSuffix
import org.mozilla.focus.widget.InlineAutocompleteEditText
import java.lang.System.exit

class FireTVActivity : LocaleAwareAppCompatActivity(), OnUrlEnteredListener {

    private val sessionManager = SessionManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.v("MainActivity", "@@@@@@MainActivity onCreate111111")

        // Enable crash reporting. Don't add anything above here because if it crashes, we won't know.
        //SentryWrapper.init(this)
        Pocket.init()
        PublicSuffix.init(this) // Used by Pocket Video feed & custom home tiles.

        initAmazonFactory()
        val intent = SafeIntent(intent)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.activity_start)

        /////////add by kuan
        setTurboMode(true);
        ///////////////////


        IntentValidator.validateOnCreate(this, intent, savedInstanceState, ::onValidBrowserIntent)
        sessionManager.sessions.observe(this, object : NonNullObserver<List<Session>>() {
            public override fun onValueChanged(value: List<Session>) {
                val sessions = value
                if (sessions.isEmpty()) {
                    // There's no active session. Start a new session with "homepage".
                    //ScreenController.showBrowserScreenForUrl(supportFragmentManager, APP_URL_HOME, Source.NONE)
                    ScreenController.showBrowserScreenForUrl(supportFragmentManager, "https://www.youtube.com/tv", Source.NONE)
                    Log.v("MainActivity", "@@@@@@MainActivity ScreenController empty")

                } else {
                    ScreenController.showBrowserScreenForCurrentSession(supportFragmentManager, sessionManager)
                    Log.v("MainActivity", "@@@@@@MainActivity ScreenController unempty")

                }
            }
        })
/*
        if (Settings.getInstance(this@MainActivity).shouldShowPocketOnboarding()) {
            val onboardingIntents =
                    Intent(this@MainActivity, PocketOnboardingActivity::class.java)
            startActivity(onboardingIntents)
        }

        if (Settings.getInstance(this@MainActivity).shouldShowTurboModeOnboarding()) {
            val onboardingIntent = Intent(this@MainActivity, OnboardingActivity::class.java)
            startActivity(onboardingIntent)
        }
*/
        WebViewProvider.preload(this)
        Log.v("MainActivity", "@@@@@@MainActivity onCreate22222222")
    }
    /////////add by kuan////////////
    private fun setTurboMode(turboModeEnabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(IWebView.TRACKING_PROTECTION_ENABLED_PREF, turboModeEnabled)
                .apply()
    }
    ///////////////////////////

    override fun onNewIntent(unsafeIntent: Intent) {
        IntentValidator.validate(this, unsafeIntent.toSafeIntent(), ::onValidBrowserIntent)
    }

    private fun onValidBrowserIntent(url: String, source: Source) {
        ScreenController.showBrowserScreenForUrl(supportFragmentManager, url, source)
    }

    override fun applyLocale() {
        // We don't care here: all our fragments update themselves as appropriate
    }

    override fun onResume() {
        super.onResume()
        //TelemetryWrapper.startSession(this)
    }

    override fun onPause() {
        super.onPause()
        //TelemetryWrapper.stopSession(this)
    }

    override fun onStart() {
        super.onStart()
        //Pocket.startBackgroundUpdates()
    }

    override fun onStop() {
        super.onStop()
        //Pocket.stopBackgroundUpdates() // Don't regularly hit the network in the background.
        //TelemetryWrapper.stopMainActivity()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {

        Log.v("MainActivity", "@@@@@@MainActivity onCreateView name:"+name)

        return if (name == IWebView::class.java.name) {
            // Inject our implementation of IWebView from the WebViewProvider.
            WebViewProvider.create(this, attrs, factory!!)

        } else super.onCreateView(name, context, attrs)
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        val browserFragment = fragmentManager.findFragmentByTag(BrowserFragment.FRAGMENT_TAG) as BrowserFragment?
        if (browserFragment != null &&
                browserFragment.isVisible &&
                browserFragment.onBackPressed()) {
            // The Browser fragment handles back presses on its own because it might just go back
            // in the browsing history.

            return
        }

        super.onBackPressed()
    }
//////////////////add by kuan///////////////////////
    private fun exitApp() {
        /*
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        */
         this.finish()
         System.exit(0);


    }
    override fun  onCreateOptionsMenu(menu:Menu):Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.v("startActivity","###########onCreateOptionsMenu  ");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.v("startActivity", "###########onOptionsItemSelected")
        if(item.getItemId()==R.id.menu_item_exitapp)
        {
            exitApp()
        }
        else if (item.getItemId()==R.id.menu_item_aboutpage)
        {
            startActivity(InfoActivity.getAboutIntent(this));
        }
        return super.onOptionsItemSelected(item)
    }
////////////////////////////////add by kuan end////////////////////////////

    private fun initAmazonFactory() {
        if (!isAmazonFactoryInit) {
            factory = AmazonWebKitFactories.getDefaultFactory()
            if (factory!!.isRenderProcess(this)) {
                return // Do nothing if this is on render process
            }
            factory!!.initialize(this.applicationContext)

            // factory configuration is done here, for example:
            factory!!.cookieManager.setAcceptCookie(true)

            isAmazonFactoryInit = true
        } else {
            factory = AmazonWebKitFactories.getDefaultFactory()
        }
    }

    override fun onNonTextInputUrlEntered(urlStr: String) {
        ViewUtils.hideKeyboard(container)
        //ScreenController.onUrlEnteredInner(this, supportFragmentManager, urlStr, false,
             //   null, null)
    }
/*
    override fun onTextInputUrlEntered(urlStr: String,
                                       autocompleteResult: InlineAutocompleteEditText.AutocompleteResult?,
                                       inputLocation: UrlTextInputLocation?) {
        ViewUtils.hideKeyboard(container)
        // It'd be much cleaner/safer to do this with a kotlin callback.
        ScreenController.onUrlEnteredInner(this, supportFragmentManager, urlStr, true,
                autocompleteResult, inputLocation)
    }*/

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val fragmentManager = supportFragmentManager
        val browserFragment = fragmentManager.findFragmentByTag(BrowserFragment.FRAGMENT_TAG) as BrowserFragment?

        return if (browserFragment != null && browserFragment.isVisible) {
            browserFragment.dispatchKeyEvent(event) || super.dispatchKeyEvent(event)
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    companion object {
        private var isAmazonFactoryInit = false
        @JvmStatic var factory: AmazonWebKitFactory? = null
    }
}
