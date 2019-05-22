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


public interface TaskGiver {
	default Task dispatch(double[] paras, String workerName) {
		Task task = new Task(paras, workerName);
		getTaskCenter().addTask(task);
		return task;
	}

	default Task dispatch(float[] paras, String workerName) {
		double[] paras2 = new double[paras.length];
		for (int i = 0; i < paras2.length; i++) {
			paras2[i] = (double) paras[i];
		}
		Task task = new Task(paras2, workerName);
		getTaskCenter().addTask(task);
		return task;
	}

	default void dispatch(Task task) {
		task.resetResult();
		getTaskCenter().addTask(task);
	}

	default void dismissAllWorkingThreads() {
		getTaskCenter().dismiss();
	}

	default void activateWorker(TaskWorker worker) {

	}

	TaskCenter getTaskCenter();
}
