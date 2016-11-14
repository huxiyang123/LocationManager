package com.example.localtionmanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private TextView showText;
	private LocationManager locationManager;
	private String provider;
	private LocationListener locationListener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			new LocationAsyncTask().execute(location);
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		showText = (TextView) findViewById(R.id.show_text);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		initLocationManager();
	}

	private void initLocationManager() {
		List<String> providers = locationManager.getProviders(true);
		if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		}else if (providers.contains(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		}else {
			Toast.makeText(this, "no have location provider to use 	", Toast.LENGTH_LONG).show();
			return ;
		}
		Log.d("huxiyang", "logcattext providers "+providers.toString());
		//通过选择好的位置提供器(provider)，得到location对象，location中包含了经度、维度、海拔等信息
		Location location = locationManager.getLastKnownLocation(provider);
		Log.d("huxiyang", "logcattext location "+location.getLatitude()+"  "+location.getLongitude());
		if (location!=null) {
			new LocationAsyncTask().execute(location);
		}
		//请求(注册)Listener
		locationManager.requestLocationUpdates(provider, 5000, 5, locationListener);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
	}

	class LocationAsyncTask extends AsyncTask<Location, Void, String>{

		@Override
		protected String doInBackground(Location... param) {
			Location location = param[0];
			//此地址被中国墙了，所以无法访问
			String url = "http://ditu.google.cn/maps/api/js?latlng="+location.getLatitude()+","+location.getLongitude()+"&sensor=true";
			return requestHttpClient(url); 
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			String isTextEmpty = null;
			if (result!=null) {
				isTextEmpty = result;
			}else {
				isTextEmpty = "服务器返回数据为空";
			}
			Log.d("huxiyang", "logcattext isTextEmpty "+isTextEmpty);
			showText.setText(isTextEmpty);
		}
		
		private String requestHttpClient(String url){
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
//				httpGet.setHeader("Accept-Language","zh-CN");//添加请求头，时返回的数据为中文
				Log.d("huxiyang", "logcattext httpClient44 url "+url);
				HttpResponse httpResponse = httpClient.execute(httpGet);
				Log.d("huxiyang", "logcattext httpResponse ");
				if (httpResponse.getStatusLine().getStatusCode()==200) {
					HttpEntity entity = httpResponse.getEntity();
					String response = EntityUtils.toString(entity, "utf-8");
					JSONObject jsonObject = new JSONObject(response);
					JSONArray resultArray = jsonObject.getJSONArray("results");
					Log.d("huxiyang", "logcattext response "+response);
					if (resultArray.length()>0) {
						JSONObject subJsonObject = resultArray.getJSONObject(0);
						String address = subJsonObject.getString("formatted_address");
						Log.d("huxiyang", "logcattext address "+address);
						return address;
					}
				}
			} catch (ClientProtocolException e) {
				Log.d("huxiyang", "logcattext ClientProtocolException "+e);
				e.printStackTrace();
			} catch (IOException e) {
				Log.d("huxiyang", "logcattext IOException "+e);
				e.printStackTrace();
			} catch (JSONException e) {
				Log.d("huxiyang", "logcattext JSONException "+e);
				e.printStackTrace();
			}
			return null;
		}
		
		private String requestHttpUrlConnection(String url){
			try {
				URL url2 = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
				conn.addRequestProperty("Accept-Language","zh-CN");
				conn.connect();
				if (conn.getResponseCode()==200) {
					InputStream inputStream = conn.getInputStream();
					ByteArrayOutputStream bao = new ByteArrayOutputStream();
					byte[] by = new byte[1024];
					int len = 0;
					while((len=inputStream.read())!=-1){
						bao.write(by,0,len);
					}
					byte[] data = bao.toByteArray();
					return new String(data,0,data.length);
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
}
