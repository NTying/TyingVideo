package com.tying.videoinfo.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tying.videoinfo.R;
import com.tying.videoinfo.activity.LoginActivity;
import com.tying.videoinfo.activity.MyCollectActivity;
import com.tying.videoinfo.constant.AppConfig;
import com.tying.videoinfo.databinding.FragmentMyBinding;
import com.tying.videoinfo.entity.ResponseResult;
import com.tying.videoinfo.entity.vo.FullUserVo;
import com.tying.videoinfo.utils.DataStorageUtils;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;
import java.util.HashMap;

import skin.support.SkinCompatManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyFragment extends BaseFragment {

    private FragmentMyBinding binding;
    private int pageNum;

    public MyFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MyFragment newInstance() {
        MyFragment fragment = new MyFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = binding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_my;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        binding.imgHeader.setOnClickListener(v -> {
            showToast("更换头像");
            getUserInfo();
        });
        binding.rlCollect.setOnClickListener(v -> {
            navigateTo(MyCollectActivity.class);
        });
        binding.rlLogout.setOnClickListener(v -> {
            DataStorageUtils dataStorageUtils = new DataStorageUtils(getActivity(), "tv_token");
            dataStorageUtils.removeByKey("token");
            navigateToWithFlag(LoginActivity.class,
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            showToast("退出登录");
        });
        binding.rlSkin.setOnClickListener(v -> {
           DataStorageUtils dataStorageUtils = new DataStorageUtils(getActivity(), "tv_token");
           String skin = dataStorageUtils.findByKey("skin");
           if (skin.equals("night")) {
               // 恢复应用默认皮肤
               SkinCompatManager.getInstance().restoreDefaultTheme();
               dataStorageUtils.insertVal("skin", "default");
           } else {
               SkinCompatManager.getInstance().loadSkin("night", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN); // 后缀加载
               dataStorageUtils.insertVal("skin", "night");
           }
        });
    }

    private void getUserInfo() {

        DataStorageUtils dataStorageUtils = new DataStorageUtils(getActivity(), "tv_token");
        String token = dataStorageUtils.findByKey("token");
        HashMap<String, String> header = new HashMap<>();
        header.put("token", token);
        HttpUtils.getInstance(AppConfig.FULL_USER_INFO, null, header).getRequest(getActivity(), new ICallback() {
            @Override
            public void onSuccess(String res) {
                ResponseResult<FullUserVo> responseResult = new Gson()
                        .fromJson(res, new TypeToken<ResponseResult<FullUserVo>>(){}.getType());
                if (responseResult != null && responseResult.getCode() == 200) {
                    FullUserVo fullUserVo = responseResult.getResult();
                }
            }
            @Override
            public void onFailure(Exception e) {
            }
        });
    }
}