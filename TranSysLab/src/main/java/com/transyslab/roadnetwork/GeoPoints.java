package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeoPoints {
    public static String toString(List<GeoPoint> points){
        StringBuilder sb = new StringBuilder();
        points.forEach(p->{
            sb.append(p.toString()).append("#");
        });
        return sb.substring(0,sb.length()-1);
    }
    public static List<GeoPoint> parse(String pointsStr){
        List<GeoPoint> points = new ArrayList<>();
        String[] pointsStrArray = pointsStr.split("#");
        Arrays.stream(pointsStrArray).forEach(ps->{
            points.add(GeoPoint.parse(ps));
        });
        return points;
    }
}
