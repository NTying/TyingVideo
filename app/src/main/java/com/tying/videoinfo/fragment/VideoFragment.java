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

    // ????????????
    private int pageNum;

    // ??????????????????
    private List<VideoVo> videos = new ArrayList<>();
    // ????????????
    private Long categoryId;

    // DKVideoPlayer ????????????
    protected VideoView mVideoView;
    protected StandardVideoController mController;
    protected ErrorView mErrorView;
    protected CompleteView mCompleteView;
    protected TitleView mTitleView;
    /**
     * ?????????????????????
     */
    protected int mCurPos = -1;
    /**
     * ???????????????????????????????????????????????????????????????
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
        // ?????????????????????
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        // RecyclerView ??????????????????
        videoAdapter = new VideoAdapter(getActivity());
        // ??????RecyclerView ???Item ???????????????
        videoAdapter.setOnItemChildClickListener(this::onItemChildClick);
        recyclerView.setAdapter(videoAdapter);

        // ?????? SmartRefreshLayout ?????????????????????????????????
        refreshLayout.setRefreshHeader(new ClassicsHeader(getActivity()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getActivity()));
        // ???????????????????????????
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            // ??????????????????????????????????????????????????????
            // ??????false?????????????????????????????????????????????true???????????????????????????????????????
            // refreshlayout.finishRefresh(2000/*,false*/);
            pageNum = 1;
            getVideoList(true);
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            // ??????????????????????????????????????????????????????
            // ??????false?????????????????????????????????????????????true???????????????????????????????????????
            // refreshlayout.finishLoadMore(2000/*,false*/);
            pageNum++;
            getVideoList(false);
        });

        // RecyclerView ?????? Item ?????????????????????????????????????????????????????? VideoView???
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

    // VideoPlayer ???????????????
    protected void initVideoView() {
        mVideoView = new VideoView(getActivity());
        mVideoView.setOnStateChangeListener(new VideoView.SimpleOnStateChangeListener() {
            @Override
            public void onPlayStateChanged(int playState) {
                //??????VideoViewManager?????????????????????
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

    // ??? Item ?????????????????????????????????
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
     * ??????onPause????????????super????????????????????????
     * ????????????????????????????????????onPause?????????
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
     * ??????onResume????????????super????????????????????????
     * ????????????????????????????????????onResume?????????
     */
    protected void resume() {
        if (mLastPos == -1)
            return;
        //???????????????????????????
        startPlay(mLastPos);
    }

    /**
     * player_container ??????????????????
     */
    @Override
    public void onItemChildClick(int position) {
        startPlay(position);
    }

    /**
     * ????????????
     *
     * @param position ????????????
     */
    protected void startPlay(int position) {
        if (mCurPos == position) return;
        if (mCurPos != -1) {
            releaseVideoView();
        }
        VideoVo videoBean = videos.get(position);
        //????????????
//        String proxyUrl = ProxyVideoCacheManager.getProxy(getActivity()).getProxyUrl(videoBean.getUrl());
//        mVideoView.setUrl(proxyUrl);

        mVideoView.setUrl(AppConfig.CHAIN_DOMAIN + videoBean.getResUrl());
        mTitleView.setTitle(videoBean.getTitle());
        View itemView = linearLayoutManager.findViewByPosition(position);
        if (itemView == null) return;
        // ??? VideoAdapter ????????? ViewHolder???ItemView?????????
        VideoAdapter.ViewHolder viewHolder = (VideoAdapter.ViewHolder) itemView.getTag();
        //?????????????????????PrepareView??????????????????????????????isDissociate???????????????true, ???????????????isDissociate?????????
        mController.addControlComponent(viewHolder.mPrepareView, true);
        VideoPlayerUtils.removeViewFormParent(mVideoView);
        viewHolder.mPlayerContainer.addView(mVideoView, 0);
        //???????????????VideoView?????????VideoViewManager????????????????????????????????????
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

    // ?????? Handler ???????????????????????????????????????UI???
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
     * ??????????????????
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
                            showToastAsync("?????????????????????");
                        } else {
                            showToastAsync("?????????????????????");
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