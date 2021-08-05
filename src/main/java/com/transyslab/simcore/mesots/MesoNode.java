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

import java.util.HashMap;

import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.Node;


public class MesoNode extends Node {

	public MesoNode() {
	}
	protected static int[][] supply_ = new int[3][]; // for each movement
	private static int[][] demand_ = new int[3][]; // for each movement
	/*
	public void calcStaticInfo()
	{
		superCalcStaticInfo();

		// Remaining capacity for each turning movement
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();

		supply_[threadid] = new int[nUpLinks() * nDnLinks()];
	}
	public void calcCapacities() {
		calcDemands();
		calcSupplies();
	}

	// Calculate the number of vehicles that may arrive at this node if
	// the capacities are not constrained. The results are stored in a
	// static array "demand_".
	private void calcDemands() {
		int i, j, n = nUpLinks(), m = nDnLinks();
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();

		Link link = new Link();
		MesoSegment ps = new MesoSegment();
		MesoTrafficCell cell = new MesoTrafficCell();
		MesoVehicle vehicle = new MesoVehicle();
		float dt = (float) MesoParameter.getInstance().getUpdateStepSize();
		for (i = 0, j = n * m; i < j; i++) {
			demand_[threadid][i] = 0;
		}

		for (i = 0; i < n; i++) {
			ps = (MesoSegment) getUpLink(i).getEndSegment();
			cell = ps.firstCell();
			while (cell != null) {
				vehicle = cell.firstVehicle();
				while (vehicle != null) {
					link = vehicle.getNextLink();
					float pos = vehicle.getDistance() - vehicle.getCurrentSpeed() * dt;
					if (link != null && pos <= 0.0) {
						j = link.getDnIndex();
						demand_[threadid][i * m + j]++;
					}
					vehicle = vehicle.trailing();
				}
				cell = cell.trailing();
			}
		}
	}

	// Given the demands for each turning movement, this function
	// calculates the number of vehicles that can pass this node for each
	// turnning movement. The result is stored in a static array
	// "supply_".
	private void calcSupplies() {
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		int num_ups = nUpLinks();
		int num_dns = nDnLinks();
		int i, j, k;

		// Capacity for a particular movement may depends on the geometry,
		// flows of all the turning movements, especially the conflict
		// movements.

		// WE MAY USE HCM MODEL. WILL BE CODED LATER.

		for (i = 0; i < num_ups; i++) {
			for (j = 0; j < num_dns; j++) {
				k = i * num_dns + j;
				if (((MesoLink) getDnLink(j)).isJammed() == 1) {// ÔÚMesoSegment£¬Returns
																// 1 if this
																// link can NOT
																// accept more
																// vehicles at
																// the upstream
					supply_[threadid][k] = 0;
				}
				else {
					supply_[threadid][k] = 100;
				}
			}
		}
	}*/
}
