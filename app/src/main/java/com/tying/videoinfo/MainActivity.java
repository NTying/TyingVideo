package com.tying.videoinfo;

import android.widget.Button;

import com.tying.videoinfo.activity.BaseActivity;
import com.tying.videoinfo.activity.HomeActivity;
import com.tying.videoinfo.activity.LoginActivity;
import com.tying.videoinfo.activity.RegisterActivity;
import com.tying.videoinfo.utils.DataStorageUtils;
import com.tying.videoinfo.utils.StringUtils;

public class MainActivity extends BaseActivity {

    private Button btnLogin;
    private Button btnRegister;

    @Override
    protected int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
    }

    @Override
    protected void initData() {
        DataStorageUtils dataStorageUtils = new DataStorageUtils(this, "tv_token");
        String token = dataStorageUtils.findByKey("token");
        if (!StringUtils.isEmpty(token)) {
            navigateTo(HomeActivity.class);
            finish();
        }

        btnLogin.setOnClickListener(v -> navigateTo(LoginActivity.class));

        btnRegister.setOnClickListener(v -> navigateTo(RegisterActivity.class));
    }
}