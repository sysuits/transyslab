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

import com.transyslab.roadnetwork.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MesoODTable {

	protected double nextTime; // time to read od pairs
	protected int type; // vehicle type
	protected double scale; // scaling factor
	protected List<MesoODCell> cells; // list of OD cells

	public String name; // file name

	public MesoODTable() {
		this.nextTime = 0;
	}
	// Open trip table file and create a OD Parser
	// public void open(const char *fn = 0);
	// public double read();

	public String getName() {
		return name;
	}

	public double scale() {
		return scale;
	}
	public double getNextTime() {
		return nextTime;
	}
	public void setNextTime(double t) {
		nextTime = t;
	}
	public List<MesoODCell> getCells() {
		return cells;
	}
	public int nCells() {
		return cells.size();
	}
	public int getType() {
		return type;
	}
	// Called by parser to setup the od matrix and update time for next
	// vehicles departure.
	public int init(double s, int t, double f) {
		if (nextTime > -86400.0) {
			/*
			 * cout << nCellsParsed_ << " OD cells (type " << type <<
			 * ") parsed at " << theSimulationClock->convertTime(nextTime) <<
			 * "." << endl;
			 */
		}
		// 按时段读入od数据
		// 清空上一时段遗留的odcell
		if (cells != null)
			cells = null;
		cells = new ArrayList<MesoODCell>();
		nextTime = s;
		type = t;
		scale = f;
		return 0;
	}
	public int init(double start) {
		return init(start, type, scale);
	}
	// Read OD table and return next updating time.
	public void insert(MesoODCell cell) {
		if (cell.rate() > Constants.RATE_EPSILON) {
			//
			cells.add(cell);
		}
	}


	// Emit vehicles until no more vehicle wants to departure at this
	// time. When vehicles are created, the corresponding OD cell is
	// dropped down in the list based on the departure time for next
	// vehicle.
	public void emitVehicles(double currentTime, MesoNetwork theNetwork) {
		MesoODCell i;
		MesoODCell c;

		// 必须按cell.nextTime_从小到大排序，每次都从nextTime最小的cell开始发车
		// 若大的在前会导致nextTime>currentTime，符合条件的小的cell无法发车
		// 调用了emitVehicle后，cell.nextTime会更新，需要重新排序
		//
		while ((i = cells.get(0)) != null && (c = i) != null
				&& c.nextTime() <= currentTime) {
			cells.remove(i);
			c.emitVehicles(theNetwork,currentTime);
			//
			cells.add(i);
			sortODCell();
		}
	}
	public void sortODCell() {
		Comparator<MesoODCell> comparator = new Comparator<MesoODCell>() {
			@Override
			public int compare(MesoODCell c1, MesoODCell c2) {
				// 按nextTime_排序
				return (c1.nextTime() < c2.nextTime() ? -1 : (c1.nextTime() == c2.nextTime() ? 0 : 1));
			}
		};
		Collections.sort(cells, comparator);
	}

}
