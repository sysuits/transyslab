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

import com.transyslab.commons.tools.GeoUtil;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Segment
 *
 */
public class Segment implements NetworkObject {

	protected long id;
	protected String name;
	protected String objInfo;
	protected List<Lane> lanes;
	protected Segment upSegment;
	protected Segment dnSegment;
	protected int index; // index in array

	protected Link link; // pointer to link
	protected double grade; // grade of the segment

	//	protected int leftLaneIndex; // index to the left lane
	protected int speedLimit; // default speed limit
	protected double freeSpeed; // free flow speed
	protected double distance; // getDistance from dn node

	protected List<CtrlStation> ctrlStations; // first (upstream) control station
	protected List<Sensor> sensors; // first (upstream) sensor station


	protected int state;
	protected int localType; // head, tail, etc


	protected List<GeoPoint> ctrlPoints;
	protected double[] linearRelation;
	protected double bulge;
	protected GeoSurface surface;

	protected double startAngle;
	protected double endAngle;

	protected double length;
	protected boolean isSelected;

	public Segment() {
		distance = 0.0;
		localType = 0;
		state = 0;
		lanes = new ArrayList<>();
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
	public int state() {
		return (state & 0xFFFF);
	}
	public void setState(int s) {
		state |= s;
	}
	public void unsetState(int s) {
		state &= ~s;
	}
	public int isHead() {
		return (localType & 0x0001);
	}
	public int isTail() {
		return (localType & 0x0002);
	}
	public int isTheEnd() {
		return (localType & 0x0020);
	}
	public int isTheBeginning() {
		return (localType & 0x0010);
	}
	public int getIndex() {
		return index;
	}
	public void setLanes(List<Lane> lanes){
		this.lanes = lanes;
	}
	public void setUpSegment(Segment upSegment){
		this.upSegment = upSegment;
	}
	public void setDnSegment(Segment dnSegment){
		this.dnSegment = dnSegment;
	}
	public List<GeoPoint> getCtrlPoints(){
		return this.ctrlPoints;
	}
	// Index within the link. 0 is the upstream.
	public int localIndex() {
		return (index - link.getStartSegment().index);
	}
	public int getType() {
		return (link.type());
	}
	public double getLength() {
		return length;
	}
	public double getBulge() {
		return bulge;
	}

	public double getStartAngle() {
		// For a straight line, startAngle is the angle of the
		// line. For a curve, startAngle is the angle of the line
		// from the center to the point startPnt.
		return startAngle;
	}
	public double getEndAngle() {
		// For a straight line, endAngle is the angle of the
		// line. For a curve, endAngle is the angle of the line from
		// the center to the point endPnt. Note: arcAngle is zero for
		// a straight line.
		return endAngle;
	}
	public GeoSurface getSurface(){
		return surface;
	}
	// Segment iterators in a link
	// Returns the upstream segment in the same link
	public Segment getUpSegment() {
		return this.upSegment;
	}
	public Segment getDnSegment() {
		return this.dnSegment;
	}

	// Lane iterators in a segmen
	// Returns the left most lane in the segment
	public Lane getLeftLane() {
		return lanes.get(0);
	}
	// Returns the right most lane in the segment

	public Lane getRightLane() {
		return lanes.get(lanes.size()-1);
	}
	// Returns the ith lane in the segment
	public Lane getLane(int index) {
		return lanes.get(index);
	}
	public Link getLink() {
		return link;
	}

	public List<CtrlStation> getCtrlList() {
		return ctrlStations;
	}
	public List<Sensor> getSensors() {
		return sensors;
	}
	public void addSensor(Sensor e){
		sensors.add(e);
	}
	public void addLane(Lane lane){
		this.lanes.add(lane);
	}
	public void init(long id, int speed_limit, int index, double speed, double grd, List<GeoPoint> ctrlPoints,Link link) {
		// YYL
		if (this.link != null) {
			// cerr << "Error:: Segment <" << id << "> "
			System.out.print("cannot be initialized twice. ");
			return ;
		}
		else {
			this.link = link;
		}
		this.id = id;
		this.speedLimit = speed_limit;
		this.grade = grd;
		this.index = index;
		this.ctrlPoints = ctrlPoints;
		if(!ctrlPoints.isEmpty()){
			int size = ctrlPoints.size();
			linearRelation = new double[size];
			linearRelation[0] = 0;
			if(size>1){
				for(int i=1;i<size;i++){
					linearRelation[i] = ctrlPoints.get(i).distance(ctrlPoints.get(i-1))+linearRelation[i-1];
				}
			}
		}
	}


	// Segment attributes

	public int nLanes() {
		return lanes.size();
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double x) {
		distance = x;
	}
	public int speedLimit() {
		return speedLimit;
	}
	public void setSpeedLimit(int sl) {
		speedLimit = sl;
	}
	/*
	public void setFreeSpeed(float f) {
		freeSpeed = f;
	}*/
	public double getFreeSpeed() {
		return this.freeSpeed;
	}

	public void setGrade(double g) {
		this.grade = g;
	}
	public double getGrade() {
		return this.grade;
	}
	public double calcCurrentTravelTime() {
		return 0;
	}



	// Make end point of each pair of connected segments snapped at
	// the same point

	public void snapCoordinates() {
		Segment ups = getUpSegment();
		if (ups != null) {// 非起始路段
			// 上游路段终点与下游路段起点间的中点
			int nPoints =  ups.ctrlPoints.size();
			GeoPoint mp = new GeoPoint(ups.ctrlPoints.get(nPoints-1), ctrlPoints.get(0), 0.5);

			// 合并
			ups.ctrlPoints.set(nPoints-1,mp);
			ctrlPoints.set(0,mp);
		}
	}

	public void calcArcInfo(WorldSpace world_space) {

		int size = ctrlPoints.size();
		linearRelation = new double[size];
		linearRelation[0] = 0;
		for(int i=0;i<size;i++){
			// 坐标平移
			ctrlPoints.set(i,world_space.worldSpacePoint(ctrlPoints.get(i)));
			if(i>=1){
				linearRelation[i] = ctrlPoints.get(i).distance(ctrlPoints.get(i-1))+linearRelation[i-1];
			}
		}

		length = linearRelation[size-1];
		startAngle = endAngle = 0;

	}

	// Calculate the data that do not change in the simulation.
	public void calcStaticInfo() {
		if ((getDnSegment() == null)) {
			localType |= 0x0001;
			if (getLink().nDnLinks() < 1 || getLink().getDnNode().type(0x0001) > 0) {
				localType |= 0x0020;
			}
		}
		if (getUpSegment() == null) {
			localType |= 0x0002;
			if (getLink().nUpLinks() < 1 || getLink().getUpNode().type(0x0001) > 0) {
				localType |= 0x0010;
			}
		}
		// 创建路段面
		createSurface();
	}
	// 路网世界坐标平移后再调用
	public void createSurface(){
		double width = 0;
		for(Lane laneInSgmt:lanes){
			width += laneInSgmt.width;
		}
		surface = GeoUtil.multiLines2Rectangles(ctrlPoints, width, false);
	}
	public double calcDensity() {
		return 0;
	}
	public double calcSpeed() {
		return 0;
	}
	public int calcFlow() {
		return 0;
	}
	// 虚拟方法，由子类继承实现流速密计算

	public int isNeighbor(Segment sgmt) {
		if (this.getLink() == sgmt.getLink()) {
			if ((this.getIndex() + 1) == sgmt.getIndex())
				return 1;
			else
				return 0;
		}
		else if (this.getDnSegment() != null || sgmt.getUpSegment() != null) {
			return 0;
		}
		else if (this.getLink().isNeighbor(sgmt.getLink()) == 0) {
			return 0;
		}
		return 1;
	}
	public void outputSegment() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(this.id).append(",");
//		sb.append(startPnt.getLocationX()).append(",");
//		sb.append(startPnt.getLocationY()).append(",");
//		sb.append(endPnt.getLocationX()).append(",");
//		sb.append(endPnt.getLocationY()).append("\n");
		String filepath = "E:\\OutputRoadNetwork.txt";
		FileOutputStream out = new FileOutputStream(filepath, true);
		OutputStreamWriter osw = new OutputStreamWriter(out, "utf-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(sb.toString());
		bw.close();
	}

	// Export geometry data in MapInfo format
	// 输出地图信息
	public void ExportMapInfo() {

	}
	public void Export() {

	}

	public void outputToOracle(PreparedStatement ps) throws SQLException {
		// TODO 自动生成的方法存根

	}

	public void outputVhcPosition() throws IOException {
		// TODO 自动生成的方法存根

	}

	public List<Lane> getLanes() {
		return lanes;
	}

	//wym
	public RoadNetwork getNetwork() {
		return link.getNetwork();
	}
}
