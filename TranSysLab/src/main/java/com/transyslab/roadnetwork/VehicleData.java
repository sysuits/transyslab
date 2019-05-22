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

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.simcore.mlp.MLPLane;
import com.transyslab.simcore.mlp.MLPVehicle;
import org.apache.commons.math3.genetics.Fitness;

//�����켣����
public class VehicleData implements NetworkObject,Comparable<VehicleData>{
	//����id
	protected int vehicleID_;
	protected String vhcID;
	//��������
	protected int vehicleType_;
	//����������Ⱦ�ı��,0:�ޱ�ǣ�1:MLPģ�����⳵
	protected int specialFlag_;
	//��������
	protected double vehicleLength_;
	//������segment��lane������λ��
	protected GeoPoint headPosition;
	//������״
	protected GeoSurface rectangle;
	//������Ϣ
	protected String info;
	protected String pathInfo;
	protected long oriNodeID;
	protected long desNodeID;
	protected double curSpeed;
	protected long curLaneID;
	protected boolean isSelected;
	protected double distance;
	protected int tarLaneID;// TODO ffff
	protected String turn;
	protected boolean queueFlag;
	public boolean isQueue(){
		return this.queueFlag;
	}
	public int getTarLaneID(){
		return this.tarLaneID;
	}
	public int getVehicleID(){
		return vehicleID_;
	}
	public String getTurnInfo(){
		return this.turn;
	}
	public int getVehicleType(){
		return vehicleType_;
	}
	public double getVhcLength(){
		return vehicleLength_;
	}
	public double getVhcLocationX(){
		return headPosition.getLocationX();
	}
	public double getVhcLocationY(){
		return headPosition.getLocationY();
	}
	public double getCurSpeed(){
		return curSpeed;
	}
	public long getOriNodeID(){
		return oriNodeID;
	}
	public long getDesNodeID(){
		return desNodeID;
	}
	public long getCurLaneID(){
		return curLaneID;
	}
	public String getPathInfo(){
		return pathInfo;
	}
	public void setPathInfo(Path path){
		StringBuilder sb = new StringBuilder();
		for (Link l : path.getLinks()) {
			sb.append("��" + l.getId());
		}
		pathInfo = sb.toString().substring(1);
	}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public long getId(){
		return Long.parseLong(this.vhcID);
	}
	public String getObjInfo(){
		return this.info;
	}
	public int getSpecialFlag(){
		return specialFlag_;
	}
	public GeoSurface getVhcShape(){
		return this.rectangle;
	}
	public String getVhcInfo(){
		return this.info;
	}
	public double getDistance(){
		return this.distance;
	}
	public void init(Vehicle vhc, boolean isSegBased, int specialflag, String info){
		this.vehicleID_ = vhc.id;
		this.vehicleType_ = vhc.getType();
		this.vehicleLength_ = vhc.getLength();
		this.specialFlag_ = specialflag;
		this.info = info;
		this.curSpeed = vhc.getCurrentSpeed();
		this.oriNodeID = vhc.oriNode().getId();
		this.desNodeID = vhc.desNode().getId();
		/*
		StringBuilder sb = new StringBuilder();
		int nLinks = vhc.path.getLinks().size();
		for(int i =0;i<vhc.path.getLinks().size();i++){
			sb.append(vhc.path.getLink(i).getId());
			if(i!=nLinks-1){
				sb.append("->");
			}
		}
		this.pathInfo = sb.toString();

		double simLength = vhc.getLane().getLength();//��������е��г�
		double rate = 1.0 - vhc.getDistance() / simLength;// linear reference
		GeoPoint startPnt=null, endPnt=null;
		double projectSegLen;
		if (vhc instanceof MLPVehicle) {
			MLPLane successiveDnLane = ((MLPLane)vhc.getLane()).successiveDnLaneInLink(((MLPVehicle) vhc).getLink());
			double l1 = vhc.getLane().getStartPnt().distance(vhc.getLane().getEndPnt());//mlpģ�ͣ���ʵlane���ȴӼ�����Ϣ��á�
			if (successiveDnLane!=null) {
				double l2 = successiveDnLane.getStartPnt().distance(vhc.getLane().getEndPnt());
				projectSegLen = l1 + l2;//�ǽ�������Σ�ͶӰ��lane + laneConnector
				if (rate > l1/projectSegLen) {//project to lane connector
					startPnt = vhc.getLane().getEndPnt();
					endPnt = successiveDnLane.getStartPnt();
					rate = (rate*projectSegLen - l1) / l2;
				}
			}
			else
				projectSegLen = l1;//���������·�Σ�ͶӰ��lane
		}
		else
			projectSegLen = vhc.getLane().getLength(); // ��mlpģ�ͣ�ͶӰ���ȴ�getLength()��á�
		if (startPnt == null && endPnt == null) {//project to lane
			startPnt = vhc.getLane().getStartPnt();
			endPnt = vhc.getLane().getEndPnt();
		}

		//��ͷλ��
		double vhcHeadX = startPnt.getLocationX() + rate * (endPnt.getLocationX() - startPnt.getLocationX());
		double vhcHeadY = startPnt.getLocationY() + rate * (endPnt.getLocationY() - startPnt.getLocationY());
		//��βλ��
		double scaledLength = vhc.getLength() * projectSegLen / simLength;
		double vhcTailX = vhcHeadX - scaledLength * (endPnt.getLocationX() - startPnt.getLocationX()) / (endPnt.distance(startPnt));
		double vhcTailY = vhcHeadY - scaledLength * (endPnt.getLocationY() - startPnt.getLocationY()) / (endPnt.distance(startPnt));
		this.headPosition = new GeoPoint(vhcHeadX, vhcHeadY, 0.0);
		GeoPoint tailPosition = new GeoPoint(vhcTailX, vhcTailY, 0.0);
		this.rectangle = GeoUtil.lineToRectangle(tailPosition, headPosition, 1.8, true);*/
	}
	// distReverse ��ʻ�����Ƿ���·�ο�����Ϊ���
	public void calcShapePoint(Object moveOn, double distance, boolean distReverse){
		double l, width;
		GeoPoint startPnt, endPnt;
		// ��������չ
		boolean bothSize;
		// TODO ������ʼ��������
		this.distance = distance;

		if(moveOn instanceof Segment){
			startPnt = null;
			endPnt = null;
			width = 0;
			bothSize = false;
			l = 0.1;
			/*
			Segment seg = (Segment) moveOn;
			l = seg.getLength();
			startPnt = seg.getStartPnt();
			endPnt = seg.getEndPnt();
			//��������ѹ��
			vehicleLength_ = vehicleLength_/seg.nLanes();
			//����
			width = seg.nLanes()*Constants.LANE_WIDTH;
			//
			bothSize = false;*/

		}
		else if(moveOn instanceof Lane){

			Lane lane = (Lane) moveOn;
			if(!distReverse)
				distance = lane.getGeoLength()-distance;

			this.curLaneID = lane.getId();

			double[] linearDistance = lane.getLinearRelation();

			int index = FitnessFunction.binarySearchIndex(linearDistance,distance);
			if(index == linearDistance.length)
				System.out.println("Error: wrong distance on connector");
			distance = distance - linearDistance[index-1];

			// ���߳���
			l = linearDistance[index] - linearDistance[index-1];
			// ͶӰ����Ӧ�����߶���
			startPnt = lane.getCtrlPoints().get(index-1);
			endPnt = lane.getCtrlPoints().get(index);

			width = Constants.DEFAULT_VEHICLE_WIDTH;
			bothSize = true;
		}
		else if(moveOn instanceof Connector){
			Connector connector = (Connector) moveOn;
			this.curLaneID = connector.getId();
			double[] linearDistance = connector.getLinearRelation();

			int index = FitnessFunction.binarySearchIndex(linearDistance,distance);
			if(index == linearDistance.length)
				System.err.println("Error: wrong distance on connector");
			distance = distance - linearDistance[index-1];

			// ���߳���
			l = linearDistance[index] - linearDistance[index-1];
			// ͶӰ����Ӧ�����߶���
			startPnt = connector.getShapePoints().get(index-1);
			endPnt = connector.getShapePoints().get(index);
			width = Constants.DEFAULT_VEHICLE_WIDTH;
			bothSize = true;
		}
		else{
			System.out.println("Error: Unknown class");
			return;
		}
		double s = distance;
		//��ͷλ��
		double vhcHeadX = startPnt.getLocationX() + s * (endPnt.getLocationX() - startPnt.getLocationX()) / l;
		double vhcHeadY = startPnt.getLocationY() + s * (endPnt.getLocationY() - startPnt.getLocationY()) / l;
		s = s - vehicleLength_;
		//��βλ��
		double vhcTrailX = startPnt.getLocationX() + s * (endPnt.getLocationX() - startPnt.getLocationX()) / l;
		double vhcTrailY = startPnt.getLocationY() + s * (endPnt.getLocationY() - startPnt.getLocationY()) / l;
		this.headPosition = new GeoPoint(vhcHeadX, vhcHeadY, 0.0);
		GeoPoint trailPosition = new GeoPoint(vhcTrailX, vhcTrailY, 0.0);
		// ע�⣺�������Ƶ��
		this.rectangle = GeoUtil.lineToRectangle(trailPosition, headPosition, width,bothSize);
	}
	public void init(String id, Object moveOn, double vhcLength, double distance, double speed,String turn, boolean queueFlag, boolean distReverse){
		this.vhcID = id;
		this.turn = turn;
		this.vehicleLength_ = vhcLength;
		this.curSpeed = speed;
		this.queueFlag = queueFlag;
//		this.info = this.vhcID + "\n"+String.valueOf(curSpeed)+ "\n" + String.valueOf(queueFlag) + "\n"+this.distance;
		if(moveOn == null) {
			System.out.println("Error: Could not find the Lane");
			return;
		}
		calcShapePoint(moveOn,distance,distReverse);
	}
	public void clean(){
		vehicleID_ = vehicleType_ = 0;
		vehicleLength_ = 0;
		queueFlag = true;
	}
	//wym
	public GeoPoint getHeadPosition(){
		return headPosition;
	}

	@Override
	public int compareTo(VehicleData vd) {
		if(this.distance > vd.distance)
			return 1;
		else if(this.distance < vd.distance)
			return -1;
		else
			return 0;
	}
}
