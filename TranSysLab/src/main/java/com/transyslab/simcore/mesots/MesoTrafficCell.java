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

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;


public class MesoTrafficCell {

	protected MesoSegment segment; // container

	// Data members calculated in update phase

	protected double tailSpeed; // upstream speed
	protected double tailPosition; // upstream position

	protected int nHeads; // number of heads
	protected double[] headSpeeds; // downstream speeds
	protected double[] headPositions; // downstream positions

	// Data members dynamically changed in advance phase

	protected int nVehicles; // number of vehicles

	protected MesoVehicle firstVehicle; // first vehicle (dn) in this TC
	protected MesoVehicle lastVehicle; // last vehicle (up) in this TC

	// Bookkeeping data members. May change in both update and
	// advance phases

	protected MesoTrafficCell trailing; // upstream traffic cell
	protected MesoTrafficCell leading; // downstream traffic cell

	protected double queueTime; // time was a queue

//	protected double simStepSize; // 全局变量


	public MesoTrafficCell() {
		nVehicles = 0;
		nHeads = 0;
	}



	public MesoTrafficCell trailing() {
		return trailing;
	}
	public MesoTrafficCell leading() {
		return leading;
	}
	// 创建对象后调用
	public void initialize(double simStepSize) {
		queueTime = -Constants.FLT_INF;

		firstVehicle = lastVehicle = null;
		nVehicles = 0;

		nHeads = link().nDnLinks();

		if (segment.isHead() == 0 || // not in the last segment in the link
				nHeads == 0) { // dead end or boundary link
			nHeads = 1;
		}

		headSpeeds = new double[nHeads];
		headPositions = new double[nHeads];

		tailSpeed = segment.maxSpeed();

		tailPosition =  segment.getLength() - tailSpeed * simStepSize;
		for (int i = 0; i < nHeads; i++) {
			headSpeeds[i] = tailSpeed;
			headPositions[i] = tailPosition;
		}
	}

	public MesoLink mesoLink() {
		return link();
	}

	public MesoVehicle firstVehicle() {
		return firstVehicle;
	}
	public MesoVehicle lastVehicle() {
		return lastVehicle;
	}
	public int nVehicles() {
		return nVehicles;
	}

	// Insert a vehicle into the cell based on its getDistance position
	/*
	 * public void insert(MESO_Vehicle pv) { // Find the front vehicle
	 *
	 * MESO_Vehicle front = lastVehicle; while (front!=null && front.getDistance()
	 * > pv.getDistance()) { front = front.leading; }
	 *
	 * // Insert after the front vehicle
	 *
	 * if (front!=null) { // pv is NOT the first in cell pv.trailing =
	 * front.trailing; front.trailing = pv; } else { // pv is the first in
	 * cell pv.trailing = firstVehicle; firstVehicle = pv; } if
	 * (pv.trailing!=null) { pv.trailing.leading = pv; } else { lastVehicle
	 * = pv; }
	 *
	 * pv.trafficCell = this; pv.calcSpaceInSegment();
	 *
	 * nVehicles ++; }
	 */


	 public void appendSnapshot(MesoVehicle vehicle,SimulationClock clock, MesoParameter parameter){
		vehicle.leading = lastVehicle;
		vehicle.trailing = null;
		
		if (lastVehicle !=null) {		// append at end
		      lastVehicle.trailing = vehicle;
		} else {			// queue is empty
		      firstVehicle = vehicle;
		}
		lastVehicle = vehicle;
		nVehicles++;
		
		vehicle.appendSnapshotTo(this);
		
		if (nVehicles <= 1) {		// first vehicle
			updateTailSpeed(clock,parameter);
			updateHeadSpeeds(clock,parameter);
		}
	}
	// Split the traffic cell into two cells at the first gap
	// smaller than the given threshold
	/*
	 * public void split() { MESO_Vehicle front = firstVehicle; MESO_Vehicle pv
	 * = null; while (front!=null && (pv = front.trailing)!=null &&
	 * pv.getDistance() <= front.upPos() +
	 * MESO_Parameter.getInstance().cellSplitGap()) { front = pv; } if
	 * (pv==null) return; // not need to split
	 *
	 * // SPLIT THE CELL BETWEEN front AND pv
	 *
	 * // Create a new cell and put it after the current cell
	 *
	 * MESO_TrafficCell cell = MESO_CellList.getInstance().recycle();
	 *
	 * // We do not call initialize() as usual, but we copy the variable // from
	 * this to the new cell
	 *
	 * cell.leading = this; cell.trailing = trailing; if (trailing!=null) {
	 * // this is not the last in segment trailing.leading = cell; } else { //
	 * this is the last in segment segment.lastCell = cell; } trailing =
	 * cell; segment.nCells ++;
	 *
	 * // Copy variables from this cell to the new cell that follows
	 *
	 * cell.segment = segment; cell.tailSpeed = tailSpeed;
	 * cell.tailPosition = tailPosition; cell.nHeads = nHeads;
	 * cell.headSpeeds = new float [nHeads]; cell.headPositions = new float
	 * [nHeads]; for (int i = 0; i < nHeads; i ++) { cell.headSpeeds[i] =
	 * headSpeeds[i]; cell.headPositions[i] = headPositions[i]; }
	 *
	 * // Cut the vehicle list into two
	 *
	 * pv.leading = null; // first in the new cell front.trailing = null; //
	 * last in this cell cell.firstVehicle = pv; // first in the new cell
	 * cell.lastVehicle = lastVehicle; // last in the new cell lastVehicle =
	 * front; // last in this cell
	 *
	 * // Change container for the vehicles in the new cell
	 *
	 * cell.nVehicles = 0; while (pv!=null) { pv.trafficCell = cell; pv =
	 * pv.trailing; cell.nVehicles ++; }
	 *
	 * // Update vehicle counter and length
	 *
	 * nVehicles -= cell.nVehicles; }
	 */

	public void remove(MesoVehicle vehicle) {
		if (vehicle.leading != null) { // not the first one
			vehicle.leading.trailing = vehicle.trailing;
		}
		else { // first one
			firstVehicle = vehicle.trailing;
		}
		if (vehicle.trailing != null) { // not the last one
			vehicle.trailing.leading = vehicle.leading;
		}
		else { // last one
			lastVehicle = vehicle.leading;
		}
		nVehicles--;
	}

	// Append the following cell to the end of this cell, the
	// following cell becomes empty but is NOT removed.
	/*
	 * public void append(MESO_TrafficCell cell) { MESO_Vehicle vehicle =
	 * cell.firstVehicle;
	 *
	 * if (vehicle==null) { return; }
	 *
	 * // Change container for the vehicles in cell
	 *
	 * while (vehicle!=null) { vehicle.trafficCell = this; vehicle =
	 * vehicle.trailing; }
	 *
	 * // Connect the two cells
	 *
	 * if (lastVehicle!=null) { // this cell is not empty
	 * lastVehicle.trailing = cell.firstVehicle; } else { // this cell is
	 * empty firstVehicle = cell.firstVehicle; } cell.firstVehicle.leading =
	 * lastVehicle; lastVehicle = cell.lastVehicle;
	 *
	 * // Update vehicle counter
	 *
	 * nVehicles += cell.nVehicles;
	 *
	 * // Update the pointers and vehicle counts in cell
	 *
	 * cell.nVehicles = 0; cell.firstVehicle = cell.lastVehicle = null; }
	 */

	public MesoLink link() {
		return ((segment != null) ? (MesoLink) segment.getLink() : (MesoLink) null);
	}
	public MesoSegment segment() {
		return segment;
	}

	// These are the dn position of the first and up position of the
	// last vehicles in the cell at the moment this function is
	// called.

	public double dnDistance(double simStepSize) {
		if (firstVehicle != null) {
			return firstVehicle.getDistance();
		}
		else {
			return maxReachablePosition(simStepSize);
		}
	}
	public double upDistance(double simStepSize) {
		if (lastVehicle != null) {
			return lastVehicle.upPos();
		}
		else {
			return maxReachablePosition(simStepSize);
		}
	}
	public double maxReachablePosition(double simStepSize) {
		double dx = segment.maxSpeed() * simStepSize;
		dx = segment.getLength() - dx;
		if (leading != null) {
			double pos = leading.upDistance(simStepSize);
			dx = (dx > pos) ? dx : pos;
		}
		return (dx > 0.0f ? dx : 0.0f);
	}

	public double length(double simStepSize) {
		if (firstVehicle != null) {
			return upDistance(simStepSize) - firstVehicle.getDistance();
		}
		else {
			return 0.0f;
		}
	}

	// These are the dn position of the first and the up position of
	// the last vehicles in the cell when this traffic cell is
	// updated. These values are changed at the update phase.

	public double tailPosition() {
		return tailPosition;
	}

	public double tailSpeed() {
		return tailSpeed;
	}

	public double headPosition(int i) {
		return (i < nHeads) ? headPositions[i] : headPositions[0];
	}

	public double headSpeed(int i) {
		return (i < nHeads) ? headSpeeds[i] : headSpeeds[0];
	}
	/*
	 * public float headSpeed() // average of the headSpeeds { if (nHeads < 2)
	 * { // single head return headSpeeds[0]; } else { // multiple heads float
	 * sum = 0.0f; for (int i = 0; i < nHeads; i ++) { sum += headSpeeds[i]; }
	 * return sum / (float)nHeads; } }
	 *
	 * public float speed() // average speed of the heads and tail { return 0.5f
	 * * (tailSpeed + headSpeed()); }
	 */

	// Set the speed of each head vehicle to the same speed and record the
	// reference position
	public void setHeadSpeeds(double spd, double pos, double currentTime, MesoParameter parameter) {
		for (int i = 0; i < nHeads; i++) {
			headPositions[i] = pos;
			if (headSpeeds[i] > 0.1) { // This stream was moving
				double maxspd = parameter.queueReleasingSpeed(timeSinceDispatching(currentTime),
						segment.maxSpeed());
				if (spd > maxspd) {
					headSpeeds[i] = maxspd;
				}
				else {
					headSpeeds[i] = spd;
				}
			}
			else { // stopped
				headSpeeds[i] = spd;
				queueTime = currentTime;
			}
		}
	}

	// These two are based on current state and referenced to the
	// last vehicle in the cell

	public double calcDensity(double simStepSize, double rspLower) {
		double len = length(simStepSize) * segment.nLanes();
		if (len >rspLower) {
			return 1000.0f * nVehicles / len;
		}
		else {
			return 0.0f;
		}
	}

	// Calculate the speed of the last vehicle
	public double calcSpeed(double simStepSize,MesoParameter params) {
		double len = length(simStepSize);
		if (len > params.cellSplitGap) {
			return calcSpeed(1000.0 * nVehicles / (len * segment.nLanes()));
		}
		else if (len < params.rspLower) {
			// for very short cell consider the leading cell also
			if (leading != null) {
				len += distance(leading,simStepSize) + leading.length(simStepSize);
				int num = nVehicles + leading.nVehicles();
				return calcSpeed((float) (1000.0 * num / (len * segment.nLanes())));
			}
			else {
				return segment.maxSpeed();
			}
		}
		else {
			// speed-density function will not work for short cells
			return params.maxSpeed(len * segment.nLanes() / nVehicles, segment.nLanes());
		}
	}

	// This is the current speed of last vehicle in the cell
	public double upSpeed() {
		if (lastVehicle != null) {
			return lastVehicle.getCurrentSpeed();
		}
		else {
			return segment.maxSpeed();
		}
	}

	// Speed for a given density

	public double calcSpeed(double density) {
		MesoSdFn sdf = segment.sdFunction;
		return sdf.densityToSpeed(density, segment.nLanes());
	}

	// These calculations are based on states at the beginning of
	// current update interval

	public void updateTailSpeed(SimulationClock simClock,MesoParameter parameter) {
		double simStep = simClock.getStepSize();
		tailPosition = upDistance(simStep);
		tailSpeed = calcSpeed(simStep,parameter);
	}

	// Calculates the downstream speeds based on the space from and speed
	// of the downstream traffic cells in ealier. This function is called
	// at least once in every update phase and when cells are created or
	// combined
	public void updateHeadSpeeds(SimulationClock simClock,MesoParameter parameter) {
		int i;
		double curTime = simClock.getCurrentTime();
		double simStep = simClock.getStepSize();
		double rspUpper = parameter.rspUpper;
		// downstream position of the cell

		double dnx = dnDistance(simStep);

		if (segment.isMoveAllowed(curTime) == 0 && dnx < 1.0) {

			// The output capacity is a constraint

			setHeadSpeeds(0.0f, dnx, curTime,parameter);
			return;
		}

		if (leading != null) {

			// There is a cell ahead, speed is determined based on the
			// reaction to that cell.

			setHeadSpeeds(calcHeadSpeed(leading,simStep,parameter), dnx, curTime,parameter);
			return;

		}
		else if (dnx > parameter.rspUpper()) {

			// Since no cell ahead and getDistance is greater than a threshold,
			// it use free flow speed.

			setHeadSpeeds(segment.maxSpeed(), dnx, curTime, parameter);
			return;

		}
		else if (segment.isHead() == 0) {

			// Not the last segment in the link, speed is based on
			// downstrean condition

			setHeadSpeeds(calcHeadSpeed(segment.getDnStream().lastCell(),simStep,parameter), dnx,  curTime, parameter);
			return;

		}
		else if (nHeads > 1) {

			// At the end of the last segment in the link and there is no
			// cell ahead. Need to check where the vehicles in this cell
			// want to go and the condition in the downstream links.

			for (i = 0; i < nHeads; i++) {
				calcHeadSpeed(i, dnx, simClock, parameter);
			}
			return;

		}
		else if (segment.isTheEnd() != 0) {

			// Boundary link, no constrains

			setHeadSpeeds(segment.maxSpeed(), dnx, curTime,parameter);
			return;

		}
		else {

			// At the end of the last segment in the link. This link has
			// one outgoing link.

			MesoSegment ps = (MesoSegment) link().dnLink(0).getStartSegment();
			setHeadSpeeds(calcHeadSpeed(ps.lastCell(),simStep,parameter), dnx, curTime,parameter);

			return;
		}
	}

	// Calculate head speed for the ith traffic stream.
	public void calcHeadSpeed(int ith, double dnx, SimulationClock simClock,MesoParameter parameter) {
		// Capacity based speed

		double maxspeed = parameter.queueReleasingSpeed(timeSinceDispatching(simClock.getCurrentTime()), segment.maxSpeed());

		MesoLink dnlink = (MesoLink) link().dnLink(ith);

		// Find the first vehicle heading to the ith downstream link and
		// number of vehicles ahead (in the same cell)

		MesoVehicle vehicle = firstVehicle;
		int n = 0;
		while (vehicle != null && vehicle.getNextLink() != dnlink) {
			vehicle = vehicle.trailing;
			n++;
		}

		MesoSegment dnseg = (MesoSegment) dnlink.getStartSegment();
		MesoTrafficCell cell = dnseg.lastCell();

		if (vehicle != null) {
			headPositions[ith] = dnx = vehicle.getDistance();
		}
		else {
			headPositions[ith] = dnx;
		}

		// Speed in response to the downstream traffic cell

		double spd_by_rsp;
		if (cell != null) {
			double gap = (dnx + dnseg.getLength() - cell.upDistance(simClock.getStepSize()));
			spd_by_rsp = calcFollowingCellSpeed(gap, cell.tailSpeed(),parameter);
		}
		else {
			spd_by_rsp = maxspeed;
		}

		// Density-based speed

		double spd_by_den;
		int nlanes = segment.nLanes();
		if (vehicle == null || dnx < 10.0 * nlanes) {
			spd_by_den = spd_by_rsp;
		}
		else if (n > nlanes) {
			double k = 1000.0f * n / (dnx * nlanes);
			spd_by_den = calcSpeed(k);
			if (nlanes > 1 && spd_by_den < parameter.minSpeed()) {
				int num = link().dnLink(ith).getStartSegment().nLanes();
				spd_by_den = parameter.minSpeed() * num;
			}
		}
		else {
			spd_by_den = maxspeed;
		}

		double spd = Math.min(spd_by_rsp, spd_by_den);

		if (spd < maxspeed) {
			headSpeeds[ith] = spd;
		}
		else {
			headSpeeds[ith] = maxspeed;
		}

		if (headSpeeds[ith] < 0.1) {
			queueTime = simClock.getCurrentTime();
		}
	}

	// Calculate the speed based on the relationship with the given
	// downstream traffic cell
	public double calcHeadSpeed(MesoTrafficCell cell,double simStepSize, MesoParameter params ) {
		double rspspd;
		if (cell != null) {
			rspspd = calcFollowingCellSpeed(distance(cell,simStepSize), cell.tailSpeed(),params);
		}
		else {
			rspspd = segment.maxSpeed();
		}
		return rspspd;
	}

	// Calculate the speed based on the headway getDistance from the leading
	// cell
	public double calcFollowingCellSpeed(double x, double v, MesoParameter params) {
		double maxgap = params.rspUpper;
		if (x >= maxgap) {
			return segment.maxSpeed();
		}
		else if (x <= 0.1) {
			return v;
		}
		else {
			double r = x / maxgap;
			return r * params.maxSpeed(x,segment.nLanes()) + (1.0f - r) * v;
		}
	}


	public double distance(MesoTrafficCell cell,double simStepSize) {
		// skip the empty cells

		while (cell != null && cell.lastVehicle == null) {
			cell = cell.leading;
		}

		double dis = (firstVehicle != null) ? dnDistance(simStepSize) : segment.getLength();
		if (cell != null) {
			if (cell.segment() == segment()) { // same segment
				dis -= cell.upDistance(simStepSize);
			}
			else { // different segment
				dis += cell.segment().getLength() - cell.upDistance(simStepSize);
			}
		}
		else { // no cell ahead
			dis = Constants.FLT_INF;
		}
		return dis;
	}

	public int isJammed() {
		if (lastVehicle != null && lastVehicle.upPos() >= segment.getLength()) {
			return 1;
		}
		else {
			return 0;
		}
	}
	public boolean isReachable(double simStepSize, double cellSplitGap) {
		return (segment.getLength() - upDistance(simStepSize)) < cellSplitGap;
	}

	public double timeSinceDispatching(double currentTime) {
		return  (currentTime - queueTime);
	}

}
