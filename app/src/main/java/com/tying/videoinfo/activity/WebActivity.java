package com.tying.videoinfo.activity;

import android.os.Bundle;
import android.webkit.WebSettings;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.tying.videoinfo.R;

public class WebActivity extends BaseActivity {

    private BridgeWebView bridgeWebView;
    private String url;

    @Override
    protected int initLayout() {
        return R.layout.activity_web;
    }

    @Override
    protected void initView() {
        bridgeWebView = findViewById(R.id.bridgeWebView);
    }

    @Override
    protected void initData() {

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            url = bundle.getString("url");
        }
        initWebView();
    }

    private void initWebView() {
        WebSettings settings = bridgeWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        bridgeWebView.loadUrl(url);
        registerJavaHandler();
    }

    private void registerJavaHandler() {

        bridgeWebView.registerHandler("goback", new BridgeHandler(){
            @Override
            public void handler(String s, CallBackFunction callBackFunction) {
                finish();
            }
        });
    }
}