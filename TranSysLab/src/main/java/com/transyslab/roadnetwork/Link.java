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

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Link实体
 *
 */
public class Link extends DefaultWeightedEdge implements NetworkObject{

	protected long id;
	protected String name;
	protected String objInfo;
	protected List<Segment> segments;
	protected int index;
	protected int type;
	protected Node upNode;
	protected Node dnNode;
	protected boolean isSelected;
	protected RoadNetwork network;
	// RN_LinkTime属性

	// protected static int infoPeriodLength; // length of each period (second)
	// protected static int infoTotalLength; // total length
	// protected static int infoPeriods = 1; // number of time periods
	// THESE VARIABLES HELP TO REPRESENT TOPOLOGY AND SPEED UP SOME
	// CALCULATION.

	// rightDnIndex is the position of the right most downstream
	// link in the array dnLinks of dnNode

	protected int rightDnIndex;

	// dnIndex is the position of this link in the array dnLinks of
	// the upNode

	protected int dnIndex;

	// Each bit of this variable represents a connection to dnLink

	protected int dnLegal; // 0=turn is prohibited

	// upIndex is the position of this link in the array upLinks of
	// the dnNode

	protected int upIndex;

	protected int laneUseRules;

	protected double length; // length of the link
	protected double travelTime; // latest travel time
	protected double freeSpeed; // free flow speed

	protected int state;

	// Link travel time

	// protected static int nPeriods;
	// protected static int nSecondsPerPeriod;

	// These functions are used to record the travel time vehicles
	// spend in each link, aggregated by the time they enter the link.

	protected int[] nSamplesTravelTimeEnteringAt;
	protected double[] sumOfTravelTimeEnteringAt;

	//wjw
	protected ArrayList<Path> paths;

	public Link() {
		state = 0;
		segments = new ArrayList<>();
		paths =new ArrayList<>();
	}
	public long getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
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
	public void setType(int t) {
		type = t;
	}
	public int getIndex() {
		return index;
	}
	public void setState(int s) {
		state |= s;
	}
	public void unsetState(int s) {
		state &= ~s;
	}
	public int type() {
		return type;
	}
	public int linkType() {
		return type & Constants.LINK_TYPE_MASK;
	}
	public int isInTunnel() {
		return type & Constants.IN_TUNNEL_MASK;
	}
	public Node getUpNode() {
		return upNode;
	}
	public Node getDnNode() {
		return dnNode;
	}
	public int nSegments() {
		return segments.size();
	}
	public Segment getSegment(int index) {
		return segments.get(index);
	}
	public Segment getStartSegment() {
		return segments.get(0);
	}
	public Segment getEndSegment() {
		return segments.get(segments.size()-1);
	}
	public List<Segment> getSegments(){
		return this.segments;
	}
	public void setSegments(List<Segment> sgmts){
		this.segments = sgmts;
	}
	public void addSegment(Segment seg){ this.segments.add(seg);}
	public void init(long id, int type, String name, int index,Node upNode, Node dnNode){
		this.id = id;
		this.type =type;
		this.name =name;
		this.upNode = upNode;
		this.dnNode = dnNode;
		this.index = index;
		if(upNode == null || dnNode == null)
			System.out.print("Error in init link");
		upNode.addDnLink(this);
		dnNode.addUpLink(this);

	}
	public void setNetwork(RoadNetwork network) {
		this.network = network;
	}

	// 新增方法，用于组织二维数组，方便输出
	public int[] getSumOfFlow() {
		return nSamplesTravelTimeEnteringAt;
	}
	// 新增方法，用于组织二维数组，方便输出

	public double[] getAvgTravelTime(int infoPeriods) {
		int num = sumOfTravelTimeEnteringAt.length;
		double[] avgtraveltime = new double[num];
		for (int i = 0; i < num; i++) {
			avgtraveltime[i] = averageTravelTimeEnteringAt(i,infoPeriods);
		}
		return avgtraveltime;
	}
	public double calcCurrentTravelTime() {
		double tt = 0.0;
		for (int i = 0; i < nSegments(); i++) {
			tt += getSegment(i).calcCurrentTravelTime();
		}
		return tt;
	}
	public double travelTime() {
		return travelTime;
	}
	public double length() {
		return length;
	}
	public double freeSpeed() {
		return freeSpeed;
	}
	public int nUpLinks() {
		return (upNode.nUpLinks());
	}
	public int nDnLinks() {
		return (dnNode.nDnLinks());
	}
	public Link upLink(int i) {
		return (upNode.getUpLink(i));
	}
	public Link dnLink(int i) {
		return (dnNode.getDnLink(i));
	}
	// Generalized travel time used in RN_TravelTime and shortest
	// getPath calculation. It takes into account the freeway biases.
	public double generalizeTravelTime(double x, double freewayBias) {
		if (linkType() != Constants.LINK_TYPE_FREEWAY) {
			x /= freewayBias;
		}
		return x;
	}
	public double getGenTravelTime(double freewayBias) {
		return generalizeTravelTime(travelTime,freewayBias);
	}

	public double inboundAngle() {
		Segment ps = getEndSegment();
		return ps.getEndAngle();

	}
	public double outboundAngle() {
		Segment ps = getStartSegment();
		return ps.getStartAngle();
	}

	public void calcIndicesAtNodes() {
		dnIndex = upNode.whichDnLink(this);
		upIndex = dnNode.whichUpLink(this);
	}
	public void calcStaticInfo() {
		double alpha = inboundAngle() + Math.PI;
		double beta, min_beta;
		int i;
		int narcs = nDnLinks();

		// TOPOLOGY
		// We want angle in range [0, 2PI)

		if (alpha >= 2 * Math.PI)
			alpha -= 2 * Math.PI;

		min_beta = Constants.DBL_INF;
		rightDnIndex = 0xff;

		for (i = 0; i < narcs; i++) {

			beta = dnLink(i).outboundAngle() - alpha;

			// Skip the U turn

			if (beta <= 0.0)
				beta += 2 * Math.PI;

			// Choose the smallest angle, which is the "right down link"

			if (beta < min_beta) {
				min_beta = beta;
				rightDnIndex = (char) i;
			}
		}

		// LENGTH, FREE FLOW TRAVEL TIME AND SPEED

		Segment ps = getEndSegment();
		length = 0.0;
		travelTime = 0.0;
		while (ps != null) {
			ps.setDistance(length);
			length += ps.getLength();
			travelTime += ps.getLength() / ps.getFreeSpeed();
			ps = ps.getUpSegment();
		}
		freeSpeed = length / travelTime;

		// CONNECTIVITY

		// Connectivity to dnLinks

		dnLegal = 0; // set all bits to 0

		Lane plane = getEndSegment().getRightLane();
		while (plane != null) {

			// Check each downstream lane connected to plane

			int n = plane.nDnLanes();
			for (i = 0; i < n; i++) {
				includeTurn(plane.dnLane(i).getLink());
			}
			plane = plane.getLeftLane();
		}

		// LANE USE RULES

		laneUseRules = Constants.VEHICLE_LANE_USE;
		plane = getStartSegment().getRightLane();
		while (laneUseRules != 0 && plane != null) {
			laneUseRules &= (plane.rules() & Constants.VEHICLE_LANE_USE);
			plane = plane.getLeftLane();
		}
	}
	public int signalIndex(Link link) {
		int i;
		if (rightDnIndex != 0xFF && // defined
				link != null) {
			int n = nDnLinks();
			i = (link.getDnIndex() - rightDnIndex + n) % n;
		}
		else {
			i = 0;
		}
		return i;
	}
	public int getDnLegal() {
		return dnLegal;
	}
	public int getDnIndex() {
		return dnIndex;
	}
	public int getRightDnIndex() {
		return rightDnIndex;
	}
	public int getUpIndex() {
		return upIndex;
	}
	public void includeTurn(Link link) {
		dnLegal |= (1 << link.getDnIndex());
	}
	public void excludeTurn(Link link) {
		dnLegal &= ~(1 << link.getDnIndex());
	}
	public int isMovementAllowed(Link other) {
		return (dnLegal & (1 << other.getDnIndex()));
	}
	public int laneUseRules() {
		return laneUseRules;
	}



	public int countNotConnectedDnLinks() {
		Link dnl;
		int cnt = 0;
		for (int i = 0; i < nDnLinks(); i++) {
			dnl = dnLink(i);
			if (!(isMovementAllowed(dnl) > 0)) {
				cnt++;
			}
		}
		return cnt;
	}
	// Find the first link (either a upLink or a dnLink) on the right side
	// of this link at the upstream node of this link. Search inbound
	// link first.
	public Link upRightNeighbor() {
		Link neighbor = null;
		Link link;
		int i, n;
		double alpha, beta;
		double min_beta = Constants.DBL_INF;

		alpha = outboundAngle();

		// For inbound links at the upstream node

		for (i = 0, n = upNode.nUpLinks(); i < n; i++) {
			link = upNode.getUpLink(i);
			beta = link.inboundAngle();
			beta += ((beta < Math.PI) ? (Math.PI) : (-Math.PI));
			beta = alpha - beta;
			if (beta < 0)
				beta += 2 * Math.PI;
			if (beta < Constants.U_ANGLE || beta > Constants.V_ANGLE)
				continue;
			if (beta < min_beta) {
				min_beta = beta;
				neighbor = link;
			}
		}

		// For outbound links at the upstream node

		for (i = 0, n = upNode.nDnLinks(); i < n; i++) {
			link = upNode.getDnLink(i);
			if (link == this)
				continue;
			beta = alpha - link.outboundAngle();
			if (beta < 0.0)
				beta += 2 * Math.PI;
			if (beta < Constants.U_ANGLE || beta > Constants.V_ANGLE)
				continue;
			if (beta + 1.E-6 < min_beta) {

				// This needs to be absolutely smaller considering float
				// number approximate error.

				min_beta = beta;
				neighbor = link;
			}
		}

		return neighbor;
	}
	// Find the first link (either a upLink or a dnLink) on the right side
	// of this link at the downstream node of this link. Searches
	// outbound link first.

	public Link dnRightNeighbor() {
		Link neighbor = null;
		Link link;
		int i, n;
		double alpha, beta;
		double min_beta = Constants.DBL_INF;

		alpha = inboundAngle();
		alpha += ((alpha < Math.PI) ? (Math.PI) : (-Math.PI));

		// For outbound links at the downstream node

		for (i = 0, n = dnNode.nDnLinks(); i < n; i++) {
			link = dnNode.getDnLink(i);
			beta = link.outboundAngle() - alpha;
			if (beta < 0.0)
				beta += 2 * Math.PI;
			if (beta < Constants.U_ANGLE || beta > Constants.V_ANGLE)
				continue;
			if (beta < min_beta) {
				min_beta = beta;
				neighbor = link;
			}
		}

		// For inbound links at the downstream node

		for (i = 0, n = dnNode.nUpLinks(); i < n; i++) {
			link = dnNode.getUpLink(i);
			if (link == this)
				continue;
			beta = link.inboundAngle();
			beta += ((beta < Math.PI) ? (Math.PI) : (-Math.PI));
			beta = beta - alpha;
			if (beta < 0)
				beta += 2 * Math.PI;
			if (beta < Constants.U_ANGLE || beta > Constants.V_ANGLE)
				continue;
			if (beta + 1.E-5 < min_beta) {

				// This needs to be absolutely smaller and considering the
				// float number approximate error.

				min_beta = beta;
				neighbor = link;
			}
		}

		return neighbor;
	}

	public int isMarked() {
		return state & Constants.STATE_MARKED;
	}

	public void unmarkLanes() {
		Segment ps = getStartSegment();
		while (ps != null) {
			Lane pl = ps.getRightLane();
			while (pl != null) {
				pl.unsetState(Constants.STATE_MARKED);
				pl = pl.getLeftLane();
			}
			ps = ps.getDnSegment();
		}
	}
	public void unmarkLanesInUpLinks() {
		int narcs = nUpLinks();
		for (int i = 0; i < narcs; i++) {
			upLink(i).unmarkLanes();
		}
	}
	public void unmarkLanesInDnLinks() {
		int narcs = nDnLinks();
		for (int i = 0; i < narcs; i++) {
			dnLink(i).unmarkLanes();
		}
	}

	public int isCorrect(Vehicle pv) {
		// 未处理，int转boolean
		if (laneUseRules == 0 || (laneUseRules & (pv.types() & Constants.VEHICLE_LANE_USE)) != 0)
			return 1;
		else
			return 0;
	}

	public void resetStatistics(int col, int ncols, int infoPeriods) {
		int num = col + ncols;
		num = Math.min(infoPeriods, num);
		for (int i = col; i < num; i++) {
			nSamplesTravelTimeEnteringAt[i] = 0;
			sumOfTravelTimeEnteringAt[i] = 0;
		}
	}
	// 新增代码，复位统计Link流量和旅行时间的数组，用于参数校准，仿真重启
	public void resetStatistics(int infoPeriods) {
		int num = infoPeriods;
		for (int i = 0; i < num; i++) {
			nSamplesTravelTimeEnteringAt[i] = 0;
			sumOfTravelTimeEnteringAt[i] = 0;
		}
	}
	// Link travel time

	public void initializeStatistics(int infoPeriods) {
		int n = infoPeriods;
		nSamplesTravelTimeEnteringAt = new int[n];
		sumOfTravelTimeEnteringAt = new double[n];
		resetStatistics(0, n, infoPeriods);
	}

	// called when a vehicle leaves the link

	public void recordTravelTime(Vehicle pv,LinkTimes linkTimes) {
		// Tavel time spent in current segment
		double tt = pv.timeInLink(linkTimes.simClock.getCurrentTime());

		// These are for calculating average travel time for the vehicle
		// who ENTER this segment during the reporting time interval.

		// Calculate the ID of the entry time period.

		int i = linkTimes.whichPeriod(pv.timeEntersLink());

		// Time spent in this link
		// idList_.add(pv.get_code());
		sumOfTravelTimeEnteringAt[i] += tt;
		nSamplesTravelTimeEnteringAt[i]++;
	}

	// called for each vehicle in the network, include these in
	// the pretrip queues, at the end of the simulation

	public void recordExpectedTravelTime(Vehicle pv,LinkTimes linkTimes) {
		// Tavel time spent in current link

		double pos;

		if (pv.getSegment() != null) {
			pos = pv.distanceFromDownNode();
		}
		else {
			pos = length;
		}

		// Calculate the ID of the entry time period.

		int i = linkTimes.whichPeriod(pv.timeEntersLink());

		double ht = linkTimes.linkTime(this, pv.timeEntersLink());
		double tt = pv.timeInLink(linkTimes.simClock.getCurrentTime());

		// Section added Joseph Scariza 11/6/01
		if (pos < 0.25 * length) {
			tt += tt * pos / (length - pos);
		}
		// Accumulate vehicle's time in this link

		sumOfTravelTimeEnteringAt[i] += tt;
		nSamplesTravelTimeEnteringAt[i]++;

	}

	// These are valid only after all the vehicles entered the
	// link in interval i have left the link.

	public double averageTravelTimeEnteringAt(double enter, LinkTimes linkTimes) {
		int i = linkTimes.whichPeriod(enter);
		return averageTravelTimeEnteringAt(i,linkTimes.infoPeriods);
	}
	public double averageTravelTimeEnteringAt(int i, int infoPeriods) {
		int num = nSamplesTravelTimeEnteringAt[i];
		if (num > 0) {
			return sumOfTravelTimeEnteringAt[i] / num;// 这玩意儿坑人！：/*/0.3047;*/
		}
		else { // no sample
			int p = i - 1, n = i + 1;
			int m = infoPeriods;

			// Find the first no empty previous time intervals
			while (p >= 0 && nSamplesTravelTimeEnteringAt[p] == 0)
				p--;

			// Find the first no empty next time intervals
			while (n < m && nSamplesTravelTimeEnteringAt[n] == 0)
				n++;

			double tt = 0;
			num = 0;
			if (p >= 0) { // use a previous interval
				tt += sumOfTravelTimeEnteringAt[p];
				num += nSamplesTravelTimeEnteringAt[p];
			}
			if (n < m) { // use a next interval
				tt += sumOfTravelTimeEnteringAt[n];
				num += nSamplesTravelTimeEnteringAt[n];
			}

			if (num > 0) {
				return tt / num;
			}
			else {  // use free flow travel time
				return  (length / freeSpeed);// 同理坑人0.3047 ;
			}
		}
	}
	//
	public int isNeighbor(Link link) {
		if (this.getDnNode().whichDnLink(link) < 0)
			return 0;
		return 1;

	}
	/*
	public void outputToOracle(PreparedStatement ps) throws SQLException {
		// String sql = "insert into MESO_OUTPUT_TEST (LinkID, CTime, Flow,
		// TravelTime) values (?, ?, ?, ?)";
		// Connection connection = JdbcUtils.getConnection();
		// ps = con.prepareStatement(sql);
		int num = nSamplesTravelTimeEnteringAt.length;
		for (int i = 0; i < num; i++) {
			// TODO 待改
			Date date;// = LinkTimes.getInstance().toDate((i + 1));
			ps.setInt(1, this.id);
			ps.setDate(2, new java.sql.Date(date.getTime()));
			ps.setTimestamp(2, new java.sql.Timestamp(date.getTime()));
			ps.setInt(3, nSamplesTravelTimeEnteringAt[i]);
			ps.setDouble(4, averageTravelTimeEnteringAt(i));
			ps.addBatch();
		}
		ps.executeBatch();
	}*/
	public int nPaths() {
		return paths.size();
	}
	public Path pathPointer(int i)
	{
		if (i < 0) {
			  return null;
			} else if (i >= paths.size()) {
			  return null;
			} else {
			  return paths.get(i);
			}
	}

	public RoadNetwork getNetwork() {
		return network;
	}

}
