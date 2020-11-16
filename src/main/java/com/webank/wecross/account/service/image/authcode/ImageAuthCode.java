package com.webank.wecross.account.service.image.authcode;

import java.time.LocalDateTime;

public class ImageAuthCode {
    private String token;
    private String code;
    private String imageBase64;
    private int validTime;
    private LocalDateTime createTime;

    /**
     * Check whether the current token is expired
     *
     * @return
     */
    public boolean isExpired() {
        if (getCreateTime() == null || getValidTime() == 0) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(createTime.plusSeconds(getValidTime()));
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getValidTime() {
        return validTime;
    }

    public void setValidTime(int validTime) {
        this.validTime = validTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    @Override
    public String toString() {
        return "ImageAuthCode{"
                + "token='"
                + token
                + '\''
                + ", code='"
                + code
                + '\''
                + ", validTime="
                + validTime
                + ", createTime="
                + createTime
                + '}';
    }
}
