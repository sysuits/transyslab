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

package com.transyslab.commons.tools.mutitask;

import java.util.HashMap;
import java.util.Map;


public class Task {
	protected double[] inputVariables;
	protected double[] objectiveValues;
	private Map<Object, Object> attributes;
	private boolean canRetrieve;
	protected String workerName;

	public Task(double[] arg_inputs, String workerName) {
		inputVariables = arg_inputs;
		attributes = new HashMap<>();
		canRetrieve = false;
		this.workerName = workerName;
	}

	public double[] getInputVariables() {
		return inputVariables;
	}

	protected synchronized void setResults(double[] objectiveVals) {
		this.objectiveValues = objectiveVals;
		canRetrieve = true;
//		System.out.println("DEBUG: SimTask finished at " + LocalDateTime.now() + " by " + Thread.currentThread().getName());
		notify();
	}

	public synchronized double[] getObjectiveValues() {
		while (!canRetrieve) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return objectiveValues;
	}

	public synchronized Object getAttribute(Object id) {
		while (!canRetrieve) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return this.attributes.get(id);
	}

	public void resetResult() {
		objectiveValues = new double[objectiveValues.length];//重置为0
		attributes.clear();//消除记录
		canRetrieve = false;
	}

	public boolean isFinished(){
		return canRetrieve;
	}

	public double getInputVariableValue(int index) {
		if (index>=0 && inputVariables.length>index)
			return inputVariables[index];
		else
			return Double.NaN;
	}

	public void setInputVariableValue(int index, double newValue) {
		if (index>=0 && inputVariables.length>index)
			inputVariables[index] = newValue;
		else
			System.err.println("wrong index");
	}

	public void setAttribute(Object k, Object v) {
		attributes.put(k,v);
	}
}
