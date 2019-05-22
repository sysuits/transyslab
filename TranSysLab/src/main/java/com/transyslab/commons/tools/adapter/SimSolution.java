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

import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.HashMap;


public class SimSolution extends Task implements DoubleSolution {
	protected SimProblem problem;
	protected final JMetalRandom randomGenerator;

	public SimSolution(SimProblem problem, String workerName) {
		super(new double[problem.getNumberOfVariables()], workerName);
		//objectiveValues �ĳ�ʼ��ֻ��Ϊ����JMetal��Ӧ��
		//�ڴ�Ϊ���������
		objectiveValues = new double[problem.getNumberOfObjectives()];
		this.problem = problem;
		randomGenerator = JMetalRandom.getInstance();
		initializeDoubleVariables();
	}

	public SimSolution(SimProblem problem) {
		this(problem, TaskWorker.ANY_WORKER);
	}

	public SimSolution(SimSolution solution) {
		this(solution.problem, solution.workerName);

		for (int i = 0; i < problem.getNumberOfVariables(); i++) {
			setVariableValue(i, solution.getVariableValue(i));
		}

		//objectiveValues �ĳ�ʼ��ֻ��Ϊ����JMetal��Ӧ��
		//�ڴ�Ϊ���������

		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			objectiveValues[i] = solution.getObjective(i);
		}
	}

	/**
	 * ��ֹ�������������objective�����ã�
	 * ֻ��TaskWorker���ܽ���Ŀ�꺯��������.
	 * ������Ϊ�պ�����
	 */
	@Override
	public void setObjective(int index, double value) {

	}

	/**
	 * ��ȡTask�첽�����������ܻᴥ��������
	 * @param index ����ֵ������
	 * @return ����ֵ
	 */
	@Override
	public double getObjective(int index) {
		return getObjectiveValues()[index];
	}

	@Override
	public Double getVariableValue(int index) {
		return getInputVariableValue(index);
	}

	@Override
	public void setVariableValue(int index, Double value) {
		setInputVariableValue(index, value);
	}

	@Override
	public String getVariableValueString(int index) {
		return getVariableValue(index).toString();
	}

	@Override
	public int getNumberOfVariables() {
		return inputVariables.length;
	}

	@Override
	public int getNumberOfObjectives() {
		return objectiveValues.length;
	}

	@Override
	public Solution copy() {
		return new SimSolution(this);
	}

	@Override
	public Double getLowerBound(int index) {
		return problem.getLowerBound(index);
	}

	@Override
	public Double getUpperBound(int index) {
		return problem.getUpperBound(index);
	}

	private void initializeDoubleVariables() {
		for (int i = 0 ; i < problem.getNumberOfVariables(); i++) {
			Double value = randomGenerator.nextDouble(getLowerBound(i), getUpperBound(i)) ;
			setVariableValue(i, value) ;
		}
	}
}
