package com.tying.videoinfo.entity.vo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * InformationVo
 */
@NoArgsConstructor
@Data
public class InformationVo implements Serializable {
    /**
     * id
     */
    @SerializedName("id")
    private Integer id;
    /**
     * title
     */
    @SerializedName("title")
    private String title;
    /**
     * type
     */
    @SerializedName("type")
    private Integer type;
    /**
     * commentCount
     */
    @SerializedName("commentCount")
    private Integer commentCount;
    /**
     * createdBy
     */
    @SerializedName("createdBy")
    private Long createdBy;
    /**
     * createTime
     */
    @SerializedName("createTime")
    private String createTime;
}
