package com.tying.videoinfo.entity.vo;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VideoVo implements Serializable {

    private Long id;

    //标题
    private String title;
    //名字
    private String name;
    //资源地址
    private String resUrl;
    //观看次数
    private Long viewCount;
    //点赞数
    private Long likeCount;
    //收藏数
    private Long collectionCount;
    //评论数
    private Long commentCount;
    //缩略图
    private String thumbnail;
    //资源上传者ID
    private Long createdBy;
    @Expose(deserialize = false,serialize = false)
    private String userHeader;
    @Expose(deserialize = false,serialize = false)
    private String userName;
    @Expose(deserialize = false,serialize = false)
    private boolean flagLike;
    @Expose(deserialize = false,serialize = false)
    private boolean flagCollect;
}
