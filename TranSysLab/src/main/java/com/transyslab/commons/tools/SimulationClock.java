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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.TimeZone;


public class SimulationClock {

	private static long baseTime; // time of 00:00:00AM today
	private static long localTime; // current local clock time

	public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private LocalDateTime zeroTime;
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
		zeroTime = LocalDateTime.MIN;
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

		if (zeroTime==null)
			zeroTime = LocalDateTime.now().with(LocalTime.MIN);
		if (start > stop || step < 0.0) {
			return 1;
		}
		this.startTime = start;
		this.stopTime = stop;
		this.stepSize = step;
		resetTimer();

//------------易引起歧义，已废除-------------
//		setStartTime(start);
//		setStopTime(stop);
//------------易引起歧义，已废除-------------

		return 0;
	}

	public static void main(String[] args) {
		System.out.println(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS));
		System.out.println(LocalDateTime.now().with(LocalTime.MIN).until(LocalDateTime.now(),ChronoUnit.SECONDS));
		System.out.println(LocalDateTime.now().with(LocalTime.MIN).until(LocalDateTime.now(),ChronoUnit.MILLIS));
		System.out.println(LocalDateTime.now().with(LocalTime.MIN).until(LocalDateTime.now(),ChronoUnit.MILLIS) / 1000.0);
		System.out.println((double)LocalDateTime.now().with(LocalTime.MIN).until(LocalDateTime.now(),ChronoUnit.MILLIS) / 1000.0);
	}

	public int init(LocalDateTime start, LocalDateTime stop, double step)// 1.0
	{
		zeroTime = start.with(LocalTime.MIN);
		double startTime = zeroTime.until(start,ChronoUnit.MILLIS) / 1000.0;
		double stopTime = zeroTime.until(stop,ChronoUnit.MILLIS) / 1000.0;
		init(startTime,stopTime,step);
		return 0;
	}

	public int init(String startStr, String stopStr, double stepStr){
		if (startStr.contains(":") && stopStr.contains(":"))
			return init(
					LocalDateTime.parse(startStr,DATETIME_FORMAT),
					LocalDateTime.parse(stopStr,DATETIME_FORMAT),
					stepStr);
		if ((!startStr.contains(":")) && (!startStr.contains(":")))
			return init(
					Double.parseDouble(startStr),
					Double.parseDouble(stopStr),
					stepStr);
		return -1;
	}

	public double parseTime(String timeStr){
		if (timeStr.contains(":")){
			LocalDateTime time = LocalDateTime.parse(timeStr,DATETIME_FORMAT);
			return secondsUntil(time);
		}
		else {
			return Double.parseDouble(timeStr);
		}
	}

	public double secondsUntil(LocalDateTime toTime){
		return zeroTime.until(toTime, ChronoUnit.MILLIS) / 1000.0;
	}

	public LocalDateTime getCurrentLocalDateTime(){
		return format(currentTime);
	}

	public LocalDateTime format(double time){
		return zeroTime.plus((long)(time*1000), ChronoUnit.MILLIS);
	}

	public LocalDateTime parseFormalTime(String timeStr){
		return format(parseTime(timeStr));
	}

	//wym 重设随start/stoptime改变的时间参数
	public void resetTimer(){
		if (Double.isNaN(startTime)||Double.isNaN(stopTime))
			System.err.println("未设置开始/结束时间");
		simulationTime = stopTime - startTime;
		currentTime = startTime;

	}

	//------------易引起歧义，已废除-------------
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
	//------------易引起歧义，已废除-------------

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
			System.err.println("未设置开始时间");
		return startTime;
	}
	public double getStopTime() {
		if (Double.isNaN(stopTime))
			System.err.println("未设置结束时间");
		return stopTime;
	}
	public double getStepSize() {
		if (Double.isNaN(stepSize))
			System.err.println("未设置仿真步长");
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
