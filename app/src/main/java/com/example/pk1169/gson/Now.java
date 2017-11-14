package com.example.pk1169.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xiaozhang on 2017/11/14.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;

    }

}
