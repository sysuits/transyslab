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


public abstract class MesoSdFn {
	
	//路段末交通流最短车头时距（最大通行能力）
	protected double capacity;
	protected double jamSpeed;
	protected double jamDensity;
	protected double freeSpeed;

	public double getJamDensity() {
		return jamDensity;
	}
	public double getCapacity() {
		return capacity;
	}
	public double getFreeSpeed(){
		return freeSpeed;
	}
	public void setFreeSpeed(float speed){
		freeSpeed = speed;
	}
	public void setCapacity(float cap){
		capacity = cap;
	}
	public void setJamSpeed(float speed){
		jamSpeed = speed;
	}
	/*
	public void updateParameters(float cap, float minspeed, float maxspeed, float maxdensity){
		capacity = cap;
		jamSpeed = minspeed;
		freeSpeed  = maxspeed;
		jamDensity = maxdensity;
	}*/
	//注释传参freeSpeed
	public double densityToSpeed(/*float freeSpeed, */double density, int nlanes) {
		return freeSpeed;
	}

	public MesoSdFn() {
		capacity = 0.5f; // 1800 vph in vps
		jamSpeed = 0; // 5 mph in mps
		jamDensity = 180.0f; // in vehicles/km
		freeSpeed = 16.67f;// 60km/h = 16.67m/s
	}
	public MesoSdFn(double cap, double spd, double kjam) {
		// capacity = (float) (cap / 3600.0);
		capacity = cap;
		jamSpeed = spd;// * Parameter.speedFactor();
		jamDensity = kjam;// * Parameter.densityFactor();
	}
	public abstract void updateParams(double[] params);

}
