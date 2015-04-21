package com.where.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NavigationDrawerFragment extends Fragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private NavigationDrawerCallbacks mCallbacks;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
    private NavigationDrawerItem[] items;

    private int mCurrentSelectedPosition = 1;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavigationDrawerItem navigationDrawerItem = (NavigationDrawerItem) parent.getAdapter().getItem(position);
                if(navigationDrawerItem.getType() != NavigationDrawerHeader.NAVIGATION_HEADER) selectItem(position);
            }
        });

        items = new NavigationDrawerItem[]{
                new NavigationDrawerHeader(getResources().getString(R.string.title_header_one)),
                new NavigationDrawerDetail(100, getResources().getString(R.string.title_detail_one), R.drawable.ic_bus),
                new NavigationDrawerDetail(101, getResources().getString(R.string.title_detail_two), R.drawable.ic_support_bus),
                new NavigationDrawerHeader(getResources().getString(R.string.title_header_two)),
                new NavigationDrawerDetail(200, getResources().getString(R.string.title_detail_three), R.drawable.ic_stops),
                new NavigationDrawerHeader(getResources().getString(R.string.title_header_three)),
                new NavigationDrawerDetail(300, getResources().getString(R.string.title_detail_four), R.drawable.ic_schedule),
                new NavigationDrawerHeader(getResources().getString(R.string.title_header_four)),
                new NavigationDrawerDetail(400, getResources().getString(R.string.title_detail_five), R.drawable.ic_info),
                new NavigationDrawerDetail(401, getResources().getString(R.string.title_detail_six), R.drawable.ic_help),
                new NavigationDrawerDetail(402, getResources().getString(R.string.title_detail_seven), R.drawable.ic_info)
        };

        mDrawerListView.setAdapter(new NavigationDrawerAdapter(getActionBar().getThemedContext(),
                R.id.list_row_detail_label,
                items));
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                mDrawerLayout,
                R.drawable.ic_navigation_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit();
                }

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }

    public class NavigationDrawerAdapter extends ArrayAdapter<NavigationDrawerItem> {

        LayoutInflater inflater;

        public NavigationDrawerAdapter(Context context, int resource, NavigationDrawerItem[] objects) {
            super(context, resource, objects);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            NavigationDrawerItem menuItem = this.getItem(position);
            if ( menuItem.getType() == NavigationDrawerDetail.NAVIGATION_DETAIL ) {
                view = getDetailView(convertView, parent, menuItem);
            }
            else {
                view = getHeaderView(convertView, parent, menuItem);
            }
            return view ;
        }

        private View getHeaderView(View convertView, ViewGroup parent, NavigationDrawerItem menuItem) {
            NavigationDrawerHeader navigationDrawerHeader = (NavigationDrawerHeader) menuItem;

            if(convertView == null){
                convertView = inflater.inflate(R.layout.list_row_header, parent, false);
                TextView labelView = (TextView) convertView.findViewById(R.id.list_row_header_label);
                labelView.setText(navigationDrawerHeader.getLabel());
            }

            return convertView;
        }

        private View getDetailView(View convertView, ViewGroup parent, NavigationDrawerItem menuItem) {
            NavigationDrawerDetail navigationDrawerDetail = (NavigationDrawerDetail) menuItem;

            if(convertView == null){
                convertView = inflater.inflate(R.layout.list_row_detail, parent, false);
                TextView labelView = (TextView) convertView.findViewById(R.id.list_row_detail_label);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.list_row_detail_icon);
                labelView.setText(navigationDrawerDetail.getLabel());
                imageView.setImageDrawable(getResources().getDrawable(navigationDrawerDetail.getIcon()));
            }

            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return this.getItem(position).getType();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getPosition(NavigationDrawerItem item) {
            return super.getPosition(item);
        }

        @Override
        public NavigationDrawerItem getItem(int position) {
            return super.getItem(position);
        }
    }
}
