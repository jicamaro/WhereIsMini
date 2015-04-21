package com.where.utils;

public class PriorityQueue {
    Node data[];
    int size, items;

    public PriorityQueue(int size){
        this.size = size;
        data = new Node[this.size];
        items = 0;
    }

    public boolean isEmpty(){
        return items == 0;
    }

    public boolean isFull(){
        return items == size;
    }

    public void add(Node item){
        int position = items-1;
        if(!isFull()){
            while (position >= 0 && data[position].distance >= item.distance){
                data[position+1] = data[position];
                --position;
            }
            data[position+1] = item;
            items++;
        }
        else{
            while (position >= 0 && data[position].distance >= item.distance){
                if(position < items-1){
                    data[position+1] = data[position];
                }
                --position;
            }
            if(position < items-1) data[position+1] = item;
        }
    }

    public Node delete(){
        --items;
        return data[items];
    }

    public Node get(int position){
        return data[position];
    }
}