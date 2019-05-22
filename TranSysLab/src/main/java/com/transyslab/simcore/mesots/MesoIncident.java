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
import com.transyslab.roadnetwork.NetworkObject;


public class MesoIncident  {
	private double stime_;
	private double etime_;
	private float capChange_;
	private MesoSegment segment_;
	private boolean needChange_;
	private boolean needResume_;
	// private String filename_;
	// private int id;

	public MesoIncident() {
		stime_ = 86400.0;
		segment_ = null;
		capChange_ = 0.0f;
	}
	// 事件编号：c;发生时间：st;结束时间：et;节段id：sid;减少的通行能力：cap
	/*
	public void init(int c, double st, double et, int sid, float cap) {
		stime_ = st;
		etime_ = et;
		segment_ = (MesoSegment) MesoNetwork.getInstance().findSegment(sid);
		capChange_ = cap;
		setCode(c);
		needChange_ = true;
		needResume_ = true;
	}
	public boolean getNeedChange() {
		return needChange_;
	}
	public boolean getNeedResume() {
		return needResume_;
	}
	public void setNeedChange(boolean b) {
		needChange_ = b;
	}
	public void setNeedResume(boolean b) {
		needResume_ = b;
	}
	public int comp(NetworkObject inc) {
		MesoIncident other = (MesoIncident) inc;
		final double epsilon = 1.0e-5;
		if (stime_ < other.stime_ - epsilon)
			return -1;
		else if (stime_ > other.stime_ + epsilon)
			return 1;
		else
			return 0;
	}

	public int comp(int id) {
		if (id < getCode())
			return -1;
		else if (id > getCode())
			return 1;
		else
			return 0;
	}

	public void updateCapacity() {
		segment_.addCapacity(capChange_);
	}
	public void resumeCapacity() {
		segment_.addCapacity(-capChange_);
	}
	public double getStartTime() {
		return stime_;
	}
	public double getEndTime() {
		return etime_;
	}*/

}