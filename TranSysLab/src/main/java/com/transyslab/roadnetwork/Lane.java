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

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.GeoUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Lane
 *
 *
 */
public class Lane implements NetworkObject,Comparable<Lane> {

	protected long id;
	protected String name;
	protected String objInfo;
	// 车道宽度
	public double width ;
	protected int index;
	protected int orderNum;
	protected Segment segment;
	protected int rules;// lane use and change rules
	protected List<Lane> upLanes;
	protected List<Lane> dnLanes;
	protected List<GeoPoint> ctrlPoints;
	protected double[] linearRelation;
	protected double geoLength;
	protected GeoSurface surface;
	protected List<SignalArrow> signalArrows;
	protected int type;
	protected int state;
	protected int cmarker;// connection marker
	protected Lane leftLane;
	protected Lane rightLane;
	protected boolean isSelected;
	protected String direction;

	public Lane() {
		segment = null;
		type = 0;
		state = 0;
		cmarker = 0;
		upLanes = new ArrayList<>();
		dnLanes = new ArrayList<>();
		signalArrows = new ArrayList<>();
		ctrlPoints = new ArrayList<>();
	}
	public long getId(){
		return this.id;
	}
	public String getName(){
		return name;
	}
	public String getObjInfo(){return this.objInfo;}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public int state() {
		return (state & 0xFFFF);
	}
	public void setState(int s) {
		state |= s;
	}
	public void unsetState(int s) {
		state &= ~s;
	}
	public List<SignalArrow> getSignalArrows(){
		return this.signalArrows;
	}
	public Segment getSegment() {
		return segment;
	}
	public double getGeoLength(){
		return this.geoLength;
	}
	public Link getLink() {
		return segment.getLink();
	}
	public int getIndex() {
		return index;
	}
	public void setLeftLane(Lane leftLane){
		this.leftLane = leftLane;
	}
	public void setRightLane(Lane rightLane){
		this.rightLane = rightLane;
	}
	public double[] getLinearRelation() {
		return linearRelation;
	}

	public List<GeoPoint> getCtrlPoints() {
		return ctrlPoints;
	}
	// distance 按从流向起点算起，插入点的线性距离
	public GeoPoint itplAmongCtrlPoints(double distance){

		int index = FitnessFunction.binarySearchIndex(linearRelation,distance);
		if(index == linearRelation.length)
			System.out.println("Error: wrong distance on connector");
		distance = distance - linearRelation[index-1];
		double l = linearRelation[index] - linearRelation[index-1];
		// 投影到对应的折线段上
		GeoPoint startPnt = ctrlPoints.get(index-1);
		GeoPoint endPnt = ctrlPoints.get(index);
		return endPnt.intermediate(startPnt,distance/l);
	}
	/*
            public GeoPoint getEndPnt(){
                return ctrlPoints.get(ctrlPoints.size() -1);
            }
            public GeoPoint getStartPnt(){
                return ctrlPoints.get(0);
            }*/
	public int linkType() {
		return segment.getType();
	}

	/*
	 * -------------------------------------------------------------------- Set
	 * type based on link type and info on connectivity.
	 * --------------------------------------------------------------------
	 */
	public void setLaneType() {
		// 检查是否为交叉口进口道
		if (getSegment().getId()==getLink().getEndSegment().getId() && (getLink().getDnNode().getType()&Constants.NODE_TYPE_INTERSECTION)!=0) {
			type |= Constants.LANE_TYPE_SIGNAL_ARROW;
		}
		/*
		 * check if this lane is a shoulder lane
		 */
		if (getRightLane() == null)
			type |= Constants.LANE_TYPE_RIGHT_MOST;
		if (getLeftLane() == null)
			type |= Constants.LANE_TYPE_LEFT_MOST;

		int i, j;
		Lane plane;

		/*
		 * check if this lane is connected to an on-ramp at the upstream end
		 */

		for (i = 0; i < nUpLanes(); i++) {
			if ((upLane(i).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
				type |= Constants.LANE_TYPE_UP_ONRAMP;
				break;
			}
		}

		/*
		 * check if this lane shares the same upstream lane with an off-ramp
		 * lane (actually this info is not very useful as other info being
		 * calculated in this function in terms of drive behavior. It is coded
		 * anyway just in case some other algorithm may use it)
		 */

		for (i = 0; i < nUpLanes() && (type & Constants.LANE_TYPE_UP_OFFRAMP) == 0; i++) {
			plane = upLane(i);
			for (j = 0; j < plane.nDnLanes(); j++) {
				if ((plane.dnLane(j).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
					type |= Constants.LANE_TYPE_UP_OFFRAMP;
					break;
				}
			}
		}

		/*
		 * check if this lane is connected to an off-ramp at the downstream end.
		 */

		for (i = 0; i < nDnLanes(); i++) {
			if ((dnLane(i).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
				type |= Constants.LANE_TYPE_DN_OFFRAMP;
				break;
			}
		}

		/*
		 * check if this lane merges into the same downstream lane with an
		 * on-ramp lane.
		 */

		for (i = 0; i < nDnLanes() && ((type & Constants.LANE_TYPE_DN_ONRAMP) == 0); i++) {
			plane = dnLane(i);
			for (j = 0; j < plane.nUpLanes(); j++) {
				if ((plane.upLane(j).linkType() & Constants.LINK_TYPE_RAMP) != 0) {
					type |= Constants.LANE_TYPE_DN_ONRAMP;
					break;
				}
			}
		}

		// Check if this is the last lane

		if (getSegment().getDnSegment() == null && // last segment
				// ('->downstream()' added
				// by Angus)
				getLink().getDnNode().type(Constants.NODE_TYPE_EXTERNAL) != 0 && // external
				// node
				nDnLanes() <= 0) { // no downstream lane
			type |= Constants.LANE_TYPE_BOUNDARY;
		}

		if (laneType(Constants.LANE_TYPE_BOUNDARY) == 0 && // not at the
				// boundary
				nDnLanes() <= 0) { // no downstream lane
			type |= Constants.LANE_TYPE_DROPPED;
		}
	}

	public int laneType(int mask) {
		return (type & mask);
	}
	public int isDropped() {
		return (type & Constants.LANE_TYPE_DROPPED);
	}
	public int isBoundary() {
		return (type & Constants.LANE_TYPE_BOUNDARY);
	}

	public int isEtcLane() {
		return rules & Constants.VEHICLE_ETC;
	}
	public int isHovLane() {
		return rules & Constants.VEHICLE_HOV;
	}
	public int isBusLane() {
		return rules & Constants.VEHICLE_COMMERCIAL;
	}

	public double getLength() {
		return segment.getLength();
	}

	public Lane getRightLane() {
		return rightLane;
	}
	public Lane getLeftLane() {
		return leftLane;
	}


	public int nUpLanes() {
		return upLanes.size();
	}
	public int nDnLanes() {
		return dnLanes.size();
	}

	public Lane upLane(int i) {
		return upLanes.get(i);
	}
	public Lane dnLane(int i) {
		return dnLanes.get(i);
	}

	/*
	 * --------------------------------------------------------------------
	 * Check if a lane is one of the downstream upLanes
	 * --------------------------------------------------------------------
	 */
	public Lane findInUpLane(long c) {
		ListIterator<Lane> i = upLanes.listIterator();
		while (i.hasNext()) {
			Lane tempLane = i.next();
			if (tempLane.id == c) {
				return tempLane;
			}
		}
		return null;
	}

	// Find if a lane is one the downstream upLanes
	public Lane findInDnLane(long c) {
		ListIterator<Lane> i = dnLanes.listIterator();
		while (i.hasNext()) {
			Lane tempLane = i.next();
			if (tempLane.id == c) {
				return tempLane;
			}
		}
		return null;
	}

	public boolean isInDnLanes(Lane plane) {
		for (int i = 0; i < nDnLanes(); i++) {
			if (dnLane(i).id == plane.id)
				return true;
		}
		return false;
	}
	public boolean isInUpLanes(Lane plane) {
		for (int i = 0; i < nUpLanes(); i++) {
			if (upLane(i).id == plane.id)
				return true;
		}
		return false;
	}

	public int rules() {
		return rules;
	}
	public void rulesExclude(int exclude) {
		rules &= ~exclude;
	}


	public void init(int id, int r, int index, double beginx, double beginy, double endx, double endy, Segment seg) {

		//startPnt =new GeoPoint(beginx,beginy);
		//endPnt =new GeoPoint(endx,endy);
		//geoLength = startPnt.distance(endPnt);
		if (this.segment != null) {
			System.out.print("Can't not init segment twice");
			return ;
		}
		else {
			this.segment = seg;
		}
		this.id = id;
		this.rules = r;
		this.index = index;
		this.segment.addLane(this);
	}
	public void init(long id, int r, int index, int orderNum, double width ,String direction ,List<GeoPoint> ctrlPoints, Segment segment) {
		this.id = id;
		this.rules = r;
		this.index = index;
		this.ctrlPoints = ctrlPoints;
		this.orderNum = orderNum;
		this.direction = direction;
		this.width = width;
		this.segment = segment;
	}
	// 路网世界坐标平移后再调用
	public void createLaneSurface(){
		surface = GeoUtil.multiLines2Rectangles(ctrlPoints,width,true);

	}
	public GeoSurface getSurface(){
		return surface;
	}
	public double getWidth() {
		return width;
	}

	public void calcStaticInfo(WorldSpace worldSpace) {
		if (getLeftLane() == null)
			rulesExclude(Constants.LANE_CHANGE_LEFT);
		if (getRightLane() == null)
			rulesExclude(Constants.LANE_CHANGE_RIGHT);
		setLaneType();

		int size = ctrlPoints.size();
		linearRelation = new double[size];
		linearRelation[0] = 0;
		for(int i=0;i<size;i++){
			// 坐标平移
			ctrlPoints.set(i,worldSpace.worldSpacePoint(ctrlPoints.get(i)));
			if(i>=1){
				linearRelation[i] = ctrlPoints.get(i).distance(ctrlPoints.get(i-1))+linearRelation[i-1];
			}
		}
		geoLength = linearRelation[size-1];

		//生成车道面
		createLaneSurface();
		//信控标识
//		if(laneType(Constants.LANE_TYPE_SIGNAL_ARROW) != 0){
//			char[] turns = direction.toCharArray();
//			if(turns.length!=0){
//				double r = (getGeoLength()+1)/getGeoLength();// 起点偏移1m
//				GeoPoint startPnt = ctrlPoints.get(0);
//				GeoPoint endPnt = ctrlPoints.get(size-1);
//				GeoPoint rectgFP = endPnt.intermediate(startPnt,r);
//				r = r + 6.0/getGeoLength();// 箭头长度6m
//				GeoPoint rectgEP = endPnt.intermediate(startPnt,r);
//				for(char turn:turns){
//					SignalArrow sa = new SignalArrow(0,turn,rectgFP,rectgEP);
//					sa.setRightTurnFree((rules()&Constants.LANE_RIGHTTURN_FREE)!=0);
//					signalArrows.add(sa);
//				}
//			}
//			// 计算流向箭头的横向位置
//			if(signalArrows.size()>1) {
//				Collections.sort(signalArrows);
//				GeoPoint refPoint = surface.getKerbList().get(surface.getKerbList().size() - 2);
//				GeoPoint endPnt = ctrlPoints.get(ctrlPoints.size()-1);
//				Vector3D translate = new Vector3D(refPoint.getLocationX() - endPnt.getLocationX(),
//						refPoint.getLocationY() - endPnt.getLocationY(),
//						refPoint.getLocationZ() - endPnt.getLocationZ());//←
//				translate = translate.normalize();
//				if(signalArrows.size()==2){
//					GeoUtil.calcTranslation(signalArrows.get(0).getArrowTip(),translate.scalarMultiply(0.3));
//					GeoUtil.calcTranslation(signalArrows.get(0).getPolyline(),translate.scalarMultiply(0.3));
//					GeoUtil.calcTranslation(signalArrows.get(1).getArrowTip(),translate.scalarMultiply(-0.3));
//					GeoUtil.calcTranslation(signalArrows.get(1).getPolyline(),translate.scalarMultiply(-0.3));
//				}
//				else{
//					GeoUtil.calcTranslation(signalArrows.get(0).getArrowTip(),translate.scalarMultiply(0.6));
//					GeoUtil.calcTranslation(signalArrows.get(0).getPolyline(),translate.scalarMultiply(0.6));
//					GeoUtil.calcTranslation(signalArrows.get(2).getArrowTip(),translate.scalarMultiply(-0.6));
//					GeoUtil.calcTranslation(signalArrows.get(2).getPolyline(),translate.scalarMultiply(-0.6));
//				}
//			}
//
//		}
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the limiting speed in this lane. Speed is in meter/sec. This
	 * function should be overloaded to consider lane position (see class
	 * TS_Lane for more details)
	 * --------------------------------------------------------------------
	 */
	public double getFreeSpeed() {
		return segment.getFreeSpeed();
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the grade of the lane, in percent. Used to calculate the
	 * Acceleration and limiting speed.
	 * --------------------------------------------------------------------
	 */
	public double getGrade() {
		return segment.getGrade();
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the 1st surveillance station in this lane (upstream end)
	 * --------------------------------------------------------------------
	 */
	public List survList() {
		return (segment.getSensors());
	}
	/*
	 * --------------------------------------------------------------------
	 * Returns the 1st control station in this lane (upstream end)
	 * --------------------------------------------------------------------
	 */
	public List ctrlList() {
		return (segment.getCtrlList());
	}

	public int hasMarker() {
		return state & Constants.STATE_MARKED;
	}

	// Check if the lane is connected to a lane upstream/downstream.
	// markConnectedUpLanes() or markConnecteDnLanes() must be
	// called (usually for each lane in a segment) before the use of
	// the function isConnected(signature_bit), where signatures is
	// the bit masks (left lane is 1, middle lane is 2, right lane
	// is 4, and so on) of the upLanes that do the marking.

	public boolean isConnected(int signatures) {
		return (cmarker & signatures) != 0 ? true : false;
	}

	public double getDistance() {
		return segment.getDistance();
	}

	//wym
	public RoadNetwork getNetwork() {
		return segment.getNetwork();
	}

	@Override
	public int compareTo(Lane o) {
		if(this.orderNum>o.orderNum)
			return 1;
		else if(this.orderNum<o.orderNum)
			return -1;
		else
			return 0;
	}
}
