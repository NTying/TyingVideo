package com.tying.videoinfo.entity.vo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ThumbVo
 */
@NoArgsConstructor
@Data
public class ThumbVo implements Serializable {
    /**
     * thumbId
     */
    @SerializedName("thumbId")
    private Integer thumbId;
    /**
     * thumbUrl
     */
    @SerializedName("thumbUrl")
    private String thumbUrl;
    /**
     * informationId
     */
    @SerializedName("informationId")
    private Integer informationId;
}
