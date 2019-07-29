package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignalTimeSerial {
    public static final int GREEN = 3;
    public static final int AMBER = 2;
    public static final int RED = 1;

    private List<Double> timeSerial;
    private int currentIdx;
    private double remain;

    public SignalTimeSerial(String numStr) {
        String[] num = numStr.split("#");
        if (num.length<2){
            System.err.println("wrong signal time serial input");
            return;
        }
        this.timeSerial = new ArrayList<>();
        for (int i = 0; i < num.length; i++) {
            timeSerial.add(Double.parseDouble(num[i]));
        }
        reset();
    }

    public void reset(){
        currentIdx = 0;
        remain = timeSerial.get(0);
    }

    public int advance(double step, boolean vehArrival){
        remain -= step;
        if (remain>0)
            return 0;
        if (currentState()<=AMBER || vehArrival)
            currentIdx += 1;
        else
            currentIdx = Math.max(currentIdx+1, getAmberIdx());
        if (currentIdx<timeSerial.size()){
            remain += timeSerial.get(currentIdx);
            return 1;
        }
        else{
            reset();
            return -1;
        }
    }

    public int currentState(){
        if (timeSerial.size()>2)
            return Math.min(3, timeSerial.size() - currentIdx);
        else
            return 3 - currentIdx;
    }

    public int getAmberIdx(){
        if (timeSerial.size()>2)
            return timeSerial.size() - 2;
        else
            return 1;
    }
}
