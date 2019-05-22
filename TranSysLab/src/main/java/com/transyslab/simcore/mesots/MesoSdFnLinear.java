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
// Linear Speed-Density Function
public class MesoSdFnLinear extends MesoSdFn {

	private double kl, vh;
	private double kh, vl;
	private double deltaL, deltaM, deltaH;

	public MesoSdFnLinear() {
		kl = 0.35f;
		vh = 0.76f;
		kh = 0.56f;
		vl = 0.20f;
	}
	public MesoSdFnLinear(double cap, double spd, double kjam, double kl, double vh, double kh, double vl) {
		super(cap, spd, kjam);
		this.kl = kl;
		this.vh = vh;
		this.kh = kh;
		this.vl = vl;
		initialize();
	}

	// void print(ostream &os = cout);
	@Override
	public double densityToSpeed(/*float free_speed, */double density, int nlanes) {
		if (density < 1.0) {
			return freeSpeed;
		}
		else if (density + 1.0 > jamDensity) {
			return jamSpeed * (nlanes - 1);
		}
		else {
			double y = density / jamDensity;
			double v0 = jamSpeed * (nlanes - 1);
			double r;
			if (y < kl) {
				r = 1.0 - deltaL * y;
			}
			else if (y > kh) {
				r = vh - deltaM * (y - kl);
			}
			else {
				r = vl - deltaH * (y - kh);
			}
			return v0 + r * (freeSpeed - v0);
		}
	}
	public void updateParams(double[] params){
		// TODO ´ýÊµÏÖ
	}
	private void initialize() {
		deltaL = (1.0 - vh) / kl;
		deltaM = (vh - vl) / (kh - kl);
		deltaH = vl / (1.0 - kh);
	}
}
