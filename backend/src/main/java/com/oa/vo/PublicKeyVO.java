package com.oa.vo;

import lombok.Data;

@Data
public class PublicKeyVO {

    private String publicKey;

    public PublicKeyVO(String publicKey) {
        this.publicKey = publicKey;
    }
}
