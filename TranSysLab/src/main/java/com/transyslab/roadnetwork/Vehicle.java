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

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.simcore.mlp.MLPVehicle;

/**
 * Vehicle
 *
 */
public abstract class Vehicle{

	protected int id;
	protected String objInfo;
	protected int type; // vehicle type and class
//	protected ODPair od; // od pair

	protected int busType; // if vehicle is a bus, type of bus != 0
							// if not a bus, type of bus = 0 (Dan)
	protected int routeID; // if vehicle is a bus, route ID != 0
							// if not a bus, route ID = 0 (Dan)

	protected double length; // vehicle length

	protected int attrs; // driver attributes

	protected Path path; // getPath this vehicle will follow

	// index in getPath (list of links) if getPath is defined or index to
	// network link array if getPath is not defined.

	protected int pathIndex;
	protected int info; // previous received info (e.g. vms)

	protected Link nextLink; // next link on its getPath
	protected double departTime;
	protected double timeEntersLink; // time enters current link
	protected double distance; // getDistance from downstream end
	protected double mileage; // total getDistance traveled
	protected double currentSpeed; // current speed in meter/sec

	public Vehicle() {
		attrs = 0;
		type = 0;
		routeID = 0;
		length =  Constants.DEFAULT_VEHICLE_LENGTH;
		pathIndex = -1;
	}

	// Driver atributes

	public void toggleAttr(int attr) {
		attrs ^= attr;
	}
	public int attr(int mask) {
		return (attrs & mask);
	}
	public void setAttr(int s) {
		attrs |= s;
	}
	public void unsetAttr(int s) {
		attrs &= ~s;
	}
	public void setId(int id){
		this.id = id;
	}
	public void setType(int type){
		this.type = type;
	}
	public int getId(){
		return this.id;
	}
	public String getObjInfo(){
		return this.objInfo;
	}
	public double getLength() {
		return length;
	}
	public void setLength(double length){
		this.length = length;
	}
	public void setDepartTime(double departTime){
		this.departTime = departTime;
	}
	//wym
	/*
	public void initPath(Node oriNode, Node desNode) {
		GraphPath<Node, Link> gpath = DijkstraShortestPath.findPathBetween(RoadNetwork.getInstance(), oriNode, desNode);
		getPath = new Path(gpath);
		//调试阶段暂时固定路径
		fixPath();
	}
	/*
	public void init(int id, int t, ODPair od, Path p) {
		this.id = id;
		// TODO 处理id自增
		/*
		if (id > 0) { // id is specified
			c = (id > 0) ? -id : id;
			setCode(c);
		}
		else { // not specified, assign a serial number
			c = (++lastId[threadid]);
			setCode(c);
		}
		this.type = t;
		this.od = od;

		info = Constants.INT_INF;
		getPath = p;
		pathIndex = -1;
		getNextLink = null;

		departTime = SimulationClock.getInstance().getCurrentTime();
		timeEntersLink = departTime;

		oriNode().nOriCounts++;
		desNode().nDesCounts++;

		initialize(); // virtual function

	}*/
	/*
    public void init(int id, int t, double len, double dis,double departtime){
		this.id = id;
        this.type = t;
        // TODO 下移
//        od = VehicleTable.getInstance().getODPair();
//        setPath(VehicleTable.getInstance().getPath());
        length = len;
        getDistance = dis;
        info = Constants.INT_INF;
        //初始化路径
        getNextLink = getPath.getFirstLink();

        departTime = departtime;
        timeEntersLink = departTime;

        oriNode().nOriCounts++;
        desNode().nDesCounts++;

        initialize();				// virtual function

    }
    public int initBus(int bid, ODPair od, Path p) {
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		int c;
		if (bid > 0) { // id is specified
			c = (bid > 0) ? -bid : bid;
			setCode(c);
		}
		else { // not specified, assign a serial number
			c = (++lastId[threadid]);
			setCode(c);
		}
		type = 0x4;
		this.od = od;

		info = 0;
		getPath = p;
		pathIndex = -1;
		getNextLink = null;

		departTime = (float) SimulationClock.getInstance().getCurrentTime();
		timeEntersLink = departTime;

		oriNode().nOriCounts++;
		desNode().nDesCounts++;

		initialize(); // virtual function

		return 1;
	}
	// Dan - initialization of buses for bus rapid transit
	/*
	 * public int initRapidBus(int t, OD_Pair od, int rid, int bt, double hw){
	 * code_ = (++ lastId); type = t; od = od;
	 *
	 * info = DefinedConstant.INT_INF;
	 *
	 * if (rid > 0 && theBusAssignmentTable != NULL) { if (!(getPath =
	 * theBusRunTable->findPath(rid))) { // cerr <<
	 * "Warning:: Unknown bus getPath <" // << rid << ">. "; return -1; } } else {
	 * getPath = null; }
	 *
	 * pathIndex = -1; getNextLink = null;
	 *
	 * departTime = (float) SimulationClock.getInstance().getCurrentTime();
	 * timeEntersLink = departTime;
	 *
	 * oriNode().nOriCounts ++; desNode().nDesCounts ++;
	 *
	 * theBusAssignmentTable.nBusesParsed_ ++;
	 * theBusAssignmentTable.addBRTAssignment(code_, bt, rid, hw);
	 *
	 * initialize(); // virtual function
	 *
	 * return 1; }
	 */

	// This function is called by vehicle table parser. It sould
	// returns 0 if the initialization is successful, -1 if error
	// (it causes program to quit) and 1 if warning error. A none
	// zero return value also indicate the caller to delete this
	// vehicle. The last two arguments are optional
	/*
	 * public int superInit(int id,int ori, int des,int type_id, int path_id){
	 * RN_Node o = MESO_Network.getInstance().findNode(ori); RN_Node d =
	 * MESO_Network.getInstance().findNode(des);
	 *
	 * if (o == null) { // cerr << "Error:: Unknown origin node <" << ori <<
	 * ">. "; return (-1); } else if ( d == null) { // cerr <<
	 * "Error:: Unknown destination node <" << des << ">. "; return (-1); }
	 *
	 * OD_Pair odpair(o, d); PtrOD_Pair odptr(odpair); OdPairSetType::iterator i
	 * = theOdPairs.find(odptr); if (i == theOdPairs.end()) { i =
	 * theOdPairs.insert(i, new OD_Pair(odpair)); } od = (*i).p();
	 *
	 * od->oriNode()->type |= NODE_TYPE_ORI; od->desNode()->type |=
	 * NODE_TYPE_DES;
	 *
	 * if (path_id > 0 && thePathTable != NULL) { if (!(getPath =
	 * thePathTable->findPath(path_id))) { // cerr << "Warning:: Unknown getPath <"
	 * // << path_id << ">. "; return -1; } } else { getPath = NULL; }
	 *
	 * theVehicleTable.nVehiclesParsed_ ++;
	 *
	 * // tomer - to allow vehicle table trips to be assigned with a getPath
	 *
	 * int error = init(id, type_id, od, getPath);
	 *
	 * PretripChoosePath();
	 *
	 * return (error); }
	 */
	// Dan - initialization of buses
	/*
	 * public int initBus(int bid,int ori_node_id, int des_node_id,int path_id){
	 * RN_Node o = theNetwork->findNode(ori); RN_Node d =
	 * theNetwork->findNode(des);
	 *
	 * if (o==null) { // cerr << "Error:: Unknown origin node <" << ori << ">. "
	 * ; return (-1); } else if (d==null) { // cerr <<
	 * "Error:: Unknown destination node <" << des << ">. "; return (-1); }
	 *
	 * OD_Pair odpair(o, d); PtrOD_Pair odptr(odpair); OdPairSetType::iterator i
	 * = theOdPairs.find(odptr); if (i == theOdPairs.end()) { i =
	 * theOdPairs.insert(i, new OD_Pair(odpair)); } od = (*i).p();
	 *
	 * od->oriNode()->type |= DefinedConstant.NODE_TYPE_ORI;
	 * od->desNode()->type |= DefinedConstant.NODE_TYPE_DES;
	 *
	 * if (path_id > 0 && theBusAssignmentTable != NULL) { if (!(getPath =
	 * theBusRunTable->findPath(path_id))) { // cerr <<
	 * "Warning:: Unknown bus getPath <" // << path_id << ">. "; return -1; } }
	 * else { getPath = NULL; }
	 *
	 * theBusAssignmentTable->nBusesParsed_ ++;
	 *
	 * // tomer - to allow vehicle table trips to be assigned with a getPath
	 *
	 * int error = initBus(bid, od, getPath);
	 *
	 * PretripChoosePath();
	 *
	 * return (error); }
	 */


	public Node desNode() {
		return this.path.getDesNode();
	}
	public Node oriNode() {
		return this.path.getOriNode();
	}
	public Path getPath() {
		return this.path;
	}

	public int isType(int flag) {
		return type & flag;
	}
	public int types() {
		return type;
	}
	public int getType() {
		return type & Constants.VEHICLE_CLASS;
	}
	public int group() {
		return type & Constants.VEHICLE_GROUP;
	}
	public int isGuided() {
		return (type & Constants.VEHICLE_GUIDED) != 0 ? 1 : 0;
	}
	public int infoType() {
		return isGuided();
	}
	
	//wym
	public void fixPath() {
		type |= Constants.VEHICLE_FIXEDPATH;
	}
	
	public double departTime() {
		return departTime;
	}

	public double getCurrentSpeed() {
		return currentSpeed;
	}
	public double getDistance() {
		return distance;
	}
	public double mileage() {
		return mileage;
	}
	public void addMileage(double s){
		mileage += s;
	}
	/*
	 * -------------------------------------------------------------------
	 * Returns the getDistance from downstream node of current link.
	 * -------------------------------------------------------------------
	 */
	public double distanceFromDownNode() {
		return (getSegment().getDistance() + distance);
	}

	// Current link the vehicle stays

	public abstract Link getLink();
	public Segment getSegment() {
		return null;
	}
	public Lane getLane() {
		return null;
	}

	// Path
	public Link getNextLink() {
		return nextLink;
	}
	// CAUTION: i has double meaning, depending on whether getPath is defined.
	// path is not null
	public void setPathIndex(int i) {
		pathIndex = i;
		if (path != null) { // has a getPath
			// i is index in getPath
			if (i >= 0 && i < path.nLinks())
				nextLink = path.getLink(i);
			else
				nextLink = null;
		}
		else { // no Path
			//TODO 重构检查
			System.out.print("you are wrong！");
		}
	}
	// path is null
	public void setNextLink(Link tarLink){
		nextLink = tarLink;
	}
	public void donePathIndex() {
		pathIndex = -1;
		nextLink = null;
	}
	public int enRoute() {
		return 0;
	} // check if enroute
		// Return 1 if the link is in getPath, 0 if not, and -1 if unknown.

	public int isLinkInPath(Link plink, int depth /* = 0xFFFF */) {
		if (path != null) {
			int n = path.nLinks();
			if (pathIndex + depth < n)
				n = pathIndex + depth;

			for (int i = pathIndex; i < n; i++) {
				if (path.getLink(i) == plink) {
					return 1;
				}
			}
			return 0;
		}
		else {
			if (nextLink == plink)
				return 1;
			else
				return -1;
		}
	}

	// These functions are called only if the getPath is defined.

	public void setPath(Path p) {
		path = p;
		pathIndex = -1;
	}
	public void setPath(Path p, int i) {
		path = p;
		setPathIndex(i);
	}
	public void advancePathIndex() {
		int i = pathIndex + 1;
		if (i >= path.links.size()) {
			donePathIndex();
		}
		else
			setPathIndex(i);
	}
	public void retrievePathIndex() {
		int i = pathIndex - 1;
		Link pl = path.getLink(i);
		if (getLink() == null || pl.isNeighbor(getLink()) != 0) {
			setPathIndex(i);
		}
	}

	public Link prevLinkOnPath() {
		int i = pathIndex - 2;
		return (path.getLink(i));
	}

	// Route choice model
	/*
	 * public RN_Route routingInfo(){ if (isGuided()!=0) {
	 *
	 * if (theGuidedRoute.preTripGuidance()) {
	 *
	 * // preTripGuidance == 1 means that the Time Table stored in //
	 * theGuidedRoute is *always* used by guided vehicle throughout // the
	 * simulation
	 *
	 * return theGuidedRoute;
	 *
	 * } else {
	 *
	 * // preTripGuidance != 1 means that the Time Table stored in //
	 * theGuidedRoute is used by guided vehicles only after they // have
	 * accessed some information
	 *
	 * return attr(ATTR_ACCESSED_INFO) ? theGuidedRoute : theUnGuidedRoute; } }
	 * else { return theUnGuidedRoute; } }
	 */
	// Find the getNextLink to travel
	public void onRouteChoosePath(Node node, RoadNetwork network) {
		if (path != null) { // getPath assigned
			if (isType(Constants.VEHICLE_FIXEDPATH) != 0 || // path is fixed
					enRoute() == 0) { // no enroute
				advancePathIndex();
			}
			else { // decide whether to switch
				network.routeSwitchingModel(node,this);
			}
		}
		else { // no path assigned
				// generate route dynamically
			network.routeGenerationModel(node,this);
		}
	}
	public void PretripChoosePath(ODPair od,RoadNetwork network) {
		if (od.splits() != null) {
			// At origin and getPath and splits are specified
			// 通过splits_定义的cdf计算选择odcell路径集某一路径的概率
			Path p = od.chooseRoute(this,network);
			setPath(p, 0);
		}
		else if (od.nPaths() != 0) { // At origin and getPath specified
			// 出行过程动态更新车辆路径
			network.routeSwitchingModel(od.getOriNode(),this);
		}
		else { // At origin and no getPath specified for the OD pair
			network.routeGenerationModel(od.getOriNode(),this);
		}
	}

	// tomer - for the vehicles read from vehicle table file

	public void PretripChoosePath(Node oriNode,RoadNetwork network) {
		Path p = path;
		if (p != null) { // vehicle has a getPath specified in the vehicle table
							// file
			// Dan: or a getPath specified in the bus assignment file
			setPath(p, 0);
		}
		else { // vehicle has no getPath specified
			network.routeGenerationModel(oriNode,this);
		}
	}
	/*
	public Route routingInfo() {
		//暂不区分guidance
		return (Route) Route.getInstance();
	}*/

	// MOE

	public double timeSinceDeparture(double currentTime) {
		return currentTime - departTime;
	}
	public double timeEntersLink() {
		return timeEntersLink;
	}
	public void setTimeEntersLink(double arg) {
		timeEntersLink = arg;
	}

	// Returns the time spent in this link
	public double timeInLink(double currentTime) {
		return currentTime - timeEntersLink;
	}
	public double speedInLink(double currentTime) {
		double WORSE_THAN_WALKING = 1.38889;
		double t = timeInLink(currentTime);
		double v;
		if (t < 0.1) {
			v = currentSpeed;
		}
		else {
			v = (getLink().length() - distanceFromDownNode()) / t;
		}
		return  Math.max(v, WORSE_THAN_WALKING);
	}

}
