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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.LinkTimes;
import com.transyslab.roadnetwork.Segment;


public class MesoSegment extends Segment {

	protected int nCells; // num of TCs

	protected MesoTrafficCell firstCell; // first downstream traffic cell
	protected MesoTrafficCell lastCell; // last upstream traffic cell
	protected MesoSdFn sdFunction;
	private double capacity; // default capacity (vps)
	private double emitTime; // time to move next vehicle

	private double density; // current density
	private double speed; // current speed

	private List<Double> densityList;
	private List<Double> speedList;
	private List<Integer> flowList;

	public MesoSegment() {
		nCells = 0;
		firstCell = null;
		lastCell = null;
		densityList = new ArrayList<Double>();
		speedList = new ArrayList<Double>();
		flowList = new ArrayList<Integer>();
		sdFunction = new MesoSdFnNonLinear();
	}

	@Override
	public MesoSegment getUpSegment() {
		return (MesoSegment) super.getUpSegment();
	}
	public MesoSegment getDnStream() {
		return (MesoSegment) super.getDnSegment();
	}

	public int nVehicles() {
		int num = 0;
		MesoTrafficCell cell = firstCell;
		while (cell != null) {
			num += cell.nVehicles();
			cell = cell.trailing();
		}
		return num;
	}
	public MesoTrafficCell getFirstCell() {
		return firstCell;
	}
	public MesoTrafficCell getLastCell() {
		return lastCell;
	}
	public MesoVehicle firstVehicle() {
		if (firstCell != null)
			return firstCell.firstVehicle();
		else
			return null;
	}
	public MesoVehicle lastVehicle() {
		if (lastCell != null)
			return lastCell.lastVehicle();
		else
			return null;
	}


	public MesoTrafficCell firstCell() {
		return firstCell;
	}
	public MesoTrafficCell lastCell() {
		return lastCell;
	}

	public void append(MesoTrafficCell cell) {
		cell.segment = this;

		cell.leading = lastCell;
		cell.trailing = null;

		if (lastCell != null) { // append at end
			lastCell.trailing = cell;
		}
		else { // queue is empty
			firstCell = cell;
		}
		lastCell = cell;
		nCells++;
	}
	public void remove(MesoTrafficCell cell) {
		if (cell.leading != null) { // not the first one
			cell.leading.trailing = cell.trailing;
		}
		else { // first one
			firstCell = cell.trailing;
		}
		if (cell.trailing != null) { // not the last one
			cell.trailing.leading = cell.leading;
		}
		else { // last one
			lastCell = cell.leading;
		}
		nCells--;
	}

	public double density() {
		return density / MesoParameter.densityFactor();
	}

	public double speed() {
		return speed / MesoParameter.speedFactor();
	}

	public int flow() {
		return calcFlow();
	}
	@Override
	public double calcCurrentTravelTime(){
		double min_spd = 2.22f; // 5 mph
		if (length < 50.0) {

			// Speed density function will not work for very short segment

			return (length / freeSpeed);
		}


		double spd = this.sdFunction.densityToSpeed(/*(float) getFreeSpeed(),*/ calcDensity(), nLanes());

		if (spd < min_spd)
			spd = min_spd;
		return (length / spd);
	}
	/*
	 * ------------------------------------------------------------------
	 * Calculate the density of a segment, in vehicle/kilometer
	 * ------------------------------------------------------------------
	 */
	@Override
	public double calcDensity() {
		density = (float) (1000.0 * nVehicles() / (length * nLanes()));
		return (density);// vehicle/km
	}
	public void calcState() {
		density = 1000.0f * nVehicles() / (length * nLanes());
		densityList.add(density);
		if (nVehicles() <= 0) {
			speedList.add(maxSpeed());
		}
		else {
			double sum = 0.0;
			MesoTrafficCell cell = firstCell;
			MesoVehicle pv;
			while (cell != null) {
				pv = cell.firstVehicle();
				while (pv != null) {
					if (pv.getCurrentSpeed() > Constants.SPEED_EPSILON) {
						sum += 1.0f / pv.getCurrentSpeed();
					}
					else {
						sum += 1.0f / Constants.SPEED_EPSILON;
					}
					pv = pv.trailing();
				}
				cell = cell.trailing();
			}
			speed = nVehicles() / sum;
			speedList.add(speed * 3.6f);
		}
		double x = 3.6f * speed * density;
		flowList.add((int)Math.round(x));// vehicle/hour

		// return (density);//vehicle/km
	}

	@Override
	public double calcSpeed() {
		if (nVehicles() <= 0) {
			return (speed = maxSpeed());
		}
		float sum = 0.0f;
		MesoTrafficCell cell = firstCell;
		MesoVehicle pv;
		while (cell != null) {
			pv = cell.firstVehicle();
			while (pv != null) {
				if (pv.getCurrentSpeed() > Constants.SPEED_EPSILON) {
					sum += 1.0f / pv.getCurrentSpeed();
				}
				else {
					sum += 1.0f / Constants.SPEED_EPSILON;
				}
				pv = pv.trailing();
			}
			cell = cell.trailing();
		}
		speed = nVehicles() / sum;
		return (speed * 3.6f);// km/hour
	}
	@Override
	public int calcFlow() {
		double x = 3.6f * speed * density;
		return (int) (x + 0.5);// vehicle/hour
	}

	/*
	 * Calculate cell variables that do NOT depend on other cells. This
	 * functions is called by a function with the same name in class
	 * MESO_Network.
	 */
	public void calcTrafficCellUpSpeed() {

	}

	/*
	 * Calculate cell variables that depend on other cells. This functions is
	 * called by a function with the same name in class MESO_Network.
	 */
	public void calcTrafficCellDnSpeeds() {

	}

	public int isJammed() {
		if (lastCell == null) { // nobody in the segment
			return 0;
		}
		else {
			return lastCell.isJammed();
		}
	}

	// Move vehicles based on their speeds


	public double maxSpeed() {
		return freeSpeed;
	}
	public void updateFreeSpeed(){
		this.freeSpeed = this.sdFunction.freeSpeed;
	}
	public double defaultCapacity() {
		double vph = this.sdFunction.getCapacity();
		return nLanes() * vph;
	}

	// Update number of vehicle allowed to move out.

	public void resetEmitTime(double currentTime) { // once every update step
		emitTime = currentTime;
		scheduleNextEmitTime();
	}

	public void scheduleNextEmitTime() {
		if (capacity > 1.E-6) {
			emitTime += 1.0 / capacity;
		}
		else {
			emitTime = Constants.DBL_INF;
		}
	}

	public int isMoveAllowed(double currentTime) {
		if (currentTime >= emitTime)
			return 1;
		else
			return 0;
	}

	// Add cap to current capacity

	public void addCapacity(double cap_delta, double currentTime) {
		setCapacity( (cap_delta + capacity),currentTime);
	}

	// Set current capacity to cap

	public void setCapacity(double cap,double currentTime) {
		double maxcap = defaultCapacity();
		if (cap < 0.0) {
			capacity = 0.0;
		}
		else if (cap > maxcap) {
			capacity = maxcap;
		}
		else {
			capacity = cap;
		}
		resetEmitTime(currentTime);
	}

	public void calcStaticInfo(double currentTime) {

		//此处赋值freeSpeed
		freeSpeed = this.sdFunction.getFreeSpeed();
		density = 0.0f;
		speed = maxSpeed();
		// Set to the default maximum capacity
		setCapacity(defaultCapacity(),currentTime);
	}


	@Override
	public void outputToOracle(PreparedStatement ps) throws SQLException {
		int num = flowList.size();
		for (int i = 0; i < num; i++) {
			// TODO date 未实现
			Date date = null;
			// simtaskid 写死，注意更改
			ps.setInt(1, 5);
			ps.setLong(2, getId());
			ps.setDate(3, new java.sql.Date(date.getTime()));
			ps.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
			ps.setInt(4, nLanes());
			ps.setInt(5, flowList.get(i));
			ps.setDouble(6, speedList.get(i));
			ps.setDouble(7, densityList.get(i));
			ps.addBatch();
		}
		ps.executeBatch();
	}
	/*
	@Override
	public void outputVhcPosition() throws IOException {
		StringBuilder sb = new StringBuilder();
		int frameid = (int) Math
				.round((SimulationClock.getInstance().getCurrentTime() - SimulationClock.getInstance().getStartTime())
						/ SimulationClock.getInstance().getStepSize());
		double s, l, vx, vy;
		MesoTrafficCell cell = firstCell;
		while (cell != null) {
			MesoVehicle vehicle = cell.firstVehicle;
			while (vehicle != null) {
				l = getLength();
				s = l - vehicle.getDistance();
				vx = startPnt.getLocationX() + s * (endPnt.getLocationX() - startPnt.getLocationX()) / l;
				vy = startPnt.getLocationY() + s * (endPnt.getLocationY() - startPnt.getLocationY()) / l;
				sb.append(frameid).append(",");
				sb.append(getId()).append(",");
				sb.append(vehicle.getId()).append(",");
				sb.append(vx).append(",");
				sb.append(vy).append("\n");
				vehicle = vehicle.trailing;
			}
			cell = cell.trailing;
		}
		String filepath = "E:\\OutputPosition.txt";
		FileOutputStream out = new FileOutputStream(filepath, true);
		OutputStreamWriter osw = new OutputStreamWriter(out, "utf-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(sb.toString());
		bw.close();

	}*/
}
