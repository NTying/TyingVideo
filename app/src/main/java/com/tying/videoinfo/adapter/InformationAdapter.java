package com.tying.videoinfo.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tying.videoinfo.R;
import com.tying.videoinfo.constant.AppConfig;
import com.tying.videoinfo.entity.ResponseResult;
import com.tying.videoinfo.entity.vo.InformationVo;
import com.tying.videoinfo.entity.vo.ThumbVo;
import com.tying.videoinfo.entity.vo.BaseUserVo;
import com.tying.videoinfo.transform.image.CircleTransform;
import com.tying.videoinfo.utils.HttpUtils;
import com.tying.videoinfo.utils.ICallback;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class InformationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<InformationVo> datas;

    public interface OnItemClickListener {
        void onItemClick(Serializable obj);
    }
    private OnItemClickListener mOnItemClickListener;

    public InformationAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<InformationVo> data) {
        this.datas = data;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return datas.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == 1) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.news_item_one, parent, false);
            return new ViewHolderOne(itemView);
        } else if (viewType == 2) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.news_item_two, parent, false);
            return new ViewHolderTwo(itemView);
        } else {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.news_item_three, parent, false);
            return new ViewHolderThree(itemView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        position = holder.getBindingAdapterPosition();
        int type = getItemViewType(position);
        InformationVo informationVo = datas.get(position);

        if (type == 1) {
            ViewHolderOne viewHolderOne = (ViewHolderOne) holder;
            viewHolderOne.title.setText(informationVo.getTitle());
            viewHolderOne.comment.setText(informationVo.getCommentCount().toString() + "评论 .");
            viewHolderOne.time.setText(informationVo.getCreateTime());
            viewHolderOne.informationVo = informationVo;

            // 根据上传用户Id 获取用户名和头像
            HashMap<String, Object> id = new HashMap<>();
            id.put("id", informationVo.getCreatedBy());
            HttpUtils.getInstance(AppConfig.BASE_USER_INFO, id, null).getRequest(mContext.getApplicationContext(), new ICallback() {
                @Override
                public void onSuccess(String res) {
                    ResponseResult<BaseUserVo> responseResult = new Gson().fromJson(res, new TypeToken<ResponseResult<BaseUserVo>>(){}.getType());
                    BaseUserVo baseUserVo = (BaseUserVo) responseResult.getResult();
                    // 因为请求是异步的，获取用户名和用户头像的请求返回结果有延迟，所以应该在请求成功返回时设置用户名和头像
                    ((Activity) mContext).runOnUiThread(() -> {
                        viewHolderOne.author.setText(baseUserVo.getNickName());
                        Glide.with(mContext)
                                .load(AppConfig.CHAIN_DOMAIN + baseUserVo.getAvatar())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transform(new CircleTransform())
                                .into(viewHolderOne.header);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println(e.getMessage());
                }
            });

            // 加载文章的缩略图
            id = new HashMap<>();
            id.put("infoId", informationVo.getId());
            HttpUtils.getInstance(AppConfig.INFO_THUMB_LIST, id, null).getRequest(mContext.getApplicationContext(), new ICallback() {
                @Override
                public void onSuccess(String res) {
                    ResponseResult<List<ThumbVo>> responseResult = new Gson()
                            .fromJson(res, new TypeToken<ResponseResult<List<ThumbVo>>>(){}.getType());
                    List<ThumbVo> thumbVoList = responseResult.getResult();
                    // 因为请求是异步的，获取用户名和用户头像的请求返回结果有延迟，所以应该在请求成功返回时设置用户名和头像
                    ((Activity) mContext).runOnUiThread(() -> {
                        Glide.with(mContext)
                                .load(thumbVoList.get(0).getThumbUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(viewHolderOne.thumb);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println(e.getMessage());
                }
            });

            // 记录RecyclerView当前的Item位置
            viewHolderOne.mPosition = position;

        }

        if (type == 2) {

            ViewHolderTwo viewHolderTwo = (ViewHolderTwo) holder;
            viewHolderTwo.title.setText(informationVo.getTitle());
            viewHolderTwo.comment.setText(informationVo.getCommentCount().toString() + "评论 .");
            viewHolderTwo.time.setText(informationVo.getCreateTime());
            viewHolderTwo.informationVo = informationVo;

            // 根据视频的上传用户Id 获取用户名和头像
            HashMap<String, Object> id = new HashMap<>();
            id.put("id", informationVo.getCreatedBy());
            HttpUtils.getInstance(AppConfig.BASE_USER_INFO, id, null).getRequest(mContext.getApplicationContext(), new ICallback() {
                @Override
                public void onSuccess(String res) {
                    ResponseResult<BaseUserVo> responseResult = new Gson().fromJson(res, new TypeToken<ResponseResult<BaseUserVo>>(){}.getType());
                    BaseUserVo baseUserVo = (BaseUserVo) responseResult.getResult();
                    // 因为请求是异步的，获取用户名和用户头像的请求返回结果有延迟，所以应该在请求成功返回时设置用户名和头像
                    ((Activity) mContext).runOnUiThread(() -> {
                        viewHolderTwo.author.setText(baseUserVo.getNickName());
                        Glide.with(mContext)
                                .load(AppConfig.CHAIN_DOMAIN + baseUserVo.getAvatar())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transform(new CircleTransform())
                                .into(viewHolderTwo.header);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println(e.getMessage());
                }
            });

            // 加载文章的缩略图
            id = new HashMap<>();
            id.put("infoId", informationVo.getId());
            HttpUtils.getInstance(AppConfig.INFO_THUMB_LIST, id, null).getRequest(mContext.getApplicationContext(), new ICallback() {
                @Override
                public void onSuccess(String res) {
                    ResponseResult<List<ThumbVo>> responseResult = new Gson()
                            .fromJson(res, new TypeToken<ResponseResult<List<ThumbVo>>>(){}.getType());
                    List<ThumbVo> thumbVoList = responseResult.getResult();
                    // 因为请求是异步的，获取用户名和用户头像的请求返回结果有延迟，所以应该在请求成功返回时设置用户名和头像
                    ((Activity) mContext).runOnUiThread(() -> {
                        Glide.with(mContext)
                                .load(thumbVoList.get(0).getThumbUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(viewHolderTwo.pic1);
                        Glide.with(mContext)
                                .load(thumbVoList.get(1).getThumbUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(viewHolderTwo.pic2);
                        Glide.with(mContext)
                                .load(thumbVoList.get(2).getThumbUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(viewHolderTwo.pic3);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println(e.getMessage());
                }
            });

            // 记录RecyclerView当前的Item位置
            viewHolderTwo.mPosition = position;
        }

        if (type == 3){

            ViewHolderThree viewHolderThree = (ViewHolderThree) holder;
            viewHolderThree.title.setText(informationVo.getTitle());
            viewHolderThree.comment.setText(informationVo.getCommentCount().toString() + "评论 .");
            viewHolderThree.time.setText(informationVo.getCreateTime());
            viewHolderThree.informationVo = informationVo;

            // 根据视频的上传用户Id 获取用户名和头像
            HashMap<String, Object> id = new HashMap<>();
            id.put("id", informationVo.getCreatedBy());
            HttpUtils.getInstance(AppConfig.BASE_USER_INFO, id, null).getRequest(mContext.getApplicationContext(), new ICallback() {
                @Override
                public void onSuccess(String res) {
                    ResponseResult<BaseUserVo> responseResult = new Gson().fromJson(res, new TypeToken<ResponseResult<BaseUserVo>>(){}.getType());
                    BaseUserVo baseUserVo = (BaseUserVo) responseResult.getResult();
                    // 因为请求是异步的，获取用户名和用户头像的请求返回结果有延迟，所以应该在请求成功返回时设置用户名和头像
                    ((Activity) mContext).runOnUiThread(() -> {
                        viewHolderThree.author.setText(baseUserVo.getNickName());
                        Glide.with(mContext)
                                .load(AppConfig.CHAIN_DOMAIN + baseUserVo.getAvatar())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .transform(new CircleTransform())
                                .into(viewHolderThree.header);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println(e.getMessage());
                }
            });

            // 加载文章的缩略图
            id = new HashMap<>();
            id.put("infoId", informationVo.getId());
            HttpUtils.getInstance(AppConfig.INFO_THUMB_LIST, id, null).getRequest(mContext.getApplicationContext(), new ICallback() {
                @Override
                public void onSuccess(String res) {
                    ResponseResult<List<ThumbVo>> responseResult = new Gson()
                            .fromJson(res, new TypeToken<ResponseResult<List<ThumbVo>>>(){}.getType());
                    List<ThumbVo> thumbVoList = responseResult.getResult();
                    // 因为请求是异步的，获取用户名和用户头像的请求返回结果有延迟，所以应该在请求成功返回时设置用户名和头像
                    ((Activity) mContext).runOnUiThread(() -> {
                        Glide.with(mContext)
                                .load(thumbVoList.get(0).getThumbUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(viewHolderThree.thumb);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    System.out.println(e.getMessage());
                }
            });

            // 记录RecyclerView当前的Item位置
            viewHolderThree.mPosition = position;
        }

    }

    @Override
    public int getItemCount() {
        if (datas != null && datas.size() > 0) {
            return datas.size();
        }
        return 0;
    }

    public class ViewHolderOne extends RecyclerView.ViewHolder {

        private int mPosition;
        private TextView title;
        private TextView author;
        private TextView comment;
        private TextView time;
        private ImageView header;
        private ImageView thumb;
        private InformationVo informationVo;

        public ViewHolderOne(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
            header = itemView.findViewById(R.id.header);
            thumb = itemView.findViewById(R.id.thumb);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(informationVo);
                    }
                }
            });
        }
    }

    public class ViewHolderTwo extends RecyclerView.ViewHolder {

        private int mPosition;
        private TextView title;
        private TextView author;
        private TextView comment;
        private TextView time;
        private ImageView header;
        private ImageView pic1;
        private ImageView pic2;
        private ImageView pic3;
        private InformationVo informationVo;

        public ViewHolderTwo(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
            header = itemView.findViewById(R.id.header);
            pic1 = itemView.findViewById(R.id.pic1);
            pic2 = itemView.findViewById(R.id.pic2);
            pic3 = itemView.findViewById(R.id.pic3);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(informationVo);
                    }
                }
            });
        }
    }

    public class ViewHolderThree extends RecyclerView.ViewHolder {

        private int mPosition;
        private TextView title;
        private TextView author;
        private TextView comment;
        private TextView time;
        private ImageView header;
        private ImageView thumb;
        private InformationVo informationVo;

        public ViewHolderThree(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
            comment = itemView.findViewById(R.id.comment);
            time = itemView.findViewById(R.id.time);
            header = itemView.findViewById(R.id.header);
            thumb = itemView.findViewById(R.id.thumb);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(informationVo);
                    }
                }
            });
        }
    }
}
