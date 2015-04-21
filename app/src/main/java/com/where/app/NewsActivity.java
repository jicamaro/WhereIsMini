package com.where.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.zip.Inflater;


public class NewsActivity extends ActionBarActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;

    SharedPreferences sharedPreferences;

    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        if(!this.checkInternet()){
            Toast.makeText(this, "No se encuentra una conexión a Internet. Por favor revise su red de datos o WiFi.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(NewsActivity.this, ScheduleActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            sharedPreferences = getSharedPreferences("whereismini", MODE_PRIVATE);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API, null)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .build();
            if(savedInstanceState == null){
                if(!sharedPreferences.getBoolean("logged", false)){
                    getSupportFragmentManager().beginTransaction().add(R.id.container, LoginFragment.newInstance()).commit();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button
                && !mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInErrors();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInClicked = false;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("logged", true).commit();

        email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Log.e("Email", email);
        NewsFragment fragment = NewsFragment.newInstance();
        Bundle b = new Bundle();
        b.putString("email", email);
        fragment.setArguments(b);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!mIntentInProgress) {
            mConnectionResult = connectionResult;

            if (mSignInClicked) {
                resolveSignInErrors();
            }
        }
    }

    private void resolveSignInErrors(){
        if (!mIntentInProgress && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    public static class CustomArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final JSONArray news;

        public CustomArrayAdapter(Context context, JSONArray news) {
            super(context, R.layout.list_row_news, new String[news.length()]);
            this.context = context;
            this.news = news;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.list_row_news, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.news_title);
            TextView textView2 = (TextView) rowView.findViewById(R.id.news_text);
            try {
                textView.setText(news.getJSONObject(position).getString("title"));
                textView2.setText(news.getJSONObject(position).getString("text"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return rowView;
        }

        @Override
        public int getCount() {
            return news.length();
        }

        public void remove(int position){
            news.remove(position);
            this.notifyDataSetChanged();
        }
    }

    public static class LoginFragment extends Fragment implements View.OnClickListener {

        View rootView;

        View.OnClickListener mListener;

        static LoginFragment newInstance(){
            return new LoginFragment();
        }

        public LoginFragment(){

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_login, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            try {
                mListener = (View.OnClickListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
            }
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            rootView.findViewById(R.id.sign_in_button).setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onClick(view);
        }
    }

    public static class NewsFragment extends Fragment implements GestureDetector.OnGestureListener{

        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        String email;

        RelativeLayout empty_layout;
        ListView news_layout;
        View rootView;
        LayoutInflater inflater;
        AlertDialog dialog;

        View.OnTouchListener mListener;
        GestureDetector gestureDetector;

        int selected;

        static NewsFragment newInstance(){
            return new NewsFragment();
        }

        public NewsFragment(){

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            this.inflater = inflater;
            rootView = inflater.inflate(R.layout.fragment_news, container, false);
            empty_layout = (RelativeLayout) rootView.findViewById(R.id.non_news_layout);
            news_layout = (ListView) rootView.findViewById(R.id.news_layout);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            gestureDetector = new GestureDetector(getActivity(), this);
            mListener = new View.OnTouchListener(){

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    gestureDetector.onTouchEvent(motionEvent);
                    return false;
                }

            };
            news_layout.setOnTouchListener(mListener);
        }

        @Override
        public void onStart() {
            super.onStart();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(inflater.inflate(R.layout.dialog_progress, null));
            builder.setCancelable(false);
            dialog = builder.show();
            RestClient.get(RestClient.NEWS + "?user=" + email, 2, null, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(JSONArray response) {
                    super.onSuccess(response);
                    news_layout.setAdapter(new CustomArrayAdapter(getActivity(), response));

                    if(response.length() == 0){
                        empty_layout.setVisibility(View.VISIBLE);
                        news_layout.setVisibility(View.GONE);
                    }
                    else{
                        empty_layout.setVisibility(View.GONE);
                        news_layout.setVisibility(View.VISIBLE);
                    }
                    dialog.dismiss();
                }
            });
        }

        @Override
        public void setArguments(Bundle args) {
            super.setArguments(args);
            email = args.getString("email");
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            try {
                CustomArrayAdapter adapter = (CustomArrayAdapter) news_layout.getAdapter();
                selected = news_layout.pointToPosition((int) motionEvent.getX(), (int) motionEvent.getY());
                if(motionEvent.getX() > motionEvent2.getX() && Math.abs(motionEvent.getX() - motionEvent2.getX()) > SWIPE_MIN_DISTANCE && Math.abs(v) > SWIPE_THRESHOLD_VELOCITY) {
                    TranslateAnimation anim = new TranslateAnimation(motionEvent.getX(), motionEvent2.getX(), motionEvent.getY(), motionEvent.getY());
                    anim.setDuration(2000);
                    View view = news_layout.getChildAt(selected);
                    view.startAnimation(anim);
                    //adapter.remove(selected);
                }else if (motionEvent.getX() < motionEvent2.getX() && motionEvent2.getX() - motionEvent.getX() > SWIPE_MIN_DISTANCE && Math.abs(v) > SWIPE_THRESHOLD_VELOCITY) {
                    //adapter.remove(selected);
                    TranslateAnimation anim = new TranslateAnimation(motionEvent2.getX(), motionEvent.getX(), motionEvent.getY(), motionEvent.getY());
                    anim.setDuration(2000);
                    View view = news_layout.getChildAt(selected);
                    view.startAnimation(anim);
                }
                if(adapter.isEmpty()){
                    news_layout.setVisibility(View.GONE);
                    empty_layout.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }
}
