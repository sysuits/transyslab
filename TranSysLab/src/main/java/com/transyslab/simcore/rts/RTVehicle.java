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

import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.Vehicle;


public class RTVehicle extends Vehicle{
	private RTLink curLink;
	private RTLane curLane;
	@Override
	public Link getLink() {
		return curLink;
	}
	public void init(int id, RTLane lane,double curSpeed, double distance){
		this.id = id;
		this.currentSpeed = curSpeed;
		this.distance = distance;
		this.curLane = lane;
	}
	public String toString(){
		return Integer.toString(id) + Long.toString(curLane.getId()) + Double.toString(distance);
	}
}
