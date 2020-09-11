package com.webank.wecross.account.service;

import lombok.Data;

import java.util.Map;

@Data
public class RestRequest<T> {
    public T data;
}
