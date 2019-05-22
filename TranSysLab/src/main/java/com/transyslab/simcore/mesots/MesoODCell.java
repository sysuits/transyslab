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

import com.transyslab.roadnetwork.*;

/**
 * MesoODCell
 *
 */
public class MesoODCell extends ODPair{


	protected double headway; // average headway (second)
	protected double nextTime; // next departure time
	protected int type; // vehicle type
	protected double randomness; // 0=uniform 1=random

	protected int busTypeBRT; // Dan: bus type for bus rapid transit assignment
	protected int runIDBRT; // Dan: assigned bus run id for bus rapid transit
	public static int emitCounter;

	public MesoODCell(MesoNode o, MesoNode d) {
		nextTime = 0;
		oriNode = o;
		desNode = d;
		emitCounter = 0;
	}

	public void setId(int id){
		this.id = id;
	}
	public double rate() {
		return 3600.0 / headway;
	}
	public double nextTime() {
		return nextTime;
	}
	public int type() {
		return type;
	}

	public int busTypeBRT() {
		return busTypeBRT;
	}
	public int runIDBRT() {
		return runIDBRT;
	}

	// This function is called by OD_Parser. Return 0 if sucess or -1 if fail
	public void init(int id, int type, double headway, double nextTime,double r) {
		this.id = id;
		// theODPairs 是od对的list，这里不将od对存进list
		// 原来把odpair存进list是为了查询是否已存在od对，存在则不用新生成，不存在则要生成，减少内存占用
		int oType = this.getOriNode().getType()| Constants.NODE_TYPE_ORI;
		int dType = this.getDesNode().getType()| Constants.NODE_TYPE_DES;
		this.getOriNode().setType(oType);
		this.getDesNode().setType(dType);
		this.randomness = r;
		this.type = type;

	}
	// Calculate the inter departure time for the next vehicle
	public double randomizedHeadway(MesoRandom rand) {
		if (randomness < 1.0e-10 || (rand.brandom(randomness)) == 0) {
			// uniform distribution
			return (headway);
		}
		else { // random distribution
			return (-Math.log(rand.urandom()) * headway);
		}
	}

	// Returns a created vehicle if it is time for a vehicle to depart or
	// NULL if no vehicle needs to depart at this time. It also update the
	// time that next vehicle departs if a vehicle is created.
	/*
	public MesoVehicle emitVehicle() {
		if (nextTime <= SimulationClock.getInstance().getCurrentTime()) {
			MesoVehicle pv = newVehicle();

			if ((type() & Constants.VEHICLE_BUS_RAPID) != 0) {
				// pv.initRapidBus(type, od, runIDBRT, busTypeBRT,
				// headway);
			}
			else
			{
				pv.init(0, type, od, null);
				//wym
				pv.initPath(od.getOriNode(), od.getDesNode());
			}		



			if ((type() & Constants.VEHICLE_BUS_RAPID) != 0) {
				pv.PretripChoosePath();
			}
			else
				pv.PretripChoosePath(this);


			nextTime += randomizedHeadway();
			return pv;
		}
		return null;
	}*/
	/*
	 * // Return a route if getPath table and splits are specified public RN_Path
	 * chooseRoute(RN_Vehicle pv){ int i =
	 * theRandomizers[MesoRandom::Routing]->drandom(nPaths(), splits); return
	 * getPath(i) ; }
	 */

	// Used when parsing the od table
	// Return a route if getPath table and splits are specified

	/*Path chooseRoute(Vehicle pv) {
//		int i = (MesoRandom.getInstance().get(2)).drandom(nPaths(), splits);
		double r = RoadNetwork.getInstance().sysRand.nextDouble();
		int n = nPaths();
		int i;
		for (n = n - 1, i = 0; i < n && r > splits[i]; i++);
		return getPath(i);
	}*/
	public void emitVehicles(MesoNetwork network, double currentTime) {
		/*MesoVehicle pv;
		while ((pv = emitVehicle()) != null) {
			pv.enterPretripQueue();
		}*/
		while(nextTime <= currentTime){
			MesoVehicle vehicle = network.createVehicle();
			// TODO DEBUG
			emitCounter ++ ;
			// double test = network.mesoRandom[MesoRandom.Departure].seed;
			if(this.id == 0)
				System.out.print(nextTime+",");
			if ((type() & Constants.VEHICLE_BUS_RAPID) != 0) {
				// pv.initRapidBus(type, odPair, runIDBRT_, busTypeBRT_,
				// headway_);
			}
			else
			{
				vehicle.setType(this.type);
				// TODO 待设计：车辆类型
				vehicle.initialize(network.getSimParameter(),network.mesoRandom[MesoRandom.Departure]);
				// yyl 使用默认最短路径
				vehicle.setPath(this.getPath(0));
				// 对v.nextLink赋值
				vehicle.setPathIndex(0);
				// yyl 固定路径
				vehicle.fixPath();
				vehicle.setDepartTime(currentTime);
				vehicle.setTimeEntersLink(currentTime);

			/*
			vehicle.init(0, type, odPair, null);
			//wym
			vehicle.initPath(odPair.getOriNode(), odPair.getDesNode());*/
			}
			// Find the first link to travel. Variable 'oriNode', 'desNode' and
			// 'type' must have valid values before the route choice model is
			// called.
			if ((type() & Constants.VEHICLE_BUS_RAPID) != 0) {
				vehicle.PretripChoosePath(oriNode,network);
			}
			else {
				// TODO 固定路径，暂无动态更新
				//vehicle.PretripChoosePath(this,network);
			}
			vehicle.enterPretripQueue(network.getSimClock().getStepSize());
			nextTime += randomizedHeadway(network.mesoRandom[MesoRandom.Departure]);
		}
	}

}
