package com.webank.wecross.account.service.authentication.packet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthCodeResponse {

    public int errorCode;
    public String message;
    public AuthCodeInfo authCode;

    public static class AuthCodeInfo {
        private String randomToken;
        private String imageBase64;

        public String getRandomToken() {
            return randomToken;
        }

        public void setRandomToken(String randomToken) {
            this.randomToken = randomToken;
        }

        public String getImageBase64() {
            return imageBase64;
        }

        public void setImageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
        }
    }
}
