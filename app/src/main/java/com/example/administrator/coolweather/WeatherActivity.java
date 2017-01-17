package com.example.administrator.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.coolweather.gson.Forecast;
import com.example.administrator.coolweather.gson.Weather;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.T;
import com.example.administrator.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private TextView tv_title;
    private TextView tv_updateTime;
    private TextView tv_degree;
    private TextView tv_weatherInfo;
    private LinearLayout forecastLayout;
    private TextView tv_aqi;
    private TextView tv_pm25;
    private TextView tv_comfort;
    private TextView tv_carWash;
    private TextView tv_sport;

    // 每日背景图
    private ImageView iv_bingPic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 背景图和状态栏融合
        if (Build.VERSION.SDK_INT >= 21 ){ // api >21 时候调用
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE); // 是指覆盖状态栏
            getWindow().setStatusBarColor(Color.TRANSPARENT); //状态栏设置透明色
        }
        setContentView(R.layout.activity_weather);
        initView();// 初始化
    }

    private void initView() {
        scrollView = (ScrollView) findViewById(R.id.weather_layout);
        tv_title = (TextView) findViewById(R.id.tv_titleCity);
        tv_updateTime = (TextView) findViewById(R.id.tv_updateTime);
        tv_degree = (TextView) findViewById(R.id.tv_degree);
        tv_weatherInfo = (TextView) findViewById(R.id.tv_weatherInfo);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        tv_aqi = (TextView) findViewById(R.id.tv_aqi);
        tv_pm25 = (TextView) findViewById(R.id.tv_pm25);
        tv_comfort = (TextView) findViewById(R.id.tv_comfort);
        tv_carWash = (TextView) findViewById(R.id.tv_carWash);
        tv_sport = (TextView) findViewById(R.id.tv_sport);
        iv_bingPic = (ImageView) findViewById(R.id.iv_bingPic);
        // 对缓存进行判断
        SharedPreferences presf = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = presf.getString("weather", null);
        String bingPic = presf.getString("bing_pic", null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(iv_bingPic);
        }else {
            loadBingPic();
        }
        if (weatherString != null){
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handlerWeatherResponse(weatherString);
            // 调用展示数据的方法
            showWeatherInfo(weather);
        }else {
            // 无缓存时去服务器请求
            String weatherId = getIntent().getStringExtra("weather_id");// 带上城市的ID去请求URL
            scrollView.setVisibility(View.INVISIBLE); // 隐藏scrollview
            // 调用请求网络的方法，获取数据
            requestWeather(weatherId);
        }
    }

    /**
     * 根据天气的ID请求城市的天气信息
     * @param weatherId
     */
    private void requestWeather(String weatherId) {
        //http://guolin.tech/api/weather?cityid=CN101190401&key=bc0418b57b2d4918819d3974ac1285d9
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId +"&key=bc0418b57b2d4918819d3974ac1285d9";
//        Log.d("AAAAAAAAAA", weatherId+">>>>>>>>>>>>>>>");
//        Log.d("AAAAAAAAAA", weatherUrl+">>>>>>>>>>>>>>>");
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handlerWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
//                            Log.d("AAAAAAAAAA", weather+">>>>>>>>>>>>>>>");
                        }else {
                            T.showShort(WeatherActivity.this, "获取天气失败");
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        T.showShort(WeatherActivity.this, "获取天气失败");
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 背景图解析
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                // 缓存设置
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(iv_bingPic);
                    }
                });

            }
            @Override
            public void onFailure(Call call, IOException e) {

            }


        });
    }


    /**
     * 处理weather中实体类的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String  weatherInfo = weather.now.more.info;
        tv_title.setText(cityName);
        tv_updateTime.setText(updateTime);
        tv_weatherInfo.setText(weatherInfo);
        tv_degree.setText(degree);
        forecastLayout.removeAllViews();
        for (Forecast forecast:
             weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.other_forecast_item, forecastLayout, false);
            TextView tv_date = (TextView) view.findViewById(R.id.tv_date);
            TextView tv_info = (TextView) view.findViewById(R.id.tv_info);
            TextView tv_max = (TextView) view.findViewById(R.id.tv_max);
            TextView tv_min = (TextView) view.findViewById(R.id.tv_min);
            tv_date.setText(forecast.date);
            tv_info.setText(forecast.more.info);
            tv_max.setText(forecast.temperature.max);
            tv_min.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null){
            tv_aqi.setText(weather.aqi.city.aqi);
            tv_pm25.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度" + weather.suggestion.comfort.info;
        String carWash = "洗车指数" + weather.suggestion.carWash.info;
        String sport = "运动建议" + weather.suggestion.sport.info;
        tv_comfort.setText(comfort);
        tv_carWash.setText(carWash);
        tv_sport.setText(sport);
        scrollView.setVisibility(View.VISIBLE);
    }
}
