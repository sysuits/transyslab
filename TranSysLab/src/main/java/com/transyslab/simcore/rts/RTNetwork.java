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
		simParameter = new RTParameter();//��Ҫ��RoadNetwork�����ʼ��
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
		/* TODO ��ʼ�������
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
		// Lane��������ҽ�����Segment��������������ν���
		for(Link itrLink:links){
			// �������ι�ϵ����·�ε�����
			List<Segment> sgmtsInLink = itrLink.getSegments();
			int nSegment = sgmtsInLink.size();
			for(int i=0;i<nSegment;i++){
				Segment itrSgmt = sgmtsInLink.get(i);
				if(i!=0)//��ʼ·��û������
					itrSgmt.setUpSegment(sgmtsInLink.get(i-1));
				if(i!=nSegment-1)//ĩ��·��û������
					itrSgmt.setDnSegment(sgmtsInLink.get(i+1));
				// �������ϵ�������ڳ���������
				List<Lane> lanesInSgmt = itrSgmt.getLanes();
				int nLanes = lanesInSgmt.size();
				for(int j=0;j<nLanes;j++){
					if(j!=0)// ����೵��
						lanesInSgmt.get(j).setLeftLane(lanesInSgmt.get(j-1));
					if(j!=nLanes-1)// ���Ҳ೵��
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

		// Boundary λ��ƽ��
		for (Boundary itrBoundary:boundaries) {
			itrBoundary.translateInWorldSpace(worldSpace);
		}

		// Sort outgoing and incoming arcs at each node.
		// Make sure RN_Link::comp() is based on angle.

		for (Node itrNode:nodes) {
			itrNode.sortUpLinks();
			itrNode.sortDnLinks();
			// ����ƽ��
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
		// ��������ƽ�Ʋ���

		for (Lane itrLane:lanes) {
			itrLane.calcStaticInfo(this.worldSpace);
		}
		// Surface λ��ƽ��
		for (GeoSurface surface:surfaces) {
			surface.translateInWorldSpace(worldSpace);
		}
		// Connector λ��ƽ��
		for (Connector connector:connectors) {
			connector.translateInWorldSpace(worldSpace);
		}
		organize();
	}

	public void organize() {
		//���䳵����ŵ���Ϣ


		for (Segment seg: segments){
			// TODO ���Բο�����������
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
			//Ԥ��
			((RTLink) l).checkConnectivity();
			//��֯laneGraph
			segments.forEach(segment -> {
				lanes.forEach(lane -> {
					((RTLink) l).addLaneGraphVertex((RTLane) lane);
				});
			});


		}
	}

	public void resetNetwork(long seed) {
		sysRand.setSeed(seed);//����ϵͳ����
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
		// ������id����
		for(VehicleData vd:vds){
			if(vd.isQueue()) {
				queueVehicles.add(vd);
				af.addVehicleData(vd);// ��Ⱦ����
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
			//����vehicle
			for (RTVehicle v : vhcList) {
				//�Ӷ���ػ�ȡvehicledata����
				vd = VehicleDataPool.getInstance().newData();
				//��¼������Ϣ
				vd.init(v,
						false,
						1,
						//String.valueOf(v.getNextLink()==null ? "NA" : v.lane.successiveDnLanes.get(0).getLink().getId()==v.getNextLink().getId())
						v.toString());
				//��vehicledata����frame
				af.addVehicleData(vd);

			}
			//��Ӷ�����Ϣ(֡��)
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
