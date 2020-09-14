package com.webank.wecross.account.service;

import lombok.Data;

@Data
public class RestRequest<T> {
    public T data;
}
