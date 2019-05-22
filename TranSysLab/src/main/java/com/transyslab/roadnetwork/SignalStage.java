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

import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalStage {

	private Map<int[],String> linkPairs2Direction;
	private List<int[]> linkIDPairs;
	private int id;
	private int planId;

//	protected double cycle;

	public SignalStage(int id) {
		this.id = id;
		this.linkIDPairs = new ArrayList<>();
		this.linkPairs2Direction = new HashedMap();
	}
	public void setPlanId(int planId){
		this.planId = planId;
	}
	public int getPlanId(){
		return this.planId;
	}
	public String getDirection(int[] linkIdPair){
		return this.linkPairs2Direction.get(linkIdPair);
	}
	public List<String> getDirections(){
		return (List)linkPairs2Direction.values();
	}
	public boolean checkDir(long fLinkID, long tLinkID) {
		return linkIDPairs.stream().anyMatch(i -> i[0]==fLinkID && i[1]==tLinkID);
	}

	public void addLIDPair(int fLID, int tLID, String direction) {
		int[] linkIdPairs = new int[]{fLID,tLID};
		linkIDPairs.add(linkIdPairs);
		linkPairs2Direction.put(linkIdPairs,direction);
	}

	public void deleteLIDPairs(int fLID, int tLID) {
		linkIDPairs.removeIf(p -> p[0]==fLID && p[1]==tLID);
	}

	public int getId(){
		return this.id;
	}

	public List<int[]> getLinkIDPairs () {
		return linkIDPairs;
	}
}
