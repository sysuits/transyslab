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

import com.transyslab.simcore.mlp.MLPNode;
import org.apache.commons.collections.map.HashedMap;

import java.util.*;

public class SignalStage {

	private Map<long[],String> linkPairs2Direction;
	private List<long[]> linkIDPairs;
	private int id;
	private int planId;
	protected SignalPlan plan;
	public SignalTimeSerial timeSerial;

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
	public String getDirection(long[] linkIdPair){
		return this.linkPairs2Direction.get(linkIdPair);
	}
	public List<String> getDirections(){
		return (List)linkPairs2Direction.values();
	}
	public boolean checkDir(long fLinkID, long tLinkID) {
		return linkIDPairs.stream().anyMatch(i -> i[0]==fLinkID && i[1]==tLinkID) &&
				(!plan.isAdaptive||timeSerial.currentState() >= SignalTimeSerial.AMBER);
	}

	public void addLIDPair(long fLID, long tLID, String direction) {
		long[] linkIdPairs = new long[]{fLID,tLID};
		linkIDPairs.add(linkIdPairs);
		linkPairs2Direction.put(linkIdPairs,direction);
	}

	public void deleteLIDPairs(int fLID, int tLID) {
		linkIDPairs.removeIf(p -> p[0]==fLID && p[1]==tLID);
	}

	public int getId(){
		return this.id;
	}

	public List<long[]> getLinkIDPairs () {
		return linkIDPairs;
	}

	public SignalStage initTimeSerial(String numStr){
		this.timeSerial = new SignalTimeSerial(numStr);
		return this;
	}

	public SignalStage initLIDPair(String ftLinkIds, MLPNode node){
		String[] ftLinkStr = ftLinkIds.split("#");
		for (int i = 0; i < ftLinkStr.length; i++) {
			String[] ftlinkId = ftLinkStr[i].split("_");
			long flid = Long.parseLong(ftlinkId[0]);
			long tlid = Long.parseLong(ftlinkId[1]);
			String turnInfo = node.getUpLink(ftLinkStr[i]).getLinkDir() + "_" +  node.findTurningString(flid,tlid);
			addLIDPair(flid, tlid,turnInfo);
		}
		return this;
	}

	public int advance(double step, MLPNode mlpNode){
		boolean vehArrival = false;
		for (int i = 0; i < getLinkIDPairs().size(); i++) {
			long[] pair = getLinkIDPairs().get(i);
			if (mlpNode.vehArrival(pair[0]+"_"+pair[1])){
				vehArrival = true;
				break;
			}
		}
		return timeSerial.advance(step,vehArrival);
	}

	public void reset(){
		timeSerial.reset();
	}

	public float[] currentColor(){
		switch (timeSerial.currentState()){
			case SignalTimeSerial.GREEN:
				return Constants.COLOR_GREEN;
			case SignalTimeSerial.AMBER:
				return Constants.COLOR_AMBER;
			case SignalTimeSerial.RED:
				return Constants.COLOR_RED;
			default:
				return Constants.COLOR_WHITE;
		}
	}
}
