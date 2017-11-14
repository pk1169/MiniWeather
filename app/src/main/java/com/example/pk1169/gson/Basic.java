package com.example.pk1169.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xiaozhang on 2017/11/14.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }
}
