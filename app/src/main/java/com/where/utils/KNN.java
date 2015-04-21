package com.where.utils;

import java.util.ArrayList;

public class KNN {

    public static Node knn(ArrayList<Node> nodes, double x, double y){
        PriorityQueue knn_list = new PriorityQueue(1);
        for(Node node:nodes){
            node.distance = distance(node, new Node(x, y));
            knn_list.add(node);
        }
        return knn_list.get(0);
    }

    private static double distance(Node p, Node d){
        return Math.sqrt(Math.pow(p.x-d.x,2)+Math.pow(p.y-d.y, 2));
    }
}
