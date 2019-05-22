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

package com.transyslab.simcore.rts;

import com.transyslab.roadnetwork.Link;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;


public class RTLink extends Link {
	private SimpleDirectedWeightedGraph<RTLane,DefaultWeightedEdge> laneGraph;
	public RTLink(){
		laneGraph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
	}
	public void checkConnectivity(){

	}
	protected void addLaneGraphVertex(RTLane rtLane) {
		laneGraph.addVertex(rtLane);
	}

	protected void addLaneGraphEdge(RTLane fLane, RTLane tLane, double weight) {
		DefaultWeightedEdge edge = new DefaultWeightedEdge();
		laneGraph.addEdge(fLane, tLane, edge);
		laneGraph.setEdgeWeight(edge,weight);
	}
	/*
	public void addLnPosInfo() {
		RTSegment theSeg = (RTSegment) getEndSegment();
		int JLNUM = 1;
		while (theSeg != null){
			for (int i = 0; i<theSeg.nLanes(); i++){
				RTLane ln = theSeg.getLane(i);
				JointLane tmpJLn = findJointLane(ln);
				if (tmpJLn == null) {
					JointLane newJLn = new JointLane(JLNUM);
					JLNUM += 1;
					newJLn.lanesCompose.add(ln);
					jointLanes.add(newJLn);
				}
				else {
					tmpJLn.lanesCompose.add(ln);
				}
			}
			theSeg = (MLPSegment) theSeg.getUpSegment();
		}
	}*/
}
