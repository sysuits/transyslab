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

package com.transyslab.commons.tools;

import java.util.HashMap;
import java.util.TimeZone;


public class SimulationClock {

	private static long baseTime; // time of 00:00:00AM today
	private static long localTime; // current local clock time

	private double startTime; // simulation start time
	private double stopTime; // simulation stop time
	private double simulationTime; // total simultion time
	private double stepSize; // simulation step size
	private double currentTime; // current simulation clock time
	private double masterTime; // current time of master clock

	public SimulationClock() {
		masterTime = 86400.0;
		/*startTime = 12 * 3600f;
		stopTime = 14 * 3600 + 50 * 60f;*/
		startTime = Double.NaN;
		stopTime = Double.NaN;
		stepSize = Double.NaN;
	}


	/*public void init() {
		init(startTime, stopTime, stepSize);
	}*/

	public int init(double start, double stop, double step)// 1.0
	{
		setBaseTime();

		if (start > stop || step < 0.0) {
			return 1;
		}
		this.startTime = start;
		this.stopTime = stop;
		this.stepSize = step;
		resetTimer();

//------------���������壬�ѷϳ�-------------
//		setStartTime(start);
//		setStopTime(stop);
//------------���������壬�ѷϳ�-------------

		return 0;
	}

	//wym ������start/stoptime�ı��ʱ�����
	public void resetTimer(){
		if (Double.isNaN(startTime)||Double.isNaN(stopTime))
			System.err.println("δ���ÿ�ʼ/����ʱ��");
		simulationTime = stopTime - startTime;
		currentTime = startTime;

	}

	//------------���������壬�ѷϳ�-------------
	/*public void setStartTime(double t) {
		startTime = t;
		currentTime = t;
		simulationTime = stopTime - startTime;
	}
	public void setStopTime(double t) {
		stopTime = t;
		simulationTime = stopTime - startTime;
	}
	public void setStepSize(double step) {
		stepSize = step;

	}*/
	//------------���������壬�ѷϳ�-------------

	public double getMasterTime() {
		return masterTime;
	}
	public void setMasterTime(double m) {
		masterTime = m;
	}
	public boolean isWaiting() {
		return (currentTime >= masterTime);
	}

	public double getStartTime() {
		if (Double.isNaN(startTime))
			System.err.println("δ���ÿ�ʼʱ��");
		return startTime;
	}
	public double getStopTime() {
		if (Double.isNaN(stopTime))
			System.err.println("δ���ý���ʱ��");
		return stopTime;
	}
	public double getStepSize() {
		if (Double.isNaN(stepSize))
			System.err.println("δ���÷��沽��");
		return stepSize;
	}


	public void advance(double step) {
		// static long cycle_clks = 0;
		// long cycle_clks = 0;
		// This deals with the simulated time

		currentTime += step;
		int sec10 = (int) (10.0 * currentTime + 0.5);
		currentTime = sec10 / 10.0;
	}

	public double getCurrentTime() {
		return currentTime;
	}



	private void setBaseTime() {
		// These deal with simulated time

		baseTime = System.currentTimeMillis() / (1000 * 3600 * 24) * (1000 * 3600 * 24)
				- TimeZone.getDefault().getRawOffset();
		localTime = System.currentTimeMillis();;

		// Reference time of 00:00:00AM today

	}
	public double getDuration() {
		return simulationTime;
	}
}
