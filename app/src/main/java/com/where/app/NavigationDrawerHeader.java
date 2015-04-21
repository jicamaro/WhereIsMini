package com.where.app;

public class NavigationDrawerHeader implements NavigationDrawerItem {

    public static final int NAVIGATION_HEADER = 0;

    private String label;

    public NavigationDrawerHeader(String label){
        this.label = label;
    }

    @Override
    public int getType() {
        return NAVIGATION_HEADER;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
