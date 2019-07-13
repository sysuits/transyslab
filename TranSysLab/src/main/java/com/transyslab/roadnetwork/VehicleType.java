package com.transyslab.roadnetwork;

import com.transyslab.simcore.mlp.MLPParameter;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

public class VehicleType {
    public final int type;
    public final double powerRate;
    public final boolean stopping;
    public final double length;
    public final double pcu;

    private static final Map<Integer, VehicleType> TYPE_MAP;
    public static final String DEFAULT_TYPE_ARRAY_STR = "0.8/0.2";//∂‘”¶TYPE_ARRAYΩ‚ Õ
    private static final int[] TYPE_ARRAY = {
            Constants.VEHICLE_REGULAR,//[0]
            Constants.VEHICLE_FREIGHT//[1]
    };

    static {
        Hashtable<Integer,VehicleType> tmp = new Hashtable<>();
        tmp.put(Constants.VEHICLE_REGULAR,
                new VehicleType(Constants.VEHICLE_REGULAR, 1.0, false, 4.6, 1.0));
        tmp.put(Constants.VEHICLE_FREIGHT,
                new VehicleType(Constants.VEHICLE_FREIGHT, 0.7, false, 12, 3.0));
        TYPE_MAP = Collections.unmodifiableMap(tmp);
    }

    public VehicleType(int type, double powerRate, boolean stopping, double length, double pcu) {
        this.type = type;
        this.powerRate = powerRate;
        this.stopping = stopping;
        this.length = length;
        this.pcu = pcu;
    }

    public static VehicleType getType(Integer type){
        return TYPE_MAP.get(type);
    }

    public static double getPowerRate(Integer type){
        VehicleType vt = getType(type);
        if (vt==null){
            System.err.println("no such predefined vehicle type.");
            return 1.0;
        }
        return vt.powerRate;
    }

    public static int getTypeID(int idx){
        if (idx>=0 && idx<TYPE_ARRAY.length)
            return TYPE_ARRAY[idx];
        else{
            System.err.println("Vehicle type idx error");
            return TYPE_ARRAY[0];
        }
    }
}
