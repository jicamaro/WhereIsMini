package com.where.app;

public class NavigationDrawerDetail implements NavigationDrawerItem {
    public static final int NAVIGATION_DETAIL = 1;

    private String label;
    private int icon;
    private int id;

    public NavigationDrawerDetail(int id, String label, int icon){
        this.id = id;
        this.label = label;
        this.icon = icon;
    }

    public int getId(){
        return id;
    }

    public int getIcon(){
        return icon;
    }

    @Override
    public int getType() {
        return NAVIGATION_DETAIL;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
