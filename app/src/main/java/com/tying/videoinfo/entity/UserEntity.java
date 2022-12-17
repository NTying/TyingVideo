package com.tying.videoinfo.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserEntity implements Serializable {

    private String name;
    private String phoneNumber;

    public UserEntity() {
    }

    public UserEntity(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
}
