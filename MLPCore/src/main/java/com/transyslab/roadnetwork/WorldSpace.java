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

/**
 * 世界坐标
 *
 *
 *
 *  We initialize the north east point to - LARGE_NUMBER and the south
 *  west point to + LARGE_NUMBER to ensure that the first network
 *  point compared in recordExtremePoints will replace the initial
 *  values.
 *
 */
public class WorldSpace {

	// These two coordinates define the boundaries of world space.
	// They will be set dynamically as a network is loaded, and at
	// the end of the load they will coorespond to the maximum end
	// points of the network

	private GeoPoint northEastPnt;
	private GeoPoint southWestPnt;

	// These are point in world space

	private GeoPoint lowLeftPnt; // for sw_pnt_, should be (0, 0)
	private GeoPoint topRightPnt; // for ne_pnt_

	// These varables are set by function createWorldSpace(), which
	// is called after the network has been loaded. Their values
	// will not change afterwards.

	private double width; /* west-east width */
	private double height; /* south-north height */
	private GeoPoint center;
	private double angle; /*
							 * angle from center to farthest east point
							 */
	public WorldSpace() {
		southWestPnt = new GeoPoint(Double.MAX_VALUE, Double.MAX_VALUE);
		northEastPnt = new GeoPoint(Double.MIN_VALUE, Double.MIN_VALUE);
	}
	// 记录ne东北、sw西南两个极点
	public void recordExtremePoints(GeoPoint point) {
		southWestPnt = new GeoPoint(Math.min(southWestPnt.getLocationX(), point.getLocationX()),
				Math.min(southWestPnt.getLocationY(), point.getLocationY()));

		northEastPnt = new GeoPoint(Math.max(northEastPnt.getLocationX(), point.getLocationX()),
				Math.max(northEastPnt.getLocationY(), point.getLocationY()));
	}
	public void recordExtremePoints(List<GeoPoint> ctrlPoints) {
		for(GeoPoint point:ctrlPoints){
			southWestPnt = new GeoPoint(Math.min(southWestPnt.getLocationX(), point.getLocationX()),
					Math.min(southWestPnt.getLocationY(), point.getLocationY()));

			northEastPnt = new GeoPoint(Math.max(northEastPnt.getLocationX(), point.getLocationX()),
					Math.max(northEastPnt.getLocationY(), point.getLocationY()));
		}

	}
	public void createWorldSpace() {
		width = (northEastPnt.getLocationX() - southWestPnt.getLocationX()); //* Parameter.lengthFactor();
		height = (northEastPnt.getLocationY() - southWestPnt.getLocationY()); //* Parameter.lengthFactor();
		// 坐标平移，将sw极点平移到坐标系原点，即ll_pnt坐标为(0,0)
		lowLeftPnt = worldSpacePoint(southWestPnt);
		topRightPnt = worldSpacePoint(northEastPnt);

		if (width < GeoPoint.POINT_EPSILON && height < GeoPoint.POINT_EPSILON) {
			// cerr << "Error: World space is empty. Check geometric data!"
			// << endl;
			// exit(1);
		}
		else if (width < GeoPoint.POINT_EPSILON) { // N-S linear network
			width = 0.1 * height;
		}
		else if (height < GeoPoint.POINT_EPSILON) { // W-E linear network
			height = 0.1 * width;
		}
		// 中心点初始化
		if (center == null)
			center = new GeoPoint();
		center.setLocCoods(0.5 * width, 0.5 * height, 0.0);
		angle = 0.0;
	}

	// Convert a point from original network database cooridinates
	// to work space coordinates.

	public GeoPoint worldSpacePoint(GeoPoint p) {
		return worldSpacePoint(p.getLocationX(), p.getLocationY());
	}
	public GeoPoint worldSpacePoint(double x, double y) {
		return new GeoPoint((x - southWestPnt.getLocationX()) /* * Parameter.lengthFactor()*/,
				(y - southWestPnt.getLocationY())  /* * Parameter.lengthFactor()*/);

	}
	//点要素坐标平移
	public void translateWorldSpacePoint(GeoPoint p){
		p.setLocCoods(p.getLocationX() - southWestPnt.getLocationX(), p.getLocationY() - southWestPnt.getLocationY(),0);
	}
	// Convert a point from work space coordinates to original
	// network database cooridinates

	public GeoPoint databasePoint(GeoPoint p) {
		return new GeoPoint(p.getLocationX() /* / Parameter.lengthFactor() */ + southWestPnt.getLocationX(),
				p.getLocationY() /* / Parameter.lengthFactor() */ + southWestPnt.getLocationY());
	}
	public GeoPoint databasePoint(double x, double y) {
		return new GeoPoint(x /* / Parameter.lengthFactor() */ + southWestPnt.getLocationX(),
				y /* / Parameter.lengthFactor() */ + southWestPnt.getLocationY());
	}

	public GeoPoint getSouthWestPoint() {
		return southWestPnt;
	}
	public GeoPoint getNorthEastPoint() {
		return northEastPnt;
	}

	public GeoPoint getLowLeftPoint() {
		return lowLeftPnt;
	}
	public GeoPoint getTopRightPoint() {
		return topRightPnt;
	}

	public GeoPoint getCenter() {
		return center;
	}
	public double getWidth() {
		return width;
	}
	public double getHeight() {
		return height;
	}
	public double getAngle() {
		return angle;
	}

}
