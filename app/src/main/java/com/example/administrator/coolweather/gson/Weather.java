package com.example.administrator.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 总体的天气的实体类
 * Created by Administrator on 2017/1/16.
 */
public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
