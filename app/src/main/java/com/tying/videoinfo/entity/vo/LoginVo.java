package com.tying.videoinfo.entity.vo;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class LoginVo implements Serializable {

    private String expiredTime;
    private String token;

}
