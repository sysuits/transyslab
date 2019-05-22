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


import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class Java2DUtil {

    public static void drawArrow(Graphics2D g2, Arrow2D arrow) {

        // 画线
        g2.drawPolyline(arrow.getPolylineXs(),arrow.getPolylineYs(),arrow.polyline.length);
        // 画箭头
        GeneralPath triangle = new GeneralPath();
        triangle.moveTo(arrow.arrowTip[0].getX(), arrow.arrowTip[0].getY());
        triangle.lineTo(arrow.arrowTip[1].getX(), arrow.arrowTip[1].getY());
        triangle.lineTo(arrow.arrowTip[2].getX(), arrow.arrowTip[2].getY());
        triangle.closePath();
        //实心箭头
        g2.fill(triangle);
        //非实心箭头
        //g2.draw(triangle);

    }

    // 计算旋转
    public static double[] rotate(double px, double py, double theta) {
        double vx =   px * Math.cos(theta) + py * Math.sin(theta);
        double vy = - px * Math.sin(theta) + py * Math.cos(theta);
        return new double[]{vx,vy};
    }
    public static Vector3D rotate(Vector3D p, double theta){
        double vx =   p.getX() * Math.cos(theta) + p.getY() * Math.sin(theta);
        double vy = - p.getX() * Math.sin(theta) + p.getY() * Math.cos(theta);
        return new Vector3D(vx,vy,0);
    }

}
