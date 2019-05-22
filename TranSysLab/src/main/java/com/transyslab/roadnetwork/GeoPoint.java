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

/**
 */
public class GeoPoint {
	public static final double POINT_EPSILON = 0.1;
	private double[] locCoods;
	private float[] locCoodsf;
	public GeoPoint() {
		locCoods = new double[3];
	}
	//二维坐标
	public GeoPoint(double x, double y) {
		locCoods = new double[3];
		locCoods[0] = x;
		locCoods[1] = y;
		locCoods[2] = 0;
	}
	//三维坐标
	public GeoPoint(double x, double y, double z) {
		locCoods = new double[3];
		locCoods[0] = x;
		locCoods[1] = y;
		locCoods[2] = z;
	}
	public GeoPoint(GeoPoint gPoint){
		locCoods = new double[3];
		this.locCoods[0] = gPoint.locCoods[0];
		this.locCoods[1] = gPoint.locCoods[1];
		this.locCoods[2] = gPoint.locCoods[2];
	}
	public GeoPoint(GeoPoint spt, GeoPoint ept, double r) {
		locCoods = new double[3];
		for(int i=0; i<3; i++){
			this.locCoods[i] = r * spt.locCoods[i] + (1.0 - r) * ept.locCoods[i];
		}
	}
	public GeoPoint(final double[] xyz){
		locCoods = new double[3];
		for(int i=0; i< xyz.length; i++){
			this.locCoods[i] = xyz[i];
		}
	}
	public void setLocCoods(double x, double y, double z) {
		locCoods[0] = x;
		locCoods[1] = y;
		locCoods[2] = z;
	}
	public void setLocCoods(double[] xyz){
		if(xyz.length>3){
			System.out.println("Error: check the length of array");
			return;
		}
		locCoods[0] = xyz[0];
		locCoods[1] = xyz[1];
		locCoods[2] = xyz[2];
	}

	/*
	 * Interpolate a point between this and p with ratio r. Notice that r
	 * corresponses to the distance from p.
	 */

	public GeoPoint intermediate(GeoPoint p, double r) {
		return new GeoPoint(r * this.locCoods[0] + (1.0 - r) * p.locCoods[0], 
						 r * this.locCoods[1] + (1.0 - r) * p.locCoods[1], 
				         r * this.locCoods[2] + (1.0 - r) * p.locCoods[2]);
	}

	public double getLocationX() {
		return locCoods[0];
	}
	public double getLocationY() {
		return locCoods[1];
	}
	public double getLocationZ(){
		return locCoods[2];
	}
	public double[] getLocCoods() {
		return locCoods;
	}

	public double distanceSquared(GeoPoint p) {
		double e_diff = p.locCoods[0] - this.locCoods[0];
		double n_diff = p.locCoods[1] - this.locCoods[1];
		double h_diff = p.locCoods[2] - this.locCoods[2];
		return (n_diff * n_diff + e_diff * e_diff + h_diff * h_diff);
	}
	// Distance to another point
	public double distance(GeoPoint p) {
		return Math.sqrt(distanceSquared(p));
	}
	public boolean equal(GeoPoint p, double epsilon) {
		return ((Math.abs(this.locCoods[0] - p.locCoods[0]) < epsilon) &&
				(Math.abs(this.locCoods[1] - p.locCoods[1]) < epsilon) && 
				(Math.abs(this.locCoods[2] - p.locCoods[2]) < epsilon));
	}
	public boolean equal(final GeoPoint p) {
		return ((Math.abs(this.locCoods[0] - p.locCoods[0]) < Constants.BULGE_EPSILON) &&
				(Math.abs(this.locCoods[1] - p.locCoods[1]) < Constants.BULGE_EPSILON) &&
				(Math.abs(this.locCoods[2] - p.locCoods[2]) < Constants.BULGE_EPSILON));
	}
	/*
	 * Compute the angle (in radian) of a line from this point to point 'pnt'.
	 */
	public double angle(GeoPoint pnt) {
		double dx, dy, dis, alpha;
		dx = pnt.locCoods[0] - this.locCoods[0];
		dy = pnt.locCoods[1] - this.locCoods[1];
		dis = dx * dx + dy * dy;

		if (dis > 1.0E-10) {
			alpha = Math.acos(dx / Math.sqrt(dis));
			if (dy < 0.0)
				alpha = 2 * Math.PI - alpha;
		}
		else {
			alpha = 0.0;
		}

		return alpha;
	}
	// Calculate a point with a distance of 'offset' and an angle of
	// 'alpha' wrt point 'p'. The angle must be in radian.
	public GeoPoint bearing(double offset, double alpha) {
		return new GeoPoint(this.locCoods[0] + offset * Math.cos(alpha), 
				         this.locCoods[1] + offset * Math.sin(alpha));
	}
	public float[] getLocCoodsf(){
		if(locCoodsf == null){
			locCoodsf = new float[3];
			for(int i=0;i<3;i++){
				locCoodsf[i] = (float) locCoods[i];
			}
		}
		return locCoodsf;
	}

}
