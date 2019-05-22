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

package com.transyslab.simcore.mlp;

public class Inflow {
	public double time;
	public double speed; // unit m/s
	public int laneIdx; 
	public long tLinkID;
	public double dis;
	public int realVID;
	
	public Inflow(double[] row, MLPNetwork mlpNetwork){
		if (row.length != 5) {
			System.err.println("length not match");
			return;
		}
		time = row[0];
		speed = row[1];
		int laneID = (int) row[2];
		laneIdx = mlpNetwork.findLane(laneID).getIndex();
		tLinkID = (int) row[3];
		if ( row[4] < 0.0)
			dis = mlpNetwork.mlpLane(laneIdx).getLength();
		else
			dis = row[4];
	}
	
	public Inflow(double time, double speed, int laneIdx, long tLinkID, double dis){
		this.time = time;
		this.speed = speed;
		this.laneIdx = laneIdx;
		this.tLinkID = tLinkID;
		this.dis = dis;
	}

	public Inflow(double time, double speed, int laneIdx, long tLinkID, double dis, int realVID){
		this.time = time;
		this.speed = speed;
		this.laneIdx = laneIdx;
		this.tLinkID = tLinkID;
		this.dis = dis;
		this.realVID = realVID;
	}

}
