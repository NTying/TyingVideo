package com.tying.videoinfo.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.tying.videoinfo.entity.vo.InformationVo;
import com.tying.videoinfo.entity.vo.ThumbVo;
import com.tying.videoinfo.entity.vo.VideoVo;
import com.tying.videoinfo.transform.image.CircleTransform;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class MyCollectAdapter extends RecyclerView.Adapter<MyCollectAdapter.ViewHolder> {

    private Context mContext;
    private List<VideoVo> datas;


    // DKVideoPlayer 相关点击事件监听器
    private OnItemChildClickListener mOnItemChildClickListener;
    private OnItemClickListener mOnItemClickListener;

    public MyCollectAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<VideoVo> data) {
        this.datas = data;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        mOnItemChildClickListener = onItemChildClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(mContext).inflate(R.layout.collect_item_video_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        position = holder.getBindingAdapterPosition();
        VideoVo videoVo = datas.get(position);

        ViewHolder viewHolder = holder;
        viewHolder.title.setText(videoVo.getTitle());
        viewHolder.comment.setText(videoVo.getCommentCount().toString() + "评论 .");
        viewHolder.videoVo = videoVo;
        Glide.with(mContext)
                .load(AppConfig.CHAIN_DOMAIN + videoVo.getThumbnail())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.thumb);

        // 根据上传用户Id 获取用户名和头像
        HashMap<String, Object> id = new HashMap<>();
        id.put("id", videoVo.getCreatedBy());
        HttpUtils.getInstance(AppConfig.BASE_USER_INFO, id, null).getRequest(mContext.getApplicationContext(), new ICallback() {
            @Override
            public void onSuccess(String res) {
                ResponseResult<BaseUserVo> responseResult = new Gson().fromJson(res, new TypeToken<ResponseResult<BaseUserVo>>() {
                }.getType());
                BaseUserVo baseUserVo = (BaseUserVo) responseResult.getResult();
                // 因为请求是异步的，获取用户名和用户头像的请求返回结果有延迟，所以应该在请求成功返回时设置用户名和头像
                ((Activity) mContext).runOnUiThread(() -> {
                    viewHolder.author.setText(baseUserVo.getNickName());
                    Glide.with(mContext)
                            .load(AppConfig.CHAIN_DOMAIN + baseUserVo.getAvatar())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .transform(new CircleTransform())
                            .into(viewHolder.header);
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println(e.getMessage());
            }
        });

        // 记录RecyclerView当前的Item位置
        viewHolder.mPosition = position;

    }

    @Override
    public int getItemCount() {
        if (datas != null && datas.size() > 0) {
            return datas.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private int mPosition;
        private TextView title;
        private TextView author;
        private TextView comment;
        private TextView like;
        private ImageView header;
        private ImageView thumb;
        private LinearLayout mCollectVideoContainer;
        private VideoVo videoVo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
            comment = itemView.findViewById(R.id.comment);
            like = itemView.findViewById(R.id.like);
            header = itemView.findViewById(R.id.header);
            thumb = itemView.findViewById(R.id.thumb);
            mCollectVideoContainer = itemView.findViewById(R.id.collect_video_container);

            if (mOnItemChildClickListener != null) {
                mCollectVideoContainer.setOnClickListener(this);
            }
            if (mOnItemClickListener != null) {
                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.collect_video_container) {
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
}
