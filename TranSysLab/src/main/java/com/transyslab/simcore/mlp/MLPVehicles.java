package com.transyslab.simcore.mlp;

import com.transyslab.roadnetwork.VehicleType;

import java.util.List;

public class MLPVehicles {
    public static double calPCU(List<MLPVehicle> vehList){
        if (vehList==null)
            return 0;
        return vehList.stream().mapToDouble(MLPVehicle::getPCU).sum();
    }
}
