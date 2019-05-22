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


public interface TaskWorker {

	String ANY_WORKER = null;

	double[] worksWith(Task task);

	default void init() {}

	default void onDismiss() {}

	default void goToWork(TaskCenter tc, boolean taskSpecified){
		init();
		while (!tc.dismissAllowed()) {

			//尝试从任务中心taskCenter取回任务
			Task task;
			if (!taskSpecified)
				task = tc.fetchUnspecificTask();
			else
				task = tc.fetchSpecificTask();

			if (task != null) {
//					System.out.println(Thread.currentThread().getName() + " received TID " + (int) task[0]);
				double[] fitness = worksWith(task);
				task.setResults(fitness);
			}
		}
		onDismiss();
	}
}
