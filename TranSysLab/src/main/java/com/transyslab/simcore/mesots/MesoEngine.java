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
	protected int runTimes; // �������д���
	protected double frequency; // 1/step size
	protected double updateTime;
	protected double pathTime;
	protected double stateTime;
	protected double updateStepSize; // update traffic cell variables
	protected double pathStepSize;
	protected int iteration; // id of the iteration
	protected int firstEntry = 1; // simulationLoop�е�һ��ѭ���ı��
	private int parseODID; // OD����ʱ����֯��Ĵ���
	private List<MesoVehicle> snapshotList;//��ʼ֡�����������б�
	private int vhcTableIndex; // ��ȡ��������к�
	private int runMode;// =0:��snapshot��������OD�������������
	                   // =1:��snapshot��������������¼��ʱ����;
					   // =2:snapshot��������OD�������������
	                   // =3:snapshot��������������¼��ʱ������

	// ���㵥Ԫ���ع�
	private MesoNetwork theNetwork;
	private MesoODTable theODTable;
	private MesoVehicleTable theVhcTable;
		
	public MesoEngine(int mode,String rootDir) {
		this.rootDir = rootDir;
		//�����������ģʽ
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
		pathStepSize = Constants.ONE_DAY;// ONE_DAY�궨��
		parseODID = 0;
	}

	public void init() {
		// ͨ��parameter ��ֵ,����ʱ��������300s
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
			// TODO ����CSVά��
			theVhcTable.getVhcList().clear();
			//MesoSetup.ParseVehicleTable();
		}
		//��������
		theNetwork.resetRandSeeds();
			
	}
	// �������������ļ�������MasterFile�����������ļ�����SimulationFile����������ļ���
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
		// TODO ������
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

	// �������
	public void run() {
		//TODO �ع�����
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
					// ������Ӧ�ȣ�������pbest
					pso.getParticle(i).evaMRE(simFlow, simTraTime, realFlow, realTraTime, 0);
					// ����gbest
					if (tempBestFitness_ > pso.getParticle(i).getFitness()) {
						tempBestFitness_ = (float) pso.getParticle(i).getFitness();
						// ���µ������Ž�
						for (int j = 0; j < pso.getDim(); j++) {
							tempBest_[j] = pso.getParticle(i).getPos()[j];
						}
						// Particle.updateGbest(pso.pars_[i].getPos());
					}
				}

				pso.posToLearn(i);

				// ���������ٶ��Լ�λ��
				pso.getParticle(i).updateVel(pso.getParaW(), pso.getParaC1(), pso.getParaPl(), pso.getParaPu(),
						pso.getParaVl(), pso.getParaVu());
				resetBeforeSimLoop();
				simFlow = null;
				simTraTime = null;
			}
		}
		else if (mode == 2) {
			// DE�㷨ͬ��gbest�ı��
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
			//�Ŷ���������
//			MesoNetwork.getInstance().updateParaSdfns(0.45f,0.0f, parameter[3], parameter[2],parameter[0],parameter[1]);
			
//			MesoNetwork.getInstance().updateParaSdfns(0.45f,0.0f, 17.69f,183.95f,2.91f,0.65f);
			//�Ŷ��ĸ�����
//			MesoNetwork.getInstance().updateParaSdfns(0.45f,0.0f, 21.95f,156.25f,1.61f,8.11f);// parameter[0],parameter[1]);
			theNetwork.getSimParameter().setRspLower(30.57f);//parameter[4]);
			theNetwork.getSimParameter().setRspUpper(91.79f);//parameter[5]);
			theNetwork.getSimParameter().updateCSG();
			//�˹��ϳ�����
//			mesonetwork.updateParaSdfns(0.5f,0.0f, 16.67f, 180.0f,parameter[0],parameter[1]);
//			mesonetwork.updateParaSdfns(0.45f,0.0f, 19.76f, 156.21f,2.0f,5.35f);
//			mesonetwork.updateSegFreeSpeed();
			while (simulationLoop() >= 0) {
			}
//			evaMRE();
			evaRMSN();
			resetBeforeSimLoop();
		}
		//�����У���ȡmatlab������������Ⱦ
		else if(mode == 4){
			int tmpframeid =-1;
			for(CSVRecord record: vhcData_){
				vhcDataID_ = Integer.parseInt(record.get(0));
/*				if(tmpframeid!=vhcDataID_){
					tmpframeid = vhcDataID_;
				}
				//�Ӷ���ػ�ȡvehicledata����
				VehicleData vd = VehicleDataPool.getVehicleDataPool().getVehicleData();
				//��¼������Ϣ
//				vd.init(Integer.parseInt(record.get(1)),Float.parseFloat(record.get(2)));
				//��vehicledata����frame
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
		  //����������ͳ��
		  theNetwork.vhcCounter = vhcnum;
		  MesoSegment seg = (MesoSegment) theNetwork.getSegment(0);
		  seg.append(theNetwork.recycleCellList.recycle());
		  seg.getLastCell().initialize(theNetwork.getSimClock().getStepSize());
		  for(int i=0;i<vhcnum-1;i++){
			  if(snapshotList.get(i+1).getDistance()- snapshotList.get(i).getDistance()<theNetwork.getSimParameter().cellSplitGap()){
				  seg.getLastCell().appendSnapshot(snapshotList.get(i),theNetwork.getSimClock(),theNetwork.getSimParameter());
				  if(i==vhcnum-2){
					  //���һ����
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

		// ��ʼ��SimulationClock,�˴�����ʼʱ�䣬����ʱ��
		init();
		// ��ȡ����xml�����ļ�
		XmlParser.parseNetwork(theNetwork, rootDir + "networkA.xml");
		// ����·�����ݺ���֯·����ͬҪ�صĹ�ϵ
		theNetwork.calcStaticInfo();
		// ����·��Ҫ�ؼ�����Ϣ�����ڻ�ͼ
		// MesoNetwork.getInstance().calcGeometircData();
		// �Ƚ���·�����ٽ���OD��OD��Ҫ�õ�·������Ϣ
		//XmlParser.parsePathTable(theNetwork,"");
		// ��ʼ����¼����ʱ��Ķ�����������
		theNetwork.getLinkTimes().initTravelTimes(theNetwork);
		// ��������ʱ������ʱ������ʼ����¼����
		theNetwork.initializeLinkStatistics();
		// ���²�ͬ·�ε����ܺ���
		// MESO_Network.getInstance().setsdIndex();

		XmlParser.parseSensors(theNetwork,rootDir + "sensor.xml");
		
		if(runMode == 2|| runMode ==3){//��SnapShot��������
 
			//��������·���ϵĳ����б�
			  snapshotList = new ArrayList<MesoVehicle>();
			  // TODO �����
			  //XmlParser.ParseSnapshotList(snapshotList);
		}
		if(runMode == 1|| runMode ==3){
			 //����������
			// TODO �����
			 //XmlParser.ParseVehicleTable();
		}
		// ���·����Ϣ
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

		state_ = Constants.STATE_OK;// STATE_OK�궨��,a step is done (clock
									// advanced)
		return 0;
	}

	@Override
	public int simulationLoop() {

		final double epsilon = 1.0E-3;
		// ʵ����·������һ���̶߳�Ӧһ��ʵ��

		double now = theNetwork.getSimClock().getCurrentTime();

		if (firstEntry != 0) {
			firstEntry = 0;
			// ���ü������ʼ������ʱ�� TODO �������ÿ�ʼ���ʱ��Ĳ���
			theNetwork.setDetStartTime(0*3600);
			// This block is called only once just before the simulation gets
			// started.
			theNetwork.resetSegmentEmitTime();
			// �����¼�
			//MesoIncident ic = new MesoIncident();
			//ic.init(1, 33000, 39900, 18, -1.0f);
			if(runMode ==2|| runMode ==3){
				//��ʼ��·�����г�����������Ϣ
				initSnapshotData();
			}

		}

		//��OD�����������
		if(runMode ==0|| runMode ==2){
			
			// Update OD trip tables

			if (theODTable.getNextTime() <= now) {
				// MESO_ODTable.theODTable.read();
				// ����Ӧʱ�ε�OD��Ϣ
				XmlParser.parseODXml(rootDir + "demandA.xml",parseODID,theNetwork);
				theODTable.sortODCell();
				parseODID++;

			}

			// Create vehicles based on trip tables

			theODTable.emitVehicles(now,theNetwork);
		}
		else if(runMode == 1|| runMode ==3){
			//��������¼��ʱ����
			while(vhcTableIndex <theVhcTable.getVhcList().size()
					&& theVhcTable.getVhcList().get(vhcTableIndex).departTime()<=now){
				theVhcTable.getVhcList().get(vhcTableIndex).enterPretripQueue(theNetwork.getSimParameter().simStepSize);
				  vhcTableIndex++;
			 }
		}
		else{
			//error, �붨�巢��ģʽ
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

		//��ǰ֡����������λ����Ϣ�洢��framequeue
		// TODO runModeδ�����Ƿ���ӻ�
		if(theNetwork.vhcCounter!=0 && runMode ==0)
			//theNetwork.recordVehicleData();
		
		
		// ������������г�����λ����Ϣ
		/*
		  try { MesoNetwork.getInstance().outputVhcPosition(); 
		  } catch(IOException e) {
			  e.printStackTrace(); }*/
		 // �����
		theNetwork.detMesure();
		// Advance the clock
		theNetwork.getSimClock().advance(theNetwork.getSimClock().getStepSize());
		if (now > theNetwork.getSimClock().getStopTime() + epsilon) {
			// �����淽����������oracle
			/*
			 * MESO_Network.getInstance().outputModelSegmentDataToOracle();
			 * MESO_Network.getInstance().outputTaskSegmentDataToOracle();
			 * MESO_Network.getInstance().outputModelSensorDataToOracle();
			 * MESO_Network.getInstance().outputTaskSensorDataToOracle();
			 */
			//������������¼
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
