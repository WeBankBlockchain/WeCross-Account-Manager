package com.webank.wecross.account.service.authentication.packet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageAuthCodeResponse {
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    public int errorCode;
    public String message;
    public ImageAuthCodeInfo imageAuthCodeInfo;

    public static class ImageAuthCodeInfo {
        private String imageToken;
        private String imageBase64;

        public String getImageToken() {
            return imageToken;
        }

        public void setImageToken(String imageToken) {
            this.imageToken = imageToken;
        }

        public String getImageBase64() {
            return imageBase64;
        }

        public void setImageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
        }
    }
}
