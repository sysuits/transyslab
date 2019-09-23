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

package com.transyslab.commons.tools.adapter;

import com.transyslab.commons.tools.adapter.SimProblem;

import java.util.ArrayList;
import java.util.List;


public abstract class BoundarySimProblem extends SimProblem {
	public BoundarySimProblem(){ }

	@Override
	public void initProblem(String masterFileName) {
		super.initProblem(masterFileName);
		setProblemBoundary();
	}

	public void setProblemBoundary() {
		String[] lowerStrArray = config.getString("lower").split(",");
		String[] upperStrArray = config.getString("upper").split(",");

		if (lowerStrArray.length<=0 || lowerStrArray.length!=upperStrArray.length)
			System.err.println("Boundary input error!");

		//���������ģ
		setNumberOfVariables(lowerStrArray.length);
		setNumberOfObjectives(1);
		setNumberOfConstraints(0);

		//���ñ߽�ֵ
		List<Double> lower = new ArrayList<>();
		for (int i = 0; i<lowerStrArray.length; i++) {
			lower.add(Double.parseDouble(lowerStrArray[i]));
		}
		List<Double> upper = new ArrayList<>();
		for (int i = 0; i<upperStrArray.length; i++) {
			upper.add(Double.parseDouble(upperStrArray[i]));
		}
		setLowerLimit(lower);
		setUpperLimit(upper);
	}

}