package com.example.pk1169.gson;

/**
 * Created by xiaozhang on 2017/11/14.
 */


/*
* 解析Json数据
* 使用GSON来解析Json数据
* 1.首先导入GSON库依赖
* 定义gson对象
* 调用fromJson方法返回数据对象
* */
public class AQI {
    public AQICity city;

    public class AQICity {

        public String aqi;//空气污染指数

        public String pm25;

    }
}
