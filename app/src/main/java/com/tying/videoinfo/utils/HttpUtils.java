package com.tying.videoinfo.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.tying.videoinfo.activity.LoginActivity;
import com.tying.videoinfo.constant.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {

    private static OkHttpClient client;
    private static String requestUrl;
    private static HashMap<String, Object> mParams;
    private static List<Object> lParams;
    private static HashMap<String, String> mHeaders;

    public static HttpUtils httpUtils = new HttpUtils();

    public static HttpUtils getInstance(String url, HashMap<String, Object> params, HashMap<String, String> headers) {

        client = new OkHttpClient.Builder().build();
        requestUrl = AppConfig.BASE_URL + url;
        mParams = params;
        mHeaders = headers;
        return httpUtils;
    }

    public void postRequest(final ICallback callback) {

        // 把参数对象转成 json
        JSONObject jsonObject = new JSONObject(mParams);
        String jsonStr = jsonObject.toString();
        RequestBody requestBody =
                RequestBody.create(jsonStr, MediaType.parse("application/json;charset=utf-9"));

        // 创建 Request
        Request.Builder builder = new Request.Builder();
        builder.url(requestUrl);
        builder.addHeader("contentType", "application/json;charset=UTF-8");
        builder.post(requestBody);
        if (mHeaders != null) {
            builder.headers(Headers.of(mHeaders));
        }
        Request request = builder.build();

        // 创建 call 回调对象
        final Call call = client.newCall(request);
        // 发起请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String result = response.body().string();
                callback.onSuccess(result);
            }
        });
    }

    public void getRequest(Context context, final ICallback callback) {

        String url = getAppendUrl(requestUrl, mParams);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.get();
        if (mHeaders != null) {
            builder.headers(Headers.of(mHeaders));
        }
        Request request = builder.build();

        // 创建 call 回调对象
        final Call call = client.newCall(request);
        // 发起请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String result = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String code = jsonObject.getString("code");
                    if (code.equals("401")) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                    } else {
                        callback.onSuccess(result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getAppendUrl(String url, Map<String, Object> map) {

        if (map != null && !map.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                if (StringUtils.isEmpty((buffer.toString()))) {
                    buffer.append("?");
                } else {
                    buffer.append("&");
                }
                buffer.append(entry.getKey()).append("=").append(entry.getValue());
            }
            url += buffer.toString();
        }
        return url;
    }
}
