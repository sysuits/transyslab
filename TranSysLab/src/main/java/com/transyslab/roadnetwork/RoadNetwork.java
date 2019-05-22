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

import com.transyslab.commons.tools.SimulationClock;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;


/**
 * roadnetwork
 *
 */
public abstract class RoadNetwork extends SimpleDirectedWeightedGraph<Node, Link>{

	// These default initial number of objects
	protected String description; // description of the network
	protected int nDestNodes;
	protected int nConnectors, nProhibitors;

	protected List<Node> nodes = new ArrayList<Node>();
	protected List<Link> links = new ArrayList<Link>();
	protected List<Segment> segments = new ArrayList<Segment>();
	protected List<Lane> lanes = new ArrayList<Lane>();
	protected List<Boundary> boundaries = new ArrayList<Boundary>();

	protected List<Sensor> sensors = new ArrayList<Sensor>();
	protected List<Signal> signals = new ArrayList<Signal>();
	protected List<BusStop> busStops = new ArrayList<BusStop>();
	protected List<Path> paths = new ArrayList<>();
	protected List<Connector> connectors = new ArrayList<>();
	protected List<GeoSurface> surfaces = new ArrayList<>();
// protected List<SurvStation> survStations = new ArrayList<SurvStation>();

	//	protected List<Path> paths = new ArrayList<>();
	// ODPair 包含一对od的所有paths
	protected List<ODPair> odPairs = new ArrayList<>();
	// 系统随机种子
	protected Random sysRand;

	// 仿真世界坐标系统
	protected WorldSpace worldSpace = new WorldSpace();

	//仿真时钟
	protected SimulationClock simClock;

	protected Parameter simParameter;

	protected LinkTimes linkTimes;
	// Some basic statistics of the network

	protected double totalLinkLength;
	protected double totalLaneLength;



	public RoadNetwork() {
		super(Link.class);
		description = null;
		totalLinkLength = 0;
		totalLaneLength = 0;
		sysRand = new Random();
		simClock = new SimulationClock();
		linkTimes = new Route(simClock);
	}


	public abstract Node createNode(long id, int type, String name, GeoPoint posPoint);

	public abstract Link createLink(long id, int type, String name,long upNodeId, long dnNodeId);

	public abstract Segment createSegment(long id, int speedLimit, double freeSpeed, double grd, List<GeoPoint> ctrlPoints);

	public abstract Lane createLane(long id, int rule, int orderNum, double width ,String direction ,List<GeoPoint> ctrlPoints);

	public abstract Sensor createSensor(long id, int type, String detName, long segId, double pos, double zone, double interval );


	public Connector createConnector(long id,List<GeoPoint> shapePoints,long upLaneId, long dnLaneId){
		Connector newConnector = new Connector(id,shapePoints,upLaneId,dnLaneId);
		this.connectors.add(newConnector);
		return newConnector;
	}
	public Connector createConnector(long id,long upLaneId, long dnLaneId,List<GeoPoint> shapePoints){
		Connector newConnector = new Connector();
		newConnector.init(id,upLaneId,dnLaneId,shapePoints);
		this.connectors.add(newConnector);
		if (updateRelation(upLaneId,dnLaneId))
			return newConnector;
		else
			return null;
	}
	public boolean updateRelation(long upLaneId, long dnLaneId){
		Lane ulane, dlane;
		if ((ulane = findLane(upLaneId)) == null) {
			System.out.println("Error: unknown upstream lane " + upLaneId);
			return false;
		}
		else if ((dlane = findLane(dnLaneId)) == null) {
			System.out.println("Error: unknown downstream lane " + dnLaneId);
			return false;
		}

		// Check if this connector make sense
		/*
        if (ulane.getSegment().isNeighbor(dlane.getSegment()) == 0) {
            System.out.println("Error: is not the neighbor");
        }*/

		if (ulane.findInDnLane(dnLaneId) != null || dlane.findInUpLane(upLaneId) != null) {
			System.out.println("Error: already connected");
			return false;
		}
		ulane.dnLanes.add(dlane);
		dlane.upLanes.add(ulane);
		return true;
	}
	public void createSurface(long id, int segId, List<GeoPoint> kerbList){
		GeoSurface surface = new GeoSurface();
		surface.init(id);
		surface.setKerbList(kerbList);
		surfaces.add(surface);
	}

//	public abstract void createVehicle(int id, int type, double length, double dis, double speed);

	// 从Path表生成路径
	public Path createPathFromFile(int id, int oriId, int desId){
		//ODPair newODPair = findODPair(oriId,desId);
		Path newPath = new Path();
		newPath.id = id;
		if ((newPath.oriNode = findNode(oriId)) == null) {
			// cerr << "Error:: Unknown origin node <" << ori << ">. ";
		}
		else if ((newPath.desNode = findNode(desId)) == null) {
			// cerr << "Error:: Unknown destination node <" << des << ">. ";
		}
		//newPath.index = newODPair.paths.size();
		//newODPair.paths.add(newPath);
		return newPath;
	}
	// ODPair初始化后调用
	public Path createPathFromGraph(Node oriNode, Node desNode){
		GraphPath<Node, Link> gpath = DijkstraShortestPath.findPathBetween(this, oriNode, desNode);
		return new Path(gpath);
		//ODPair tmpODPair = findODPair(oriNode,desNode);
		//tmpODPair.paths.add(new Path(gpath));
	}
	// TODO ODTable 维护，可删
	public void createODPair(Node oriNode, Node desNode){
		ODPair newodPair = new ODPair(oriNode,desNode);
		this.odPairs.add(newodPair);

	}
	public void createBoundary(int id, double beginx, double beginy, double endx, double endy){
		Boundary newBoundary = new Boundary();
		newBoundary.init(id,nBoundaries(),beginx,beginy,endx,endy);
		this.worldSpace.recordExtremePoints(newBoundary.getStartPnt());
		this.worldSpace.recordExtremePoints(newBoundary.getEndPnt());
		this.boundaries.add(newBoundary);
	}
	public WorldSpace getWorldSpace() {
		return this.worldSpace;
	}
	public double getTotalLinkLength() {
		return this.totalLinkLength;
	}
	public double getTotalLaneLength() {
		return this.totalLaneLength;
	}
	public SimulationClock getSimClock(){
		return this.simClock;
	}
	// These takes an index and return pointer to a object

	public Node getNode(int i) {
		return this.nodes.get(i);
	}
	public Link getLink(int i) {
		return this.links.get(i);
	}
	public Segment getSegment(int i) {
		return this.segments.get(i);
	}
	public Lane getLane(int i) {
		return this.lanes.get(i);
	}
	public Path getPath(int i){ return this.paths.get(i);}
	public Boundary getBoundary(int i){
		return this.boundaries.get(i);
	}
	public Sensor getSensor(int i){return this.sensors.get(i);}
	public Connector getConnector(int i){
		return this.connectors.get(i);
	}
	public GeoSurface getSurface(int i){
		return this.surfaces.get(i);
	}
	public int nLinks(){return links.size();}
	public int nNodes(){
		return nodes.size();
	}
	public int nSegments(){
		return segments.size();
	}
	public int nLanes(){
		return lanes.size();
	}
	public int nSensors(){
		return sensors.size();
	}
	public int nPaths(){return paths.size();}
	public int nBoundaries(){
		return boundaries.size();
	}
	public int nConnectors() {return connectors.size();}
	public int nSurfaces(){
		return surfaces.size();
	}
	public Node findNode(long id) {
		return nodes.stream().filter(n -> n.getId() == id).findFirst().orElse(null);
	}
	public Link findLink(long id) {
		return links.stream().filter(l ->l.getId() == id).findFirst().orElse(null);
	}
	public Segment findSegment(long id){
		return segments.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
	}
	public Lane findLane(long id) {
		return lanes.stream().filter( lane -> lane.getId() == id).findFirst().orElse(null);
	}
	public Boundary findBoundary(long id){
		return boundaries.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
	}
	public Connector findConnector(int id){
		return connectors.stream().filter(c ->c.getId() == id).findFirst().orElse(null);
	}
	public ODPair findODPair(int id){
		return odPairs.stream().filter(odPair -> odPair.getId() == id).findFirst().orElse(null);
	}
	public ODPair findODPair(int oriId, int desId){
		return odPairs.stream().filter(odPair -> odPair.oriNode.getId() == oriId && odPair.desNode.getId() == desId)
				.findFirst().orElse(null);
	}
	public ODPair findODPair(Node oriNode, Node desNode){
		return odPairs.stream().filter(p -> p.oriNode == oriNode && p.desNode == desNode).findFirst().orElse(null);
	}

	public List<Sensor> getSurvStations() {
		return this.sensors;
	}
	public List<Lane> getLanes(){
		return this.lanes;
	}
	// Connects lane 'up' with lane 'dn'. Return -1 if error, 1 if these
	// two upLanes are already connected, or 0 if success.
	public int addLaneConnector(long id,long up, long dn, int successiveFlag, List<GeoPoint> polyline) {
		Lane ulane, dlane;
		if ((ulane = findLane(up)) == null) {
			// cerr << "Error:: unknown upstream lane <" << up << ">. ";
			return -1;
		}
		else if ((dlane = findLane(dn)) == null) {
			// cerr << "Error:: unknown upstream lane <" << dn << ">. ";
			return -1;
		}

		// Check if this connector make sense

		if (ulane.getSegment().isNeighbor(dlane.getSegment()) == 0) {
			// cerr << "Error:: upLanes <" << up << "> and <" << dn << "> "
			// << "are not neighbors. ";
			return -1;
		}

		if (ulane.findInDnLane(dn) != null || dlane.findInUpLane(up) != null) {
			// cerr << "Warning:: upLanes <" << up << "> and <" << dn << "> "
			// << "are already connected. ";
			return 1;
		}
		ulane.dnLanes.add(dlane);
		dlane.upLanes.add(ulane);

		nConnectors++;
		return 0;

	}
	// Exclude turn movements from link 'up' to link "dn". Returns 0 if
	// it success, -1 if error, or 1 if the turn is already excluded.
	public int addTurnProhibitor(long up, long dn) {
		Link ulink, dlink;
		if ((ulink = findLink(up)) == null) {
			// cerr << "Error:: unknown upstream link <" << up << ">. ";
			return -1;
		}
		else if ((dlink = findLink(dn)) == null) {
			// cerr << "Error:: unknown upstream link <" << dn << ">. ";
			return -1;
		}

		// Check if this prohibitor make sensor

		if (ulink.isNeighbor(dlink) <= 0) {
			return -1;
		}

		ulink.excludeTurn(dlink);

		nProhibitors++;

		return 0;
	}
	public void routeGenerationModel(Node pn, Vehicle pv){
		// the link by which vehicle pv came

		Link slink = pv.getLink();

		if (slink!=null && slink.getDnNode() != pn) {
			// Error in calling this function
			pv.donePathIndex();
			return;
		}

		// destination node of this vehicle

		Node  dnode = pv.desNode();

		if (pn.nDnLinks() < 1 || pn == dnode) {
			// reached destination
			pv.donePathIndex();
			return;
		}

		double[] util = new double [pn.nDnLinks()];
		double NOT_CONNECTED = Double.POSITIVE_INFINITY - 1.0;

		//Route info = pv.routingInfo();
		// TODO 检查
		Route info = (Route)linkTimes;
		double sum = 0.0;		// sum of utilities
		double cost;			// travel time
		int i;
		int itype = pv.infoType(); // to be checked
		float beta = Parameter.routingBeta(itype);
		double entry = simClock.getCurrentTime();

		// expected travel time from this node to destination node

		double cost0;
		Link  plink;

		if (slink != null) {

			cost0 = info.dnRouteTime(slink, dnode, entry, nDestNodes);
			entry += info.linkTime(slink.getIndex(), entry);

		} else {
			cost0 = Double.POSITIVE_INFINITY;
			for (i = 0; i < pn.nDnLinks(); i ++) {
				plink = pn.getDnLink(i);//当前节点的下游节段
				cost = info.upRouteTime(plink, dnode, entry, nDestNodes);

				if (cost < cost0) {
					cost0 = cost;
				}
			}
		}

		plink = null;
		for (i = 0; i < pn.nDnLinks(); i ++) {
			plink = pn.getDnLink(i);

			if (slink!=null && (slink.isMovementAllowed(plink))==0) {

				util[i] = 0.0;	// Turn is not allowed

			} else if ((plink.isCorrect(pv))==0) {

				util[i] = 0.0;	// Can not use the link

			} else if ((cost = info.upRouteTime(
					plink, dnode, entry, nDestNodes)) >= NOT_CONNECTED) {

				util[i] = 0.0;	// Not connected to destination

			} else if (cost < cost0 * simParameter.validPathFactor()) {

				// Cost1 is the travel time on next link.

				double cost1 = info.linkTime(plink, entry);

				// Cost2 is the travel time on the shorted getPath from the
				// downstream end of plink to pv's destination.

				double cost2 = cost - cost1;

				// Add the diversion penalty if change from freeway to
				// ramp/urban


				// tomer - adding restriction such that the driver doesn't choose
				// a link that takes it further away from the destination.

				if (simParameter.rationalLinkFactor() * cost2 <= cost0) {
					double cost3;

					if (slink != null &&
							slink.linkType() == Constants.LINK_TYPE_FREEWAY &&
							plink.linkType() != Constants.LINK_TYPE_FREEWAY) {
						cost3 = simParameter.diversionPenalty();
					} else {
						cost3 = 0;
					}

					cost = cost1 + cost2 + cost3;

					util[i] = Math.exp(beta * cost / cost0);
					sum += util[i];
					//System.out.println(" util "+util[i]+" cost "+cost+" cost0 "+cost0+" cost1 "+cost1+" cost2 "+cost2+" cost3 "+cost3);
				}
			} else {
				//System.out.println("--"+plink.get_code()+" "+dnode.get_code());
				util[i] = 0.0;	// this getPath is too long or contains a cycle
			}

		}

		// Select one of the outgoing links based on the probabilities
		// calculated using a logit model

		if (sum > 0.0) {	// At least one link is valid sum > Constants.DBL_EPSILON

			// a uniform (0,1] random number

			double rnd = this.sysRand.nextDouble();
			double cdf;
			for (i = pn.nDnLinks() - 1, cdf = util[i] / sum;
				 i > 0 && rnd > cdf;
				 i --) {
				cdf += util[i-1] / sum;
			}
			// TODO 检验重构结果
			//pv.setPathIndex(getDnLink(i).getIndex());
			pv.setNextLink(pn.getDnLink(i));

		} else {			// No link is valid

			if (slink==null ||		// not enter the network yet
					pn.type(Constants.NODE_TYPE_EXTERNAL)!=0) {

				// will be removed (ori or external node)

				pv.donePathIndex();

			} else if ((i = slink.getRightDnIndex()) != 0xFF) {

				// choose the right most link, hopefully it is a off-ramp if freeway
				// TODO path 为空
				pv.setNextLink(pn.getDnLink(i));

			} else {			// no where to go

				pv.donePathIndex();
			}
		}
	}
	// For vehicles that already has a getPath, they use this function
	// to check whether they should keep their current paths or enroute
	public void routeSwitchingModel(Node pn, Vehicle pv){
		Path curPath = pv.getPath();
		ODPair od = findODPair(curPath.oriNode,curPath.desNode);
		// the link from which the vehicle pv came

		Link slink = pv.getLink();

		if (slink!=null && slink.getDnNode() != pn) {

			// Error in calling this function

			pv.donePathIndex();
			return;
		}

		// destination node of this vehicle

		Node dnode = pv.desNode();

		// Information used to routing the vehicle

		// Route info = pv.routingInfo();
		// TODO 检查
		Route info = (Route)linkTimes;
		int flag;
		int i, j, n;
		double cost;

		//Vector<Pointer<RN_PathPointer> ALLOCATOR> choices;
		//Vector<float ALLOCATOR> costs;
		ArrayList<Double>costs = new ArrayList<>();
		ArrayList<Path>choices = new ArrayList<>();
		// Prepare the choice sets and find the shortest route

		double smallest = Double.POSITIVE_INFINITY;
		if (slink!=null) {					// enroute
			for (i = 0; i < slink.nPaths(); i ++) {
				Path pp = slink.pathPointer(i);
				if (pp.getDesNode() == dnode) { // goes to my desination
					// add to my choice set
					cost = pp.cost(info,simClock.getCurrentTime());
					costs.add(cost);
					choices.add(pp);
					if (cost < smallest) {
						smallest = cost;
					}
				}
			}
			flag = 1;
		} else {						// at the origin
			for (i = 0; i < pn.nDnLinks(); i ++) { // check each out going link
				slink = pn.getDnLink(i);
				for (j = 0; j < slink.nPaths(); j ++) { // each getPath
					Path pp = slink.pathPointer(j);

					// Dan: bus run paths should not be considered

					//	if (!theBusAssignmentTable ||
					//                        (theBusAssignmentTable && !theBusRunTable.findPath(pp.getPath().code())))

					if (pp.getDesNode() == dnode && // goes to my desination
							(od!=null || pp.IsUsedBy(od))) {// this getPath should be also in my MesoODCell getPath set
						// add to my choice set
						cost = pp.cost(info,simClock.getCurrentTime());
						costs.add(cost);
						choices.add(pp);
						if (cost < smallest) {
							smallest = cost;
						}
					}

				}
			}
			flag = 0;
		}

		n = choices.size();

		if (n > 1) {

			// Find the utility of choosing each route

			double util[] = new double[n];
			int itype = pv.infoType();
			float beta = Parameter.routingBeta(itype);
			float alpha = simParameter.commonalityFactor();
			double sum = 0.0;		// sum of utilities

			for (i = 0; i < n; i ++) {
				Path pp = choices.get(i);
				// diversion penalty
				//wym 更改判断逻辑 考虑到在行驶中改变path 原逻辑： && pp != pv.getPath()
				if (flag==1 && pv.path.links.containsAll(pp.links)) {
					cost = simParameter.pathDiversionPenalty();
				} else {
					cost = 0;
				}
				cost += costs.get(i);
				util[i] = Math.exp(beta * cost / smallest + alpha * pp.cf());
				sum += util[i];
			}

			// Select getPath based on the probabilities calculated using a
			// logit model

			if (sum > 0.0) {

				// a uniform (0,1] random number

				double rnd = this.sysRand.nextDouble();

				double cdf;
				for (i = n - 1, cdf = util[i] / sum;
					 i > 0 && rnd > cdf;
					 i --) {
					cdf += util[i-1] / sum;
				}
			} else {
				i = this.sysRand.nextInt(choices.size());
			}

			Path pp = choices.get(i);
			pv.setPath(pp, flag);


		} else if (n==1) {				// n == 1

			Path pp = choices.get(0);
			pv.setPath(pp, flag);

		} else {

			// No available getPath at this node. Switch to route generation
			// model

			pv.setPath(null);
			routeGenerationModel(pn,pv);
		}
	}


	// Before we use the parsed network, this function must called to
	// calculate some static information, sort objects, etc.
	// --------------------------------------------------------------------
	// Requires: Called after network database has been sucessfully parsed
	// Modifies: variables in link and node
	// Effects : Sorts links and calculates topology variables.
	// --------------------------------------------------------------------
	public void calcStaticInfo() {
		// Create the world space
		worldSpace.createWorldSpace();
		// Lane必须从左到右解析，Segment必须从上游至下游解析
		for(Lane itrLane:lanes){
			int leftLaneInSeg = itrLane.segment.getLeftLane().index;
			// 非最右车道
			if (itrLane.index != leftLaneInSeg + itrLane.segment.nLanes() - 1)
				itrLane.rightLane =  this.getLane(itrLane.index + 1);
			// 非最左车道
			if (itrLane.index != leftLaneInSeg)
				itrLane.leftLane = this.getLane(itrLane.index - 1);
		}
		// 组织前后路段关系
		for (Segment itrSegment:segments) {
			int upSegmentInLink = itrSegment.link.getStartSegment().index;
			// 非最下游子路段
			if (itrSegment.index != upSegmentInLink + itrSegment.link.nSegments() - 1)
				itrSegment.dnSegment = this.getSegment(itrSegment.index + 1);
			// 非最上游子路段
			if (itrSegment.index != upSegmentInLink)
				itrSegment.upSegment = this.getSegment(itrSegment.index - 1);
			itrSegment.snapCoordinates();
		}

		for (Segment itrSegment:segments) {

			// Generate arc info such as angles and length from the two
			// endpoints and bulge. This function also convert the
			// coordinates from database format to world space format
			itrSegment.calcArcInfo(worldSpace);

		}

		// Boundary 位置平移
		for (Boundary itrBoundary:boundaries) {
			itrBoundary.translateInWorldSpace(worldSpace);
		}

		// Sort outgoing and incoming arcs at each node.
		// Make sure RN_Link::comp() is based on angle.

		for (Node itrNode:nodes) {
			itrNode.sortUpLinks();
			itrNode.sortDnLinks();
			// 坐标平移
			itrNode.calcStaticInfo(worldSpace);
		}

		// Set destination index of all destination nodes

		for (Node itrNode:nodes) {
			itrNode.destIndex = -1;
		}
		for (int i = nDestNodes = 0; i < nodes.size(); i++) {
			if ((nodes.get(i).getType() & Constants.NODE_TYPE_DES) != 0)
				nodes.get(i).destIndex = nDestNodes++;
		}
		if (nDestNodes == 0) {
			for (int i = 0; i < nodes.size(); i++) {
				nodes.get(i).destIndex = nDestNodes++;
			}
		}

		// Set upLink and dnLink indices

		for (Link itrLink:links) {
			itrLink.calcIndicesAtNodes();
		}

		// Set variables in links

		for (Link itrLink:links) {
			itrLink.calcStaticInfo();
		}

		// Set variables in segments

		for (Segment itrSegment: segments) {
			itrSegment.calcStaticInfo();
		}

		// Set variables in upLanes
		// 增加坐标平移操作

		for (Lane itrLane:lanes) {
			itrLane.calcStaticInfo(this.worldSpace);
		}
		// Surface 位置平移
		for (GeoSurface surface:surfaces) {
			surface.translateInWorldSpace(worldSpace);
		}
		// Connector 位置平移
		for (Connector connector:connectors) {
			connector.translateInWorldSpace(worldSpace);
		}
	}
	public void calcDbStaticInfo(){
		// Create the world space
		worldSpace.createWorldSpace();
		// Lane必须从左到右解析，Segment必须从上游至下游解析
		for(Link itrLink:links){
			// 按上下游关系保存路段的引用
			List<Segment> sgmtsInLink = itrLink.getSegments();
			int nSegment = sgmtsInLink.size();
			for(int i=0;i<nSegment;i++){
				Segment itrSgmt = sgmtsInLink.get(i);
				if(i!=0)//起始路段没有上游
					itrSgmt.setUpSegment(sgmtsInLink.get(i-1));
				if(i!=nSegment-1)//末端路段没有下游
					itrSgmt.setDnSegment(sgmtsInLink.get(i+1));
				// 按横向关系保存相邻车道的引用
				List<Lane> lanesInSgmt = itrSgmt.getLanes();
				int nLanes = lanesInSgmt.size();
				for(int j=0;j<nLanes;j++){
					if(j!=0)// 最左侧车道
						lanesInSgmt.get(j).setLeftLane(lanesInSgmt.get(j-1));
					if(j!=nLanes-1)// 最右侧车道
						lanesInSgmt.get(j).setRightLane(lanesInSgmt.get(j+1));
				}
			}
		}
		for (Segment itrSegment:segments) {

			// Generate arc info such as angles and length from the two
			// endpoints and bulge. This function also convert the
			// coordinates from database format to world space format
			itrSegment.calcArcInfo(worldSpace);
		}

		// Boundary 位置平移
		for (Boundary itrBoundary:boundaries) {
			itrBoundary.translateInWorldSpace(worldSpace);
		}

		// Sort outgoing and incoming arcs at each node.
		// Make sure RN_Link::comp() is based on angle.

		for (Node itrNode:nodes) {
			itrNode.sortUpLinks();
			itrNode.sortDnLinks();
			// 坐标平移
			itrNode.calcStaticInfo(worldSpace);
		}

		// Set destination index of all destination nodes

		for (Node itrNode:nodes) {
			itrNode.setDestIndex(-1);
		}
		for (int i = nDestNodes = 0; i < nodes.size(); i++) {
			if ((nodes.get(i).getType() & Constants.NODE_TYPE_DES) != 0)
				nodes.get(i).setDestIndex(nDestNodes++);
		}
		if (nDestNodes == 0) {
			for (int i = 0; i < nodes.size(); i++) {
				nodes.get(i).setDestIndex( nDestNodes++);
			}
		}

		// Set upLink and dnLink indices

		for (Link itrLink:links) {
			itrLink.calcIndicesAtNodes();
		}

		// Set variables in links

		for (Link itrLink:links) {
			// 无Segment的路段不计算
			if(itrLink.nSegments()>0){
				itrLink.calcStaticInfo();
			}

		}

		// Set variables in segments

		for (Segment itrSegment: segments) {
			itrSegment.calcStaticInfo();
		}

		// Set variables in upLanes
		// 增加坐标平移操作

		for (Lane itrLane:lanes) {
			itrLane.calcStaticInfo(this.worldSpace);
		}
		// Surface 位置平移
		for (GeoSurface surface:surfaces) {
			surface.translateInWorldSpace(worldSpace);
		}
		// Connector 位置平移
		for (Connector connector:connectors) {
			connector.translateInWorldSpace(worldSpace);
		}

	}
	public void initializeLinkStatistics() {
		for (int i = 0; i < links.size(); i++) {
			getLink(i).initializeStatistics(linkTimes.infoPeriods);
		}
	}
	public void resetLinkStatistics(int col, int ncols) {
		for (int i = 0; i < links.size(); i++) {
			getLink(i).resetStatistics(col, ncols, linkTimes.infoPeriods);
		}
	}
	// 新增代码，复位统计流量和旅行时间的数组，用于参数校准模块，仿真重启
	public void resetLinkStatistics() {
		for (int i = 0; i < links.size(); i++) {
			getLink(i).resetStatistics(linkTimes.infoPeriods);
		}
	}

	//wym
	public Random getSysRand() {
		return sysRand;
	}
	public Parameter getSimParameter() {
		return simParameter;
	}
	public void	renewSysRandSeed() {
		sysRand.setSeed(sysRand.nextLong());
	}
	public void setArrowColor(double now, HashMap<SignalArrow, float[]> signalColor) {
		{
//			double now = getSimClock().getCurrentTime();
			nodes.stream().
					filter(n->(n.getType()&Constants.NODE_TYPE_SIGNALIZED_INTERSECTION)!=0).
					forEach(n-> {
						for (int i = 0; i < n.nUpLinks(); i++) {
							Segment seg = n.getUpLink(i).getEndSegment();
							for (int j = 0; j < seg.nLanes(); j++) {
								Lane lane = seg.getLane(j);
								lane.getSignalArrows().stream().forEach(a->{
									//a.setColor(Constants.COLOR_RED);
									if (a.rightTurnFree())
										signalColor.put(a,Constants.COLOR_WHITE);
									else
										signalColor.put(a,Constants.COLOR_RED);
								});
							}
						}
						SignalPlan plan = n.findPlan(now);
						SignalStage stage = plan.findStage(now);
						if (stage!=null) {
							stage.getLinkIDPairs().forEach(p->{
								Segment seg = findLink(p[0]).getEndSegment();
								Link l1 = null;
								Link L2 = null;
								for (int i = 0; i < seg.nLanes(); i++) {
									seg.getLane(i).getSignalArrows().forEach(a->{
										String checkDir = stage.getDirection(p).split("_")[1];
										if (a.getDirection().equals(checkDir)) {
											if (plan.isAmber(now)) {
												//a.setColor(Constants.COLOR_AMBER);
												signalColor.put(a,Constants.COLOR_AMBER);
											}
											else {
												//a.setColor(Constants.COLOR_GREEN);
												signalColor.put(a,Constants.COLOR_GREEN);
											}
										}
									});
								}
							});
						}

					});
		}
	}
	public void rmLastLink(){
		Link rmLink = links.remove(links.size()-1);
		System.out.println("DEBUG: remove centerRoad ID = " + rmLink.getId());
		rmLink.getUpNode().rmDnLink(rmLink);
		rmLink.getDnNode().rmUpLink(rmLink);
	}
	public void rmSegments(List<Segment> rmSegments){
		segments.removeAll(rmSegments);
	}
	public void rmLanes(List<Lane> rmLanes){
		lanes.removeAll(rmLanes);
	}
	public void setArrowColor() {
		{
			{
//			double now = getSimClock().getCurrentTime();
				nodes.stream().
						filter(n->(n.getType()&Constants.NODE_TYPE_SIGNALIZED_INTERSECTION)!=0).
						forEach(n-> {
							for (int i = 0; i < n.nUpLinks(); i++) {
								Segment seg = n.getUpLink(i).getEndSegment();
								for (int j = 0; j < seg.nLanes(); j++) {
									Lane lane = seg.getLane(j);
									lane.getSignalArrows().stream().forEach(a->{
										a.setColor(Constants.COLOR_WHITE);
									});
								}
							}

						});
			}
		}
	}

}
