package org.example;

import java.awt.*;

class Vector{
    private final float[] points;
    Vector(float... points){
        this.points = points;
    }

    Vector(int size){
        points = new float[size];
    }

    public float x(int i){
        return points[i];
    }

    public void setX(int pos, float value){
        points[pos] = value;
    }

    public int size(){
        return points.length;
    }

    public float distanceFrom(Vector x){
        if(size() != x.size()){
            return Float.NaN;
        }

        float total = 0;
        for(int i=0;i<x.size();i++){
            total += (x.x(i) - x(i)) * (x.x(i) - x(i));
        }

        return (float) Math.sqrt(total);
    }

    public void add(Vector v){
        for(int i=0;i<points.length;i++){
            points[i] += v.x(i);
        }
    }

    public Vector scaleBy(float x){
        for(int i=0;i<points.length;i++){
            points[i] *= x;
        }

        return this;
    }

    public int intRGB(){
        Color color = new Color(Math.round(points[0]), Math.round(points[1]), Math.round(points[2]));

        return color.getRGB();
    }
}