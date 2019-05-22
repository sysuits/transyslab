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

import com.transyslab.commons.io.XmlParser;

import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MacroCharacter;


public class MesoEngine extends SimulationEngine {
	protected String rootDir;
	protected int runTimes; // 仿真运行次数
	protected double frequency; // 1/step size
	protected double updateTime;
	protected double pathTime;
	protected double stateTime;
	protected double updateStepSize; // update traffic cell variables
	protected double pathStepSize;
	protected int iteration; // id of the iteration
	protected int firstEntry = 1; // simulationLoop中第一次循环的标记
	private int parseODID; // OD矩阵按时间组织后的次序
	private List<MesoVehicle> snapshotList;//初始帧的在网车辆列表
	private int vhcTableIndex; // 读取发车表的行号
	private int runMode;// =0:非snapshot启动，按OD流量随机发车；
	                   // =1:非snapshot启动，按过车记录定时发车;
					   // =2:snapshot启动，按OD流量随机发车；
	                   // =3:snapshot启动，按过车记录定时发车；

	// 计算单元化重构
	private MesoNetwork theNetwork;
	private MesoODTable theODTable;
	private MesoVehicleTable theVhcTable;
		
	public MesoEngine(int mode,String rootDir) {
		this.rootDir = rootDir;
		//定义仿真运行模式
		this.runMode = mode;
		theNetwork = new MesoNetwork();
		theNetwork.setMesoRandoms(MesoRandom.create(3));
		if(this.runMode ==0|| this.runMode ==2){
			theODTable = new MesoODTable();
			theNetwork.setOdTable(theODTable);
		}
		vhcTableIndex = 0;
		runTimes = 1;
		updateStepSize = 10;
		theNetwork.getSimParameter().setUpdateStepSize(updateStepSize); // update traffic cell variables
		pathStepSize = Constants.ONE_DAY;// ONE_DAY宏定义
		parseODID = 0;
	}

	public void init() {
		// 通过parameter 赋值,结束时间往后推300s
		theNetwork.getSimClock().init(0*3600,3600, 0.2);
		theNetwork.getSimParameter().setSimStepSize(0.2f);
		double now = theNetwork.getSimClock().getCurrentTime();
		updateTime = now;
		pathTime = now + pathStepSize;
		stateTime = now;
		frequency = (1.0 / theNetwork.getSimClock().getStepSize());
	}

	public void resetBeforeSimLoop() {
		firstEntry = 1;
		theNetwork.getSimClock().init(0*3600,2*3600, 0.2);
		theNetwork.setDetStartTime(0*3600);
		double now = theNetwork.getSimClock().getCurrentTime();
		updateTime = now;
		
		pathTime = now + pathStepSize;
		stateTime = now;
		frequency =  (1.0 / theNetwork.getSimClock().getStepSize());
		theNetwork.resetLinkStatistics();
		theNetwork.clean();
		if(runMode ==0|| runMode ==2){
			parseODID = 1;
			theODTable.setNextTime(0);
		}
		if(runMode == 1){
			vhcTableIndex =0;
			// TODO 利用CSV维护
			theVhcTable.getVhcList().clear();
			//MesoSetup.ParseVehicleTable();
		}
		//重置种子
		theNetwork.resetRandSeeds();
			
	}
	// 读入所有输入文件，包含MasterFile（仿真配置文件）和SimulationFile（仿真参数文件）
	@Override
	public void loadFiles() {
		if (canStart() > 0) {
			loadMasterFile();
		}
		
		loadSimulationFiles();
	}

	@Override
	public MesoNetwork getNetwork(){
		return this.theNetwork;
	}

	@Override
	public int repeatRun() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void close() {

	}

	@Override
	public void stop() {
		// TODO 待补充
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

	// 多次运行
	public void run() {
		//TODO 重构待改
		//theNetwork.updateParaSdfns(0.5,0.0, 19.76, 158.75,2.04,5.35);
		//theNetwork.getSimParameter().setRspLower(30.87f);//parameter[4]);
		//theNetwork.getSimParameter().setRspUpper(91.58f);//parameter[5]);
		//theNetwork.getSimParameter().updateCSG();
		while (simulationLoop() >= 0) {

		}
		/*
		else if (mode == 1) {

			HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
			int threadid = hm.get(Thread.currentThread().getName()).intValue();

			for (int i = threadid * pso.getPcount() / Constants.THREAD_NUM; i < threadid * pso.getPcount()
					/ Constants.THREAD_NUM + pso.getPcount() / Constants.THREAD_NUM; i++) {
				if (pso.getParticle(i).isFeasible()) {
//					theNetwork.updateSdFns(pso.getParticle(i).getAlpha(),
//							pso.getParticle(i).getBeta());
					// MESO_Parameter.getInstance().setRspLower(pso.getParticle(i).getDLower());
					// MESO_Parameter.getInstance().setRspUpper(pso.getParticle(i).getDUpper());
					// MESO_Parameter.getInstance().updateCSG();
					while (simulationLoop() >= 0) {
					}
					// 计算适应度，并更新pbest
					pso.getParticle(i).evaMRE(simFlow, simTraTime, realFlow, realTraTime, 0);
					// 更新gbest
					if (tempBestFitness_ > pso.getParticle(i).getFitness()) {
						tempBestFitness_ = (float) pso.getParticle(i).getFitness();
						// 更新当代最优解
						for (int j = 0; j < pso.getDim(); j++) {
							tempBest_[j] = pso.getParticle(i).getPos()[j];
						}
						// Particle.updateGbest(pso.pars_[i].getPos());
					}
				}

				pso.posToLearn(i);

				// 更新粒子速度以及位置
				pso.getParticle(i).updateVel(pso.getParaW(), pso.getParaC1(), pso.getParaPl(), pso.getParaPu(),
						pso.getParaVl(), pso.getParaVu());
				resetBeforeSimLoop();
				simFlow = null;
				simTraTime = null;
			}
		}
		else if (mode == 2) {
			// DE算法同步gbest的标记
			HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
			int threadid = hm.get(Thread.currentThread().getName()).intValue();
			int si = threadid * de.getPopulation() / Constants.THREAD_NUM;
			int ei = threadid * de.getPopulation() / Constants.THREAD_NUM + de.getPopulation() / Constants.THREAD_NUM;
			for (int i = si; i < ei; i++) {
//				theNetwork.updateParaSdfns(0.5f, 0.0f, 16.67f, 180.0f, de.getNewPosition(i)[0], de.getNewPosition(i)[1]);
//				MesoNetwork.getInstance().updateSdFns(de.getNewPosition(i)[2], de.getNewPosition(i)[3]);
//				MesoParameter.getInstance().setRspLower(de.getNewPosition(i)[0]);
//				MesoParameter.getInstance().setRspUpper(de.getNewPosition(i)[1]);
//				MesoParameter.getInstance().updateCSG();
				while (simulationLoop() >= 0) {
				}
//				de.getNewIdvds()[i].evaMRE(simFlow, simTraTime, realFlow, realTraTime, 0);
				evaRMSN();
				de.getNewIdvds()[i].setFitness(objFunction_);
				de.selection(i);
				if (tempBestFitness_ > de.getFitness(i)) {
					tempBestFitness_ = de.getFitness(i);
					// tempIndex_ = i;
					for (int j = 0; j < tempBest_.length; j++) {
						tempBest_[j] = de.getPosPoint(i)[j];
					}

				}
				de.changePos(i);
				resetBeforeSimLoop();
				simFlow = null;
				simTraTime = null;

			}
		}
		else if(mode ==3){

			//0.45f,0.0f, 21.95f, 156.25f,1.61f,6.31f
			//2.2822566,5.56166,154.72292,19.469088,32.80778,91.904686
			//扰动六个参数
//			MesoNetwork.getInstance().updateParaSdfns(0.45f,0.0f, parameter[3], parameter[2],parameter[0],parameter[1]);
			
//			MesoNetwork.getInstance().updateParaSdfns(0.45f,0.0f, 17.69f,183.95f,2.91f,0.65f);
			//扰动四个参数
//			MesoNetwork.getInstance().updateParaSdfns(0.45f,0.0f, 21.95f,156.25f,1.61f,8.11f);// parameter[0],parameter[1]);
			theNetwork.getSimParameter().setRspLower(30.57f);//parameter[4]);
			theNetwork.getSimParameter().setRspUpper(91.79f);//parameter[5]);
			theNetwork.getSimParameter().updateCSG();
			//人工合成数据
//			mesonetwork.updateParaSdfns(0.5f,0.0f, 16.67f, 180.0f,parameter[0],parameter[1]);
//			mesonetwork.updateParaSdfns(0.45f,0.0f, 19.76f, 156.21f,2.0f,5.35f);
//			mesonetwork.updateSegFreeSpeed();
			while (simulationLoop() >= 0) {
			}
//			evaMRE();
			evaRMSN();
			resetBeforeSimLoop();
		}
		//假运行，读取matlab计算结果进行渲染
		else if(mode == 4){
			int tmpframeid =-1;
			for(CSVRecord record: vhcData_){
				vhcDataID_ = Integer.parseInt(record.get(0));
/*				if(tmpframeid!=vhcDataID_){
					tmpframeid = vhcDataID_;
				}
				//从对象池获取vehicledata对象
				VehicleData vd = VehicleDataPool.getVehicleDataPool().getVehicleData();
				//记录车辆信息
//				vd.init(Integer.parseInt(record.get(1)),Float.parseFloat(record.get(2)));
				//将vehicledata插入frame
				try {
					FrameQueue.getInstance().offer(vd, Integer.parseInt(record.get(3)));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}*/
	}

	  public void initSnapshotData(){
		  int vhcnum = snapshotList.size();
		  //在网车辆数统计
		  theNetwork.vhcCounter = vhcnum;
		  MesoSegment seg = (MesoSegment) theNetwork.getSegment(0);
		  seg.append(theNetwork.recycleCellList.recycle());
		  seg.getLastCell().initialize(theNetwork.getSimClock().getStepSize());
		  for(int i=0;i<vhcnum-1;i++){
			  if(snapshotList.get(i+1).getDistance()- snapshotList.get(i).getDistance()<theNetwork.getSimParameter().cellSplitGap()){
				  seg.getLastCell().appendSnapshot(snapshotList.get(i),theNetwork.getSimClock(),theNetwork.getSimParameter());
				  if(i==vhcnum-2){
					  //最后一辆车
					  seg.getLastCell().appendSnapshot(snapshotList.get(i+1),theNetwork.getSimClock(),theNetwork.getSimParameter());
				  }
			  }
			  else{
				  seg.append(theNetwork.recycleCellList.recycle());
				  seg.getLastCell().initialize(theNetwork.getSimClock().getStepSize());
				  seg.getLastCell().appendSnapshot(snapshotList.get(i+1),theNetwork.getSimClock(),theNetwork.getSimParameter());
			  }
		  }
	  }

	public int loadSimulationFiles() {

		// 初始化SimulationClock,此处赋开始时间，结束时间
		init();
		// 读取所有xml输入文件
		XmlParser.parseNetwork(theNetwork, rootDir + "networkA.xml");
		// 读入路网数据后组织路网不同要素的关系
		theNetwork.calcStaticInfo();
		// 计算路网要素几何信息，用于绘图
		// MesoNetwork.getInstance().calcGeometircData();
		// 先解析路径表再解析OD表，OD表要用到路径表信息
		//XmlParser.parsePathTable(theNetwork,"");
		// 初始化记录旅行时间的对象，新增代码
		theNetwork.getLinkTimes().initTravelTimes(theNetwork);
		// 根据旅行时间的输出时间间隔初始化记录数组
		theNetwork.initializeLinkStatistics();
		// 更新不同路段的速密函数
		// MESO_Network.getInstance().setsdIndex();

		XmlParser.parseSensors(theNetwork,rootDir + "sensor.xml");
		
		if(runMode == 2|| runMode ==3){//从SnapShot启动仿真
 
			//解析已在路网上的车辆列表
			  snapshotList = new ArrayList<MesoVehicle>();
			  // TODO 待设计
			  //XmlParser.ParseSnapshotList(snapshotList);
		}
		if(runMode == 1|| runMode ==3){
			 //解析车辆表
			// TODO 待设计
			 //XmlParser.ParseVehicleTable();
		}
		// 输出路网信息
		/*try {
			MesoNetwork.getInstance().outputSegments();
		}
		catch (IOException e) {
			e.printStackTrace();
		}*/

		parseODID = 1;
		return 0;
	}

	public int loadMasterFile() {

		state_ = Constants.STATE_OK;// STATE_OK宏定义,a step is done (clock
									// advanced)
		return 0;
	}

	@Override
	public int simulationLoop() {

		final double epsilon = 1.0E-3;
		// 实例化路网对象，一个线程对应一个实例

		double now = theNetwork.getSimClock().getCurrentTime();

		if (firstEntry != 0) {
			firstEntry = 0;
			// 设置检测器开始工作的时间 TODO 增加设置开始检测时间的参数
			theNetwork.setDetStartTime(0*3600);
			// This block is called only once just before the simulation gets
			// started.
			theNetwork.resetSegmentEmitTime();
			// 加载事件
			//MesoIncident ic = new MesoIncident();
			//ic.init(1, 33000, 39900, 18, -1.0f);
			if(runMode ==2|| runMode ==3){
				//初始化路网已有车辆的所有信息
				initSnapshotData();
			}

		}

		//按OD流量随机发车
		if(runMode ==0|| runMode ==2){
			
			// Update OD trip tables

			if (theODTable.getNextTime() <= now) {
				// MESO_ODTable.theODTable.read();
				// 读对应时段的OD信息
				XmlParser.parseODXml(rootDir + "demandA.xml",parseODID,theNetwork);
				theODTable.sortODCell();
				parseODID++;

			}

			// Create vehicles based on trip tables

			theODTable.emitVehicles(now,theNetwork);
		}
		else if(runMode == 1|| runMode ==3){
			//按过车记录定时发车
			while(vhcTableIndex <theVhcTable.getVhcList().size()
					&& theVhcTable.getVhcList().get(vhcTableIndex).departTime()<=now){
				theVhcTable.getVhcList().get(vhcTableIndex).enterPretripQueue(theNetwork.getSimParameter().simStepSize);
				  vhcTableIndex++;
			 }
		}
		else{
			//error, 请定义发车模式
		}

		// ENTER VEHICLES INTO THE NETWORK

		// Move vehicles from vitual queue into the network if they could
		// enter the network at present time.

		theNetwork.enterVehiclesIntoNetwork();

		// UPDATE PHASE : Calculate the density of speeds of all TCs in the
		// network based on their current state.

		// Every update step we reset segment capacity balance to 0 to
		// prevent consumption of accumulated capacities.

		if (now >= updateTime) {
			// UpdateCapacities(); // update incident related capacity
			/*
			 * if(now>=ic_.getStartTime()&&ic_.getNeedChange()){
			 * ic_.updateCapacity(); ic_.setNeedChange(false); }
			 * if(now>=ic_.getEndTime()&&ic_.getNeedResume()){
			 * ic_.resumeCapacity(); ic_.setNeedResume(false); }
			 */
			theNetwork.resetSegmentEmitTime();

			updateTime = now + updateStepSize;
		}

		theNetwork.calcTrafficCellUpSpeed();
		theNetwork.calcTrafficCellDnSpeeds();

		// ADVANCE PHASE : Update position of TCs, move vehicles from TC to
		// TC and create/destroy TCs as necessary.

		theNetwork.advanceVehicles();
		//MesoVehicle.increaseCounter();
		theNetwork.stepCounter++;

		//当前帧在网车辆的位置信息存储到framequeue
		// TODO runMode未包含是否可视化
		if(theNetwork.vhcCounter!=0 && runMode ==0)
			//theNetwork.recordVehicleData();
		
		
		// 输出步长内所有车辆的位置信息
		/*
		  try { MesoNetwork.getInstance().outputVhcPosition(); 
		  } catch(IOException e) {
			  e.printStackTrace(); }*/
		 // 检测器
		theNetwork.detMesure();
		// Advance the clock
		theNetwork.getSimClock().advance(theNetwork.getSimClock().getStepSize());
		if (now > theNetwork.getSimClock().getStopTime() + epsilon) {
			// 将仿真方案结果输出到oracle
			/*
			 * MESO_Network.getInstance().outputModelSegmentDataToOracle();
			 * MESO_Network.getInstance().outputTaskSegmentDataToOracle();
			 * MESO_Network.getInstance().outputModelSensorDataToOracle();
			 * MESO_Network.getInstance().outputTaskSensorDataToOracle();
			 */
			//输出检测器检测记录
			/*
			  try {
				MESO_Network.getInstance().outputSectionRecord();
			} catch (IOException e) {

				e.printStackTrace();
			}*/
			// TODO test ODCell emitvhc
			System.out.print(MesoODCell.emitCounter);
			return (state_ = Constants.STATE_DONE);//  simulation is done
		}
		else
			return state_ = Constants.STATE_OK;
	}
}
