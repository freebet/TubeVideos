package com.kyy.tubevideos;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import android.webkit.ValueCallback;

import android.widget.ProgressBar;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.MobileAds;

import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;


//public class SimpleWebViewActivity extends XWalkActivity{
    public class SimpleWebViewActivity extends AppCompatActivity  {

    private static final String TAG = "SimpleWebViewActivity";

    private ProgressBar mProgressBar = null;//进度条，表示加载进度
    private XWalkView mXWalkView;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simplewebview);

        mUrl=getIntent().getStringExtra("URL");

        onXWalkReady();
        Log.v(TAG,"########onCreate.....................");
    }/////onCreate

    //@Override
    public void onXWalkReady() {
        // Do anyting with the embedding API
        Log.v(TAG,"########onXWalkReady.....................");

        mProgressBar=(ProgressBar)findViewById(R.id.progressBar_webloading_progress);//进度条
        mXWalkView=(XWalkView)findViewById(R.id.webView);
        mXWalkView.setResourceClient(new myXWalkResourceClient(mXWalkView));
        mXWalkView.setUIClient(new myXWalkUIClient(mXWalkView));
        mXWalkView.load(mUrl,null);
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




    @Override
    protected void onStart()
    {
        super.onStart();

        Log.v(TAG,"########SimpleWebViewActivity onStart");
    }
    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.v(TAG,"########SimpleWebViewActivity onRestart");
    }
    @Override

    protected void onResume()
    {
        super.onResume();
        if (mXWalkView != null) {
            mXWalkView.resumeTimers();
            mXWalkView.onShow();
        }
        Log.v(TAG,"########SimpleWebViewActivity onResume");
    }
    @Override
    protected void onPause()
    {
        super.onPause();

        if (mXWalkView != null) {
            mXWalkView.pauseTimers();
            mXWalkView.onHide();
        }
        Log.v(TAG,"########SimpleWebViewActivity onPause");
    }
    @Override
    protected void onStop()
    {
        super.onStop();

        Log.v(TAG,"########SimpleWebViewActivity onStop");
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


    }


    @Override   //默认点回退键，会退出Activity，需监听按键操作，使回退在WebView内发生
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.v(TAG,"###########onKeyDown  ");
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {

            if(mXWalkView.getNavigationHistory().canGoBack())
            {
                Log.v(TAG,"###########webview go back  ");
                mXWalkView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
                return  true;
            }
            //this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onSearchRequested () {
        // Add customized Search button behavior
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
		/*
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}*/
        return super.onOptionsItemSelected(item);
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


            return super.shouldOverrideUrlLoading(view, url);    //To change body of overridden methods use File | Settings | File Templates.
        }



        @Override
        public void onReceivedLoadError(XWalkView view, int errorCode, java.lang.String description, java.lang.String failingUrl)
        {
            super.onReceivedLoadError(view,errorCode,description,failingUrl);
            Log.v(TAG,"########onReceivedLoadError errorCode="+errorCode+"description:"+description+"failurl:"+failingUrl);

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
            XWalkView newView = new XWalkView(SimpleWebViewActivity.this);
            callback.onReceiveValue(newView);
            return true;
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
    }
}
