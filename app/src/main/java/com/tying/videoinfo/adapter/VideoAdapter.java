package com.tying.videoinfo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tying.videoinfo.R;
import com.tying.videoinfo.adapter.listener.OnItemChildClickListener;
import com.tying.videoinfo.adapter.listener.OnItemClickListener;
import com.tying.videoinfo.constant.AppConfig;
import com.tying.videoinfo.entity.ResponseResult;
import com.tying.videoinfo.entity.vo.BaseUserVo;
import com.tying.videoinfo.entity.vo.VideoVo;
import com.tying.videoinfo.transform.image.CircleTransform;
import com.tying.videoinfo.utils.DataStorageUtils;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;

import java.util.HashMap;
import java.util.List;

import xyz.doikki.videocontroller.component.PrepareView;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private Context mContext;
    private List<VideoVo> data;

    // DKVideoPlayer 相关点击事件监听器
    private OnItemChildClickListener mOnItemChildClickListener;
    private OnItemClickListener mOnItemClickListener;

    public VideoAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<VideoVo> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_video_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ViewHolder viewHolder = holder;
        position = holder.getBindingAdapterPosition();
        VideoVo videoVo = data.get(position);

        // 根据视频的上传用户Id 获取用户名和头像
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", videoVo.getCreatedBy());
        HttpUtils.getInstance(AppConfig.BASE_USER_INFO, params, null)
                .getRequest(mContext.getApplicationContext(), new ICallback() {
                    @Override
                    public void onSuccess(String res) {
                        ResponseResult<BaseUserVo> responseResult = new Gson().fromJson(
                                res, new TypeToken<ResponseResult<BaseUserVo>>() {
                                }.getType());
                        BaseUserVo baseUserVo = responseResult.getResult();
                        videoVo.setUserName(baseUserVo.getNickName());
                        videoVo.setUserHeader(baseUserVo.getAvatar());
                        // 因为请求是异步的，获取用户名和用户头像的请求返回结果有延迟，所以应该在请求成功返回时设置用户名和头像
                        ((Activity) mContext).runOnUiThread(() -> {
                            viewHolder.authorTextView.setText(videoVo.getUserName());
                            Glide.with(mContext)
                                    .load(AppConfig.CHAIN_DOMAIN + videoVo.getUserHeader())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .transform(new CircleTransform())
                                    .into(viewHolder.headImg);
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.out.println(e.getMessage());
                    }
                });

        // 获取该用户是否评论、点赞、收藏过该视频
        params = new HashMap<>();
        params.put("videoId", videoVo.getId());
        DataStorageUtils dataStorageUtils = new DataStorageUtils(mContext.getApplicationContext(), "tv_token");
        String token = dataStorageUtils.findByKey("token");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("token", token);
        int finalPosition = position;
        HttpUtils.getInstance(AppConfig.IF_INTERACT_WITH_CUR_USER, params, headers)
                .getRequest(mContext.getApplicationContext(), new ICallback() {
                    @Override
                    public void onSuccess(String res) {
                        ResponseResult<boolean[]> responseResult = new Gson().fromJson(
                                res, new TypeToken<ResponseResult<boolean[]>>() {
                                }.getType());
                        boolean[] flags = responseResult.getResult();
                        ((Activity)mContext).runOnUiThread(() -> {
                            if (flags[1]) {
                                viewHolder.collectionTextView.setTextColor(Color.parseColor("#E21918"));
                                viewHolder.imgCollect.setImageResource(R.mipmap.collect_select);
                            } else {
                                viewHolder.collectionTextView.setTextColor(Color.BLACK);
                                viewHolder.imgCollect.setImageResource(R.mipmap.collect);
                            }
                            if (flags[2]) {
                                viewHolder.likeTextView.setTextColor(Color.parseColor("#E21918"));
                                viewHolder.imgLike.setImageResource(R.mipmap.dianzan_select);
                            } else {
                                viewHolder.likeTextView.setTextColor(Color.BLACK);
                                viewHolder.imgLike.setImageResource(R.mipmap.dianzan);
                            }
                            viewHolder.flagLike = flags[2];
                            viewHolder.flagCollect = flags[1];
                        });

                        // 记录RecyclerView当前的Item位置
                        viewHolder.mPosition = finalPosition;
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.out.println(e.getMessage());
                    }
                });

        viewHolder.titleTextView.setText(videoVo.getTitle());
        viewHolder.commentTextView.setText(videoVo.getCommentCount().toString());
        viewHolder.collectionTextView.setText(videoVo.getCollectionCount().toString());
        viewHolder.likeTextView.setText(videoVo.getLikeCount().toString());
        // 缩略图的地址是在包含在视频信息里面的，不是和用户头像一样在加载的时候才获取
        Glide.with(mContext)
                .load(AppConfig.CHAIN_DOMAIN + videoVo.getThumbnail())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.mThumb);
        // 不能在这里设置用户名和头像，因为请求是异步的，不用等返回结果就执行了设置
        // 因为有延迟，请求结果可能还没返回，可能是空值

    }

    @Override
    public int getItemCount() {
        if (data != null && data.size() > 0) {
            return data.size();
        }
        return 0;
    }

    /**
     * ViewHolder
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private int mPosition;
        private TextView titleTextView;
        private TextView authorTextView;
        private TextView commentTextView;
        private TextView collectionTextView;
        private TextView likeTextView;
        private ImageView headImg;
        private ImageView imgCollect;
        private ImageView imgLike;
        private boolean flagCollect;
        private boolean flagLike;
        public FrameLayout mPlayerContainer;
        public ImageView mThumb;
        public PrepareView mPrepareView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCollect = itemView.findViewById(R.id.img_collect);
            imgLike = itemView.findViewById(R.id.img_like);
            headImg = itemView.findViewById(R.id.img_header);
            titleTextView = itemView.findViewById(R.id.title);
            authorTextView = itemView.findViewById(R.id.author);
            commentTextView = itemView.findViewById(R.id.comment);
            collectionTextView = itemView.findViewById(R.id.collect);
            likeTextView = itemView.findViewById(R.id.like);

            mPlayerContainer = itemView.findViewById(R.id.player_container);
            mPrepareView = itemView.findViewById(R.id.prepare_view);
            mThumb = mPrepareView.findViewById(R.id.thumb);

            // 获取 token 和 视频ID
            DataStorageUtils dataStorageUtils = new DataStorageUtils(mContext.getApplicationContext(), "tv_token");
            String token = dataStorageUtils.findByKey("token");

            if (mOnItemChildClickListener != null) {
                mPlayerContainer.setOnClickListener(this);
            }
            if (mOnItemClickListener != null) {
                itemView.setOnClickListener(this);
            }
            imgCollect.setOnClickListener(v -> {
                VideoVo videoVo = data.get(mPosition);
                Long videoId = videoVo.getId();
                int collectNum = Integer.parseInt(collectionTextView.getText().toString());
                if (flagCollect) {
                    // 如果收藏过，那么点击收藏按钮就是取消收藏
                    collectionTextView.setText(--collectNum + "");
                    collectionTextView.setTextColor(Color.parseColor("#161616"));
                    imgCollect.setImageResource(R.mipmap.collect);
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("videoId", videoId+"");
                    params.put("type", 2);
                    params.put("value", 0);
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("token", token);
                    HttpUtils.getInstance(AppConfig.INTERACT_WITH_CUR_USER, params, headers)
                            .postRequest(new ICallback() {
                                @Override
                                public void onSuccess(String res) {
                                    HashMap<String, Object> params = new HashMap<>();
                                    params = new HashMap<>();
                                    params.put("videoId", videoId+"");
                                    params.put("type", 2);
                                    params.put("flagInteract", flagCollect);
                                    HttpUtils.getInstance(AppConfig.UPDATE_INTERACT_COUNT, params, headers)
                                            .postRequest(new ICallback() {
                                                @Override
                                                public void onSuccess(String res) {
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                }
                                            });
                                    // 无论之前是否收藏过，点击收藏后该标记值应该取反
                                    flagCollect = !flagCollect;
                                }

                                @Override
                                public void onFailure(Exception e) {
                                }
                            });

                } else {
                    // 如果没有收藏过，那么点击收藏按钮就是收藏
                    collectionTextView.setText(++collectNum + "");
                    collectionTextView.setTextColor(Color.parseColor("#E21918"));
                    imgCollect.setImageResource(R.mipmap.collect_select);
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("videoId", videoId+"");
                    params.put("type", 2);
                    params.put("value", 1);
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("token", token);
                    HttpUtils.getInstance(AppConfig.INTERACT_WITH_CUR_USER, params, headers)
                            .postRequest(new ICallback() {
                                @Override
                                public void onSuccess(String res) {
                                    HashMap<String, Object> params = new HashMap<>();
                                    params.put("videoId", videoId+"");
                                    params.put("type", 2);
                                    params.put("flagInteract", flagCollect);
                                    HttpUtils.getInstance(AppConfig.UPDATE_INTERACT_COUNT, params, headers)
                                            .postRequest(new ICallback() {
                                                @Override
                                                public void onSuccess(String res) {
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                }
                                            });
                                    // 无论之前是否收藏过，点击收藏后该标记值应该取反
                                    flagCollect = !flagCollect;
                                }

                                @Override
                                public void onFailure(Exception e) {
                                }
                            });
                }
            });
            imgLike.setOnClickListener(v -> {
                VideoVo videoVo = data.get(mPosition);
                Long videoId = videoVo.getId();
                int likeNum = Integer.parseInt(likeTextView.getText().toString());
                if (flagLike) {
                    // 如果已经点赞了，再次点击就是取消
                    likeTextView.setText(--likeNum + "");
                    likeTextView.setTextColor(Color.parseColor("#161616"));
                    imgLike.setImageResource(R.mipmap.dianzan);
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("videoId", videoId+"");
                    params.put("type", 3);
                    params.put("value", 0);
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("token", token);
                    HttpUtils.getInstance(AppConfig.INTERACT_WITH_CUR_USER, params, headers)
                            .postRequest(new ICallback() {
                                @Override
                                public void onSuccess(String res) {
                                    HashMap<String, Object> params = new HashMap<>();
                                    params.put("videoId", videoId+"");
                                    params.put("type", 3);
                                    params.put("flagInteract", flagLike);
                                    HttpUtils.getInstance(AppConfig.UPDATE_INTERACT_COUNT, params, headers)
                                            .postRequest(new ICallback() {
                                                @Override
                                                public void onSuccess(String res) {
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                }
                                            });
                                    // 无论之前是否收藏过，点击收藏后该标记值应该取反
                                    flagLike = !flagLike;
                                }

                                @Override
                                public void onFailure(Exception e) {
                                }
                            });

                } else {
                    likeTextView.setText(++likeNum + "");
                    likeTextView.setTextColor(Color.parseColor("#E21918"));
                    imgLike.setImageResource(R.mipmap.dianzan_select);
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("videoId", videoId+"");
                    params.put("type", 3);
                    params.put("value", 1);
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("token", token);
                    HttpUtils.getInstance(AppConfig.INTERACT_WITH_CUR_USER, params, headers)
                            .postRequest(new ICallback() {
                                @Override
                                public void onSuccess(String res) {
                                    HashMap<String, Object> params = new HashMap<>();
                                    params = new HashMap<>();
                                    params.put("videoId", videoId+"");
                                    params.put("type", 3);
                                    params.put("flagInteract", flagLike);
                                    HttpUtils.getInstance(AppConfig.UPDATE_INTERACT_COUNT, params, headers)
                                            .postRequest(new ICallback() {
                                                @Override
                                                public void onSuccess(String res) {
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                }
                                            });
                                    // 无论之前是否收藏过，点击收藏后该标记值应该取反
                                    flagLike = !flagLike;
                                }

                                @Override
                                public void onFailure(Exception e) {
                                }
                            });

                }
            });

            //通过tag将ViewHolder和itemView绑定
            itemView.setTag(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.player_container) {
                if (mOnItemChildClickListener != null) {
                    mOnItemChildClickListener.onItemChildClick(mPosition);
                }
            } else {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mPosition);
                }
            }

        }
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        mOnItemChildClickListener = onItemChildClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
}
