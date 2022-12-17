package com.tying.videoinfo.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.tying.videoinfo.R;
import com.tying.videoinfo.adapter.InformationAdapter;
import com.tying.videoinfo.adapter.MyCollectAdapter;
import com.tying.videoinfo.adapter.listener.OnItemChildClickListener;
import com.tying.videoinfo.constant.AppConfig;
import com.tying.videoinfo.entity.ResponseResult;
import com.tying.videoinfo.entity.vo.InformationVo;
import com.tying.videoinfo.entity.vo.VideoVo;
import com.tying.videoinfo.fragment.VideoFragment;
import com.tying.videoinfo.utils.DataStorageUtils;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyCollectActivity extends BaseActivity implements OnItemChildClickListener {

    private RecyclerView recyclerView;
    private RefreshLayout refreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private MyCollectAdapter myCollectAdapter;

    // 当前分页
    private int pageNum;

    // 资讯信息列表
    List<VideoVo> videoVoList = new ArrayList<>();

    @Override
    protected int initLayout() {
        return R.layout.activity_mycollect;
    }

    @Override
    protected void initView() {
        recyclerView = this.findViewById(R.id.recyclerView);
        refreshLayout = this.findViewById(R.id.refreshLayout);
    }

    @Override
    protected void initData() {
        // 设置布局管理器
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        // RecyclerView 的数据适配器
        myCollectAdapter = new MyCollectAdapter(this);
        // 设置RecyclerView 的Item 的点击事件
        myCollectAdapter.setOnItemChildClickListener(this::onItemChildClick);

        recyclerView.setAdapter(myCollectAdapter);

        // 设置 SmartRefreshLayout 的刷新的顶部和底部动画
        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        // 下拉刷新事件监听器
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            // 传入时间标识多久后关闭刷新的动画
            // 传入false表示刷新失败，不关闭刷新动画，true表示刷新成功，关闭刷新动画
            // refreshlayout.finishRefresh(2000/*,false*/);
            pageNum = 1;
            getMyCollectVideos(true);
        });

        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            // 传入时间标识多久后关闭刷新的动画
            // 传入false表示刷新失败，不关闭刷新动画，true表示刷新成功，关闭刷新动画
            // refreshlayout.finishLoadMore(2000/*,false*/);
            pageNum++;
            getMyCollectVideos(false);
        });
        getMyCollectVideos(true);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    myCollectAdapter.setData(videoVoList);
                    myCollectAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    public void getMyCollectVideos(boolean isRefresh) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("limit", AppConfig.PAGE_SIZE);

        DataStorageUtils dataStorageUtils = new DataStorageUtils(this, "tv_token");
        String token = dataStorageUtils.findByKey("token");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("token", token);

        HttpUtils.getInstance(AppConfig.MY_COLLECT_VIDEOS, params, headers).getRequest(this, new ICallback() {
            @Override
            public void onSuccess(String res) {
                if (isRefresh) {
                    refreshLayout.finishRefresh(true);
                } else {
                    refreshLayout.finishLoadMore(true);
                }

                ResponseResult<List<VideoVo>> responseResult = new Gson()
                        .fromJson(res, new TypeToken<ResponseResult<List<VideoVo>>>() {}.getType());
                if (responseResult != null && responseResult.getCode() == 200) {

                    List<VideoVo> videoVosResponse = responseResult.getResult();
                    if (videoVosResponse != null && videoVosResponse.size() > 0) {
                        if (isRefresh) {
                            videoVoList = videoVosResponse;
                        } else {
                            videoVoList.addAll(videoVosResponse);
                        }

                        handler.sendEmptyMessage(0);
                    } else {
                        if (isRefresh) {
                            showToastAsync("暂时无加载数据");
                        } else {
                            showToastAsync("没有更多数据了");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isRefresh) {
                    refreshLayout.finishRefresh(true);
                } else {
                    refreshLayout.finishLoadMore(true);
                }
            }
        });

    }

    @Override
    public void onItemChildClick(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("videoUrl", videoVoList.get(position).getResUrl());
        bundle.putString("videoTitle", videoVoList.get(position).getTitle());
        bundle.putString("videoThumb", videoVoList.get(position).getThumbnail());
        navigateTo(VideoDetailsActivity.class, bundle);
    }
}
