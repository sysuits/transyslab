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

/**
 */
public class MesoSdFnNonLinear extends MesoSdFn {
	// Non Linear Speed-Density Function
	private double speedBeta; // parameters in nonlinear sd functions
	private double densityBeta;


	public MesoSdFnNonLinear() {
		speedBeta = 5;
		densityBeta = 1.8;
	}
	public MesoSdFnNonLinear(double cap, double spd, double kjam, double alpha, double beta) {
		super(cap, spd, kjam);
		densityBeta = alpha;
		speedBeta = beta;
	}
	public MesoSdFnNonLinear(double alpha, double beta) {
		densityBeta = alpha;
		speedBeta = beta;
	}
	public void updateParams(double[] params){
		if(params.length <6)
			System.out.print("Error: length of params is shorter than 6");
		capacity = params[0];
		jamSpeed = params[1];
		freeSpeed = params[2];
		jamDensity = params[3];
		densityBeta = params[4];
		speedBeta = params[5];
	}
	@Override
	public double densityToSpeed(/*float free_speed,*/ double density, int nlanes) {
		if (density < 1.0) {
			return freeSpeed;
		}
		else if (density + 1.0 > jamDensity) {
			return jamSpeed * (nlanes - 1);
		}
		else {
			double y = density / jamDensity;
			double k =  Math.pow(y, densityBeta);
			double r = Math.pow(1.0 - k, speedBeta);
			double v0 = jamSpeed * (nlanes - 1);
			return v0 + r * (freeSpeed - v0);
		}
	}
}
