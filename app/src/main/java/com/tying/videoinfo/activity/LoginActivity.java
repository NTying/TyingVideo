package com.tying.videoinfo.activity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tying.videoinfo.R;
import com.tying.videoinfo.constant.AppConfig;
import com.tying.videoinfo.entity.ResponseResult;
import com.tying.videoinfo.entity.vo.LoginVo;
import com.tying.videoinfo.utils.DataStorageUtils;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;
import com.tying.videoinfo.utils.StringUtils;
import java.util.HashMap;

public class LoginActivity extends BaseActivity {

    private EditText etAccount;
    private EditText etPwd;
    private Button btnLogin;

    @Override
    protected int initLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        etAccount = findViewById(R.id.et_account);
        etPwd = findViewById(R.id.et_pwd);
        btnLogin = findViewById(R.id.btn_login);
    }

    @Override
    protected void initData() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString().trim();
                String pwd = etPwd.getText().toString().trim();
                login(account, pwd);
            }
        });
    }

    private void login(String account, String pwd) {

        if (StringUtils.isEmpty(account)) {
            showToast("请输入账号");
            return;
        }

        if (StringUtils.isEmpty(pwd)) {
            showToast("请输入密码");
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("userName", account);
        map.put("password", pwd);

        HttpUtils httpUtils = HttpUtils.getInstance(AppConfig.LOGIN_URL, map, null);
        httpUtils.postRequest(new ICallback() {
            @Override
            public void onSuccess(String res) {
                Log.i("onSuccess", res);
                try {

                    Gson gson = new Gson();
                    ResponseResult<LoginVo> responseResult = gson
                            .fromJson(res, new TypeToken<ResponseResult<LoginVo>>(){}.getType());
                    if (responseResult.getCode() == 200) {
                        LoginVo loginVo = responseResult.getResult();
                        String token = loginVo.getToken();
                        DataStorageUtils save = new DataStorageUtils(getBaseContext(), "tv_token");
                        save.insertVal("token", token);
                        navigateToWithFlag(HomeActivity.class,
                                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        showToastAsync("登录成功");
                    } else {
                        showToastAsync(responseResult.getMsg());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("onFailure", e.getMessage());
            }
        });
    }
}