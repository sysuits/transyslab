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

package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignalPlan {
	protected int id;
	private List<SignalStage> stages;
	private List<double[]> signalTable;
	private double fTime;
	private double tTime;
	private double amberTime;

	public SignalPlan(int id) {
		stages = new ArrayList<>();
		signalTable = new ArrayList<>();
		this.id = id;
		this.amberTime = 3.0;
	}

	public int getId(){
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void addStage(SignalStage stage) {
		stage.setPlanId(this.id);
		stages.add(stage);
	}
	public List<double[]> getSignalTable(){
		return this.signalTable;
	}
	public List<SignalStage> getStages(){
		return stages;
	}
	public void setFTime(double ft) {
		this.fTime = ft;
	}

	public void setTTime(double tt) {
		this.tTime = tt;
	}
	public double getFTime(){
		return this.fTime;
	}
	public double getTime(){
		return this.tTime;
	}
	public boolean check(double t, long fLID, long tLID) {
		SignalStage stage = findStage(t);
		return (stage!=null && stage.checkDir(fLID,tLID));
	}

	public boolean beingApplied(double now) {
		return (fTime <= now && now < tTime);
	}

	public SignalStage findStage(int stageId) {
		return stages.stream().filter(s->s.getId()==stageId).findFirst().orElse(null);
	}

	private int findSID(double time) {
		double[] tmp = signalTable.stream().filter(r -> r[1]<=time && r[2]>time).findFirst().orElse(null);
		return (tmp==null ? -1 : (int)tmp[0]);
	}

	public SignalStage findStage(double time) {
		return findStage(findSID(time));
	}

	public double[] getStageTimeTable(double time) {
		double[] tmp = signalTable.stream().filter(r -> r[1]<=time && r[2]>time).findFirst().orElse(null);
		return tmp;
	}

	public boolean isAmber(double time) {
		double[] tmp = signalTable.stream().filter(r -> r[1]<=time && r[2]>time).findFirst().orElse(null);
		return (tmp!=null && tmp[2]-time<=amberTime);
	}

	public void addStage(int stageId) {
		stages.add(new SignalStage(stageId));
	}

	public void deleteStage(int stageId) {
		stages.removeIf(s -> s.getId() == stageId);
	}

	public void addSignalRow(int sid, double ft, double tt) {
		signalTable.add(new double[] {sid, ft, tt});
	}

	public double getAmberTime() {
		return amberTime;
	}

	public void setAmberTime(double amberTime) {
		this.amberTime = amberTime;
	}

}
