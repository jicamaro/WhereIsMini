package com.where.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.where.utils.KNN;
import com.where.utils.Node;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, LocationListener{

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public static Timer t;
    public static Timer t2;
    AlertDialog dialog;
    LayoutInflater inflater;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private GoogleMap mMap;
    android.support.v4.app.FragmentManager fragmentManager;

    private SharedPreferences sharedPreferences;
    private LocationManager mLocation;
    private String provider;
    private Location location;
    private Location bestLocation;

    public boolean timerIsWorking;
    public boolean timer2IsWorking;

    private int selected_section;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocation = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.sharedPreferences = getSharedPreferences("whereismini", MODE_PRIVATE);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if(status != ConnectionResult.SUCCESS){
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocation.removeUpdates(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocation.removeUpdates(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mLocation.requestLocationUpdates(provider, 0, 0, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocation.getProvider(LocationManager.GPS_PROVIDER);

        provider = mLocation.getProvider(LocationManager.GPS_PROVIDER).getName();
        if(mLocation.isProviderEnabled(provider)){
            location = mLocation.getLastKnownLocation(mLocation.getProvider(LocationManager.GPS_PROVIDER).getName());
        }
        else{
            provider = mLocation.getProvider(LocationManager.NETWORK_PROVIDER).getName();
            if(mLocation.isProviderEnabled(provider)){
                location = mLocation.getLastKnownLocation(mLocation.getProvider(LocationManager.NETWORK_PROVIDER).getName());
            }
            else{
                location = mLocation.getLastKnownLocation(mLocation.getProvider(LocationManager.PASSIVE_PROVIDER).getName());
            }
        }
        if(location != null) {
            bestLocation = location;
            onLocationChanged(location);
        }
        if(selected_section == 4){
            getStops();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpMapIfNeeded();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        final Handler handler = new Handler();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        inflater = this.getLayoutInflater();
        fragmentManager = this.getSupportFragmentManager();
        Intent intent;
        switch(position){
            case 1:
                if(!this.checkInternet()){
                    Toast.makeText(this, "No se encuentra una conexión a Internet. Por favor revise su red de datos o WiFi.", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MainActivity.this, ScheduleActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    selected_section = position;
                    fragmentManager.beginTransaction().replace(R.id.container, new Fragment(), "map_fragment").commit();
                    builder.setView(inflater.inflate(R.layout.dialog_progress, null));
                    builder.setCancelable(false);
                    dialog = builder.show();
                    if(timer2IsWorking){
                        t2.cancel();
                        timer2IsWorking = false;
                    }
                    if(!timerIsWorking){
                        t = new Timer();
                        t.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        RequestParams params = new RequestParams();
                                        params.add("limit", "1");
                                        RestClient.get(RestClient.MINIBUS_LOCATION, 1, params, new JsonHttpResponseHandler(){

                                            @Override
                                            public void onSuccess(JSONArray response) {
                                                super.onSuccess(response);
                                                if (mMap == null) {
                                                    mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                                                            .getMap();
                                                }
                                                if (mMap != null) {
                                                    mMap.clear();
                                                    for(int i=0;i<response.length();i++){
                                                        try {
                                                            JSONObject jsonObject = response.getJSONObject(i);
                                                            MarkerOptions options = new MarkerOptions();
                                                            options.position(new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
                                                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
                                                            mMap.addMarker(options);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                                if(dialog.isShowing()) dialog.dismiss();
                                            }

                                            @Override
                                            public void onFailure(Throwable e, JSONObject errorResponse) {
                                                super.onFailure(e, errorResponse);
                                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                builder.setView(inflater.inflate(R.layout.dialog_error, null));
                                                builder.show();
                                            }
                                        });
                                        timerIsWorking = true;
                                    }
                                });
                            }
                        }, 0, 5000);
                    }
                }
                break;
            case 2:
                if(!this.checkInternet()){
                    Toast.makeText(this, "No se encuentra una conexión a Internet. Por favor revise su red de datos o WiFi.", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MainActivity.this, ScheduleActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    selected_section = position;
                    fragmentManager.beginTransaction().replace(R.id.container, new Fragment(), "map_fragment").commit();
                    builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setView(inflater.inflate(R.layout.dialog_progress, null));
                    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB){
                        builder.setInverseBackgroundForced(true);
                    }
                    builder.setCancelable(false);
                    dialog = builder.show();
                    if(timerIsWorking){
                        t.cancel();
                        timerIsWorking = false;
                    }
                    if(!timer2IsWorking){
                        t2 = new Timer();
                        t2.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        RequestParams params = new RequestParams();
                                        params.add("limit", "1");
                                        RestClient.get(RestClient.SUPPORT_LOCATION, 1,params, new JsonHttpResponseHandler(){

                                            @Override
                                            public void onSuccess(JSONArray response) {
                                                super.onSuccess(response);
                                                Log.e("Response",response.toString());
                                                if (mMap == null) {
                                                    mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                                                            .getMap();
                                                }
                                                if (mMap != null) {
                                                    mMap.clear();
                                                    for(int i=0;i<response.length();i++){
                                                        try {
                                                            JSONObject jsonObject = response.getJSONObject(i);
                                                            MarkerOptions options = new MarkerOptions();
                                                            options.position(new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
                                                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
                                                            mMap.addMarker(options);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                                if(dialog.isShowing()) dialog.dismiss();
                                            }

                                            @Override
                                            public void onFailure(Throwable e, JSONObject errorResponse) {
                                                super.onFailure(e, errorResponse);
                                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                builder.setView(inflater.inflate(R.layout.dialog_error, null));
                                                builder.show();
                                            }
                                        });
                                        timer2IsWorking = true;
                                    }
                                });
                            }
                        }, 0, 5000);
                    }
                }
                break;
            case 4:
                if(!this.checkInternet()){
                    Toast.makeText(this, "No se encuentra una conexión a Internet. Por favor revise su red de datos o WiFi.", Toast.LENGTH_SHORT).show();
                    intent = new Intent(MainActivity.this, ScheduleActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    selected_section = position;
                    fragmentManager.beginTransaction().replace(R.id.container, new Fragment(), "map_fragment").commit();
                    if(timerIsWorking){
                        t.cancel();
                        timerIsWorking = false;
                    }
                    if(timer2IsWorking){
                        t2.cancel();
                        timer2IsWorking = false;
                    }
                    builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setView(inflater.inflate(R.layout.dialog_progress, null));
                    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB){
                        builder.setInverseBackgroundForced(true);
                    }
                    builder.setCancelable(false);
                    dialog = builder.show();
                    getStops();
                }
                break;
            case 6:
                intent = new Intent(MainActivity.this, ScheduleActivity.class);
                startActivity(intent);
                break;
            case 8:
                intent = new Intent(MainActivity.this, NewsActivity.class);
                startActivity(intent);
                break;
            case 9:
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB){
                    builder.setInverseBackgroundForced(true);
                }
                builder.setView(inflater.inflate(R.layout.dialog_help, null));
                builder.show();
                break;
            case 10:
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB){
                    builder.setInverseBackgroundForced(true);
                }
                builder.setView(inflater.inflate(R.layout.dialog_about, null));
                builder.show();
                break;
        }
    }

    public boolean checkInternet(){
        ConnectivityManager conMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if (i == null){
            return false;
        }
        if (!i.isConnected()){
            return false;
        }
        if (!i.isAvailable()){
            return false;
        }
        return true;
    }

    public void getStops(){
        RestClient.get(RestClient.STOPS, 1, null, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(JSONArray response) {
                super.onSuccess(response);

                ArrayList<Node> nodes = new ArrayList<Node>();
                Node nearest = null;
                if(response.length() > 0){
                    mMap.clear();
                    MarkerOptions options;
                    for(int i=0;i<response.length();i++){
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            nodes.add(new Node(jsonObject.getString("name"), jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if(nodes.size() > 0){
                        nearest = KNN.knn(nodes, bestLocation.getLatitude(), bestLocation.getLongitude());
                        options = new MarkerOptions();
                        options.title(nearest.name);
                        options.position(new LatLng(nearest.x, nearest.y));
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop_selected));
                        mMap.addMarker(options);
                    }
                    for(int i=0;i<response.length();i++){
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);
                            if(nearest != null){
                                if(jsonObject.getString("name").compareToIgnoreCase(nearest.name) != 0){
                                    options = new MarkerOptions();
                                    options.title(jsonObject.getString("name"));
                                    options.position(new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
                                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
                                    mMap.addMarker(options);
                                }
                            }
                            else{
                                options = new MarkerOptions();
                                options.title(jsonObject.getString("name"));
                                options.position(new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
                                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
                                mMap.addMarker(options);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if(dialog.isShowing()) dialog.dismiss();
                }
            }

            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                super.onFailure(e, errorResponse);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(inflater.inflate(R.layout.dialog_error, null));
                builder.show();
            }
        });
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(isBetterLocation(location, bestLocation)){
            this.location = location;
        }
        if(selected_section == 4){
            if(sharedPreferences.getString("stops", "").compareToIgnoreCase("") != 0){
                String stops = sharedPreferences.getString("stops", "");
                try {
                    JSONArray response = new JSONArray(stops);
                    ArrayList<Node> nodes = new ArrayList<Node>();
                    Node nearest = null;
                    if(response.length() > 0) {
                        mMap.clear();
                        MarkerOptions options;
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                nodes.add(new Node(jsonObject.getString("name"), jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (nodes.size() > 0) {
                            nearest = KNN.knn(nodes, bestLocation.getLatitude(), bestLocation.getLongitude());
                            options = new MarkerOptions();
                            options.title(nearest.name);
                            options.position(new LatLng(nearest.x, nearest.y));
                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop_selected));
                            mMap.addMarker(options);
                        }
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                if (nearest != null) {
                                    if (jsonObject.getString("name").compareToIgnoreCase(nearest.name) != 0) {
                                        options = new MarkerOptions();
                                        options.title(jsonObject.getString("name"));
                                        options.position(new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
                                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
                                        mMap.addMarker(options);
                                    }
                                } else {
                                    options = new MarkerOptions();
                                    options.title(jsonObject.getString("name"));
                                    options.position(new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
                                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
                                    mMap.addMarker(options);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (dialog.isShowing()) dialog.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }
}
