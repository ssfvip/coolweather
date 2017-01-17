package com.example.administrator.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 天气信息的实体类
 * Created by Administrator on 2017/1/13.
 */
public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt_d")
        public String info;
    }


    public class Temperature {
        public String max;
        public String min;
    }
}
