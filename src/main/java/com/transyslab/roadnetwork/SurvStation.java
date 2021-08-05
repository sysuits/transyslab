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

import com.transyslab.commons.tools.GeoUtil;

/**

 * 断面检测器
 */
public class SurvStation implements Sensor{
	protected int type; // sensor type 1:loop; 2:microwave;3:video;4:kakou
	protected int task; // data items
	protected long id;
	protected String objInfo;
	protected Segment segment; // pointer to segment
	protected double zoneLength; // length of detection zone
	protected double position; // position in % from segment end
	protected List<Sensor> sensors; // array of pointers to sensors
	protected int index; // (YYL) index in segment.survlist
	protected int recordedCount;
	protected double meanSpeed;
	protected double recordedSpeed;
	protected double interval;
	protected double detTime;
	protected List<Double> speedList; // measure speed in specific time interval
	protected List<Integer> flowList; // measure flow in specific time interval
	// protected List<Integer> vhcidList_ = new ArrayList<Integer>();
	protected GeoSurface surface;
	protected Boolean isSelected;

	public SurvStation() {
		index = -1;
		recordedCount = 0;
		recordedSpeed = 0;
	}
	public String getObjInfo(){
		return this.objInfo;
	}
	public long getId(){
		return this.id;
	}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public int type() {
		return type;
	}

	public Link getLink() {
		// Returns the link contains the surveillance station
		return segment.getLink();
	}

	public int nLanes() {
		return segment.nLanes();
	}
	public int nSensors() {
		return sensors.size();
	}
	public void addSensor(Sensor sensor) {
		sensors.add(sensor);
	}

	// Returns pointer to the sensor in ith lane. It may be NULL if
	// there is no sensor in the ith lane.

	public Sensor getSensor(int i) {
		return sensors.get(i);
	}

	// Connect ith point to the sensor

	public double getInterval() {
		return interval;
	}

	public double zoneLength() {
		return zoneLength;
	}
	public double getPosition() {
		return position;
	}

	public List<Double> getSpeedList() {
		return speedList;
	}
	public List<Integer> getFlowList() {
		return flowList;
	}

	public void init(long id,int ty,int index,Segment seg, double pos,double zone, double iv) {
		this.id = id;
		this.type = ty; // sensor type
		this.interval = iv; // statistic time interval in seconds

		zoneLength = zone; // * Parameter::lengthFactor(); in meter
		// position = (float) (1.0 - pos); // position in % from segment end

		speedList = new ArrayList<Double>();
		flowList = new ArrayList<Integer>();
		this.segment = seg;
		position = (1.0 - pos) * segment.getLength();
		// (YYL)
		sensors = new ArrayList<Sensor>();

		if (segment.sensors == null)
			segment.sensors = new ArrayList<Sensor>();
		this.index = index;
		this.createSurface();
		segment.addSensor(this);
	}
	public void initDectTime(double startTime,double interval){
		this.detTime = startTime;
		this.interval = interval;
	}
	public void setSDetTime(double startTime){
		this.detTime = startTime+this.interval;
	}
	/*
	public void outputToOracle(PreparedStatement ps) throws SQLException {
		int num = flowList.size();
		int lanenum;
		// 卡口和视频属于断面检测
		if (type == 2 || type == 3)
			lanenum = station.nSensors;
		else
			lanenum = station_.segment.nLanes();
		for (int i = 0; i < num; i++) {
			Date date1 = LinkTimes.getInstance().toDate(i);
			Date date2 = LinkTimes.getInstance().toDate((i + 1));
			// simtaskid，写死注意更改
			ps.setInt(1, 5);
			// 视频或卡口
			if (station_.type == 2 || station_.type == 3)
				ps.setInt(2, station_.getCode());
			else// 线圈
				ps.setInt(2, getCode());
			ps.setInt(3, station_.type);
			ps.setDate(4, new java.sql.Date(date1.getTime()));
			ps.setTimestamp(4, new java.sql.Timestamp(date1.getTime()));
			ps.setDate(5, new java.sql.Date(date2.getTime()));
			ps.setTimestamp(5, new java.sql.Timestamp(date2.getTime()));
			ps.setInt(6, lanenum);
			ps.setInt(7, Math.round((float) (station_.flowList.get(i)) / (float) lanenum));
			if (Float.isNaN(station_.speedList.get(i))) {
				System.out.println("x");
			}
			ps.setFloat(8, station_.speedList.get(i));
			ps.addBatch();
		}
		ps.executeBatch();
	}
	// computes the flow across the section - used in incident detection
/*
	public int sumLaneCount() {
		int sum = 0;

		// int nlanes = nLanes();

		for (int i = 0; i < nSensors; i++)
			sum += (getSensor(i)).getCount();

		return sum;
	}
	public float sumLaneAvgSpeed() {
		float sumspeed = 0.0f;
		// int nlanes = nLanes();
		for (int i = 0; i < nSensors; i++) {
			sumspeed += (getSensor(i)).getAvgSpeed(0);
		}
		sumspeed = sumspeed / nSensors;
		return sumspeed;
	}*/

	/*
		 * public void addVehicleID(MESO_Vehicle pv){
		 * vhcidList_.add(pv.get_code()); }
		 */
	public void aggregate(double curTime) {
		if(detTime<=curTime){
			flowList.add(recordedCount);
			if (recordedCount != 0)
				meanSpeed = (recordedCount) / recordedSpeed;
			else
				meanSpeed = 0.0f;
			speedList.add(meanSpeed);
			recordedCount = 0;
			recordedSpeed = 0.0f;
			detTime += interval;
		}

	}
	@Override
	public void measure(double speed) {
		recordedCount++;
		// 转换为km/h
		speed = 3.6f * speed;
		if (speed > Constants.SPEED_EPSILON) {
			recordedSpeed += 1.0f / speed;
		}
		else {
			recordedSpeed += 1.0f / Constants.SPEED_EPSILON;
		}
		
	}

	@Override
	public void createSurface(){
		GeoPoint segSPnt = segment.ctrlPoints.get(0);
		GeoPoint segEPnt = segment.ctrlPoints.get(segment.ctrlPoints.size()-1);
		GeoPoint startPnt = new GeoPoint(segSPnt, segEPnt, position);
		double lenScale = zoneLength / segment.getLength();
		GeoPoint endPnt = new GeoPoint(startPnt, segEPnt,lenScale);
		double width = segment.nLanes() * segment.getLeftLane().getWidth();
		surface = GeoUtil.lineToRectangle(startPnt, endPnt, width,true);
	}
	public void clean(){
		recordedCount = 0;
		recordedSpeed = 0;
		int nInterval = flowList.size();
		detTime = detTime - nInterval*interval;
		flowList.clear();
		speedList.clear();
	}

	@Override
	public GeoSurface getSurface() {
		return this.surface;
	}

	@Override
	public String getName() {
		return objInfo;
	}
}
