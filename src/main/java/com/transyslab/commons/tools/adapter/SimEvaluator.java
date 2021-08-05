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

import com.transyslab.commons.tools.mutitask.TaskGiver;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.List;

public class SimEvaluator implements SolutionListEvaluator{
	private TaskGiver scheduler;

	@Override
	public List evaluate(List solutionList, Problem problem) {
		//异步发送任务
		solutionList.forEach(s -> problem.evaluate(s));
		//任务结果同步
		solutionList.forEach(s -> ((SimSolution) s).getObjectiveValues());
		return solutionList;
	}

	@Override
	public void shutdown() {
		if (this.scheduler!=null)
			this.scheduler.dismissAllWorkingThreads();
	}

	public SimEvaluator setScheduler(TaskGiver scheduler) {
		this.scheduler = scheduler;
		return this;
	}
}
