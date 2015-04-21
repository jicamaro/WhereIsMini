package com.where.app;

import android.app.ActionBar;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ScheduleActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        if(!this.checkInternet()){
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        else{
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        LinearLayout l1 = (LinearLayout) findViewById(R.id.schedule_minibus_from_university);
        LinearLayout l2 = (LinearLayout) findViewById(R.id.schedule_minibus_to_university);
        LinearLayout l3 = (LinearLayout) findViewById(R.id.schedule_support_from_university);
        LinearLayout l4 = (LinearLayout) findViewById(R.id.schedule_support_to_university);

        String[] array1 = getResources().getStringArray(R.array.schedule_minibus_from_university);
        String[] array2 = getResources().getStringArray(R.array.schedule_minibus_to_university);
        String[] array3 = getResources().getStringArray(R.array.schedule_support_from_university);
        String[] array4 = getResources().getStringArray(R.array.schedule_support_to_university);

        for (String anArray1 : array1) {
            TextView textView = new TextView(this);
            textView.setText(anArray1);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            l1.addView(textView);
        }
        for (String anArray2 : array2) {
            TextView textView = new TextView(this);
            textView.setText(anArray2);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            l2.addView(textView);
        }
        for (String anArray3 : array3) {
            TextView textView = new TextView(this);
            textView.setText(anArray3);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            l3.addView(textView);
        }
        for (String anArray4 : array4) {
            TextView textView = new TextView(this);
            textView.setText(anArray4);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            l4.addView(textView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
}
