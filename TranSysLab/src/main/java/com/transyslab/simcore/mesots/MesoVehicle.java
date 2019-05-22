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

import java.util.ListIterator;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.*;


public class MesoVehicle extends Vehicle {


	protected final int FLAG_PROCESSED = 0x10000000;
	protected MesoTrafficCell trafficCell; // pointer to current traffic cell
	protected MesoVehicle leading; // downstream vehicle
	protected MesoVehicle trailing; // upstream vehicle
	protected long sensorIDFlag;
	protected int flags; // indicator for internal use
	protected boolean needRecycle;
	// These variables are use to cache the calculation for speeding
	// up

	protected double spaceInSegment; // update each time it enter a new segment

	public MesoVehicle() {

	}

	public void toggleFlag(int flag) {
		flags ^= flag;
	}
	public int flag(int mask) {// 0xffffffff
		return (flags & mask);
	}
	public void setFlag(int s) {
		flags |= s;
	}
	public void unsetFlag(int s) {
		flags &= ~s;
	}

	public MesoLink getNextMesoLink() {
		return (MesoLink) nextLink;
	}

	public MesoLink mesoLink() {
		return (MesoLink) getLink();
	}

	public MesoSegment mesoSegment() {
		return (MesoSegment) getSegment();
	}

	@Override
	public Link getLink() 
	{
		return trafficCell != null ? trafficCell.link() : null;
	}
	@Override
	public Segment getSegment() 
	{
		return trafficCell != null ? trafficCell.segment() :  null;
	}

	public MesoTrafficCell trafficCell() {
		return trafficCell;
	}

	public void leading(MesoVehicle pv) {
		leading = pv;
	}
	public void trailing(MesoVehicle pv) {
		trailing = pv;
	}
	public MesoVehicle leading() {
		return leading;
	}
	public MesoVehicle trailing() {
		return trailing;
	}
	public double dnPos() {
		return distance;
	}
	public double upPos() {
		return distance + spaceInSegment;
	}
	public double spaceInSegment() {
		return spaceInSegment;
	}
	public void calcSpaceInSegment() {
		spaceInSegment = length / getSegment().nLanes();
	}

	public MesoVehicle leadingVehicleInStream(double channelizeDistance) {
		double dndis = distanceFromDownNode();

		if (dndis > channelizeDistance) {
			return leading;
		}

		MesoVehicle front = leading;

		// Find the first vehicle in the same traffic stream that goes
		// to the same direction as this vehicle

		while (front != null && front.nextLink != nextLink) {
			front = front.leading;
		}

		return front;
	}

	public double gapDistance(MesoVehicle front) {
		if (front == null)
			return Constants.FLT_INF;

		MesoSegment ps = front.trafficCell.segment();
		if (trafficCell.segment() == ps) { // same segment
			return distance - front.upPos();
		}
		else { // different segment
			double gap =  distance + (ps.getLength() - front.upPos());
			return gap;
		}
	}
	// minSpeed, minHeadWayGap in MesoParameter
	public void updateSpeed(double minSpeed, double minHeadWayGap) // interpolate speed
	{
		int i;

		if (trafficCell.nHeads > 1 && nextLink != null && getSegment().isHead() != 0) {
			i = nextLink.getDnIndex();
		}
		else {
			i = 0;
		}

		double headspd = trafficCell.headSpeed(i);
		double headpos = trafficCell.headPosition(i);

		if (distance <= headpos) {
			currentSpeed = headspd;
		}
		else if (distance >= trafficCell.tailPosition()) {
			currentSpeed = trafficCell.tailSpeed();
		}
		else {
			double dx = trafficCell.tailPosition() - headpos;
			if (dx < 0.1) {
				currentSpeed = headspd;
			}
			else {
				double dv = (trafficCell.tailSpeed() - headspd) / dx;
				currentSpeed = headspd + dv * (distance - headpos);
			}
		}

		// This avoid large gaps in queue stream

		if (currentSpeed < minSpeed
				&& gapDistance(leading) > minHeadWayGap) {
			currentSpeed = minSpeed;
		}
	}
	/*
	 * public int init(int id, int ori, int des, int type_id, int path_id ) //
	 * virtual int type_id = 0, int path_id = -1 { int error = superInit(id,
	 * ori, des, type_id, path_id); if (error < 0) return 1; // show a warning
	 * msg enterPretripQueue(); return 0; }
	 */
	// 用于读发车表
	public void init(int id, int t, double len, double dis,double departtime){
		this.id = id;
		this.type = t;
		// TODO 下移
//        od = VehicleTable.getInstance().getODPair();
//        setPath(VehicleTable.getInstance().getPath());
		length = len;
		distance = dis;
		info = Constants.INT_INF;
		//初始化路径
		nextLink = path.getFirstLink();

		departTime = departtime;
		timeEntersLink = departTime;

	}
	public void initialize(MesoParameter params, MesoRandom random) // called by init()
	{
		flags = 0;
		sensorIDFlag = -100000;
		int prefix = type & (~Constants.VEHICLE_CLASS); // prefix, e.g., HOV
		int vehicle_class = (type & Constants.VEHICLE_CLASS);

		if (vehicle_class == 0) {
			vehicle_class = random.drandom(params.nVehicleClasses(),
					params.vehicleClassCDF());
		}
		type = vehicle_class;

		// Attach some extra bits to "type" based on given probabilities if
		// the prefix is not specified

		if (prefix != 0) {
			type |= prefix;
		}
		else {
			if (random.brandom(params.etcRate()) != 0) {

				// This implementation treats all ETC vehicles as AVI

				type |= (Constants.VEHICLE_ETC | Constants.VEHICLE_PROBE);
			}

			if (random.brandom(params.guidedRate()) != 0) {
				type |= Constants.VEHICLE_GUIDED;
			}

			if (random.brandom(params.hovRate()) != 0) {
				type |= Constants.VEHICLE_HOV;
			}
		}
		mileage = 0.0f;
	}

	public void enterPretripQueue(double simStep) {

		// 所有车的路径已初始化
		if (nextLink == null) { // No path
			//MesoVehiclePool.getInstance().recycle(this);
			needRecycle = true;
			return;
		}
		getNextMesoLink().queue(this);
		trafficCell = null;
		double spd = ((MesoSegment) nextLink.getStartSegment()).maxSpeed();
		distance = -spd * simStep;
	}


	public void appendTo(MesoTrafficCell cell, int stepCounter, double minHeadWayGap) {
		MesoSegment ps = cell.segment();
		trafficCell = cell;
		calcSpaceInSegment();
		distance += ps.getLength();
		currentSpeed = cell.tailSpeed();

		// Make sure not crash into front vehicle

		if (leading != null) { // no overtaking
			double pos = leading.upPos() + minHeadWayGap / ps.nLanes();
			distance = Math.max(distance, pos);
		}

		if ((stepCounter & 0x1) != 0) {
			setFlag(FLAG_PROCESSED);
		}
		//奇数步长：flag =  FLAG_PROCESSED异或FLAG_PROCESSED = 0
  	    //偶数步长：flag = 0异或FLAG_PROCESSED = FLAG_PROCESSED
		markAsProcessed();
	}
    public void appendSnapshotTo(MesoTrafficCell cell)
    {
    	  MesoSegment ps = cell.segment();
    	  trafficCell = cell;
    	  calcSpaceInSegment();
    }
	public void move(MesoNetwork network) // update position
	{
		double stepSize =  network.getSimClock().getStepSize();
		// Prevent to be processed twice if it moved into a new link

		markAsProcessed();

		// Leading vehicle in the same traffic stream

		MesoVehicle front = leadingVehicleInStream(network.getSimParameter().channelizeDistance);

		double frequency =  1.0 / stepSize;
		double oldpos = distance;
		int gone = 0;

		// Calculate the current speed

		updateSpeed(network.getSimParameter().minSpeed(),network.getSimParameter().minHeadwayGap());

		// Position at the end of this interval

		distance -= currentSpeed * stepSize;

		if (front != null) { // no overtaking
			double pos = front.upPos() + network.getSimParameter().minHeadwayGap() / getSegment().nLanes();
			distance = Math.max(distance, pos);
		}

		// 节段是否有检测器
		if (getSegment().getSensors() != null) {
			ListIterator<Sensor> i = getSegment().getSensors().listIterator();
			while (i.hasNext()) {
				Sensor tmp = i.next();
				// 车辆是否经过检测断面
				// 两个检测器之间距离需超过8米
				if (distance <= tmp.getPosition() && tmp.getPosition() - distance < 8 && sensorIDFlag != tmp.getId()) {
					sensorIDFlag = tmp.getId();
					tmp.measure(currentSpeed);
				}

			}
		}

		if (distance < 0.0) { // cross segment

			if ((gone = transpose(network)) == 0) { // can not moved out
				distance = 0.0f; // the maximum it can move
			}
		}

		if (gone < 0) { // removed from the network
			return;
		}
		else if (gone == 0) { // still in the same segment
			if (distance >= oldpos) { // not moved
				currentSpeed = 0.0f;
				return; // no need to enter advance()
			}
			else {
				currentSpeed = (oldpos - distance) * frequency;
			}
		}
		else { // moved into a downstream segment
			currentSpeed = (oldpos + getSegment().getLength() - distance) * frequency;
		}
		advance(); // sort position in list
	}


	// Advance the vehicle to a position in the vehicle list that
	// corresponding to its current value of "getDistance". This function
	// is invoked when a vehicle is moved (including moved into a
	// downstream segment), so that the vehicles in macro vehicle list is
	// always sorted by their position.
	public void advance() // update position in list
	{
		// (0) Check if this vehicle should be advanced in the list

		if (leading == null || distance >= leading.distance) {

			// no need to advance this vehicle in the list

			return;
		}

		// (1) Find the vehicle's position in the list

		MesoVehicle front = leading;

		while (front != null && distance < front.distance) {
			front = front.leading;
		}

		// (2) Take this vehicle out from the list

		leading.trailing = trailing;

		if (trailing != null) {
			trailing.leading = leading;
		}
		else { // last vehicle in the segment
			trafficCell.lastVehicle = leading;
		}

		// (3) Insert this vehicle after the front

		// (3.1) Pointers with the leading vehicle

		leading = front;

		if (leading != null) {
			trailing = leading.trailing;
			leading.trailing = this;
		}
		else {
			trailing = trafficCell.firstVehicle;
			trafficCell.firstVehicle = this;
		}

		// (3.2) Pointers with the trailing vehicle

		if (trailing != null) {
			trailing.leading = this;
		}
		else { // this vehicle becomes the last one
			trafficCell.lastVehicle = this;
		}
	}
	public int transpose(MesoNetwork network) // returns 1 if it success
	{

		MesoSegment ps = mesoSegment();

		if (ps.isMoveAllowed(network.getSimClock().getCurrentTime()) == 0)
			return 0;

		mileage += ps.getLength(); // record mileage

		int done = 0;
		MesoSegment ds = (MesoSegment) getSegment().getDnSegment();

		if (ds != null) {

			if (ds.isJammed() == 0) {
				trafficCell.remove(this);
				//ds.append(this);
				network.appendVhc2Sgmt(ds,this);
				// 重构旧
				//this.append2Sgmt = ds;
				//this.needAppend2Sgmt = true;
				done = 1;
			}

		}
		else if (nextLink != null) { // cross link

			if (getNextMesoLink().isJammed() == 0) {

				mesoLink().recordTravelTime(this,network.getLinkTimes());

				// Estimate the time this vehicle enter the link

				double dt;
				if (currentSpeed > 1.0) {
					dt = distance / currentSpeed;
				}
				else {
					dt = 0.5 * network.getSimClock().getStepSize();
				}
				timeEntersLink =  network.getSimClock().getCurrentTime() - dt;

				trafficCell.remove(this);

				if (getLink().isNeighbor(nextLink) != 0) {
					//getNextMesoLink().append(this);
					network.appendVhc2Sgmt((MesoSegment) getNextMesoLink().getStartSegment(),this);
					//重构旧
					//this.append2Sgmt = (MesoSegment) getNextMesoLink().getStartSegment();
					//this.needAppend2Sgmt = true;
					// 更新nextLink
					onRouteChoosePath(this.nextLink.getDnNode(),network);
					done = 1;
				}
				else {

					mesoLink().recordTravelTime(this,network.getLinkTimes());
					removeFromNetwork();
					done = -1;
				}
			}

		}
		else { // arrive destination
			mesoLink().recordTravelTime(this,network.getLinkTimes());
			removeFromNetwork();
			done = -1;
		}

		if (done != 0) {
			ps.scheduleNextEmitTime();

			if (done > 0) {
				calcSpaceInSegment();
			}
		}

		return done;
	}

	@Override
	public int enRoute() { // virtual
		return attr(Constants.ATTR_ACCESSED_INFO);
	}

	// Call route choice model

	public void changeRoute(MesoNetwork network) {

		if (getLink() != null) { // alreay in the network
			onRouteChoosePath(getLink().getDnNode(),network);
		}
		else { // in spillback queue
			onRouteChoosePath(nextLink.getUpNode(),network);
		}
	}

	// Called when a vehicle arrived its desitination

	public void removeFromNetwork() {

		trafficCell.remove(this);
		needRecycle = true;

	}


	// These are used to prevent to process a vehicle twice in a
	// single iteration

	public void markAsProcessed() {
		flags ^= FLAG_PROCESSED;
	}
	public int isProcessed(int stepCounter) {
		if ((stepCounter & 0x1) != 0) { // odd step
			if ((flags & FLAG_PROCESSED) == 0)
				return 1;
			else
				return 0;
		}
		else { // even step
			return (flags & FLAG_PROCESSED);
		}
	}

}
