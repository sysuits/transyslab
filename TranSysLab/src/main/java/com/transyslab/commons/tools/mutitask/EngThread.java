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

import com.google.inject.Inject;
import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.simcore.SimulationEngine;
import org.apache.commons.configuration2.Configuration;

import java.io.File;
import java.util.Arrays;

public class EngThread extends Thread implements TaskWorker{
	protected Configuration config;
	private TaskCenter taskCenter;
	protected SimController controller;
	private boolean logOn;
	private boolean taskSpecified;
	private static TXTUtils writer;
	private boolean broadcastNeeded;

	@Inject
	public EngThread(SimController ctrl){
		taskSpecified = false;//non-specified task by default
		this.controller = ctrl;
	}

	public EngThread(String masterFileName, SimController ctrl){
		this(ctrl);
		config(masterFileName);
	}

	public EngThread(String thread_name, String masterFileName, SimController ctrl) {
		this(masterFileName,ctrl);
		setName(thread_name);
	}

	public EngThread(TaskCenter taskCenter, String thread_name, String masterFileName, SimController ctrl) {
		this(thread_name,masterFileName,ctrl);
		assignTo(taskCenter);
	}

	public EngThread config(String masterFileDir){
		this.controller.config(masterFileDir);
		config = ConfigUtils.createConfig(masterFileDir);
		broadcastNeeded = config.getBoolean("broadcast");
		logOn = Boolean.parseBoolean(config.getString("positionLogOn"));
		if (writer==null && logOn){
			String rootDir = new File(masterFileDir).getParent() + "/";
			String outputPath = rootDir + config.getString("outputPath");
			writer = new TXTUtils(  outputPath + "/" +"solutions.csv");
		}
		return this;
	}

	@Override
	public double[] worksWith(Task task) {
		long t_start = System.currentTimeMillis();

		double[] fitness = controller.simulate(task);

		//Êä³ö½âµÄlog
		if (logOn) {
			double timeUse = System.currentTimeMillis() - t_start;
			if(broadcastNeeded) {
				System.out.println(getName() + "runtimes: " + controller.countRunTimes() + " timer: " + timeUse);
				System.out.println("parameter: " + Arrays.toString(task.getInputVariables()) + "fitness: " + Arrays.toString(fitness));
			}
			writer.writeNFlush(Arrays.toString(task.getInputVariables())
					.replace(" ","")
					.replace("[","")
					.replace("]","") + "," +
					Arrays.toString(fitness).replace(" ","")
							.replace("[","")
							.replace("]","") + "," +
					Thread.currentThread().getName() + "_" + controller.countRunTimes() + "_" + Arrays.toString(task.getInputVariables())
					+ "\r\n");
		}

		return fitness;
	}

	@Override
	public void run() {
		if (taskCenter == null){
			System.err.println("Engine not been assigned.");
			return;
		}
		if (config == null){
			System.err.println("Engine has no conf");
			return;
		}
		if (controller == null){
			System.err.println("Engine not been initiated.");
			return;
		}
		goToWork(taskCenter, taskSpecified);
	}

	@Override
	public void init() {
		controller.init();
	}

	@Override
	public void onDismiss() {
		controller.close();
	}

	public EngThread assignTo(TaskCenter tc) {
		this.taskCenter = tc;
		return this;
	}

	public EngThread assignTo(SimProblem problem) {
		return assignTo(problem.getTaskCenter());
	}

	protected EngThread setTaskSpecified(){
		taskSpecified = true;
		return this;
	}

	protected SimulationEngine getEngine() {
		return controller.getEngine();
	}

}
