package com.tying.videoinfo.entity.vo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FullUserVo implements Serializable {

    /**
     * id
     */
    @SerializedName("id")
    private Long id;
    /**
     * userName
     */
    @SerializedName("userName")
    private String userName;
    /**
     * nickName
     */
    @SerializedName("nickName")
    private String nickName;
    /**
     * email
     */
    @SerializedName("email")
    private String email;
    /**
     * phoneNumber
     */
    @SerializedName("phoneNumber")
    private Object phoneNumber;
    /**
     * sex
     */
    @SerializedName("sex")
    private Object sex;
    /**
     * avatar
     */
    @SerializedName("avatar")
    private String avatar;
}
