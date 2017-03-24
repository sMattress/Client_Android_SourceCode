package com.wtf.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wtf.R;
import com.wtf.utils.ConnectDirect;

import java.lang.ref.WeakReference;

/**
 * Created by liyan on 2017/1/4.
 */

public class HelpActivity extends Activity {
    private WebView wv_help;
    private ProgressBar myProgressBar;
    private String url = "";
    private LinearLayout load;
    private LinearLayout error;
    private TextView tv_error_info;
    private content_handler contentHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        url = getIntent().getStringExtra("url");
        initView();
        getData();
    }

    private void initView() {
        wv_help = (WebView) findViewById(R.id.wv_help);
        myProgressBar = (ProgressBar) findViewById(R.id.myProgressBar);
        load = (LinearLayout) findViewById(R.id.load_help);
        load.setVisibility(View.VISIBLE);
        error = (LinearLayout) findViewById(R.id.error_help);
        TextView tv_refresh = (TextView) error.findViewById(R.id.tv_refresh);
        tv_refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                error.setVisibility(View.GONE);
                load.setVisibility(View.VISIBLE);
                getData();
            }
        });
        tv_error_info = (TextView) error.findViewById(R.id.tv_error_info);
        contentHandler = new content_handler(this);

    }

    public void getData() {
        // 判断网络连接
        ConnectDirect cd = new ConnectDirect(this.getApplicationContext());
        Boolean isInternetPresent = cd.isConnectingToInternet();
        if (!isInternetPresent) {
            contentHandler.sendEmptyMessage(3);
        } else {
            contentHandler.sendEmptyMessage(0);
        }
    }

    // Handler
    static class content_handler extends Handler {
        WeakReference<HelpActivity> mActivity;

        content_handler(HelpActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final HelpActivity theActivity = mActivity.get();
            switch (msg.what) {
                case 0:
                    theActivity.error.setVisibility(View.GONE);
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.wv_help.setBackgroundColor(0);
                    theActivity.wv_help.getSettings().setDefaultTextEncodingName("utf-8");
                    theActivity.wv_help.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

                    //加载需要显示的网页
                    theActivity.wv_help.setWebChromeClient(new WebChromeClient() {
                        @Override
                        public void onProgressChanged(WebView view, int newProgress) {
                            if (newProgress == 100) {
                                theActivity.myProgressBar.setVisibility(View.INVISIBLE);
                            } else {
                                if (View.INVISIBLE == theActivity.myProgressBar.getVisibility()) {
                                    theActivity.myProgressBar.setVisibility(View.VISIBLE);
                                }
                                theActivity.myProgressBar.setProgress(newProgress);
                            }
                            super.onProgressChanged(view, newProgress);
                        }

                    });
                    theActivity.wv_help.loadUrl(theActivity.url);
                    //theActivity.wv_help.loadData("<html><body>" + theActivity.url + "</body></html>", "text/html; charset=UTF-8", null);
                    break;
                case 1:
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.tv_error_info.setText("网络连接超时了，请重试");
                    theActivity.error.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.tv_error_info.setText("未获取到信息，请检查您的网络");
                    theActivity.error.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    theActivity.load.setVisibility(View.GONE);
                    theActivity.tv_error_info.setText("未检测到网络，请打开网络连接");
                    theActivity.error.setVisibility(View.VISIBLE);
                    break;

            }
        }
    }

    public void back(View v) {
        finish();
    }


}
