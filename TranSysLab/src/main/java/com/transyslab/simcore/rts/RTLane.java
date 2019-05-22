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

import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.roadnetwork.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;


public class RTLane extends Lane {
	public static final double FREESPEED = 15;//54km/h
	//	private MLPVehicle head_;
//	private MLPVehicle tail_;
//	private double emitTime_;
	//private double capacity = 0.5;
//	private MLPLane upConectLane_;
//	private MLPLane dnConectLane_;
	public int lateralCutInAllowed; // ʮλ����0(1)��ʾ(��)������೵�����ߣ���λ����0(1)��ʾ(��)�����Ҳ೵������
	//	public boolean LfCutinAllowed;	//left cut in allowed = true=������೵���������˳�����
//	public boolean RtCutinAllowed;	//left cut in allowed = true=������೵���������˳�����
	public boolean enterAllowed;	//true=�����󷽣���������ʻ��;false=������������ʻ��(���ڵ�·���)

	protected double queueLength;
	protected double avgSpeed;

	public RTLane(){

		lateralCutInAllowed = 0;
//		LfCutinAllowed = true;
//		RtCutinAllowed = true;
		enterAllowed = true;

	}

	public void calcState(List<VehicleData> queueVehicles, List<VehicleData> movingVehicles){
		queueLength = 0;
		avgSpeed = FREESPEED;
		double sumSpeed = 0;
		int count = 0;
		if(queueVehicles!=null) {// �г��Ŷ�
			// ��λ�����������ҳ��Ŷ�λ��
			Collections.sort(queueVehicles);
			queueLength = getGeoLength() - queueVehicles.get(0).getDistance() + Constants.DEFAULT_VEHICLE_LENGTH;// ����
			sumSpeed += queueVehicles.stream().filter(e->e.getCurSpeed()>0).mapToDouble(VehicleData::getCurSpeed).sum();
			count += queueVehicles.stream().filter(e->e.getCurSpeed()>0).count();
		}
		if(movingVehicles!=null){// �г��ڳ�����
			//avgSpeed = movingVehicles.stream().mapToDouble(VehicleData::getCurSpeed).average().getAsDouble();
			sumSpeed += movingVehicles.stream().mapToDouble(VehicleData::getCurSpeed).sum();
			count += movingVehicles.size();
		}
		if(count != 0)
			avgSpeed = sumSpeed / count;
	}

	public void setAvgSpeed(double avgSpeed){
		this.avgSpeed = avgSpeed;
	}
	public double getAvgSpeed(){
		return this.avgSpeed;
	}
	public double getQueueLength(){
		return this.queueLength;
	}



	@Override
	public RTSegment getSegment(){
		return (RTSegment) segment;
	}


}
