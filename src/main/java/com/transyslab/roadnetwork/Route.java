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
import java.util.List;

import com.transyslab.commons.tools.SimulationClock;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;

/**
 * 路径
 *
 *
 */
public class Route extends LinkTimes {
	
	protected int state; // 0=new 1=old
	protected int matrixSize;
	protected List<Double> routeTimes;// times from each link to des nodes
	protected AllDirectedPaths<Node, Link> linkGraph;

	public Route(SimulationClock simClock) {
		super(simClock);
		routeTimes = new ArrayList<>();
	}


	public void initialize(RoadNetwork theNetwork) {
		linkGraph = new AllDirectedPaths<>(theNetwork);
		matrixSize = theNetwork.links.size() * theNetwork.nDestNodes;
	}

	public int pathPeriods() {
		return 1;
	}
	
	public int matrixSize() {
		return matrixSize;
	}
	
	// Return travel time on the shorted getPath from upstream end of link
	// "olink" to destination node "dnode".
	public double upRouteTime(Link olink, Node dnode, double timesec, int nDestNodes) {
		int d = dnode.destIndex;
		//只是判断dnode是否为终点 没有判断olink是否可以到达dnode
		if (d >= 0 && d < nDestNodes) {
			return upRouteTime(olink.getIndex(), d, timesec,nDestNodes);
		} else {
			return Double.POSITIVE_INFINITY;
		}
	}

	public double upRouteTime(int olink, int dnode, double timesec,int nDestNodes) {
		return routeTimes.get(olink * nDestNodes + dnode);
	}

	// Travel time from dnstream end of olink to dnode
	// Return travel time on the shorted getPath from dnstream end of link
	// "olink" to node "dnode".
	public double dnRouteTime(Link olink, Node dnode, double timesec, int nDestNodes ){
		int d = dnode.getDestIndex();
		if( d >= 0 && d < nDestNodes) {
			return dnRouteTime(olink.getIndex(), d, timesec, nDestNodes);
		}else {
			return Double.POSITIVE_INFINITY;
		}
	}

	public double dnRouteTime(int olink, int dnode, double timesec ,int nDestNodes) {
		double total = upRouteTime(olink, dnode, 0.0,nDestNodes);
		double dt = avgLinkTime(olink);
		return total - dt;
	}
	
	public AllDirectedPaths<Node, Link> getLinkGraph() {
		return linkGraph;
	}
	
	public void findShortestPathTrees(double ftime, double ttime) {
		
	}
	
	public void calcShortestPathTree(int i) {
		
	}
}
