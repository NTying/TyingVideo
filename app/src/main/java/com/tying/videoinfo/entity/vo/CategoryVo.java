package com.tying.videoinfo.entity.vo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CategoryVo
 */
@NoArgsConstructor
@Data
public class CategoryVo implements Serializable {
    /**
     * id
     */
    @SerializedName("id")
    private Long id;
    /**
     * name
     */
    @SerializedName("name")
    private String name;
    /**
     * pid
     */
    @SerializedName("pid")
    private Long pid;
}