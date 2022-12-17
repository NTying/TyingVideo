package com.tying.videoinfo.fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.tying.videoinfo.R;
import com.tying.videoinfo.adapter.InformationAdapter;
import com.tying.videoinfo.constant.AppConfig;
import com.tying.videoinfo.entity.ResponseResult;
import com.tying.videoinfo.entity.vo.InformationVo;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InformationFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private RefreshLayout refreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private InformationAdapter informationAdapter;

    // 当前分页
    private int pageNum;

    // 资讯信息列表
    List<InformationVo> informationVoList = new ArrayList<>();

    public InformationFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static InformationFragment newInstance() {
        InformationFragment fragment = new InformationFragment();
        return fragment;
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_information;
    }

    @Override
    protected void initView() {
        recyclerView = mRootView.findViewById(R.id.recyclerView);
        refreshLayout = mRootView.findViewById(R.id.refreshLayout);
    }

    @Override
    protected void initData() {
        // 设置布局管理器
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        // RecyclerView 的数据适配器
        informationAdapter = new InformationAdapter(getActivity());
        // 设置RecyclerView 的Item 的点击事件
        informationAdapter.setOnItemClickListener(new InformationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Serializable obj) {
                showToast("点击");
                InformationVo informationVo = (InformationVo) obj;
                String url = informationVo.getCreatedBy().toString();
                Bundle bundle = new Bundle();
                bundle.putString("url", url);
                //navigateTo(WebActivity.class, bundle);
            }
        });

        recyclerView.setAdapter(informationAdapter);

        // 设置 SmartRefreshLayout 的刷新的顶部和底部动画
        refreshLayout.setRefreshHeader(new ClassicsHeader(getActivity()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(getActivity()));
        // 下拉刷新事件监听器
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            // 传入时间标识多久后关闭刷新的动画
            // 传入false表示刷新失败，不关闭刷新动画，true表示刷新成功，关闭刷新动画
            // refreshlayout.finishRefresh(2000/*,false*/);
            pageNum = 1;
            getInformationList(true);
        });

        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            // 传入时间标识多久后关闭刷新的动画
            // 传入false表示刷新失败，不关闭刷新动画，true表示刷新成功，关闭刷新动画
            // refreshlayout.finishLoadMore(2000/*,false*/);
            pageNum++;
            getInformationList(false);
        });
        getInformationList(true);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    informationAdapter.setData(informationVoList);
                    informationAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    public void getInformationList(boolean isRefresh) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("pageNum", pageNum);
        params.put("limit", AppConfig.PAGE_SIZE);
        HttpUtils.getInstance(AppConfig.INFO_LIST, params, null).getRequest(getActivity(), new ICallback() {
            @Override
            public void onSuccess(String res) {
                if (isRefresh) {
                    refreshLayout.finishRefresh(true);
                } else {
                    refreshLayout.finishLoadMore(true);
                }

                ResponseResult<List<InformationVo>> responseResult = new Gson()
                        .fromJson(res, new TypeToken<ResponseResult<List<InformationVo>>>() {}.getType());
                if (responseResult != null && responseResult.getCode() == 200) {

                    List<InformationVo> informationResponseResult = responseResult.getResult();
                    if (informationResponseResult != null && informationResponseResult.size() > 0) {
                        if (isRefresh) {
                            informationVoList = informationResponseResult;
                        } else {
                            informationVoList.addAll(informationResponseResult);
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
}