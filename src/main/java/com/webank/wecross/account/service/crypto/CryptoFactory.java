package com.webank.wecross.account.service.crypto;

public class CryptoFactory {
    /**
     * @param cryptoType
     * @return
     */
    public static CryptoInterface newCryptoInstance(int cryptoType) {
        if (cryptoType == CryptoType.None) {
            return new CryptoNoneImpl();
        } else if (cryptoType == CryptoType.AES) {
            return new CryptoAESImpl();
        } else if (cryptoType == CryptoType.AES_BASE64) {
            return new CryptoAESBase64Impl();
        } else if (cryptoType == CryptoType.RSA) {
            return new CryptoRSAImpl();
        } else if (cryptoType == CryptoType.RSA_BASE64) {
            return new CryptoRSABase64Impl();
        }

        // default crypto
        return new CryptoNoneImpl();
    }
}
