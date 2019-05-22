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


import com.transyslab.commons.renderer.AnimationFrame;
import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.roadnetwork.*;
import org.apache.commons.csv.CSVRecord;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;



public class RTNetwork extends RoadNetwork{

	private List<RTVehicle> vhcList;
	private LinkedList<RTVehicle> vhcPool;

	public RTNetwork() {
		simParameter = new RTParameter();//需要在RoadNetwork子类初始化
		vhcList = new ArrayList<>();
		vhcPool = new LinkedList<>();
	}

	@Override
	public Node createNode(long id, int type, String name, GeoPoint posPoint) {
		RTNode newNode = new RTNode();
		newNode.init(id, type, nNodes() ,name,posPoint);
		this.nodes.add(newNode);
		this.addVertex(newNode);
		return newNode;
	}

	@Override
	public Link createLink(long id, int type, String name ,long upNodeId, long dnNodeId) {
		RTLink newLink = new RTLink();
		newLink.init(id,type,name,nLinks(),findNode(upNodeId),findNode(dnNodeId));
		links.add(newLink);
		this.addEdge(newLink.getUpNode(),newLink.getDnNode(),newLink);
		this.setEdgeWeight(newLink,Double.POSITIVE_INFINITY);
		return newLink;
	}

	@Override
	public Segment createSegment(long id, int speedLimit, double freeSpeed, double grd, List<GeoPoint> ctrlPoints) {
		RTSegment newSegment = new RTSegment();
		newSegment.init(id,speedLimit,nSegments(),freeSpeed,grd,ctrlPoints,links.get(nLinks()-1));
		worldSpace.recordExtremePoints(ctrlPoints);
		segments.add(newSegment);
		return newSegment;
	}

	@Override
	public Lane createLane(long id, int rule,int orderNum, double width ,String direction,List<GeoPoint> ctrlPoints) {
		RTLane newLane = new RTLane();
		newLane.init(id,rule,nLanes(),orderNum,width,direction,ctrlPoints,segments.get(nSegments()-1));
		worldSpace.recordExtremePoints(ctrlPoints);
		lanes.add(newLane);
		return newLane;
	}

	@Override
	public Sensor createSensor(long id, int type, String detName, long segId, double pos, double zone, double interval) {
		RTSegment seg = (RTSegment) findSegment(segId);
		RTLink lnk = (RTLink) seg.getLink();
		/* TODO 初始化检测器
		double dsp = seg.startDSP + seg.getLength()*pos;
		for (int i = 0; i < seg.nLanes(); i++) {
			RTLane ln = (RTLane)seg.getLane(i);
			RTLoop loop = new RTLoop(ln, seg, lnk, detName, dsp, pos);
			sensors.add(loop);
		}*/
		return null;
	}

	public void calcStaticInfo() {
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
		organize();
	}

	public void organize() {
		//补充车道编号的信息


		for (Segment seg: segments){
			// TODO 线性参考上移至父类
			Segment tmpseg = seg;
			double startDSP = 0;
			while (tmpseg.getUpSegment() != null) {
				tmpseg = tmpseg.getUpSegment();
				startDSP += tmpseg.getLength();
			}
			((RTSegment) seg).setStartDSP(startDSP);
			double endDSP = ((RTSegment) seg).getStartDSP() + seg.getLength();
			((RTSegment) seg).setEndDSP(endDSP);
		}


		for (Link l: links){
			//预留
			((RTLink) l).checkConnectivity();
			//组织laneGraph
			segments.forEach(segment -> {
				lanes.forEach(lane -> {
					((RTLink) l).addLaneGraphVertex((RTLane) lane);
				});
			});


		}
	}

	public void resetNetwork(long seed) {
		sysRand.setSeed(seed);//重置系统种子
	}
	public void generateVehicle(int id, int laneId, double speed, double distance){
		RTVehicle newVehicle = vhcPool.poll();
		if(newVehicle == null)
			newVehicle = new RTVehicle();
		newVehicle.init(id,(RTLane) findLane(laneId),speed,distance);
		this.vhcList.add(newVehicle);
	}
	public void removeVehicle(RTVehicle vehicle){
		vhcList.remove(vehicle);
		vhcPool.offer(vehicle);
	}
	public void renderState(List<VehicleData> vds,double secondOfDay){

		AnimationFrame af = new AnimationFrame();
		List<VehicleData> queueVehicles = new ArrayList<>();
		List<VehicleData> movingVehicles = new ArrayList<>();
		// 按车道id分组
		for(VehicleData vd:vds){
			if(vd.isQueue()) {
				queueVehicles.add(vd);
				af.addVehicleData(vd);// 渲染车辆
			}
			else
				movingVehicles.add(vd);
		}

		Map<Long,List<VehicleData>> qvdsByLane = queueVehicles.stream().collect(groupingBy(VehicleData::getCurLaneID));
		Map<Long,List<VehicleData>> mvdsByLane = movingVehicles.stream().collect(groupingBy(VehicleData::getCurLaneID));
		for(int i=0;i<nLanes();i++){
			RTLane rtLane = (RTLane) getLane(i);
			long key = rtLane.getId();
			rtLane.calcState(qvdsByLane.get(key),mvdsByLane.get(key));
			StateData sd = new StateData(rtLane);
			af.addStateData(sd);
		}
		setArrowColor(secondOfDay, af.getSignalColors());
		af.setSimTimeInSeconds(secondOfDay);
		try {
			FrameQueue.getInstance().offer(af);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void recordVehicleData(){
		VehicleData vd;
		AnimationFrame af;
		if (!vhcList.isEmpty()) {
			af = new AnimationFrame();
			//遍历vehicle
			for (RTVehicle v : vhcList) {
				//从对象池获取vehicledata对象
				vd = VehicleDataPool.getInstance().newData();
				//记录车辆信息
				vd.init(v,
						false,
						1,
						//String.valueOf(v.getNextLink()==null ? "NA" : v.lane.successiveDnLanes.get(0).getLink().getId()==v.getNextLink().getId())
						v.toString());
				//将vehicledata插入frame
				af.addVehicleData(vd);

			}
			//添加额外信息(帧号)
			/*
			int count = 0;
			for(int i = 0; i< nSensors(); i++){
				RTLoop tmpSensor = (RTLoop) getSensor(i);
				count = count + tmpSensor.getRecords().size();
			}
			af.setInfo("Count",count);*/
			try {
				FrameQueue.getInstance().offer(af);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public void renderVehicle(List<VehicleData> extVd, double secondOfDay){
		AnimationFrame af = new AnimationFrame();
		for(VehicleData vd:extVd){
			af.addVehicleData(vd);
		}
		setArrowColor(secondOfDay, af.getSignalColors());
		af.setSimTimeInSeconds(secondOfDay);
		try {
			FrameQueue.getInstance().offer(af);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
