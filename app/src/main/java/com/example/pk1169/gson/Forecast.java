package com.example.pk1169.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xiaozhang on 2017/11/14.
 */

public class Forecast {
    public String date;

    @SerializedName("tmp")//对应json中的数据
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {

        public String max;

        public String min;

    }

    public class More {

        @SerializedName("txt_d")
        public String info;

    }
}
