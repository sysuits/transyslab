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

import java.util.*;

import org.jgrapht.GraphPath;

/**
 */

public class Path implements NetworkObject {

	public static final int MAX_PATH_LENGTH = 20;

	protected long id;
	protected String objInfo;
	protected int index; // index in the array;
	// สตภýปฏ
	protected List<Link> links; // list of links of the
														// getPath
	protected Node oriNode; // origin node
	protected Node desNode; // destination node

	// Total travel time from origin to destination. This value is
	// read from getPath table and not changed during the simulation.

	protected float pathTravelTime;
	protected boolean isSelected;

	private float cf;				// commonality factor
	
	public Path() {
		links = new ArrayList<>();
	}
	public long getId(){
		return this.id;
	}
	public String getObjInfo(){
		return this.objInfo;
	}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public Node getOriNode() {
		return oriNode;
	}
	public Node getDesNode() {
		return desNode;
	}

	public int nLinks() {
		return links.size();
	}
	public Link getLink(int i) {
		if (i < 0)
			return null;
		else if (i >= nLinks())
			return null;
		else
			return links.get(i);
	}
	public Link getNextLink(int i) {
		if (i < nLinks() - 1)
			return links.get(i + 1);
		else
			return null;
	}
	public Link getFirstLink() {
		if (nLinks() > 0)
			return links.get(0);
		else
			return null;
	}
	public Link getLastLink() {
		if (nLinks() > 0)
			return links.get(nLinks() - 1);
		else
			return null;
	}
	public List<Link> getLinks(){
		return this.links;
	}
	public int index() {
		return index;
	}

	public float getPathTravelTime() {
		return pathTravelTime;
	}
	public void setPathTravelTime(float s) {
		pathTravelTime = s;
	}

	// Travel time from the kth link to the destination if entering
	// at the calling time (currentTiem of the SimulationClock)

	// Calculate travel times from kth link on the getPath to the destination
	// based on the given information, assuming enter the kth link at the
	// current time.

	public double travelTime(LinkTimes info, int kth, double currentTime) {
		double t =  currentTime;
		double x = 0.0;
		double y = 0.0;
		int i = kth, n = nLinks();
		while (i < n) {
			int k = links.get(i).getIndex(); // index of the link
			x += info.linkTime(k, t); // add the travel time on link k
			y = info.linkTime(k, t);

			t += y;
			i++;
		}
		return x;
	}
	// Calculate travel times from kth link on the getPath to the destination
	// based on the given information, assuming enter the kth link at time
	// t.
	public double travelTime(LinkTimes info, int kth, float t) {
		double x = 0.0f;
		int i = kth, n = nLinks();
		while (i < n) {
			int k = links.get(i).getIndex(); // index of the link
			x += info.linkTime(k, t); // add the travel time on link k
			t += x; // entry time for next link
			i++;
		}
		return x;
	}

	// These two are called by RN_PathParser
	// Called by RN_PathParser to initialize a getPath
	/*
	public int init(int id, int oriId, int desId) {

		this.id = id;

		if ((oriNode = RoadNetwork.getInstance().findNode(ori)) == null) {
			// cerr << "Error:: Unknown origin node <" << ori << ">. ";
			return (-1);
		}
		else if ((desNode = RoadNetwork.getInstance().findNode(des)) == null) {
			// cerr << "Error:: Unknown destination node <" << des << ">. ";
			return (-1);
		}
		index = idx++;

		return 0;
	}*/
	
	public double cost(LinkTimes info, double currentTime)
	{
	   return travelTime(info, 0,currentTime);
	}
	
	public boolean IsUsedBy(ODPair od)
	{
		if (od==null) return false ;
		for (int i = 0; i < od.nPaths(); i ++) {
			if (this == od.getPath(i)) return true ;
		}
		return false ;
	}
	
	public double cf() {
		return cf;
	}
	
	public void cf(float arg) {
		cf = arg;
	}
	
	//wym
	public Path(GraphPath<Node, Link> GPath) {
		links = new Vector<>();
		List<Link> gp_links = GPath.getEdgeList();
		for (Link LN : gp_links) {
			links.add(LN);
		}
		oriNode = GPath.getStartVertex();
		desNode = GPath.getEndVertex();
	}

	public Path(Node o, Node d, List<Link> links){
		this.oriNode = o;
		this.desNode = d;
		this.links = links;
	}

}
