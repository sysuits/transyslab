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

import com.transyslab.commons.tools.SimulationClock;

/**
 * LinkTime
 *
 */
public class LinkTimes {

	protected int mode;// 新增属性，mode=0：默认模式，仿真结果只统计一次，mode=1：自定义模式，
						// 集计时间间隔按设定执行

	protected int infoStartPeriod; // the start interval
	protected long infoStartTime; // start time of link time table
	protected long infoStopTime; // stop time of link time table
	protected long infoPeriodLength; // length of each period (second)
	protected long infoTotalLength; // total length
	protected int infoPeriods; // number of time periods

	// Travel time are all measured in seconds.

	protected double[] linkTimes; // 2D travel times on each link
	protected double[] avgLinkTime; // 1D average travel time

	// 0 = used only when the vehicle passes a info node (e.g. vms)
	// 1 = also used as pretrip.
	// default = 1

	protected int preTripGuidance;
	protected SimulationClock simClock;

	public LinkTimes(SimulationClock simClock) {
		linkTimes = null;
		avgLinkTime = null;
		infoPeriods = 0;
		preTripGuidance = 1;
		// 自定义设置模式
		mode = 1;
		this.simClock = simClock;
	}
	public void initTravelTimes(RoadNetwork network) {
		// 待完善的处理，割掉了读文件中每条link的traveltime信息

		if (mode != 0) {

			// Parameters will be read from the file

			// read(filename_);
			// start column
			infoStartPeriod = 0;

			// length in seconds per period
			infoPeriodLength = 300;
			// number of colomns
			infoPeriods = 49;
			infoTotalLength = infoPeriods * infoPeriodLength;
			infoStartTime = Math.round(this.simClock.getStartTime())
					+ infoStartPeriod * infoPeriodLength;
			infoStopTime = infoStartTime + infoTotalLength;
			// link长度/freespeed
			calcLinkTravelTimes(network);

		}
		else {

			infoStartPeriod = 0;
			infoStartTime = Math.round(this.simClock.getStartTime());
			infoStopTime = Math.round(this.simClock.getStopTime());
			infoPeriodLength = infoStopTime - infoStartTime;
			infoTotalLength = infoPeriodLength;
			infoPeriods = 1;

			calcLinkTravelTimes(network);
		}

	}

/*
	public int nDestNodes() {
		return RoadNetwork.getInstance().nDestNodes();
	}*/

	public int infoStartPeriod() {
		return infoStartPeriod;
	}
	public long infoStartTime() {
		return infoStartTime;
	}
	public long infoStopTime() {
		return infoStopTime;
	}
	public int infoPeriods() {
		return infoPeriods;
	}
	public long infoPeriodLength() {
		return infoPeriodLength;
	}
	public long infoTotalLength() {
		return infoTotalLength;
	}

	public int whichPeriod(double t) {
		// Returns the interval that contains time t
		int p = (int) t;
		if (p <= infoStartTime) { // earlier
			return 0;
		}
		else if (p >= infoStopTime) { // later
			return infoPeriods - 1;
		}
		else { // between
			return (int) ((t - infoStartTime) / infoPeriodLength);
		}
	}
	// Returns the time that represents interval p.
	public double whatTime(int p) {
		return infoStartTime + (p + 0.5) * infoPeriodLength;
	}

	public double linkTime(Link i, double timesec) {
		return linkTime(i.getIndex(), timesec);
	}
	// Returns the expected link travel time at the given entry time
	public double linkTime(int k, double timesec) {
		if (infoPeriods > 1) {
			double[] y = linkTimes;
			// float y = linkTimes[k * infoPeriods];
			double dt = ((timesec - infoStartTime) / infoPeriodLength + 0.5);
			int i = (int) dt;
			if (i < 1) {
				return y[k * infoPeriods];
			}
			else if (i >= infoPeriods) {
				return y[k * infoPeriods + infoPeriods - 1];
			}
			else {
				dt = (float) (timesec - infoStartTime - (i - 0.5) * infoPeriodLength);
				double z = y[k * infoPeriods + i - 1];
				return z + (y[k * infoPeriods + i] - z) / infoPeriodLength * dt;
			}
		}
		else {
			return linkTimes[k];
		}
	}
	// Returns the average travel time on a given link
	public double avgLinkTime(Link i) {
		// average
		return avgLinkTime(i.getIndex());
	}
	public double avgLinkTime(int i) {
		return avgLinkTime[i];
	}

	// This function is called by Graph::labelCorrecting(...)

	public double cost(int i, double timesec) {
		return linkTime(i, timesec);
	}
	public double cost(int i) {
		return avgLinkTime[i];
	}
	// Create the default travel times for each link. This function
	// should be called only once.
	public void calcLinkTravelTimes(RoadNetwork network) {
		// should called only once
		int i, j, n = network.nLinks();

		if (linkTimes == null) {
			linkTimes = new double[network.nLinks() * infoPeriods];
		}

		for (i = 0; i < n; i++) {
			double x = network.getLink(i).getGenTravelTime(network.simParameter.freewayBias);
			for (j = 0; j < infoPeriods; j++) {
				linkTimes[i * infoPeriods + j] = x;
			}
		}

		if (infoPeriods > 1) {
			if (avgLinkTime == null)
				avgLinkTime = new double[n];
			for (i = 0; i < n; i++) {
				float sum = 0;
				for (j = 0; j < infoPeriods; j++) {
					sum += linkTimes[i * infoPeriods + j];
				}
				avgLinkTime[i] = sum / infoPeriods;
			}
		}
		else {
			avgLinkTime = linkTimes;
		}		
		update2Graph(network);
	}
	// Update the link travel times. The result is a linear combination
	// of the previous data and new data calculated in the simulation
	// (e.g., based on the average of the expected travel times of all
	// vehicles that are currently in the link or based on the sensors
	// data collected in the most recent time interval, depends on the
	// current value of RN_Link::travelTime() is defined in the
	// derived class).
	public void updateLinkTravelTimes(float alpha, RoadNetwork network) {
		double x;
		double[] py = linkTimes;
		int i, j, n = network.nLinks();
		int k = whichPeriod(network.simClock.getCurrentTime());
		for (i = 0; i < n; i++) {
			Link pl = network.getLink(i);

			// Calculate travel time based speed/density relationship

			double z = pl.calcCurrentTravelTime();

			x = pl.generalizeTravelTime(z,network.simParameter.freewayBias);

			// Update travel time for all future intervals using the same
			// estimates. This is simulating the system that provides
			// guidance based on prevailing/instaneous traffic condition.

			for (j = k; j < infoPeriods; j++) {
				// py = linkTimes + i * infoPeriods + j;
				py[i * infoPeriods + j] =  (alpha * x + (1.0 - alpha) * (py[i * infoPeriods + j]));
			}

			if (infoPeriods > 1) {
				avgLinkTime[i] = x;
			}
		}
		update2Graph(network);
	}
	public void preTripGuidance(int flag) {
		preTripGuidance = flag;
	}
	public int preTripGuidance() {
		return preTripGuidance;
	}
	//wym 将结果更新至RoadNetwork图结构中
	protected void update2Graph(RoadNetwork theRN) {		
		for (int i = 0; i < avgLinkTime.length; i++) {
			Link theLink = theRN.getLink(i);
			theRN.setEdgeWeight(theLink, avgLinkTime[i]);
		}
	}

}
