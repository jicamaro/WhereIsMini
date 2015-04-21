package com.where.utils;


public class Node {

    public String name;
    public double x;
    public double y;
    double distance;

    public Node(){
        x = 0;
        y = 0;
        distance = Double.NEGATIVE_INFINITY;
    }

    public Node(double x, double y){
        this.name = "";
        this.x = x;
        this.y = y;
        distance = Double.NEGATIVE_INFINITY;
    }

    public Node(String name, double x, double y){
        this.name = name;
        this.x = x;
        this.y = y;
        distance = Double.NEGATIVE_INFINITY;
    }

    @Override
    public String toString() {
        return name;
    }
}

