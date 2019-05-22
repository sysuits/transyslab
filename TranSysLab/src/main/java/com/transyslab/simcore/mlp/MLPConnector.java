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

package com.transyslab.simcore.mlp;

import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.roadnetwork.Connector;
import com.transyslab.roadnetwork.GeoPoint;
import com.transyslab.roadnetwork.Lane;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MLPConnector extends Connector {
    protected LinkedList<MLPVehicle> vehsOnConn;
    private List<MLPConnector> conflictConns;
    private MLPNode node;
    private double length;
    public MLPLane upLane;
    public MLPLane dnLane;

    public MLPConnector(long id, List<GeoPoint> shapePoints, MLPLane upLane, MLPLane dnLane) {
        super(id, shapePoints, upLane.getId(), dnLane.getId());
        vehsOnConn = new LinkedList<>();
        conflictConns = new ArrayList<>();
        length = -1.0;
        node = null;
        this.upLane = upLane;
        this.dnLane = dnLane;
        this.init(id,upLaneId,dnLaneId,shapePoints);
    }

    public void setNode(MLPNode node) {
        this.node = node;
    }

    protected void clearVehsOnConn(){
        vehsOnConn.clear();
    }

    public int vehNumOnConn() {
        return vehsOnConn.size();
    }

    public double getLength() {
        if (length<0) {
            length = 0.0;
            for (int i = 0; i < shapePoints.size() - 1; i++)
                length += shapePoints.get(i).distance(shapePoints.get(i+1));
        }
        return length;
    }

    public double spaceOccupied() {
        return vehsOnConn.getLast().getDistance() / this.getLength();
    }

    public int queueNum() {
        return vehsOnConn.size();
    }

    public boolean checkVolume(MLPVehicle mlpv) {
        MLPVehicle tail_ = getTail();
        if (tail_ != null &&
                getLength() - tail_.getDistance() <
                        (mlpv.getLength() + ((MLPParameter)mlpv.getMLPNetwork().getSimParameter()).minGap(mlpv.getCurrentSpeed()))) {
            return false;
        }
        else
            return true;
    }

    public MLPVehicle getTail(){
        if (vehsOnConn.isEmpty())
            return null;
        else
            return vehsOnConn.getLast();
    }

    protected void addConflictConn(MLPConnector connector) {
        if (checkConflict(connector))
            conflictConns.add(connector);
    }

    protected void addConflictConns(List<Connector> connectors) {
        connectors.forEach(c->conflictConns.add((MLPConnector) c));
    }

    public boolean checkConflict(MLPConnector connector) {
        return GeoUtil.isCross(this.getShapePoints(),connector.getShapePoints());
    }

    public double conflictCoef(){
        double yita = 1.0, alpha = 1.0, beta = 1.0;
        double c = 1.0;
        for (int i = 0; i < conflictConns.size(); i++) {
//            c *= conflictConns.get(i).spaceOccupied();
            MLPConnector conflictConn = conflictConns.get(i);
            double qRate = getQRate(conflictConn);
            double k = ((double) conflictConn.vehsOnConn.size()) / conflictConn.getLength();
            double km = ((MLPLink) dnLane.getLink()).dynaFun.linkCharacteristics[2];
            if (k>=km)
                return 0.0;//deadlock
            c *= Math.pow(1 - qRate*yita*Math.pow(k/km,alpha),beta);
        }
        return c;
    }

    public double getQRate(MLPConnector conflictConn){
        //todo: ÁÙÊ±ÐÞ¸Ä²âÊÔº¯Êý
        return 1.0;
//        double top = (double) this.node.lcList.stream().filter(c -> c.upLinkID()==conflictConn.upLinkID() && c.dnLinkID()==conflictConn.dnLinkID()).count();
//        double butt = (double) this.node.lcList.stream().filter(c -> c.upLinkID()==this.upLinkID() && c.dnLinkID()==this.dnLinkID()).count();
//        return top / butt;
    }

    protected List<MLPVehicle> updateVehs(){
        List<MLPVehicle> outputs = new ArrayList<>();
        for (MLPVehicle veh:vehsOnConn) {
            //todo update newdis newSpd here
            if (veh.newDis <=0 )
                outputs.add(veh);
        }
        return outputs;
    }

    public double calSpd(){
        MLPLink link = (MLPLink) upLane.getLink();
//        double spd_normal = link.dynaFun.sdFun(((double)queueNum())/getLength());
//        double rate = node.getPassSpd() / link.dynaFun.getFreeFlow();
//        return spd_normal*rate*conflictCoef();
        double alpha=1.0,beta=1.0;
        double k = ((double)queueNum())/getLength();
        double km = ((MLPLink) dnLane.getLink()).dynaFun.linkCharacteristics[2];
        if (k>=km)
            return 0.0;
        double spd_normal = node.getPassSpd() * Math.pow(1-Math.pow(k/km,alpha),beta);
        double spd = spd_normal * conflictCoef();
        return spd;
    }

//    public GeoPoint getStartPoint(){
//        return upLane.getEndPnt();
//    }
//
//    public GeoPoint getEndPoint(){
//        return dnLane.getStartPnt();
//    }

    public long upLinkID() {
        return upLane.getLink().getId();
    }

    public long dnLinkID() {
        return dnLane.getLink().getId();
    }

    public double getTailPos() {
        if (vehsOnConn.size()<=0)
            return getLength();
        return getLength() - vehsOnConn.getLast().getDistance() - vehsOnConn.getLast().getLength();
    }
}
