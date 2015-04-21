package com.where.app;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class RestClient {
    private static final String BASE_URL = "http://gps.iccutal.cl/";
    private static final String BASE_URL2 = "http://jicamaro.info/slim/";

    public static final String DEVICES = "devices.json";
    public static final String STOPS = "bus_stops.json";
    public static final String MINIBUS_LOCATION = "devices/3/trackpoints.json";
    public static final String SUPPORT_LOCATION = "devices/1/trackpoints.json";

    public static final String NEWS = "news";
    public static final String USER = "user";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, int base ,RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(10);
        client.get(getAbsoluteUrl(url, base), params, responseHandler);
    }

    public static void post(String url, int base, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url, base), params, responseHandler);
    }

    public static void cancelRequests(Context context){
        client.cancelRequests(context, true);
    }

    private static String getAbsoluteUrl(String relativeUrl, int base) {
        if(base == 1){
            Log.e("Absolute url", BASE_URL + relativeUrl);
            return BASE_URL + relativeUrl;
        }
        Log.e("Absolute url", BASE_URL2 + relativeUrl);
        return BASE_URL2 + relativeUrl;
    }
}
