package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author zhangyun
 *
 */
public class WeatherActivity extends Activity implements OnClickListener{
	String TAG = "WeatherActivity";

	private LinearLayout weatherInfoLayout;
	/*
	 *显示城市名
	 */
	private TextView cityNameText;
	/*
	 * 显示发布时间
	 */
	private TextView publishText;
	/*
	 * 显示天气信息
	 */
	private TextView weatherdesptext;
	/*
	 * 显示气温1
	 */
	private TextView temp1Text;
	/*
	 * 显示气温2
	 */
	private TextView temp2Text;
	/*
	 *显示当前日期
	 */
	private TextView currentDateText;

	private Button switchCity;
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化各控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherdesptext = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		Log.d(TAG, "hello");
		String countyCode = getIntent().getStringExtra("county_code");
		Log.d(TAG, "haha" + countyCode + "haha");
		if(!countyCode.isEmpty()){
			//有县级代号时就去查询天气
			publishText.setText("ͬ同步中");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeathercode(countyCode);
		}else {
			//没有县级代号时就直接查询天气
			showWeather();
		}
	}

	/*
	 * 从sharedPreferences文件中读取存储的天气信息，并显示到界面
	 */
	private void showWeather() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(sharedPreferences.getString("city_name", ""));
		temp1Text.setText(sharedPreferences.getString("temp1", ""));
		temp2Text.setText(sharedPreferences.getString("temp2", ""));
		weatherdesptext.setText(sharedPreferences.getString("weather_Desp", ""));
		publishText.setText("今天" + sharedPreferences.getString("publish_time", "") + "发布");
		currentDateText.setText(sharedPreferences.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
		

	/*
	 * 查询县级代码所对应的天气代号
	 */
	private void queryWeathercode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address,"countyCode");
	}

	/**
	 *根据传入的地址和类型去向服务器查询天气代号或者天气信息
	 * @param address
	 * @param string
	 */
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						//从服务器返回的数据解析出天气代号
						String[] array = response.split("\\|");
						if(array != null && array.length == 2){
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if("weatherCode".equals(type)){
					//处理服务器返回的天气信息
					Utility.handlerWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						publishText.setText("ͬ同步失败");
					}
				});
			}
		});
	}

	/**
	 * 查询天气代号所对应的天气
	 * @param weatherCode
	 */
	private void queryWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		Log.d(TAG, address);
		//101010101   http://www.weather.com.cn/data/cityinfo/101010100.html
		queryFromServer(address,"weatherCode");
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			Log.d(TAG, 123+"");
			intent.putExtra("from_weather_activity", true);
			Log.d(TAG, 12223+"");
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("ͬ同步中");
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = sharedPreferences.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
				}
			break;
		default:
		break;
		}
			
	}
}
