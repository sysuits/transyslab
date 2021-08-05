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

package com.transyslab.gui;

import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.roadnetwork.GeoPoint;
import com.transyslab.roadnetwork.GeoSurface;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.awt.geom.Rectangle2D;

public class Arrow2D {
    //public final static int SCALER = 5;
    public final static int LEFTARROW = 1;
    public final static int STRAIGHTARROW = 2;
    public final static int RIGHTARROW = 3;
    public final static double LINELENGTH = 15;
    public final static double TIPLENGTH =7.5;
    public final static double TURNLENGTH = 5;
    public final static double TURNANGLE= 45.0/180.0*Math.PI;
    protected int id;
    protected int type;
    protected Vector3D[] polyline;
    protected Vector3D[] arrowTip;// Èý½ÇÐÎ
    protected Vector3D deltaXY;

    public Arrow2D(int id, int type, int[] fPoint, int[] deltaXY){
        this.id = id;
        this.type = type;
        this.arrowTip = new Vector3D[3];
        this.polyline = new Vector3D[3];
        this.polyline[0] = new Vector3D(fPoint[0],fPoint[1],0.0);
        this.deltaXY = new Vector3D(deltaXY[0],deltaXY[1],0.0).normalize();
        initialize();
    }
    public Arrow2D(int id, String type, int[] fPoint, int[] deltaXY){
        this.id = id;
        switch (type){
            case "L":
                this.type = Arrow2D.LEFTARROW;
                break;
            case "S":
                this.type = Arrow2D.STRAIGHTARROW;
                break;
            case "R":
                this.type = Arrow2D.RIGHTARROW;
                break;
            default:
                System.out.println("Error: No such type of arrow");
                break;
        }
        this.arrowTip = new Vector3D[3];
        this.polyline = new Vector3D[3];
        this.polyline[0] = new Vector3D(fPoint[0],fPoint[1],0.0);
        this.deltaXY = new Vector3D(deltaXY[0],deltaXY[1],0.0).normalize();
        initialize();
    }
    public int[] getPolylineXs(){
        int[] result = new int[3];
        for(int i=0;i<polyline.length;i++){
            result[i] = (int)Math.ceil(polyline[i].getX());
        }
        return result;
    }
    public int[] getPolylineYs(){
        int[] result = new int[3];
        for(int i=0;i<polyline.length;i++){
            result[i] = (int)Math.ceil(polyline[i].getY());
        }
        return result;
    }
    private void initialize() {
        // polyline fpoint
        polyline[1] = polyline[0].add(deltaXY.scalarMultiply(LINELENGTH - TURNLENGTH));
        polyline[2] = polyline[1].add(deltaXY.scalarMultiply(TURNLENGTH));
        arrowTip[0] = polyline[2].add(deltaXY.scalarMultiply(TIPLENGTH));
        GeoPoint fPoint = new GeoPoint(polyline[2].getX(), polyline[2].getY(), polyline[2].getZ());
        GeoPoint tPoint = new GeoPoint(arrowTip[0].getX(), arrowTip[0].getY(), arrowTip[0].getZ());
        GeoSurface rectangle2 = GeoUtil.lineToRectangle(fPoint, tPoint, 2*TIPLENGTH / Math.tan(Math.PI / 3.0), true);
        arrowTip[1] = new Vector3D(rectangle2.getKerbList().get(0).getLocCoods());
        arrowTip[2] = new Vector3D(rectangle2.getKerbList().get(1).getLocCoods());
        if (type != STRAIGHTARROW) {
            double theta;
            if (type == LEFTARROW)
                theta = TURNANGLE;
            else
                theta = -1 * TURNANGLE;
            polyline[2] = polyline[2].subtract(polyline[1]);
            arrowTip[0] = arrowTip[0].subtract(polyline[1]);
            arrowTip[1] = arrowTip[1].subtract(polyline[1]);
            arrowTip[2] = arrowTip[2].subtract(polyline[1]);

            double[] rotateXY = Java2DUtil.rotate(polyline[2].getX(), polyline[2].getY(), theta);
            polyline[2] = new Vector3D(rotateXY[0], rotateXY[1], 0.0);
            rotateXY = Java2DUtil.rotate(arrowTip[0].getX(), arrowTip[0].getY(), theta);
            arrowTip[0] = new Vector3D(rotateXY[0], rotateXY[1], 0.0);
            rotateXY = Java2DUtil.rotate(arrowTip[1].getX(), arrowTip[1].getY(), theta);
            arrowTip[1] = new Vector3D(rotateXY[0], rotateXY[1], 0.0);
            rotateXY = Java2DUtil.rotate(arrowTip[2].getX(), arrowTip[2].getY(), theta);
            arrowTip[2] = new Vector3D(rotateXY[0], rotateXY[1], 0.0);

            polyline[2] = polyline[2].add(polyline[1]);
            arrowTip[0] = arrowTip[0].add(polyline[1]);
            arrowTip[1] = arrowTip[1].add(polyline[1]);
            arrowTip[2] = arrowTip[2].add(polyline[1]);
        }

    }
}
