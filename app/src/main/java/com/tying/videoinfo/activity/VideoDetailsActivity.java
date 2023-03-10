package com.tying.videoinfo.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.tying.videoinfo.R;
import com.tying.videoinfo.adapter.VideoAdapter;
import com.tying.videoinfo.adapter.listener.OnItemChildClickListener;
import com.tying.videoinfo.constant.AppConfig;
import com.tying.videoinfo.entity.ResponseResult;
import com.tying.videoinfo.entity.vo.VideoVo;
import com.tying.videoinfo.fragment.VideoFragment;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;
import com.tying.videoinfo.utils.VideoPlayerTag;
import com.tying.videoinfo.utils.VideoPlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xyz.doikki.videocontroller.StandardVideoController;
import xyz.doikki.videocontroller.component.CompleteView;
import xyz.doikki.videocontroller.component.ErrorView;
import xyz.doikki.videocontroller.component.GestureView;
import xyz.doikki.videocontroller.component.PrepareView;
import xyz.doikki.videocontroller.component.TitleView;
import xyz.doikki.videocontroller.component.VodControlView;
import xyz.doikki.videoplayer.ijk.IjkPlayerFactory;
import xyz.doikki.videoplayer.player.VideoView;

public class VideoDetailsActivity extends BaseActivity{
    
    private LinearLayoutManager linearLayoutManager;

    // ????????????
    private int pageNum;

    // ????????????
    private Long categoryId;
    // ??????????????????
    private String videoUrl;
    private String videoTitle;
    private String videoThumb;

    // DKVideoPlayer ????????????
    private VideoView mVideoView;
    private StandardVideoController mController;
    private ErrorView mErrorView;
    private CompleteView mCompleteView;
    private TitleView mTitleView;
    private PrepareView mPrepareView;
    private ImageView thumb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initLayout());
        Intent intent = getIntent();
        savedInstanceState = intent.getExtras();
        videoUrl = savedInstanceState.getString("videoUrl");
        videoTitle = savedInstanceState.getString("videoTitle");
        videoThumb = savedInstanceState.getString("videoThumb");
        initView();
        initData();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mVideoView.resume();
    }

    @Override
    public void onBackPressed() {
        if (!mVideoView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseVideoView();
    }

    @Override
    protected int initLayout() {
        return R.layout.activity_video_details;
    }

    @Override
    protected void initView() {

        FrameLayout mPlayerContainer = this.findViewById(R.id.player_container);
        mPlayerContainer.setOnClickListener(v -> startPlay());
        initVideoView();
    }

    @Override
    protected void initData() {
        // ?????????????????????
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        
//        recyclerView.setLayoutManager(linearLayoutManager);
//        // RecyclerView ??????????????????
//        videoAdapter = new VideoAdapter(this);
//        // ??????RecyclerView ???Item ???????????????
//        videoAdapter.setOnItemChildClickListener(this::onItemChildClick);
//        recyclerView.setAdapter(videoAdapter);
//        // ?????? SmartRefreshLayout ?????????????????????????????????
//        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
//        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
//        // ???????????????????????????
//        refreshLayout.setOnRefreshListener(refreshLayout -> {
//            // ??????????????????????????????????????????????????????
//            // ??????false?????????????????????????????????????????????true???????????????????????????????????????
//            // refreshlayout.finishRefresh(2000/*,false*/);
//            pageNum = 1;
//            getVideoList(true);
//        });
//        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
//            // ??????????????????????????????????????????????????????
//            // ??????false?????????????????????????????????????????????true???????????????????????????????????????
//            // refreshlayout.finishLoadMore(2000/*,false*/);
//            pageNum++;
//            getVideoList(false);
//        });
//
//        // RecyclerView ?????? Item ?????????????????????????????????????????????????????? VideoView???
//        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
//            @Override
//            public void onChildViewAttachedToWindow(@NonNull View view) {
//
//            }
//
//            @Override
//            public void onChildViewDetachedFromWindow(@NonNull View view) {
//                FrameLayout playerContainer = view.findViewById(R.id.player_container);
//                View v = playerContainer.getChildAt(0);
//                if (v != null && v == mVideoView && !mVideoView.isFullScreen()) {
//                    releaseVideoView();
//                }
//            }
//        });
//
    }

    // VideoPlayer ???????????????
    protected void initVideoView() {
        mVideoView = findViewById(R.id.player_container);
        mVideoView.setOnStateChangeListener(new VideoView.SimpleOnStateChangeListener() {
            @Override
            public void onPlayStateChanged(int playState) {
                //??????VideoViewManager?????????????????????
                if (playState == VideoView.STATE_IDLE) {
                    VideoPlayerUtils.removeViewFormParent(mVideoView);
                }
            }
        });
        mController = new StandardVideoController(this);

        mPrepareView = new PrepareView(this);//??????????????????
        mPrepareView.setClickStart();
        ImageView thumb = mPrepareView.findViewById(R.id.thumb);//?????????
        Glide.with(this)
                .load(AppConfig.CHAIN_DOMAIN + videoThumb)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(thumb);
        mController.addControlComponent(mPrepareView);

        mErrorView = new ErrorView(this);
        mController.addControlComponent(mErrorView);

        mCompleteView = new CompleteView(this);
        mController.addControlComponent(mCompleteView);

        mTitleView = new TitleView(this);
        mController.addControlComponent(mTitleView);
        mTitleView.setTitle(videoTitle);

        mController.addControlComponent(new VodControlView(this));
        mController.addControlComponent(new GestureView(this));
        mController.setEnableOrientation(true);
        mVideoView.setVideoController(mController);

        //??????IjkPlayer??????
        mVideoView.setPlayerFactory(IjkPlayerFactory.create());
        mVideoView.setUrl(AppConfig.CHAIN_DOMAIN + videoUrl);
    }

    // ??? Item ?????????????????????????????????
    private void releaseVideoView() {
        mVideoView.release();
        if (mVideoView.isFullScreen()) {
            mVideoView.stopFullScreen();
        }
        if (this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * ????????????
     */
    protected void startPlay() {
        //????????????
//        String proxyUrl = ProxyVideoCacheManager.getProxy(this).getProxyUrl(videoBean.getUrl());
//        mVideoView.setUrl(proxyUrl);
//        View itemView = linearLayoutManager.findViewByPosition(position);
//        if (itemView == null) return;
        // ??? VideoAdapter ????????? ViewHolder???ItemView?????????
//        VideoAdapter.ViewHolder viewHolder = (VideoAdapter.ViewHolder) itemView.getTag();
        //?????????????????????PrepareView??????????????????????????????isDissociate???????????????true, ???????????????isDissociate?????????
        mController.addControlComponent(mPrepareView, true);
        VideoPlayerUtils.removeViewFormParent(mVideoView);
//        viewHolder.mPlayerContainer.addView(mVideoView, 0);
        mVideoView.start();
    }
}