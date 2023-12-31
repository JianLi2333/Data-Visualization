package io.dataease.auth.filter.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder //使用builder模式
public class TokenInfo implements Serializable {

    private String username;

    private Long userId;

    public String format(){
        return username + "," +userId;
    }
}
