package com.tying.videoinfo.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tying.videoinfo.R;
import com.tying.videoinfo.constant.AppConfig;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;
import com.tying.videoinfo.utils.StringUtils;
import java.util.HashMap;

public class RegisterActivity extends BaseActivity {

    private EditText etAccount;
    private EditText etPwd;
    private Button btn_Register;

    @Override
    protected int initLayout() {
        return R.layout.activity_register;
    }

    @Override
    protected void initView() {
        etAccount = findViewById(R.id.et_account);
        etPwd = findViewById(R.id.et_pwd);
        btn_Register = findViewById(R.id.btn_register);
    }

    @Override
    protected void initData() {
        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString().trim();
                String pwd = etPwd.getText().toString().trim();
                register(account, pwd);
            }
        });
    }

    private void register(String account, String pwd) {
        if (StringUtils.isEmpty(account)) {
            showToast("请输入账号");
        }

        if (StringUtils.isEmpty(pwd)) {
            showToast("请输入密码");
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("userName", account);
        map.put("password", pwd);

        HttpUtils httpUtils = HttpUtils.getInstance(AppConfig.REGISTER_URL, map, null);
        httpUtils.postRequest(new ICallback() {
            @Override
            public void onSuccess(String res) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        navigateTo(LoginActivity.class);
                        showToast("注册成功，请登录！");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("onFailure", e.getMessage());
            }
        });
    }
}