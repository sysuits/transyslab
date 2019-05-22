/*
 * Copyright 2019 The TranSysLab Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.transyslab.roadnetwork;

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.simcore.rts.RTLane;


public class StateData implements NetworkObject{
    protected NetworkObject stateOn;// Segment or Lane
    protected GeoSurface surface;
    protected double avgSpeed;
    protected double queueLength;
    public StateData(NetworkObject stateOn){
        this.stateOn = stateOn;
        initialize();
    }
    private void initialize(){
        if(stateOn instanceof RTLane){
            RTLane curLane = (RTLane) stateOn;
            this.avgSpeed = curLane.getAvgSpeed();
            this.queueLength = curLane.getQueueLength();//
            if(queueLength <curLane.getGeoLength()){
                double[] linearDistance = curLane.getLinearRelation();
                double distance = curLane.getGeoLength() - queueLength;
                int index = FitnessFunction.binarySearchIndex(linearDistance,distance);
                if(index == linearDistance.length)
                    System.out.println("Error: wrong distance on connector");

                this.surface = GeoUtil.multiLines2Rectangles(curLane.getCtrlPoints().subList(index-1,curLane.getCtrlPoints().size()),Constants.LANE_WIDTH,true);
            }
            else
                this.surface = null;// 排队长度超出车道长度时surface为空

        }
    }

    public GeoSurface getSurface(){
        return this.surface;
    }
    public double getAvgSpeed(){
        return this.avgSpeed;
    }
    public NetworkObject getStateOn(){
        return this.stateOn;
    }
    @Override
    public long getId() {
        return stateOn.getId();
    }

    @Override
    public String getObjInfo() {
        return null;
    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public void setSelected(boolean flag) {

    }
}
