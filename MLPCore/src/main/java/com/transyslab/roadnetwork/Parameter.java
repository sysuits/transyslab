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



public class Parameter {


	// Constants for transfering between I/O and internal
	// units. Internal units are in metric system

	protected static float lengthFactor = 0.3048f; // length and coordinates
													// (1=meter)
	protected static float speedFactor = 0.4470f; // speed (1=km/hr)
	protected static float densityFactor = 0.6214f; // density (1=vehicles/km)
	protected static float flowFactor = 1.0000f; // flow & capacity
													// (1=vehicles/hr)
	protected static float timeFactor = 60.0000f; // travel time (1=minutes)
	protected static float odFactor_ = 1.0000f;

	protected static String densityLabel = "Density(vpm)";
	protected static String speedLabel = "Speed(mph)";
	protected static String flowLabel = "Flow(vph)";
	protected static String occupancyLabel = "Occupancy(%)";

	protected float visibilityScaler;
	protected float visibility;

	protected static int resolution[]; // for view accuracy

	protected float pathAlpha = 0.5f; // parameter for updating travel time

	// For route choice model

	protected static float[][] routingParams = new float[2][2]; // in logic
																	// route
																	// choice
																	// model
	protected float commonalityFactor = 0.0f; // in getPath choice model
	protected float[] diversionPenalty = {300}; // cost added in util func
	protected float validPathFactor = 1.5f; // compared to shorted getPath
	protected float rationalLinkFactor = 0.0f; // reduces irrational link choices
	protected float freewayBias = 1.0f; // travel time
	protected float busToStopVisibility; // distance from bus stop at which bus
											// begins to change upLanes
	protected float busStopSqueezeFactor; // reduction in speed in lane next to
											// bus at a stop

	// Check if the two tokens are the same

	// protected int isEqual(const char *s1, const char *s2);

	public Parameter() {
		routingParams[0][0] = 0.7f;
		routingParams[0][1] = -5.0f;
		routingParams[1][0] = 0.3f;
		routingParams[1][1] = -5.0f;
	}
	/*
	public static Parameter getInstance() {
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return RoadNetworkPool.getInstance().getParameter(threadid);
	}*/


	// Unit transfer

	public static float lengthFactor() {
		return lengthFactor;
	}
	public static float speedFactor() {
		return speedFactor;
	}
	public static float densityFactor() {
		return densityFactor;
	}
	public static float flowFactor() {
		return flowFactor;
	}
	public static float timeFactor() {
		return timeFactor;
	}

	public static String densityLabel() {
		return densityLabel;
	}
	public static String speedLabel() {
		return speedLabel;
	}
	public static String flowLabel() {
		return flowLabel;
	}
	public static String occupancyLabel() {
		return occupancyLabel;
	}

	public static int resolution(int i) {
		return resolution[i];
	}
	// public static int loadResolution(GenericVariable &);

	public float pathAlpha() {
		return pathAlpha;
	}

	// Route choice

	public float guidedRate() {
		return routingParams[1][0];
	}
	public static float routingBeta(int type) {
		return routingParams[type][1];
	}
	public float commonalityFactor() {
		return commonalityFactor;
	}
	public float diversionPenalty() {
		return diversionPenalty[0];
	}
	public float rationalLinkFactor() {
		return rationalLinkFactor;
	}
	public float busToStopVisibility() {
		return busToStopVisibility;
	}
	public float busStopSqueezeFactor() {
		return busStopSqueezeFactor;
	}
	public float pathDiversionPenalty() {
		return diversionPenalty[1];
	}

	public float validPathFactor() {
		return validPathFactor;
	}
	public float freewayBias() {
		return freewayBias;
	}

	public float visibilityScaler() { // virtual
		return visibilityScaler;
	}
	public float visibility() {
		return visibility;
	}

}
