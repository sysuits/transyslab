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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.transyslab.commons.renderer.AnimationFrame;
import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.roadnetwork.*;



public class MesoNetwork extends RoadNetwork {
	protected int[] permuteLink;
	protected int nPermutedLinks;
	protected List<MesoSdFn> sdFns;
	protected MesoODTable odTable;
	protected MesoVehiclePool recycleVhcList;
	protected MesoCellList recycleCellList;
	protected MesoVehicleTable vhcTable;// 外部读入的发车表
	protected MesoRandom[] mesoRandom;

	protected int stepCounter;  // 车辆move()迭代计数器
	protected int vhcCounter;  	// 在网车辆计数器

	public MesoNetwork() {
		simParameter = new MesoParameter();
		sdFns = new ArrayList<>();
		recycleVhcList = new MesoVehiclePool();
		recycleCellList = new MesoCellList(recycleVhcList);
		stepCounter = 0;
		vhcCounter = 0;
	}
	public MesoODTable getODTable(){
		return odTable;
	}
	public void setOdTable(MesoODTable odTable){
		this.odTable = odTable;
	}
	public void setVhcTable(MesoVehicleTable vhcTable){
		this.vhcTable = vhcTable;
	}
	public void setMesoRandoms(MesoRandom[] randoms){
		this.mesoRandom = randoms;
	}
	public LinkTimes getLinkTimes(){
		return this.linkTimes;
	}
	public MesoParameter getSimParameter(){
		return (MesoParameter) simParameter;
	}
	public MesoNode mesoNode(int i) {
		return (MesoNode) getNode(i);
	}
	public MesoLink mesoLink(int i) {
		return (MesoLink) getLink(i);
	}
	public MesoSegment mesoSegment(int i) {
		return (MesoSegment) getSegment(i);
	}
	public MesoVehicle createVehicle(){
		MesoVehicle newVehicle = this.recycleVhcList.recycle();
		this.vhcCounter++;
		return newVehicle;
	}
	public Node createNode(long id, int type, String name,GeoPoint posPoint){
		MesoNode newNode = new MesoNode();
		newNode.init(id, type, nNodes(),name,posPoint);
		worldSpace.recordExtremePoints(newNode.getPosPoint());
		this.nodes.add(newNode);
		this.addVertex(newNode);
		return newNode;
	}

	public Link createLink(long id, int type, String name,long upNodeId, long dnNodeId){
		MesoLink newLink = new MesoLink();
		newLink.init(id,type,name,nLinks(),findNode(upNodeId),findNode(dnNodeId));
		newLink.setNetwork(this);
		links.add(newLink);
		this.addEdge(newLink.getUpNode(),newLink.getDnNode(),newLink);
		this.setEdgeWeight(newLink,Double.POSITIVE_INFINITY);
		return newLink;
	}

	public Segment createSegment(long id, int speedLimit, double freeSpeed, double grd, List<GeoPoint> ctrlPoints){
		MesoSegment newSegment = new MesoSegment();
		newSegment.init(id,speedLimit,nSegments(),freeSpeed,grd,ctrlPoints,links.get(nLinks()-1));
		worldSpace.recordExtremePoints(ctrlPoints);
		segments.add(newSegment);
		return newSegment;
	}

	public Lane createLane(long id, int rule,int orderNum, double width ,String direction, List<GeoPoint> ctrlPoints ){
		MesoLane newLane = new MesoLane();
		newLane.init(id,rule,nLanes(),orderNum,width,direction,ctrlPoints,segments.get(nSegments()-1));
		// TODO 暂无车道坐标和车道边界关联
		/*
		worldSpace.recordExtremePoints(newLane.getStartPnt());
		worldSpace.recordExtremePoints(newLane.getEndPnt());*/
		lanes.add(newLane);
		return newLane;
	}

	public Sensor createSensor(long id, int type, String detName, long segId, double pos, double zone, double interval ){
		SurvStation newSurvStt = new SurvStation();
		MesoSegment mesoSegment = (MesoSegment) findSegment(segId);
		newSurvStt.init(id,type, nSensors(),findSegment(segId),pos,zone,interval);
		sensors.add(newSurvStt);
		return newSurvStt;
	}


	public MesoVehicle createVehicle(int id, int type, double length, double dis, double speed){
		MesoVehicle newVehicle = this.recycleVhcList.recycle();
		newVehicle.setPath(vhcTable.getPath());
		newVehicle.init(id,type,length,dis,speed);
		newVehicle.initialize(getSimParameter(),mesoRandom[MesoRandom.Departure]);
		this.vhcCounter++;
		return newVehicle;
	}
	public MesoODCell createODCell(int ori, int des, double rate, double var, double r){
		double emitHeadway;
		double emitNextTime;
		MesoNode o = (MesoNode)findNode(ori);
		MesoNode d = (MesoNode)findNode(des);
		// TODO 处理异常的OD对
		if (o == null) {
		}
		else if (d == null) {
		}
		else if (d.getDestIndex() == -1) {

		}
		MesoODCell newODCell = new MesoODCell(o,d);
		// Departure rate, assume a normal distribution
		// OD流量倍率
		rate *= odTable.scale();
		// 方差足够大，按正态分布扰动OD流量
		if (var > 1.0E-4) {
			var *= odTable.scale();
			rate = mesoRandom[MesoRandom.Departure].nrandom(rate, var);
		}
		// OD流量足够大
		if (rate >= Constants.RATE_EPSILON) {
			emitHeadway = 3600.0 / rate;
			emitNextTime = simClock.getCurrentTime()
					- Math.log(mesoRandom[MesoRandom.Departure].urandom()) * emitHeadway;

		}
		else {
			emitHeadway = Constants.DBL_INF;
			emitNextTime = Constants.DBL_INF;
		}

		newODCell.init(odTable.getCells().size(), odTable.getType() ,emitHeadway,emitNextTime,r);
		// ODCell初始化同时分配路径
		newODCell.addPath(createPathFromGraph(o,d));
		odTable.insert(newODCell);
		return newODCell;
	}
	public void resetRandSeeds(){
		for(int i=0;i<mesoRandom.length;i++){
			mesoRandom[i].resetSeed();
		}
	}

	public void calcStaticInfo() {
		super.calcStaticInfo();
		// 初始化子路段通行能力
		for(int i=0;i<nSegments();i++){
			((MesoSegment)segments.get(i)).calcStaticInfo(simClock.getCurrentTime());
		}
		organize();
	}
	// 检测器工作
	public void detMesure(){
		for(Sensor sensor: sensors){
			sensor.aggregate(simClock.getCurrentTime());
		}
	}
	public void setDetStartTime(double startTime){
		for(Sensor sensor:sensors){
			((SurvStation)sensor).setSDetTime(startTime);
		}
	}
	public void updateSegFreeSpeed(){
		MesoSegment mesosegment;
		for(Segment segment:segments){
			mesosegment = (MesoSegment) segment;
			mesosegment.updateFreeSpeed();
		}
		
	}

	//更新速密函数参数 capacity, minspeed, freespeed, kjam, alpha, beta
	public void updateParaSdfn(int segmentId,double[] params){
		MesoSegment tmpSegment = (MesoSegment)findSegment(segmentId);
		//更新参数
		tmpSegment.sdFunction.updateParams(params);
		//更新capacity
		tmpSegment.setCapacity(tmpSegment.defaultCapacity(),simClock.getCurrentTime());
	}

	public void organize() {
		for (Link link:links) {
			((MesoLink) link).checkConnectivity();
		}
	}

	public void calcSegmentData() {
		MesoSegment ps = new MesoSegment();
		for (Segment segment:segments) {
			ps = (MesoSegment) segment;
			ps.calcDensity();
			ps.calcSpeed();
		}
	}
	public void calcSegmentInfo() {
		MesoSegment ps;
		for (Segment segment:segments) {
			ps = (MesoSegment) segment;
			ps.calcState();
		}
	}
	/*
	 * --------------------------------------------------------------------
	 * Enter vehicles queued at starting link into the network.
	 * --------------------------------------------------------------------
	 */
	public void enterVehiclesIntoNetwork() {
		MesoVehicle pv;
		for (Link link:links) {
			/*
			 * Find the first vehicle in the queue and enter it into the network
			 * if space is available. If the link is full, there is no need for
			 * checking other vehicles in the queue, skip to the next link.
			 */

			while ((pv = ((MesoLink) link).queueHead()) != null) {
				// pv.enterNetwork()
				if (pv.getNextMesoLink().isJammed() != 0)
					break;
				// Delete this vehicle from the link queue.
				pv.getNextMesoLink().dequeue(pv);
				// getNextMesoLink().append(pv);
				// Add a vehicle at the upstream end of the link.
				MesoSegment ps = (MesoSegment) pv.getNextMesoLink().getStartSegment();
				appendVhc2Sgmt(ps, pv);
				pv.onRouteChoosePath(pv.getNextMesoLink().getDnNode(),this);
				pv.updateSpeed(getSimParameter().minSpeed(),getSimParameter().minHeadwayGap());
				vhcCounter ++ ;
				/* push static vehicle attributes on message buffer */
			}
		}
	}
	public void appendVhc2Sgmt(MesoSegment ps, MesoVehicle pv){
		// ps.append(vehicle);
		// Append a vehicle at the end of the segment
		if (ps.lastCell == null ||
			!ps.lastCell.isReachable(simClock.getStepSize(),getSimParameter().cellSplitGap)) {
			// isReachable T：cell尾车距segment末端的距离<车团分离阈值
			// F: cell尾车距segment末端的距离>=车团分离阈值
			// 若segment存在cell，新车与原lastcell距离大于分离阈值，则新生成lastcell
			ps.append(recycleCellList.recycle());
			ps.lastCell.initialize(simClock.getStepSize());
		}
		MesoTrafficCell lastCellInSgmt = ps.lastCell;
		// Put the vehicle in the traffic cell
		// Append a vehicle at the end of the cell

		pv.leading = lastCellInSgmt.lastVehicle;
		pv.trailing = null;

		if (lastCellInSgmt.lastVehicle != null) { // append at end
			lastCellInSgmt.lastVehicle.trailing = pv;
		}
		else { // queue is empty
			lastCellInSgmt.firstVehicle = pv;
		}
		lastCellInSgmt.lastVehicle = pv;
		lastCellInSgmt.nVehicles++;

		pv.appendTo(lastCellInSgmt,stepCounter,getSimParameter().minHeadwayGap() );

		if (lastCellInSgmt.nVehicles <= 1) { // first vehicle
			lastCellInSgmt.updateTailSpeed(simClock,(MesoParameter)simParameter);
			lastCellInSgmt.updateHeadSpeeds(simClock,(MesoParameter)simParameter);
		}
	}
	/*
	 * ------------------------------------------------------------------- Add
	 * number of vehicles allowed to move out during this time step to the
	 * segment balance.
	 * -------------------------------------------------------------------
	 */
	public void resetSegmentEmitTime() {
		MesoSegment ps;
		for (Segment segment:segments) {
			ps = (MesoSegment) segment;
			ps.resetEmitTime(simClock.getCurrentTime());
		}
	}

	public void guidedVehiclesUpdatePaths() {
		MesoTrafficCell cell;
		MesoVehicle pv;
		int i;

		// Vehicles already in the network

		for (Segment segment:segments) {
			cell = ((MesoSegment) segment).firstCell();
			while (cell != null) {
				pv = cell.firstVehicle();
				while (pv != null) {
					if (pv.isGuided() != 0) {
						pv.changeRoute(this);
					}
					pv = pv.trailing();
				}
				cell = cell.trailing();
			}
		}

		// Vehicles waiting for entering the network

		for (Link link:links) {
			pv = ((MesoLink)link).queueHead();
			while (pv != null) {
				if (pv.isGuided() != 0) {
					pv.changeRoute(this);
				}
				pv = pv.trailing();
			}
		}
	}

	// Calculate capacity of the nodes
	/*
	public void updateNodeCapacities() {
		for (Node node:nodes) {
			((MesoNode)node).calcCapacities();
		}
	}*/

	// Update phase
	/*
	 * -------------------------------------------------------------------- For
	 * each traffic cells in the network, calculate its density and upSpeed.
	 * These two variables depend only on the state of a particular traffic cell
	 * itself.
	 * --------------------------------------------------------------------
	 */
	public void calcTrafficCellUpSpeed() {
		MesoSegment ps;
		for (Link link:links) {
			ps = (MesoSegment) link.getEndSegment();
			while (ps != null) {
				// Calculate density and upSpeed for each traffic cell
				MesoTrafficCell cell = ps.firstCell;
				while (cell != null) {
					cell.updateTailSpeed(simClock,getSimParameter());
					cell = cell.trailing();
				}
				ps = ps.getUpSegment();
			}
		}
	}
	/*
	 * -------------------------------------------------------------------- For
	 * each traffic cells in the network, calculate its dnSpeed. This variable
	 * variable depends on its own state and the states of the traffic cells
	 * ahead. This function is called after calcIndependentTrafficCellStates()
	 * is called.
	 * --------------------------------------------------------------------
	 */
	public void calcTrafficCellDnSpeeds() {
		MesoSegment ps;
		for (Link link:links) {
			ps = (MesoSegment) link.getEndSegment();
			while (ps != null) { // downstream first
				MesoTrafficCell cell = ps.firstCell;
				while (cell != null) {
					cell.updateHeadSpeeds(simClock,getSimParameter());
					cell = cell.trailing();
				}
				ps = ps.getUpSegment();
			}
		}
	}

	// Advance phase

	public void advanceVehicles() {
		permuteLink = null;
		nPermutedLinks = 0;
		int i;

		if (nPermutedLinks != links.size()) { // this piece is executed only once
			if (permuteLink != null) {
				// 未处理 delete [] permuteLink;
			}
			nPermutedLinks = links.size();
			permuteLink = new int[nPermutedLinks];
			for (i = 0; i < nPermutedLinks; i++) {
				permuteLink[i] = i;
			}
		}

		// Randomize the link order

		mesoRandom[MesoRandom.Misc].permute(links.size(), permuteLink);

		for (i = 0; i < links.size(); i++) {
			MesoLink itrLink = mesoLink(permuteLink[i]);
			MesoSegment itrSegment = (MesoSegment) itrLink.getEndSegment();
			MesoTrafficCell itrCell;
			MesoVehicle itrVehicle,tmpVehicle;
			while (itrSegment != null) {
				// ps.advanceVehicles();
				itrCell = itrSegment.firstCell;
				while (itrCell != null) {
					//cell.advanceVehicles();
					itrVehicle = itrCell.firstVehicle;
					while (itrVehicle != null) {
						// 保存更新位置前vehicle的后车引用
						tmpVehicle = itrVehicle.trailing;
						if (itrVehicle.isProcessed(stepCounter) == 0) {
							itrVehicle.move(this);

						}
						// 回收驶出路网的车辆
						if(itrVehicle.needRecycle == true){
							this.recycleVhcList.recycle(itrVehicle);
							vhcCounter --;
						}
						itrVehicle = tmpVehicle;
					}
					itrCell = itrCell.trailing;
				}
				formatTrafficCells(itrSegment);
				itrSegment = itrSegment.getUpSegment();
			}

		}
	}

	// Remove, merge, and split cells

	public void formatTrafficCells(MesoSegment mesoSegment) {
		// Remove, merge and split traffic cells
		MesoTrafficCell cell;
		MesoTrafficCell front;

		// Remove the empty traffic cells
		cell = mesoSegment.firstCell;
		while ((front = cell) != null) {
			cell = cell.trailing;
			if (front.nVehicles() <= 0 || front.firstVehicle() == null || front.lastVehicle() == null) { // no
				// vehicle
				// left

				// use vehicle count should be enough but it seems that there
				// is a bug somewhere that causes vehicle count to be 1 when
				// actually there is no vehicle left.

				mesoSegment.remove(front);
				this.recycleCellList.recycle(front);
			}
		}

	}

	// Remove all vehicles in the network, including those in
	// pretrip queues

	public void clean() {

		// Release current cells
		MesoLink itrLink;
		MesoSegment itrSegment;
		for (Link link:links) {
			//((MesoLink)link).clean();
			// Remove vehicles in pretrip queue
			itrLink = (MesoLink)link;
			while (itrLink.queueHead != null) {
				itrLink.queueTail = itrLink.queueHead;
				itrLink.queueHead = itrLink.queueHead.trailing;
				this.recycleVhcList.recycle(itrLink.queueTail);
			}
			itrLink.queueTail = null;
			itrLink.queueLength_ = 0;

			// Remove vehicles in each segment

			itrSegment = (MesoSegment) itrLink.getStartSegment();
			while (itrSegment != null) {
			//	((MesoSegment) ps).clean();
				// remove all traffic cells
				while (itrSegment.firstCell != null) {
					itrSegment.lastCell = itrSegment.firstCell;
					itrSegment.firstCell = itrSegment.firstCell.trailing;
					this.recycleCellList.recycle(itrSegment.lastCell);
				}
				itrSegment.lastCell = null;
				itrSegment.nCells = 0;
				itrSegment = (MesoSegment)itrSegment.getDnSegment();
			}
		}


		// Restore capacities
		for (Segment segment:segments) {
			itrSegment = ((MesoSegment)segment);
			itrSegment.setCapacity(itrSegment.defaultCapacity(),simClock.getCurrentTime());
		}
		//清除检测数据
		for(Sensor itrSensor:sensors){
			itrSensor.clean();
		}
	}


	public void recordLinkTravelTimeOfActiveVehicle() {
		MesoTrafficCell cell;
		MesoVehicle pv;
		int i;

		// Record travel time for vehicle still in the network

		for (Segment segment:segments) {
			cell = ((MesoSegment)segment).firstCell();
			while (cell != null) {
				pv = cell.firstVehicle();
				while (pv != null) {
					pv.getLink().recordExpectedTravelTime(pv,linkTimes);
					pv = pv.trailing();
				}
				cell = cell.trailing();
			}
		}

		// Record travel time for vehicle in ths spill back queues

		for (Link link:links) {
			pv = ((MesoLink)link).queueHead();
			while (pv != null) {
				pv.getNextLink().recordExpectedTravelTime(pv,linkTimes);
				pv = pv.trailing();
			}
		}
	}

	public void recordVehicleData(){
		MesoSegment ps;
		MesoTrafficCell tc;
		MesoVehicle vhc;
		VehicleData vd;
		AnimationFrame af;
		//从对象池中获取frame对象
		if(vhcCounter>0){
			af = new AnimationFrame();
			ListIterator<Segment> i = segments.listIterator();
			//遍历segment
			while (i.hasNext()) {
				ps = (MesoSegment) i.next();
				tc = ps.firstCell();
				//遍历cell

				while (tc != null) {
					vhc = tc.firstVehicle();
					//遍历vehicle
					while (vhc != null) {
						//从对象池获取vehicledata对象
						vd = VehicleDataPool.getInstance().newData();
						//记录车辆信息
						vd.init(vhc,true,0,null);
						//将vehicledata插入frame
						af.addVehicleData(vd);
						//下一辆车
						vhc = vhc.trailing();
					}
					//下一个车队
					tc = tc.trailing();
				}
			}
			try {
				FrameQueue.getInstance().offer(af);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
