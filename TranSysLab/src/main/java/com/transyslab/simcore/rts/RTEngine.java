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

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.io.NetworkCreator;
import com.transyslab.commons.io.XmlParser;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.*;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class RTEngine extends SimulationEngine{
	//引用路网结构
	private RTNetwork rtNetwork;

	//引擎运行输入文件配置
	private HashMap<String,String> runProperties;
	//
	//引擎运行时间设置信息
	private double timeStart;
	private double timeEnd;
	private double timeStep;
	private String rootDir;
	private Configuration config;
	private CSVParser frameParser;
	private int curFrameId;
	private LocalTime frameTime;
	private boolean firstEntry;
	private boolean isStop;
	public static boolean isState;

	public RTEngine(){
		curFrameId = -1;
		rtNetwork = new RTNetwork();
		runProperties = new HashMap<>();
		firstEntry = true;
		isStop = false;
		isState = false;
	}
	public RTEngine(String masterFilePath) {
		this();
		rootDir = new File(masterFilePath).getParent() + "/";
		parseProperties(masterFilePath);
	}
	private void parseProperties(String configFilePath) {
		config = ConfigUtils.createConfig(configFilePath);

		//input files
		runProperties.put("roadNetworkPath", rootDir + config.getString("roadNetworkPath"));
		runProperties.put("sensorPath", rootDir + config.getString("sensorPath"));
		String tmp = config.getString("extVhcPath");
		runProperties.put("extVhcPath", tmp==null || tmp.equals("") ? null : rootDir + tmp);
		runProperties.put("signalPlan", rootDir + config.getString("signalPlan"));
		//time setting
		LocalTime stime = LocalTime.parse(config.getString("timeStart"),DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss"));
		LocalTime etime = LocalTime.parse(config.getString("timeEnd"),DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss"));
		timeStart = stime.toSecondOfDay();
		timeEnd = etime.toSecondOfDay();

		timeStep = Double.parseDouble(config.getString("timeStep"));

	}
	@Override
	public int simulationLoop() {
		return 0;
	}
	public void run(){
		// frameid, vhcid, distance, laneid
		// 数据已按帧号排序
		CSVRecord curRecord;
		List<VehicleData> vds = new ArrayList<>();
		int frameConter = 1;
		Iterator<CSVRecord> frameIterator = frameParser.iterator();
		while(frameIterator.hasNext()){
			curRecord =  frameIterator.next();
			//int frameid = Integer.parseInt(curRecord.get(0));
			LocalTime time = LocalTime.parse(curRecord.get(3),DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss.SSS"));
			if(firstEntry) {
				frameTime = time;
				firstEntry = false;
			}
			String vhcId = curRecord.get(0);
			double distance = Double.parseDouble(curRecord.get(5));
			int laneid = Integer.parseInt(curRecord.get(4));
			int flag = Integer.parseInt(curRecord.get(6));
			boolean queueFlag = false;
			if(flag == 1)
				queueFlag = true;
			String turn = curRecord.get(8);
			double speed = Double.parseDouble(curRecord.get(7));
			boolean onConnector;
			if(Integer.parseInt(curRecord.get(9)) == 0)
				onConnector = false;
			else
				onConnector = true;
			VehicleData vd = new VehicleData();
			if(onConnector)
				vd.init(vhcId,rtNetwork.findConnector(laneid), Constants.DEFAULT_VEHICLE_LENGTH,distance,speed,"B",queueFlag,true);
			else
				vd.init(vhcId,rtNetwork.findLane(laneid), Constants.DEFAULT_VEHICLE_LENGTH,distance,speed,turn,queueFlag,true);
			if(frameTime.compareTo(time)!=0){//新的一帧
				if(!vds.isEmpty()){
					if (isStop) {
						forceReset();
						isStop = false;
						// 跳出循环
						break;
					}
					if(!isState) {
						rtNetwork.renderVehicle(vds,frameTime.toNanoOfDay()/1000_000_000.0);
					}
					else{
						//if(frameConter%30 == 0)
						rtNetwork.renderState(vds,frameTime.toNanoOfDay()/1000_000_000.0);
					}
					rtNetwork.getSimClock().advance(timeStep);
					vds.clear();
				}
				frameConter ++;
				frameTime = time;
			}
			vds.add(vd);
		}
		//mlpNetwork.setArrowColor();
	}
	@Override
	public void loadFiles() {
		loadSimulationFiles();
		// 读取外部车辆轨迹数据
		try {
			frameParser = CSVUtils.getCSVParser(runProperties.get("extVhcPath"),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SimulationClock clock = rtNetwork.getSimClock();
		clock.init(timeStart, timeEnd, timeStep);
	}
	private void loadSimulationFiles(){
		// 读取路网xml
		//XmlParser.parseNetwork(rtNetwork, runProperties.get("roadNetworkPath"));
		try {
			NetworkCreator.readDataFromDB(rtNetwork,"4927,4893,4944,4808,4806,4835,4694,4697,4706",true);
			// 读入路网数据后组织路网不同要素的关系
			rtNetwork.calcStaticInfo();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// TODO 检测器
		//读入配时方案
		if (!(config.getString("signalPlan")==null||config.getString("signalPlan").equals("")))
			readSignalPlan(runProperties.get("signalPlan"));
	}
	@Override
	public RoadNetwork getNetwork() {
		return rtNetwork;
	}

	@Override
	public int repeatRun() {
		return 0;
	}

	@Override
	public void close() {
		if(frameParser!=null) {
			try {
				frameParser.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		isStop = true;
	}
	public void forceReset(){
		this.firstEntry = true;
		this.curFrameId = -1;
		try {
			this.frameParser.close();
			this.frameParser = CSVUtils.getCSVParser(runProperties.get("extVhcPath"),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public HashMap<String, List<MacroCharacter>> getEmpMap() {
		return null;
	}

	@Override
	public HashMap<String, List<MacroCharacter>> getSimMap() {
		return null;
	}

	@Override
	public int countRunTimes() {
		return 0;
	}

}
