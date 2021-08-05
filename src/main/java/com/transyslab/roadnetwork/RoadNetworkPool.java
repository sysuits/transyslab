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

import java.util.HashMap;
import java.util.Vector;

import com.transyslab.simcore.mesots.MesoRandom;

public abstract class RoadNetworkPool {

	protected PathTable[] pathTableArray_;
	protected LinkTimes[] linkTimesArray_;
	protected Vector<Vector<MesoRandom>> randomArray_;
	protected HashMap<String, Integer> threadMap_;

	protected int[] threadIndex_;
	protected int threadNum_;

	private static RoadNetworkPool theRNPool_;
	public static RoadNetworkPool getInstance() {
		return theRNPool_;
	}
	public void setInstance(RoadNetworkPool pool) {
		theRNPool_ = pool;
	}
	
	protected RoadNetworkPool() {
		
	}
	public abstract void init(int n, RoadNetworkPool pool);

	public abstract void initArrays();
/*
	public abstract RoadNetwork getNetwork(int i);
	public abstract Parameter getParameter(int i);
	public abstract SimulationClock getSimulationClock(int i);
	public abstract VehicleTable getVhcTable(int i);
	public HashMap<String, Integer> getHashMap() {
		return threadMap_;
	}
	public PathTable getPathTable(int i) {
		return pathTableArray_[i];
	}
	public LinkTimes getLinkTimes(int i) {
		return linkTimesArray_[i];
	}
	public Vector<MesoRandom> getRandom(int i) {
		return randomArray_.get(i);
	}
	public void organizeHM(Thread[] tl) {
		if (threadMap_ == null)
			threadMap_ = new HashMap<String, Integer>();
		threadMap_.clear();
		for (int i = 0; i < threadNum_; i++) {
			threadMap_.put(tl[i].getName(), threadIndex_[i]);
		}
	}*/
}
