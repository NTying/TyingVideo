package com.tying.videoinfo.fragment;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.tying.videoinfo.R;
import com.tying.videoinfo.activity.LoginActivity;
import com.tying.videoinfo.adapter.VideoAdapter;
import com.tying.videoinfo.adapter.listener.OnItemChildClickListener;
import com.tying.videoinfo.constant.AppConfig;
import com.tying.videoinfo.entity.ResponseResult;
import com.tying.videoinfo.entity.vo.VideoVo;
import com.tying.videoinfo.utils.DataStorageUtils;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;
import com.tying.videoinfo.utils.StringUtils;
import com.tying.videoinfo.utils.VideoPlayerTag;
import com.tying.videoinfo.utils.VideoPlayerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xyz.doikki.videocontroller.StandardVideoController;
import xyz.doikki.videocontroller.component.CompleteView;
import xyz.doikki.videocontroller.component.ErrorView;
import xyz.doikki.videocontroller.component.GestureView;
import xyz.doikki.videocontroller.component.TitleView;
import xyz.doikki.videocontroller.component.VodControlView;
import xyz.doikki.videoplayer.player.VideoView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFragment extends BaseFragment implements OnItemChildClickListener {

    private RecyclerView recyclerView;
    private RefreshLayout refreshLayout;
    private VideoAdapter videoAdapter;
    private LinearLayoutManager linearLayoutManager;

    // 当前分页
    private int pageNum;

    // 视频信息列表
    private List<VideoVo> videos = new ArrayList<>();
    // 视频分类
    private Long categoryId;

    // DKVideoPlayer 相关设置
    protected VideoView mVideoView;
    protected StandardVideoController mController;
    protected ErrorView mErrorView;
    protected CompleteView mCompleteView;
    protected TitleView mTitleView;
    /**
     * 当前播放的位置
     */
    protected int mCurPos = -1;
    /**
     * 上次播放的位置，用于页面切回来之后恢复播放
     */
    protected int mLastPos = mCurPos;

    public VideoFragment() {
        // Required empty public constructor
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_video;
    }

    @Override
    protected void initView() {
        recyclerView = mRootView.findViewById(R.id.recyclerView);
        refreshLayout = mRootView.findViewById(R.id.refreshLayout);
        initVideoView();
    }

    @Override
    protected void initData() {
        // 设置布局管理器
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        // RecyclerView 的数据适配器
        videoAdapter = new VideoAdapter(getActivity());
        // 设置RecyclerView 的Item 的点击事件
        videoAdapter.setOnItemChildClickListener(this::onItemChildClick);
        recyclerView.setAdapter(videoAdapter);

        // 设置 SmartRefreshLayout 的刷新的顶部和底部动画
        refreshLayout.setRefreshHeader(new ClassicsHeader(getActivity()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getActivity()));
        // 下拉刷新事件监听器
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            // 传入时间标识多久后关闭下拉刷新的动画
            // 传入false表示刷新失败，不关闭刷新动画，true表示刷新成功，关闭刷新动画
            // refreshlayout.finishRefresh(2000/*,false*/);
            pageNum = 1;
            getVideoList(true);
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            // 传入时间标识多久后关闭下拉刷新的动画
            // 传入false表示刷新失败，不关闭刷新动画，true表示刷新成功，关闭刷新动画
            // refreshlayout.finishLoadMore(2000/*,false*/);
            pageNum++;
            getVideoList(false);
        });

        // RecyclerView 中的 Item 的状态改变的监听器（不在屏幕内时释放 VideoView）
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                FrameLayout playerContainer = view.findViewById(R.id.player_container);
                View v = playerContainer.getChildAt(0);
                if (v != null && v == mVideoView && !mVideoView.isFullScreen()) {
                    releaseVideoView();
                }
            }
        });
        getVideoList(true);
    }

    // VideoPlayer 控件初始化
    protected void initVideoView() {
        mVideoView = new VideoView(getActivity());
        mVideoView.setOnStateChangeListener(new VideoView.SimpleOnStateChangeListener() {
            @Override
            public void onPlayStateChanged(int playState) {
                //监听VideoViewManager释放，重置状态
                if (playState == VideoView.STATE_IDLE) {
                    VideoPlayerUtils.removeViewFormParent(mVideoView);
                    mLastPos = mCurPos;
                    mCurPos = -1;
                }
            }
        });
        mController = new StandardVideoController(getActivity());
        mErrorView = new ErrorView(getActivity());
        mController.addControlComponent(mErrorView);
        mCompleteView = new CompleteView(getActivity());
        mController.addControlComponent(mCompleteView);
        mTitleView = new TitleView(getActivity());
        mController.addControlComponent(mTitleView);
        mController.addControlComponent(new VodControlView(getActivity()));
        mController.addControlComponent(new GestureView(getActivity()));
        mController.setEnableOrientation(true);
        mVideoView.setVideoController(mController);
    }

    // 当 Item 不在屏幕内时，释放资源
    private void releaseVideoView() {
        mVideoView.release();
        if (mVideoView.isFullScreen()) {
            mVideoView.stopFullScreen();
        }
        if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mCurPos = -1;
    }

    /**
     * 由于onPause必须调用super。故增加此方法，
     * 子类将会重写此方法，改变onPause的逻辑
     */
    protected void pause() {
        releaseVideoView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resume();
    }

    /**
     * 由于onResume必须调用super。故增加此方法，
     * 子类将会重写此方法，改变onResume的逻辑
     */
    protected void resume() {
        if (mLastPos == -1)
            return;
        //恢复上次播放的位置
        startPlay(mLastPos);
    }

    /**
     * player_container 点击事件处理
     */
    @Override
    public void onItemChildClick(int position) {
        startPlay(position);
    }

    /**
     * 开始播放
     *
     * @param position 列表位置
     */
    protected void startPlay(int position) {
        if (mCurPos == position) return;
        if (mCurPos != -1) {
            releaseVideoView();
        }
        VideoVo videoBean = videos.get(position);
        //边播边存
//        String proxyUrl = ProxyVideoCacheManager.getProxy(getActivity()).getProxyUrl(videoBean.getUrl());
//        mVideoView.setUrl(proxyUrl);

        mVideoView.setUrl(AppConfig.CHAIN_DOMAIN + videoBean.getResUrl());
        mTitleView.setTitle(videoBean.getTitle());
        View itemView = linearLayoutManager.findViewByPosition(position);
        if (itemView == null) return;
        // 在 VideoAdapter 已经将 ViewHolder和ItemView绑定了
        VideoAdapter.ViewHolder viewHolder = (VideoAdapter.ViewHolder) itemView.getTag();
        //把列表中预置的PrepareView添加到控制器中，注意isDissociate此处只能为true, 请点进去看isDissociate的解释
        mController.addControlComponent(viewHolder.mPrepareView, true);
        VideoPlayerUtils.removeViewFormParent(mVideoView);
        viewHolder.mPlayerContainer.addView(mVideoView, 0);
        //播放之前将VideoView添加到VideoViewManager以便在别的页面也能操作它
        getVideoViewManager().add(mVideoView, VideoPlayerTag.LIST);
        mVideoView.start();
        mCurPos = position;

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter "title".
     * @return A new instance of fragment VideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoFragment newInstance(String title, Long categoryId) {
        VideoFragment fragment = new VideoFragment();
        fragment.categoryId = categoryId;
        return fragment;
    }

    // 通过 Handler 进行线程间通信（这里是更新UI）
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    videoAdapter.setData(videos);
                    videoAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 获取视频信息
     *
     * @param isRefresh
     */
    public void getVideoList(boolean isRefresh) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("limit", AppConfig.PAGE_SIZE);
        params.put("categoryId", categoryId);
        HttpUtils.getInstance(AppConfig.GET_VIDEOS_BY_CATEGORY, params, null).getRequest(getActivity(), new ICallback() {
            @Override
            public void onSuccess(String res) {
                if (isRefresh) {
                    refreshLayout.finishRefresh(true);
                } else {
                    refreshLayout.finishLoadMore(true);
                }

                ResponseResult<List<VideoVo>> responseResult = new Gson()
                        .fromJson(res, new TypeToken<ResponseResult<List<VideoVo>>>() {
                        }.getType());
                if (responseResult != null && responseResult.getCode() == 200) {

                    List<VideoVo> videoVoList = responseResult.getResult();
                    if (videoVoList != null && videoVoList.size() > 0) {
                        if (isRefresh) {
                            videos = videoVoList;
                        } else {
                            videos.addAll(videoVoList);
                        }
                        handler.sendEmptyMessage(0);
                    } else {
                        if (isRefresh) {
                            showToastAsync("暂时加载无数据");
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
}