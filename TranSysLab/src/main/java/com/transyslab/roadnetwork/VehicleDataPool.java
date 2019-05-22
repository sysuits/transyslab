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

import java.util.concurrent.ConcurrentLinkedQueue;

//VehicleData对象池，用于回收和产生VehicleData
public class VehicleDataPool {
	
	private static VehicleDataPool vhcDataPool_ = new VehicleDataPool();;
	private int counter; // ConcurrentLinkedQueue.size()需要遍历集合，效率较低
	//线程安全队列
	private ConcurrentLinkedQueue<VehicleData> recycleList_;
	
	private VehicleDataPool(){
		recycleList_ = new ConcurrentLinkedQueue<>();
		counter = 0;
	}
	
	public static VehicleDataPool getInstance(){
		return vhcDataPool_;
	}
	public void recycle(VehicleData vd){
		vd.clean();
		recycleList_.offer(vd);
		counter ++;
	}
	public VehicleData newData() /* get a vehicle from the list */
	{
		VehicleData vd;
		if (recycleList_.isEmpty()) { // list is empty
			vd = new VehicleData();
		}
		else { // get head from the list
			vd = recycleList_.poll();
			counter --;
		}
		return vd;
	}
	public int nRows(){
		return counter;
	}
}
