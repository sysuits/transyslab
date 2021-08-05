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

import com.transyslab.roadnetwork.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MacroCharacter {
	//属性挑选
	public static final int SELECT_FLOW = 1;
	public static final int SELECT_SPEED = 2;
	public static final int SELECT_DENSITY = 4;
	public static final int SELECT_TRAVELTIME = 8;
	//所有属性都为宏观统计均值；且未平均车道
	protected double flow; //unit: veh/s/lane
	protected double speed; //unit: m/s
	protected double density; //unit: veh/m/lane
	protected double travelTime; //unit: s

	public MacroCharacter(double flow, double speed, double density, double travelTime) {
		this.flow = flow;
		this.speed = speed;
		this.density = density;
		this.travelTime = travelTime;
	}

	public double getHourFlow() {
		return flow * 3600; //unit: veh/h
	}

	public double getKmSpeed() {
		return speed * 3.6;
	}

	public double getKmDensity() {
		return density * 1000;
	}

	public double getHourTravelTime() {
		return travelTime / 3600.0;
	}

	public static double getHourFlow(double flow) {
		return flow * 3600.0;
	}

	public static double[] getHourFlow(double[] flows) {
		return Arrays.stream(flows).map(e->e*3600.0).toArray();
	}

	public static double getKmSpeed(double speed) {
		return speed * 3.6;
	}

	public static double[] getKmSpeed(double[] speeds) {
		return Arrays.stream(speeds).map(e->e*3.6).toArray();
	}

	public static double getKmDensity(double density) {
		return density * 1000.0;
	}

	public static double[] getKmDensity(double[] densities) {
		return Arrays.stream(densities).map(e->e*1000.0).toArray();
	}

	/**
	 * 将宏观测量量转换为double序列
	 * @param mcList
	 * @return double[]序列
	 */
	public static List<double[]> transfer(List<MacroCharacter> mcList) {
		List<double[]> results = new ArrayList<>();

		double[] flow = mcList.stream().mapToDouble(e -> e.flow).toArray();
		double[] speed = mcList.stream().mapToDouble(e -> e.speed).toArray();
		double[] density = mcList.stream().mapToDouble(e -> e.density).toArray();
		double[] travelTime = mcList.stream().mapToDouble(e -> e.travelTime).toArray();

		results.add(flow);
		results.add(speed);
		results.add(density);
		results.add(travelTime);

		return results;
	}

	public static double[] select(List<MacroCharacter> mcList, int mask) {
		switch (mask) {
			case SELECT_FLOW:
				return mcList.stream().mapToDouble(e -> e.flow).toArray();
			case SELECT_SPEED:
				return mcList.stream().mapToDouble(e -> e.speed).toArray();
			case SELECT_DENSITY:
				return mcList.stream().mapToDouble(e -> e.density).toArray();
			case SELECT_TRAVELTIME:
				return mcList.stream().mapToDouble(e -> e.travelTime).toArray();
			default:
				return null;
		}
	}

	public MacroCharacter copy() {
		return new MacroCharacter(this.flow, this.speed, this.density, this.travelTime);
	}
}
