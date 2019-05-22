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
import java.util.List;

import com.transyslab.commons.tools.GeoUtil;

/**
 * 车道检测器
 *
 */
// NOTE! The following forward declarations are required because of
// the multiple inheritance structure problem defined at the end of

public class Loop implements Sensor{

	protected long id;
	protected String name;
	protected String objInfo;

	protected int indexInLoops; // index in SurvStation array
	protected Lane lane; // pointer to lane
	protected int state; // occupied/working etc
	protected double position;
	protected int type;
	protected double zoneLength;
	protected int recordedCount;    // 用于中间计数
	protected double meanSpeed;
	protected double recordedSpeed; // 用于中间计数
	protected double interval;
	protected List<Double> speedList; // measure speed in specific time interval
	protected List<Integer> flowList; // measure flow in specific time interval
	protected GeoSurface surface;
	protected double detTime;
	protected boolean isSelected;
	
	public Loop() {
		indexInLoops = -1;
		zoneLength = 2;
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
	public GeoSurface getSurface(){
		return surface;
	}
	public int state() {
		return state;
	}

	public int getIndexInLoops() {
		return indexInLoops;
	}

	public Lane getLane() {
		return lane;
	}
	public Segment getSegment() {
		return lane.getSegment();
	}
	public Link getLink() {
		return lane.getLink();
	}
	// relative getDistance from the end of the segment
	public double getPosition() {
		return this.position;
	}

/*
	public int init(int ty, float iv, float zoneLen,int laneId, int id, double pos) {

		this.id = id;
		type = ty;
		interval = iv;
		zoneLength = zoneLen;
		lane = RoadNetwork.getInstance().findLane(laneId);
		if(lane == null){
			System.out.println("Can't not find lane which id is"+ laneId);
			return -1;
		}			

		indexInLoops = RoadNetwork.getInstance().nSensors();

		RoadNetwork.getInstance().addSensor(this);

		return 0;
	}*/
	
	// isLinkwide == true调用
	/*
	public void measureFromStation() {
		count_ = station_.sectionCount_ / station_.nSensors;
		avgSpeed_ = station_.sectionAvgSpeed;
	}

	public void outputToOracle(PreparedStatement ps) throws SQLException {
		int num = station_.flowList.size();
		int lanenum;
		// 卡口和视频属于断面检测
		if (station_.type == 2 || station_.type == 3)
			lanenum = station_.nSensors;
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
	}*/
	// public float measurement() { return occupancy_; }

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
	public void createSurface() {
		// TODO 不用百分比，用具体线性参考位置
		GeoPoint itplPoint = lane.itplAmongCtrlPoints(position*lane.getGeoLength());
		double lenScale = zoneLength/ itplPoint.distance(lane.getCtrlPoints().get(0));
		GeoPoint startPnt = itplPoint.intermediate(lane.getCtrlPoints().get(0),1-lenScale);
		surface = GeoUtil.lineToRectangle(startPnt, itplPoint, lane.getWidth(),true);
	}
	public void clean(){
		recordedCount = 0;
		recordedSpeed = 0;
		int nInterval = flowList.size();
		detTime = detTime - nInterval*interval;
		flowList.clear();
		speedList.clear();
	}
}
