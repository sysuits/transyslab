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


package com.transyslab.simcore.mesots;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Parameter;


public class MesoParameter extends Parameter {

	final static float ETC_RATE = 0.3f;
	final static float HOV_RATE = 0.25f;

	final static float VEHICLE_LENGTH = 6.0960f; // 单位米， 20 feet

	final static float CELL_RSP_LOWER = 30.48f; // 单位米，about 200 feet
	final static float CELL_RSP_UPPER = 91.44f; // 单位米，about 500 feet
	final static float CHANNELIZE_DISTANCE = 60.96f; // 单位米，about 400 feet

	protected int nVehicleClasses;
	protected float[] vehicleClassCDF;
	protected String[] vehicleName;
	protected double updateStepSize;// 从MESO_Engine转移过来
	protected float hovRate;
	protected float etcRate;
	protected float[] vehicleLength; // meter

	protected float[] limitingParam; // min headway, headway/speed slope,
	// max acc, min speed, etc.

	protected float[] queueParam; // max speed for queue releasing

	protected double cellSplitGap; // gap threshold for split a cell
	protected double rspLower;
	protected double rspUpper;
	protected double channelizeDistance; // from dnNode (meter)
	protected double simStepSize;
	/*
	public static MesoParameter getInstance() {
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MesoNetworkPool.getInstance().getParameter(threadid);
	}*/

	public MesoParameter() {
		nVehicleClasses = 1;
		vehicleClassCDF = new float[1];
		vehicleLength = new float[1];
		vehicleName = new String[1];
		etcRate = ETC_RATE;
		hovRate = HOV_RATE;
		rspLower = CELL_RSP_LOWER;
		rspUpper = CELL_RSP_UPPER;
		cellSplitGap = 0.5f * (CELL_RSP_LOWER + CELL_RSP_UPPER);
		channelizeDistance = CHANNELIZE_DISTANCE;
		// limitingParam = null;
		// queueParam = null;
		limitingParam = new float[3];
		queueParam = new float[3];

		vehicleClassCDF[0] = 1.0f;
		vehicleLength[0] = VEHICLE_LENGTH;
		vehicleName[0] = new String("Cars");
		// 从mesolib文件读入的默认参数值
		limitingParam[0] = (float) (5.0 * lengthFactor);
		limitingParam[1] = 1.36f;
		limitingParam[2] = (float) (5.0 * speedFactor);// km/hour
		queueParam[0] = -0.001f;
		queueParam[1] = (float) (25.0 * speedFactor);
		queueParam[2] = 100.0f;// seconds

		simStepSize = 0.2f;
	}
	public void setSimStepSize(float stepSize){
		this.simStepSize = stepSize;
	}
	public double getUpdateStepSize() {
		return updateStepSize;
	}
	public void setUpdateStepSize(double uss) {
		updateStepSize = uss;
	}
	public int nVehicleClasses() {
		return nVehicleClasses;
	}
	public float[] vehicleClassCDF() {
		return vehicleClassCDF;
	}
	public float vehicleClassCDF(int i) {
		return vehicleClassCDF[i];
	}
	public float hovRate() {
		return hovRate;
	}
	public float etcRate() {
		return etcRate;
	}
	public float vehicleLength(int i) {
		if (i >= 0 && i < nVehicleClasses)
			return vehicleLength[i];
		else
			return vehicleLength[0];
	}
	public String vehicleName(int i) {
		return vehicleName[i];
	}

	// This returns the minimum getDistance gap for a given speed
	public double minGap(double speed) {
		return minHeadwayGap() + headwaySpeedSlope() * speed;
	}

	// This returns a maximum speed for a give gap, in a n-lane segment
	public double maxSpeed(double gap, int n) {
		double dt =  (this.simStepSize+ headwaySpeedSlope() / n);
		double dx = gap - minHeadwayGap() / n;
		double v = dx / dt;
		if (v > 40)
			return 40;
		return (v > 0.0) ? v : 0.0f;
	}

	public double cellSplitGap() {
		return cellSplitGap;
	}
	public double rspLower() {
		return rspLower;
	}
	public double rspUpper() {
		return rspUpper;
	}
	public double rspRange() {
		return rspUpper - rspLower;
	}
	public void setCellSplitGap(float dmax) {
		cellSplitGap = dmax;
	}
	public void updateCSG() {
		cellSplitGap = 0.5f * (rspLower + rspUpper);
	}
	public void setRspLower(float dmin) {
		rspLower = dmin;
	}
	public void setRspUpper(float dupper) {
		rspUpper = dupper;
	}
	public double channelizeDistance() {
		return channelizeDistance;
	}

	public double minHeadwayGap() {
		return limitingParam[0];
	}
	public float headwaySpeedSlope() {
		return limitingParam[1];
	}
	public float minSpeed() {
		return limitingParam[2];
	}

	public double queueReleasingSpeed(double t, double v_f) {
		if (t > queueParam[2])
			return v_f;

		double r = 1.0 -  Math.exp(queueParam[0] * t * t);
		return queueParam[1] + (v_f - queueParam[1]) * r;
	}

}
