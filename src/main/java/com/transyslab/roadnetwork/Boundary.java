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

import com.transyslab.commons.tools.LinearAlgebra;

public class Boundary implements NetworkObject{
	protected GeoPoint startPnt;
	protected GeoPoint endPnt;
	protected int index;
	protected long id;
	protected String objInfo;
	protected boolean isSelected;
	public Boundary() {

	}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public void init(long id, int index,double beginx, double beginy, double endx, double endy ){
		this.id = id;
		this.index = index;
		startPnt = new GeoPoint(beginx,beginy);
		endPnt = new GeoPoint(endx,endy);
	}
	public long getId(){
		return this.id;
	}
	public String getObjInfo(){
		return this.objInfo;
	}
	public void translateInWorldSpace(WorldSpace world_space) {
		startPnt = world_space.worldSpacePoint(startPnt);
		endPnt = world_space.worldSpacePoint(endPnt);
	}
	public GeoPoint getStartPnt() {
		return startPnt;
	}
	public GeoPoint getEndPnt() {
		return endPnt;
	}
	public double[] getDelta() {
		return LinearAlgebra.minus(endPnt.getLocCoods(), startPnt.getLocCoods());
	}
}
