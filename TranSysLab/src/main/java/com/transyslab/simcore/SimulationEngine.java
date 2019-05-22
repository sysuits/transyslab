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

package com.transyslab.simcore;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.roadnetwork.*;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.csv.CSVRecord;

import javax.swing.event.EventListenerList;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public abstract class SimulationEngine {

	protected String master_;
	protected int state_;
	protected int mode_;
	protected double[] breakPoints_; // preset break points
	protected int nextBreakPoint_;
	protected double beginTime_; // start time for this run
	protected double endTime_; // end time for this run
	protected EventListenerList listenerList;

	public SimulationEngine() {
		master_ = null;
		state_ = Constants.STATE_NOT_STARTED;
		mode_ = 0;
		breakPoints_ = null;
		nextBreakPoint_ = 0;

		listenerList = new EventListenerList();
	}

	public void setState(int s) {
		state_ = s;
	}
	public int getState() {
		return state_;
	}

	public int getMode() {
		return (mode_);
	}
	public void setMode(int s) {
		mode_ = s;
	}
	public void setMaster(String name) {
		master_ = name;
	}
	public String getMaster() {
		return master_;
	}

	// Returns 0 if no error, negative if fatal error and positive
	// if warning error

	public int canStart() {
		if (master_ == null)
			return 1;
		return 0;
		// 检查master是否为有效路径
	}/*
		 * public int isRunning() { if
		 * (!(SimulationClock.getInstance().isPaused()>0 &&
		 * SimulationClock.getInstance().isStarted()>0)) { return 1; } else {
		 * return 0; } }
		 */


	

	// This call simulationLoop in a loop. You have to overload this
	// function in graphical mode. In batch mode, this function
	// would be good enough. This function will NOT return until the
	// simulation is done.

	public void run() {
		while (simulationLoop() >= 0);
	}
	public abstract int simulationLoop();
	public abstract void loadFiles();
	// One step of the simulation. This function needs to be
	// overloaded in derived class to do the real things. The dummy
	// function just prints the current time in the console window.

	public abstract RoadNetwork getNetwork();

	public abstract int repeatRun();

	public abstract void close();
	public abstract void stop();
	public abstract HashMap<String, List<MacroCharacter>> getEmpMap();

	public abstract HashMap<String, List<MacroCharacter>> getSimMap();
	public abstract int countRunTimes();

	public void addActionLisener(ActionListener listener) {
		listenerList.add(ActionListener.class, listener);
	}

	public void removeActionLisener(ActionListener listener) {
		listenerList.remove(ActionListener.class, listener);
	}

	public void readSignalPlan(String fileName) {
		String[] headers = {"NODEID","PLANID","STAGEID","FLID","TLID","FTIME","TTIME","TURN"};
		try {
			List<CSVRecord> results = CSVUtils.readCSV(fileName,headers);
			Node sNode = null;
			SignalPlan plan = null;
			SignalStage stage = null;
			boolean newDirNeeded = false;
			for (int i = 1; i < results.size(); i++) {
				Long nodeId = Long.parseLong(results.get(i).get("NODEID"));
				int planId = Integer.parseInt(results.get(i).get("PLANID"));
				int stageId = Integer.parseInt(results.get(i).get("STAGEID"));
				int flid = Integer.parseInt(results.get(i).get("FLID"));
				int tlid = Integer.parseInt(results.get(i).get("TLID"));
				LocalTime stime = LocalTime.parse(results.get(i).get("FTIME"),DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss"));
				LocalTime etime = LocalTime.parse(results.get(i).get("TTIME"),DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss"));
				String turnInfo = results.get(i).get("TURN");
				double ft = stime.toSecondOfDay();
				double tt = etime.toSecondOfDay();
				if (sNode == null || sNode.getId() != nodeId) {
					sNode = getNetwork().findNode(nodeId);
					plan = null;
				}
				if (plan == null || plan.getId() != planId) {
					plan = new SignalPlan(planId);
					plan.setFTime(ft);
					sNode.addSignalPlan(plan);
					stage = null;
				}
				if (stage == null || stage.getId() != stageId) {
					plan.setTTime(tt);
					plan.addSignalRow(stageId,ft,tt);
					stage = plan.findStage(stageId);
					if (stage == null) {
						stage = new SignalStage(stageId);
						plan.addStage(stage);
						newDirNeeded  = true;
					}
					else
						newDirNeeded = false;
				}
				if (newDirNeeded) {
					stage.addLIDPair(flid, tlid,turnInfo);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
