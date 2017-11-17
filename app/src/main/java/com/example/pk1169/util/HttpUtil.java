package com.example.pk1169.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by xiaozhang on 2017/11/14.
 */


/*
*
* */
public class HttpUtil {
    // 发送http请求
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        // 创建 okhttpClient实例
        OkHttpClient client = new OkHttpClient();
        // 创建一个request对象
        Request request = new Request.Builder().url(address).build();
        // 使用OkHttpClient的newCall()方法来创建一个对象
        client.newCall(request).enqueue(callback);
    }
}
