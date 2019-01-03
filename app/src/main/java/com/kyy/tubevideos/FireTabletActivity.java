package com.kyy.tubevideos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import android.webkit.ValueCallback;
import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.AdSize;
import com.amazon.device.ads.AdTargetingOptions;
import com.amazon.device.ads.DefaultAdListener;
import com.amazon.device.ads.InterstitialAd;

import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;


//public class MainActivity extends XWalkActivity implements AdListener{
    public class FireTabletActivity extends AppCompatActivity implements AdListener {

    private static final String TAG = "FireTabletActivity";
   // private static final String MOBILETUBE_SETTING_DATA="mobile_tube_setting_data_field";
    //private static final String MOBILE_MODE_AUTO_FLAG="mobile_mode_auto_flag";
    //private static final String DESKTOP_MODE_AUTO_FLAG="desktop_mode_auto_flag";
   // private static final int NOTIFICATION = 19172439;
    private static final int HIDETWITTER = 30;


   // private RelativeLayout mLayoutRoot;
    private RelativeLayout mLayoutLayer0;

    private RelativeLayout mLayoutLayer1;
    //private RelativeLayout mLayoutPopWin;
    //private RelativeLayout mLayoutFloatBar;
    private RelativeLayout mLayoutBottomBar;


    private Button mBtGoToApps;
    //private Button mBtClearData;
    //private boolean mIsInstallGoogle;
    //private Button mBtExitApp;
   //private Button mBtClosePopWin;
    //private CheckBox mCheckBoxShowPopWin;

    private Button mBtDelBottomBar;
    private boolean mIsDelAd;
    private TextView mLoadingProgressTip;

    private XWalkView mXWalkView;
    private ProgressBar mProgressBar = null;//进度条，表示加载进度
    private String mBaseUrl="https://m.youtube.com";
    private String mOriUserAgent="";
    private boolean mIsShowPopWin=true;
    private boolean mIsDesktopMod=false;
    private boolean mIsTabet=true;
    private boolean mIsFinishLoadMainPage=false;
    private boolean mIsScreenPortrait=true;

    private boolean mIsStopThread=false;
    private Handler mAdRefreshHandler;
    private Runnable mAdRefreshRunnable;
    private boolean mIsLoadAd=true;
    private boolean mIsGetAdOK=false;
    private LinearLayout mAdLayout;
    private AdLayout mAmazonAdView;
    private boolean mIsAmazonAdEnabled;
    private int mAdFlushInterval=30*1000;

    private InterstitialAd mAmazonInterstitialAd;

    private Handler mHandler=null;
    private Runnable mRunnable=null;

    private boolean mIsHasVideo=false;
    //private boolean mIsHideTwitterConnect=false;
    private int mHideTwitterConnectCount=HIDETWITTER;

    //private boolean mIsTVDevice=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //mIsTVDevice=IsTvDevice();



        mIsDelAd=false;
        mLayoutLayer0=(RelativeLayout)findViewById(R.id.layer0_welcome);

        mLayoutLayer1=(RelativeLayout)findViewById(R.id.layer1_webview);

        mLayoutBottomBar=(RelativeLayout)findViewById(R.id.layer_bottom_bar);

        mLoadingProgressTip=(TextView)findViewById(R.id.textView_loadingprogress);





        mBtDelBottomBar=(Button)findViewById(R.id.bt_del_bottom_bar);
        mBtDelBottomBar.setOnClickListener(new View.OnClickListener()//无名内部类实现接口
        {
            public void onClick(View v)
            {
                mLayoutBottomBar.setVisibility(View.GONE);
                mIsDelAd=true;

            }

        });
        mAdLayout = (LinearLayout) findViewById(R.id.linearLayout_bottom_ad);




        //amazon ads
        AdRegistration.setAppKey("cd77c949e2fa4353aa2849d91c8f9a69");
        mIsAmazonAdEnabled=true;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("IABConsent_SubjectToGDPR", "1");

        // Consent String is not an arbitrary string, please follow the guidelines at
        // http://advertisingconsent.eu to set a proper consent string value.
        editor.putString("IABConsent_ConsentString", "1");

        // set the "aps_gdpr_pub_pref_li" property only if you are relying on legitimate
        // interest as your processing justification
        editor.putString("aps_gdpr_pub_pref_li", "1");
        editor.apply();
        //AdRegistration.enableLogging(true);
        //AdRegistration.enableTesting(true);

        mAmazonAdView = new AdLayout(this, AdSize.SIZE_320x50);
        //mAmazonAdView=(AdLayout) findViewById(R.id.adview);
        //mAmazonAdView = new AdLayout(this);

        mAmazonAdView.setListener(this);


        // Set the correct width and height of the ad.
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        mAdLayout.addView(mAmazonAdView,lp);
        AdTargetingOptions opt = new AdTargetingOptions().enableGeoLocation(true);
        mAmazonAdView.loadAd(opt); // async task to retrieve an ad

        mAdRefreshHandler=new Handler();//定时刷新广告
        mAdRefreshRunnable=new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                //要做的事情
                Log.v(TAG, "################main Runnable running...");
                if(mIsStopThread)
                    return;
                if(mIsLoadAd)//加载广告开关
                {
                    Log.v(TAG,"################main try to load ad");
                    AdTargetingOptions opt = new AdTargetingOptions().enableGeoLocation(true);
                    mAmazonAdView.loadAd(opt);
                    mBtDelBottomBar.setVisibility(View.GONE);
                }
                mAdRefreshHandler.postDelayed(this, mAdFlushInterval);
            }

        };
        mAdRefreshHandler.postDelayed(mAdRefreshRunnable, mAdFlushInterval);

        mAmazonInterstitialAd=new InterstitialAd(this);
        mAmazonInterstitialAd.setListener(new MyInterstitialAdListener());
        mAmazonInterstitialAd.loadAd();
        ////amazon end/////

        onXWalkReady();

        Log.v(TAG,"########onCreate.....................");
    }/////onCreate


   // @Override
    public void onXWalkReady() {
        // Do anyting with the embedding API
        Log.v(TAG,"########onXWalkReady.....................");

        mProgressBar=(ProgressBar)findViewById(R.id.progressBar_webloading_progress);//进度条
        mXWalkView=(XWalkView)findViewById(R.id.webView);
        mXWalkView.setResourceClient(new myXWalkResourceClient(mXWalkView));
        mXWalkView.setUIClient(new myXWalkUIClient(mXWalkView));

        mOriUserAgent=mXWalkView.getSettings().getUserAgentString();
        if(mOriUserAgent.contains("Mobile"))//含mobile 是phone，否则是tablet
            mIsTabet=false;

        Log.v(TAG,"########"+mOriUserAgent);

        mXWalkView.addJavascriptInterface(new JsInterface(this), "appJs");

        tryToOpenMobileYouTubeSite();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG,"########onActivityResult");
        if (mXWalkView != null) {
            mXWalkView.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG,"########onNewIntent");
        if (mXWalkView != null) {
            mXWalkView.onNewIntent(intent);
        }
    }
    private boolean IsMainPageUrl(String inUrl)
    {
        String strMainUrl=mBaseUrl+"/";
        return (inUrl.equalsIgnoreCase(mBaseUrl) || inUrl.equalsIgnoreCase(strMainUrl));
    }



    @Override
    protected void onStart()
    {
        super.onStart();

        Log.v(TAG,"########MainActivity onStart");
    }
    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.v(TAG,"########MainActivity onRestart");
    }
    @Override

    protected void onResume()
    {
        super.onResume();
        if (mXWalkView != null) {
            mXWalkView.resumeTimers();
            mXWalkView.onShow();

            //if(mIsHasVideo)
             // resumeVideo();
        }
        //if(null!=mAdmobAdView)
           //mAdmobAdView.resume();

        Log.v(TAG,"########MainActivity onResume");

    }
    @Override
    protected void onPause()
    {
        super.onPause();
        if (mXWalkView != null) {
            mXWalkView.pauseTimers();
            mXWalkView.onHide();
           //if(mIsHasVideo)
              //pauseVideo();
        }
        //if(null!=mAdmobAdView)
           //mAdmobAdView.pause();
        Log.v(TAG,"########MainActivity onPause");
    }
    @Override
    protected void onStop()
    {
        super.onStop();

        Log.v(TAG,"########MainActivity onStop");
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        /*
        if(null!=mWebView)
        {
            mWebView.setWebChromeClient(null);
            mWebView.setWebViewClient(null);
            mWebView.destroy();
        }*/
        if (mXWalkView != null) {
            mXWalkView.clearCache(true);
            mXWalkView.onDestroy();

        }

        mIsStopThread=true;
        //mAdRefreshHandler.removeCallbacks(mAdRefreshRunnable);
        if(null!=mAdRefreshHandler)
             mAdRefreshHandler.removeCallbacksAndMessages(mAdRefreshRunnable);
        //mTimerRefreshHandler.removeCallbacksAndMessages(mTimerRefreshRunnable);

        if(null!=mAmazonAdView)
            mAmazonAdView.destroy();

        //if(null!=mAdmobAdView)
           // mAdmobAdView.destroy();
    }
    public void onConfigurationChanged(Configuration newConfig)
    {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if(mIsTabet)
            return;
        if (!mIsFinishLoadMainPage)
            return;
        if(mXWalkView.hasEnteredFullscreen())
            return;

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        {   //竖屏
            Log.v(TAG,"###########onConfigurationChanged:PORTRAIT");
          mIsScreenPortrait=true;
          showBottomAndFloatBar();
        }
        else
        {
          Log.v(TAG,"###########onConfigurationChanged:LANDSCAPE");
          mIsScreenPortrait=false;
         hideBottomAndFloatBar();
        }

    }

    @Override   //默认点回退键，会退出Activity，需监听按键操作，使回退在WebView内发生
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.v(TAG,"###########onKeyDown  ");


        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            int index=mXWalkView.getNavigationHistory().getCurrentIndex();
            String url=mXWalkView.getUrl();
            Log.v(TAG,"###########index:"+index);
            Log.v(TAG,"###########current url:"+url);


            if(0==index)///last page to exit///want to exit app
            {
                if(mAmazonInterstitialAd.isReady())
                {
                    Log.v(TAG,"###########show Interstitial Ad  ");
                    mAmazonInterstitialAd.showAd();
                    return true;
                }
            }

            if(mXWalkView.hasEnteredFullscreen()) {
                mXWalkView.leaveFullscreen();
                return true;
            }
            if(mXWalkView.getNavigationHistory().canGoBack())
            {
                Log.v(TAG,"###########webview go back  ");
                mHideTwitterConnectCount=0;//

                mXWalkView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
                return  true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onSearchRequested () {
        // Add customized Search button behavior
        return true;
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.v(TAG,"###########onCreateOptionsMenu  ");
        if(mIsTVDevice)
        {
            getMenuInflater().inflate(R.menu.main, menu);
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Log.v(TAG,"###########onOptionsItemSelected");
        if(!mIsTVDevice)
            return super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case R.id.menu_item_homepage:
            {
                goToMainPage();

                return true;
            }
            case R.id.menu_item_exitapp:
            {
                exitApp();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/


    private void link_web_with_default_browser(String url)
    {
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);

        }catch(Exception e)
        {

        }
    }
    private void startSimpleWebView(String url)
    {
        Log.v(TAG,"#######startSimpleWebView");
        Intent intent = new Intent(FireTabletActivity.this,SimpleWebViewActivity.class);
        intent.putExtra("URL", url);
        startActivity(intent);
    }
    private  void checkHasVideo()
    {
        mIsHasVideo=false;
        String js = "javascript:var VideoTags = document.getElementsByTagName('video');" +
                " var isHtmlVideoPaused = false;" +
                "if(VideoTags.length > 0) {" +
                "try{window.appJs.log('#########has video'); window.appJs.hasVideo();}catch(err){}" +
                "}";
        mXWalkView.load(js, null);
    }
    private void pauseVideo()
    {
        if(mIsHasVideo)
        {
            Log.v(TAG,"#######has video try to pause js");
            String js="javascript:videoEty = document.getElementsByTagName('video')[0]; " +
                    "isHtmlVideoPaused = videoEty.paused;" +
                    "if(!videoEty.paused) {" +
                    "try{window.appJs.log('######to pause video'); videoEty.pause();}catch(err){}" +
                    "}";
            mXWalkView.load(js,null);
        }
    }
    private void resumeVideo()
    {
        if(mIsHasVideo)
        {
            Log.v(TAG,"#######has video try to resume js");
            String js="javascript:videoEty = document.getElementsByTagName('video')[0]; " +
                    "if( videoEty.paused) {" +
                    "try{window.appJs.log('#######to resume video'); videoEty.play();}catch(err){}" +
                    "} else {" +
                    "window.appJs.log('########video not paused');" +
                    "}";
            mXWalkView.load(js,null);
        }
    }


    private void setUserAgentString(boolean isDesktopMod)
    {
        int sdk_ver=Build.VERSION.SDK_INT;
        String ori_ua=mOriUserAgent;

        //String new_ua = "Mozilla/5.0 (Android "+Build.VERSION.RELEASE+"; Tablet; rv:48.0) Gecko/48.0 Firefox/48.0";
        String new_ua ="";

        //if(sdk_ver<=17)
        new_ua=ori_ua.replace("Mobile","");//phone统一显示tablet页面

        if(isDesktopMod)
        {
            String desktop_os="X11;Linux x86_64";
            String temp=new_ua.substring(new_ua.indexOf('(')+1, new_ua.indexOf(')'));
            new_ua=new_ua.replaceFirst(temp,desktop_os);
        }

        mXWalkView.getSettings().setUserAgentString(new_ua);
    }

    private void tryToOpenWebSite(boolean isDesktopMod,String url)
    {
        /*
        mLayoutLayer0_5.setVisibility(View.GONE);
        mLayoutSubLayerLoadingProgress.setVisibility(View.VISIBLE);
        if(getMobileModeAutoFlag()||getDesktopModeAutoFlag())
            mBtClearData.setVisibility(View.VISIBLE);
            */

        setUserAgentString(isDesktopMod);
        mXWalkView.load(mBaseUrl,null);
        String agentstring=mXWalkView.getUserAgentString();
        Log.v(TAG,"########agentstring url:"+agentstring);
    }
    private void goToMainPage()
    {
        mXWalkView.load(mBaseUrl,null);
    }

    private void tryToOpenMobileYouTubeSite()
    {
        mIsDesktopMod=false;
        tryToOpenWebSite(mIsDesktopMod,mBaseUrl);

    }

    @Override
    public void onAdLoaded(Ad arg0, AdProperties arg1) {
        // TODO Auto-generated method stub
        Log.v(TAG,"###########main load amazon ad okkkkkkkk");
        mIsGetAdOK=true;
        if(!mIsDelAd) {
            mBtDelBottomBar.setVisibility(View.VISIBLE);
            mLayoutBottomBar.setVisibility(View.VISIBLE);
        }

/*
        if (!mIsAmazonAdEnabled)
        {
            mIsAmazonAdEnabled = true;

            mAdLayout.removeAllViews();
            mAdLayout.addView(mAmazonAdView);
        }
        */


    }
    @Override
    public void onAdFailedToLoad(Ad arg0, AdError arg1) {
        // TODO Auto-generated method stub
        Log.v(TAG,"###########main load amazon ad fail,errorCode="+arg1.getCode()+"  message:"+arg1.getMessage());
        mIsGetAdOK=false;
        //mLayoutBottomBar.setVisibility(View.GONE);////no need to hide full bar
        mBtDelBottomBar.setVisibility(View.GONE);

/*
        if (mIsAmazonAdEnabled)
        {
            mIsAmazonAdEnabled = false;
            mAdLayout.removeAllViews();

            if(null!=mAdmobAdView)
               mAdLayout.addView(mAdmobAdView);


        }
        if(null!=mAdmobAdView)
           mAdmobAdView.loadAd(mAdRequestBuilder.build());
           */


/*
        mFacebookBannerAdView.setAdListener(new com.facebook.ads.AdListener() {
            @Override
            public void onError(com.facebook.ads.Ad ad, com.facebook.ads.AdError error) {
                // Ad failed to load.
                Log.v(TAG,"###########main load facebool ad fail,errorCode="+error.getErrorCode()+"  message:"+error.getErrorMessage());
            }
            @Override
            public void onAdLoaded(com.facebook.ads.Ad ad) {
                // Ad was loaded
                Log.v(TAG,"###########main load facebool ad okkkkkkkk");
                mIsGetAdOK=true;
                if(!mIsDelAd)
                    mLayoutBottomBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdClicked(com.facebook.ads.Ad ad) {
                // Use this function to detect when an ad was clicked.
            }


        });
        //com.facebook.ads.AdSettings.addTestDevice("704f28579da51d45de4f17fcdde753e0");
        mFacebookBannerAdView.loadAd();
        */

    }

    @Override
    public void onAdCollapsed(Ad arg0) {
        // TODO Auto-generated method stub
        Log.v(TAG,"###########onAdCollapsed");
    }

    @Override
    public void onAdDismissed(Ad arg0) {
        // TODO Auto-generated method stub
        Log.v(TAG,"###########onAdDismissed");
    }

    @Override
    public void onAdExpanded(Ad arg0) {
        // TODO Auto-generated method stub
        Log.v(TAG,"###########onAdExpanded");
    }

    class MyInterstitialAdListener extends DefaultAdListener
    {
        @Override
        public void onAdLoaded(Ad ad, AdProperties adProperties)
        {
        }

        @Override
        public void onAdFailedToLoad(Ad ad, AdError error)
        {// Call backup ad network.
        }

        @Override
        public void onAdDismissed(Ad ad)
        {
            // Start the level once the interstitial has disappeared.
           exitApp();
        }
    }


    private void PopMsgDlg(String strMsg)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.ExitAppTipDialogStyle);
        alertDialogBuilder.setTitle("Message!!!");
        alertDialogBuilder
                .setMessage(strMsg)
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                exitApp();
                                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                            }
                        })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                        exitApp();
                    }
                });



        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }



    private void exitApp()
    {
        /*
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);*/
        this.finish();
    }
    private boolean isPkgInstalled(String pkgName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    private void linkAmazonAppStore(String pkgName)
    {
        String url="amzn://apps/android?p="+pkgName;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        this.startActivity(intent);
    }
    private void startAnApp(String pkgName)
    {
        Intent i = this.getPackageManager().getLaunchIntentForPackage(pkgName);
        //如果该程序不可启动（像系统自带的包，有很多是没有入口的）会返回NULL
        if (i != null)
            this.startActivity(i);
    }
    private void tryLinkThisApp(String pkgName)
    {

        if(isPkgInstalled(pkgName))
        {
            startAnApp(pkgName);
        }
        else
        {
            linkAmazonAppStore(pkgName);
        }

    }
    private void tryOpenDefaultWebview(String url)
    {
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);

        }catch(Exception e)
        {

        }
    }
    /*
    private void hideSoftKey()
    {
        View decorView=getWindow().getDecorView();
        int opt=View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(opt);

    }*/
    private void hideBottomAndFloatBar() {

        Log.v(TAG,"########hideBottomAndFloatBar......");


        //if(!mIsInstallGoogle)//
        //mLayoutFloatBar.setVisibility(View.GONE);
        mLayoutBottomBar.setVisibility(View.GONE);
        mIsDelAd=true;
        mAdFlushInterval=60*1000;


    }
    private void showBottomAndFloatBar() {
        Log.v(TAG,"########showBottomAndFloatBar......");

        //if(!mIsInstallGoogle)
       // mLayoutFloatBar.setVisibility(View.VISIBLE);
        if(mIsGetAdOK)//
        {
            mBtDelBottomBar.setVisibility(View.VISIBLE);
            mLayoutBottomBar.setVisibility(View.VISIBLE);
        }

        mIsDelAd=false;
        mAdFlushInterval=30*1000;
    }

    class myXWalkResourceClient extends XWalkResourceClient {

        myXWalkResourceClient(XWalkView view)
        {
            super(view);
        }

        @Override
        public void onLoadStarted(XWalkView view, java.lang.String url)
        {
            Log.v(TAG,"########onLoadStarted url:"+url);



        }
        @Override
        public void onLoadFinished(XWalkView view, java.lang.String url)
        {
            Log.v(TAG,"########onLoadFinished url:"+url);

            String urlsetting="https://m.youtube.com/select_site";

            if(url.startsWith(urlsetting))
            {
                mHideTwitterConnectCount=HIDETWITTER;


                mHandler=new Handler();
                mRunnable=new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        //要做的事情

                       if(mHideTwitterConnectCount<=0)
                           return;

                        String js="javascript:(function() { " +
                                "var _el=[];var element;"
                                +"_el = document.getElementsByTagName('a');"
                                +"for (var i=0; i<_el.length; i++ ){if('https://m.youtube.com/account_privacy'==_el[i].href){element=_el[i].nextSibling;} }"
                                +"element.style.visibility='hidden';"
                                +"})()";

                        mXWalkView.load(js,null);
                        mHideTwitterConnectCount--;

                        Log.v(TAG,"########load js to hide TwitterConnect:");

                        mHandler.postDelayed(this,100);
                    }

                };
                mHandler.postDelayed(mRunnable,100);


            }////
        }

        @Override
        public void onProgressChanged(XWalkView view, int progressInPercent) {
            super.onProgressChanged(view,progressInPercent);
            // Activities and WebViews measure progress with different scales.
            // The progress meter will automatically disappear when we reach 100%
            if(mProgressBar!=null)
            {
                mProgressBar.setProgress(progressInPercent);
            }
            mLoadingProgressTip.setText("loading... %"+progressInPercent);




            Log.v(TAG,"########loading Progress:"+progressInPercent);
        }

        @Override
        public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request)
        {
            String url=request.getUrl().toString();

            Log.v(TAG,"########shouldInterceptLoadRequest url:"+url);


            return super.shouldInterceptLoadRequest(view,request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {

            Log.v(TAG,"########shouldOverrideUrlLoading url:"+url);

            String urlshare_googleplus="https://plus.google.com";
            String urlshare_twitter="https://twitter.com/intent/tweet?text&url=";
            String urlshare_facebook="https://m.facebook.com/sharer?u=";
            //String urlshare_twitter_api="https://api.twitter.com/oauth/";
            //String urlshare_twitter_auto="https://m.youtube.com/autoshare?";
            String urlsetting="https://m.youtube.com/select_site?disable_polymer=true";

            if (url.startsWith(urlshare_googleplus) || url.startsWith(urlshare_twitter) || url.startsWith(urlshare_facebook))
            {
                startSimpleWebView(url);
                return true;
            }

/*
           //view.load(url,null);
            if(url.endsWith("https://www.facebook.com/dialog/return/close?#_=_")) ///this url will close webview
            {
                if(view.getNavigationHistory().canGoBack())
                {
                    view.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
                    return  true;
                }
            }*/

            if(url.startsWith("https://www.youtube.com/?app=desktop") || url.startsWith("https://www.youtube.com/watch?app=desktop"))
            {
                //tryLinkThisApp(TUBE_VIDEO_PKG);
                link_web_with_default_browser(url);
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);    //To change body of overridden methods use File | Settings | File Templates.
        }



        @Override
        public void onReceivedLoadError(XWalkView view, int errorCode, java.lang.String description, java.lang.String failingUrl)
        {
            super.onReceivedLoadError(view,errorCode,description,failingUrl);
            Log.v(TAG,"########onReceivedLoadError errorCode="+errorCode+"description:"+description+"failurl:"+failingUrl);
            if(IsMainPageUrl(failingUrl))
            {
                PopMsgDlg("Sorry,Connect Youtube Fail,Please Check Your Network.");
            }
        }

    }
    class myXWalkUIClient extends XWalkUIClient {

        myXWalkUIClient(XWalkView view)
        {
            super(view);
        }

        @Override
        public boolean onCreateWindowRequested(XWalkView view, InitiateBy initiator,
                                               ValueCallback<XWalkView> callback) {
            XWalkView newView = new XWalkView(FireTabletActivity.this);
            callback.onReceiveValue(newView);
            return true;
        }

        @Override
        public void onFullscreenToggled(XWalkView view, boolean enterFullscreen) {
            super.onFullscreenToggled(view, enterFullscreen);
            Log.v(TAG,"########onFullscreenToggled enterFullscreen:"+enterFullscreen);

            if (!mIsTabet &&!mIsScreenPortrait)//is phone and orientation do not call this
                return;

            onEnterFullScreen(enterFullscreen);
        }

        @Override
        public void onPageLoadStarted(XWalkView view, java.lang.String url)
        {
            super.onPageLoadStarted(view,url);

            if(null!=mProgressBar)
                mProgressBar.setVisibility(View.VISIBLE);

            //mLoadingProgressTip.setText("loading... %"+0);

            Log.v(TAG,"########onPageLoadStarted url:"+url);
        }

        @Override
        public void onPageLoadStopped(XWalkView view, java.lang.String url, XWalkUIClient.LoadStatus status)
        {
            super.onPageLoadStopped(view,url,status);
            Log.v(TAG,"########onPageLoadStopped url:"+url);
            mIsFinishLoadMainPage=true;
            mLayoutLayer0.setVisibility(View.GONE);
            mLayoutLayer1.setVisibility(View.VISIBLE);
            //mLayoutLayer2.setVisibility(View.VISIBLE);


            if ("about:blank".equals(url) && view.getTag() != null)
            {
                view.load(view.getTag().toString(),null);
            }
            else
            {
                view.setTag(url);
            }

            if(mProgressBar!=null)
            {
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.GONE);
            }


        }

        @Override
        public void onReceivedTitle( XWalkView view, String title){
            super.onReceivedTitle(view,title);
            Log.v(TAG,"########onReceivedTitle title:"+title);

        }

        private void onEnterFullScreen(boolean isFullScreen)
        {
            if(isFullScreen)
                hideBottomAndFloatBar();
            else
                showBottomAndFloatBar();

        }
    }

    public class JsInterface  {
        private Context mContext;
        /** Instantiate the interface and set the context */
        public JsInterface(Context c) {
            mContext = c;
        }
        @JavascriptInterface
        public void hasVideo() {
            //现在只在加载完html之后做了一次是否有音频的检测
            //但如果有动态生成的audio Tag，最好不依赖isHasAudio变量
            //而是每次进入onPause和onResume的时候都重新检测下
            mIsHasVideo = true;
            Log.d(TAG, "######this webview has video");
        }
        @JavascriptInterface
        public void hasHideTwitterConnect() {

            //mIsHideTwitterConnect = true;

        }
        @JavascriptInterface
        public void log(String msg) {
            if(msg != null) {
                Log.d(TAG, "######from webview log: " + msg);
            }
        }

    }



}////class end
