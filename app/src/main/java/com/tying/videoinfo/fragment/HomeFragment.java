package com.tying.videoinfo.fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.flyco.tablayout.SlidingTabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tying.videoinfo.R;
import com.tying.videoinfo.adapter.BasePagerAdapter;
import com.tying.videoinfo.constant.AppConfig;
import com.tying.videoinfo.entity.ResponseResult;
import com.tying.videoinfo.entity.vo.CategoryVo;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends BaseFragment {

    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private String[] mTitles;

    private ViewPager mViewPager;
    private SlidingTabLayout slidingTabLayout;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView() {
        mViewPager = mRootView.findViewById(R.id.home_fragment_vp);
        slidingTabLayout = mRootView.findViewById(R.id.slidingTabLayout);
    }

    @Override
    protected void initData() {
        getVideoCategoryList();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    mViewPager.setOffscreenPageLimit(mFragments.size());
                    mViewPager.setAdapter(new BasePagerAdapter(getFragmentManager(), mTitles, mFragments));
                    slidingTabLayout.setViewPager(mViewPager);
                    break;
            }
        }
    };

    private void getVideoCategoryList() {

        HttpUtils.getInstance(AppConfig.CATEGORY_LIST, null, null).getRequest(getActivity(), new ICallback() {
            @Override
            public void onSuccess(String res) {
                ResponseResult<List<CategoryVo>> responseResult = new Gson()
                        .fromJson(res, new TypeToken<ResponseResult<List<CategoryVo>>>() {
                        }.getType());

                if (responseResult != null && responseResult.getCode() == 200) {
                    List<CategoryVo> categoryVoList = responseResult.getResult();
                    if (categoryVoList != null && categoryVoList.size() > 0) {
                        mTitles = new String[categoryVoList.size()];
                        for (int i = 0; i < categoryVoList.size(); i++) {
                            mTitles[i] = categoryVoList.get(i).getName();
                            mFragments.add(VideoFragment.newInstance(mTitles[i], categoryVoList.get(i).getId()));
                        }
                        handler.sendEmptyMessage(0);
                    }
                }

            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }
}