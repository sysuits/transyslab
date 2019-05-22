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

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.math.geom.AABBox;

public class GeoSurface implements NetworkObject{
	public static final int CONVEX_POLYGON = 1;
	public static final int MULTI_POLYGONS = 2;
	public static final int RANDOM_POINTS = 3;
	protected List<GeoPoint> kerbPoints;
	protected int type;
	protected int index;
	protected long id;
	protected String objInfo;
	protected boolean isSelected;
	public GeoSurface(){
		this.kerbPoints = new ArrayList<>();
		this.type = CONVEX_POLYGON;
	}
	public GeoSurface(int type){
		this.kerbPoints = new ArrayList<>();
		this.type = type;
	}
	public void init(long code) {
		this.id =  code;

	}
	public long getId(){
		return this.id;
	}
	public int getType(){
		return this.type;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public String getObjInfo(){
		return this.objInfo;
	}
	public boolean isSelected(){
		return this.isSelected;
	}

	public void addKerbPoint(GeoPoint p){
		kerbPoints.add(p);

	}
	public void translateInWorldSpace(WorldSpace world_space) {
		for(GeoPoint p: kerbPoints){
			world_space.translateWorldSpacePoint(p);
		}
	}
	public List<GeoPoint> getKerbList(){
		return kerbPoints;
	}
	public void setKerbList(List<GeoPoint> kerbList){
		kerbPoints = kerbList;
	}

}
