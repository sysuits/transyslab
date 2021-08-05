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


package com.transyslab.simcore.mesots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.transyslab.roadnetwork.*;


public class MesoVehicleTable {
	protected List<MesoVehicle> vhcList;
	protected ODPair odPair;
	protected Path path;
	
	public MesoVehicleTable() {
	}
	public ODPair getODPair(){
		return odPair;
	}
	public Path getPath(){
		return path;
	}

	//重写init方法，根据车辆od，路径id来生成车辆
	public void init(int o, int d, int pid){
		// TODO 待实现, 考虑车辆的不同OD
		vhcList = new ArrayList<MesoVehicle>();
		/*
	    Node ori = MesoNetwork.getInstance().findNode(o);
	    Node des = MesoNetwork.getInstance().findNode(d);
	    odPair = new ODPair(ori,des);
	    path = PathTable.getInstance().findPath(pid);*/
	}
	public List<MesoVehicle> getVhcList(){
		return vhcList;
	}

}
