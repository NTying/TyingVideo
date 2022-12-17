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

    // 当前分页
    private int pageNum;

    // 视频分类
    private Long categoryId;
    // 视频相关信息
    private String videoUrl;
    private String videoTitle;
    private String videoThumb;

    // DKVideoPlayer 相关设置
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
        // 设置布局管理器
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        
//        recyclerView.setLayoutManager(linearLayoutManager);
//        // RecyclerView 的数据适配器
//        videoAdapter = new VideoAdapter(this);
//        // 设置RecyclerView 的Item 的点击事件
//        videoAdapter.setOnItemChildClickListener(this::onItemChildClick);
//        recyclerView.setAdapter(videoAdapter);
//        // 设置 SmartRefreshLayout 的刷新的顶部和底部动画
//        refreshLayout.setRefreshHeader(new ClassicsHeader(this));
//        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
//        // 下拉刷新事件监听器
//        refreshLayout.setOnRefreshListener(refreshLayout -> {
//            // 传入时间标识多久后关闭下拉刷新的动画
//            // 传入false表示刷新失败，不关闭刷新动画，true表示刷新成功，关闭刷新动画
//            // refreshlayout.finishRefresh(2000/*,false*/);
//            pageNum = 1;
//            getVideoList(true);
//        });
//        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
//            // 传入时间标识多久后关闭下拉刷新的动画
//            // 传入false表示刷新失败，不关闭刷新动画，true表示刷新成功，关闭刷新动画
//            // refreshlayout.finishLoadMore(2000/*,false*/);
//            pageNum++;
//            getVideoList(false);
//        });
//
//        // RecyclerView 中的 Item 的状态改变的监听器（不在屏幕内时释放 VideoView）
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

    // VideoPlayer 控件初始化
    protected void initVideoView() {
        mVideoView = findViewById(R.id.player_container);
        mVideoView.setOnStateChangeListener(new VideoView.SimpleOnStateChangeListener() {
            @Override
            public void onPlayStateChanged(int playState) {
                //监听VideoViewManager释放，重置状态
                if (playState == VideoView.STATE_IDLE) {
                    VideoPlayerUtils.removeViewFormParent(mVideoView);
                }
            }
        });
        mController = new StandardVideoController(this);

        mPrepareView = new PrepareView(this);//准备播放界面
        mPrepareView.setClickStart();
        ImageView thumb = mPrepareView.findViewById(R.id.thumb);//封面图
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

        //使用IjkPlayer解码
        mVideoView.setPlayerFactory(IjkPlayerFactory.create());
        mVideoView.setUrl(AppConfig.CHAIN_DOMAIN + videoUrl);
    }

    // 当 Item 不在屏幕内时，释放资源
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
     * 开始播放
     */
    protected void startPlay() {
        //边播边存
//        String proxyUrl = ProxyVideoCacheManager.getProxy(this).getProxyUrl(videoBean.getUrl());
//        mVideoView.setUrl(proxyUrl);
//        View itemView = linearLayoutManager.findViewByPosition(position);
//        if (itemView == null) return;
        // 在 VideoAdapter 已经将 ViewHolder和ItemView绑定了
//        VideoAdapter.ViewHolder viewHolder = (VideoAdapter.ViewHolder) itemView.getTag();
        //把列表中预置的PrepareView添加到控制器中，注意isDissociate此处只能为true, 请点进去看isDissociate的解释
        mController.addControlComponent(mPrepareView, true);
        VideoPlayerUtils.removeViewFormParent(mVideoView);
//        viewHolder.mPlayerContainer.addView(mVideoView, 0);
        mVideoView.start();
    }
}