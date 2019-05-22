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

package com.transyslab.simcore.mlp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.transyslab.commons.io.*;
import com.transyslab.gui.EngineEvent;
import com.transyslab.roadnetwork.SignalPlan;
import com.transyslab.roadnetwork.SignalStage;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;

import javax.swing.event.EventListenerList;


public class MLPEngine extends SimulationEngine{

    public boolean seedFixed;
    public long runningSeed;
	public boolean displayOn = false;
	protected double updateTime_;
	protected double LCDTime_;
	protected double statTime_;
	protected double loadTime;
	private boolean firstEntry; // simulationLoop�е�һ��ѭ���ı��
	protected boolean needRndETable; //needRndETable==true,������ɷ�����needRndETable==false�����ļ����뷢����
	protected TXTUtils loopRecWriter;
	protected boolean rawRecOn;
	protected TXTUtils trackWriter;
	protected boolean trackOn;
	protected TXTUtils infoWriter;
	protected boolean infoOn;
	protected boolean statRecordOn;
	private HashMap<String, List<MacroCharacter>> empMap;
	private HashMap<String, List<MicroCharacter>> empMicroMap;
	private HashMap<String, List<MacroCharacter>> simMap;
	private int mod;//�ܼ����д�������������������ź�ʱ����
	private boolean stopSignal;

	//����·���ṹ
	private MLPNetwork mlpNetwork;

	//�������������ļ�����
	private HashMap<String,String> runProperties;
	//��������ʱ��������Ϣ
	private double timeStart;
	private double timeEnd;
	private double timeStep;
	//�������в���
	private int repeatTimes;

	String rootDir;
	String emitSource;

	private Configuration config;

	protected int status;

	public String fileOutTag;

	public int getStatus() {
		return status;
	}

	public InterConstraints interConstraints; //��ÿ���趨�۲����ʱ��ʼ��

	private MLPEngine(){
		master_ = null;
		state_ = Constants.STATE_NOT_STARTED;
		mode_ = 0;
		breakPoints_ = null;
		nextBreakPoint_ = 0;

		firstEntry = true;
		mod = 0;

		mlpNetwork = new MLPNetwork();
		runProperties = new HashMap<>();
		fileOutTag = "";

		stopSignal = false;
	}

	public MLPEngine(String masterFilePath) {
		this();
		rootDir = new File(masterFilePath).getParent() + "/";
		parseProperties(masterFilePath);
		//todo: �������ܼ���
		JdbcUtils.setPropertiesFileName(masterFilePath);
	}


	private void parseProperties(String configFilePath) {
		config = ConfigUtils.createConfig(configFilePath);

		//input files
		runProperties.put("roadNetworkPath", rootDir + config.getString("roadNetworkPath"));
		runProperties.put("signalPlan", rootDir + config.getString("signalPlan"));
		runProperties.put("sensorPath", rootDir + config.getString("sensorPath"));
		String tmp = config.getString("empDataPath");
		runProperties.put("empDataPath", tmp==null || tmp.equals("") ? null : rootDir + tmp);
		tmp = config.getString("empMicroDataPath");
		runProperties.put("empMicroDataPath", tmp==null || tmp.equals("") ? null : rootDir + tmp);
		runProperties.put("outputPath", rootDir + config.getString("outputPath"));
		if (!new File(runProperties.get("outputPath")).exists())
			System.err.println("output dir does NOT exist.");

		runProperties.put("emitSourceType", config.getString("emitSourceType"));
		if (runProperties.get("emitSourceType").equals("FILE"))
			runProperties.put("emitSource", rootDir + config.getString("emitSource"));
		else
			runProperties.put("emitSource", config.getString("emitSource"));

		//time setting
//		timeStart = Double.parseDouble(config.getString("timeStart"));
//		timeEnd = Double.parseDouble(config.getString("timeEnd"));
		timeStart = LocalTime.parse(config.getString("timeStart"),DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss")).toSecondOfDay();
		timeEnd = LocalTime.parse(config.getString("timeEnd"),DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss")).toSecondOfDay();
		timeStep = Double.parseDouble(config.getString("timeStep"));

		//the value will be false if config.getString() returns null
		needRndETable = Boolean.parseBoolean(config.getString("needRndETable"));
		rawRecOn = Boolean.parseBoolean(config.getString("rawRecOn"));
		trackOn = Boolean.parseBoolean(config.getString("trackOn"));
		infoOn = Boolean.parseBoolean(config.getString("infoOn"));
		statRecordOn = Boolean.parseBoolean(config.getString("statRecordOn"));
		seedFixed = Boolean.parseBoolean(config.getString("seedFixed"));
		runningSeed = seedFixed ? Long.parseLong(config.getString("runningSeed")) : 0l;

		//Statistic Output setting
		getSimParameter().statWarmUp = Double.parseDouble(config.getString("statWarmUp"));//set time to Parameter
		getSimParameter().statStepSize = Double.parseDouble(config.getString("statTimeStep"));
		//���������loadfiles���ٽ��г�ʼ������ǰֻ��String����
		runProperties.put("statLinkIds",config.getString("statLinkIds"));
		runProperties.put("statDetNames",config.getString("statDetNames"));

		/*//repeatRun setting
		repeatTimes = Integer.parseInt(config.getString("repeatTimes"));
		String[] parasStrArray = config.getString("obParas").split(",");
		ob_paras = new double[parasStrArray.length];
		for (int i = 0; i<parasStrArray.length; i++) {
			ob_paras[i] = Double.parseDouble(parasStrArray[i]);
		}

		//read-in free parameters setting
		String[] freeStrArray = config.getString("freeParas").split(",");
		free_paras = new double[freeStrArray.length];
		for (int i = 0; i<freeStrArray.length; i++) {
			free_paras[i] = Double.parseDouble(freeStrArray[i]);
		}*/
		//other parameters
		runProperties.put("lcBufferTime",config.getString("lcBufferTime"));
		runProperties.put("lcdStepSize",config.getString("lcdStepSize"));
		runProperties.put("lcSensitivity",config.getString("lcSensitivity"));

		//average mode
		runProperties.put("avgMode",config.getString("avgMode"));
	}

	@Override
	public int simulationLoop() {
		final double epsilon = 1.0E-3;
		long t1 = System.currentTimeMillis();

		if (firstEntry) {
			// This block is called only once just before the simulation gets started.
			firstEntry = false;
			//ȷ����Simloopǰִ��resetBeforeSimloop
			resetBeforeSimLoop();
		}

		double now = mlpNetwork.getSimClock().getCurrentTime();
		
		if (now >= updateTime_){
			//������������У��������ǿ������
			mlpNetwork.renewSysRandSeed();
			mlpNetwork.resetReleaseTime();
			updateTime_ = now + ((MLPParameter) mlpNetwork.getSimParameter()).updateStepSize_;
			//update event
			informEngineListeners(new EngineEvent(this, EngineEvent.UPDATE));
		}

		if (now >= statTime_){
			double stepSize = ((MLPParameter) mlpNetwork.getSimParameter()).statStepSize;
			mlpNetwork.sectionStatistics(statTime_ - stepSize, now, config.getString("avgMode").equals("harmonic") ? Constants.HARMONIC_MEAN : Constants.ARITHMETIC_MEAN);//����ʹ�õ��;�ֵ
			mlpNetwork.linkStatistics(statTime_ - stepSize, now);
			statTime_ = now + stepSize;
		}

		if (now >= loadTime) {
			double loadTimeStep = 3600 * 12;
			String sourceType = runProperties.get("emitSourceType");
			if (needRndETable) {
				//TODO: δ��ɴ����ݿ��������
				mlpNetwork.genInflowFromFile(runProperties.get("emitSource"), loadTime + loadTimeStep);
			}
			else {
				if (sourceType.equals("SQL"))
					mlpNetwork.loadInflowFromSQL(runProperties.get("emitSource"), loadTime, loadTime + loadTimeStep);
				else if (sourceType.equals("FILE"))
					mlpNetwork.loadInflowFromFile(runProperties.get("emitSource"), loadTime + loadTimeStep);
			}
			loadTime += loadTimeStep;
//			System.out.println(Thread.currentThread().getName() + " loading day " + now/3600/24 );
		}
		
		//���뷢����
		mlpNetwork.loadEmtTable();
		
		//·�����ڳ���������²Ž��м���
		if (mlpNetwork.veh_list.size()>0) {
			//����ʶ��
			mlpNetwork.platoonRecognize();
			//�����뻻������ʱ�̣����л��������복������ʶ��
			if (now >= LCDTime_) {
				for (int i = 0; i < mlpNetwork.nLinks(); i++){
					mlpNetwork.mlpLink(i).lanechange();
				}
				mlpNetwork.eliminateLCDeadLock();
				mlpNetwork.platoonRecognize();
				LCDTime_ = now + ((MLPParameter) mlpNetwork.getSimParameter()).LCDStepSize_;
			}
			//�����ٶȼ���
			for (int i = 0; i < mlpNetwork.nLinks(); i++){
				mlpNetwork.mlpLink(i).move();
			}
			//��Ȧ���
			for (int j = 0; j < mlpNetwork.nSensors(); j++){
				String msg = ((MLPLoop) mlpNetwork.getSensor(j)).detect(now);
				if (rawRecOn) {//���������¼
					loopRecWriter.write(msg);
				}
			}
			//�ڵ��˶�����
			for (int i = 0; i < mlpNetwork.nNodes(); i++) {
				mlpNetwork.mlpNode(i).update();
			}
			//����״̬����(ͬʱ����)
			//·��->�ڵ�
			for (int k = 0; k<mlpNetwork.veh_list.size(); k++) {
				MLPVehicle theVeh = mlpNetwork.veh_list.get(k);
				if (theVeh.updateMove()==Constants.VEHICLE_RECYCLE)
					k -=1;
			}
			//�ڵ�->·��
			//����transpose����
			for (int i = 0; i < mlpNetwork.nNodes(); i++) {
				mlpNetwork.mlpNode(i).dispatchStatedVeh();
			}
			//�����ƽ�
			for (MLPVehicle vehicle : mlpNetwork.veh_list) {
				vehicle.advance();
			}

		}
		
		//���ӻ���Ⱦ
		SimulationClock clock = mlpNetwork.getSimClock();
		int tmp = (int) Math.floor(clock.getCurrentTime()*clock.getStepSize());
        if (displayOn) { // && (tmp%10)==0
            mlpNetwork.recordVehicleData();
//            mlpNetwork.setArrowColor();
		}
		
		//����켣
		if (trackOn) {
			if (!mlpNetwork.veh_list.isEmpty() ) {//&& now - 2*Math.floor(now/2) < 0.001
				int LV;
				int FV;
				for (MLPVehicle v : mlpNetwork.veh_list) {
					LV = 0; FV = 0;
					if (v.leading !=null)
						LV = v.leading.getId();
					if (v.trailing != null)
						FV = v.trailing.getId();
					trackWriter.write(
							now + "," +
								v.rvId + "," +
								v.getId() + "," +
								v.virtualType + "," +
								v.buffer + "," +
								v.lane.getLnPosNum() + "," +
								v.segment.getId() + "," +
								v.link.getId() + "," +
								v.Displacement() + "," +
								v.getCurrentSpeed() + "," +
								LV + "," +
								FV + "," +
								v.resemblance + "," +
								v.diMap.get(v.lane) + "," +
								v.calMLC() + "," +
								v.getPath().getDesNode().getId() +
								/*Thread.currentThread().getName() + "_" + mod + "," +
								LocalDateTime.now() +*/ "\r\n"
					);
				}
			}
		}

//		System.out.println("DEBUG Sim world day: " + (now / 3600.0 / 24 + 1)  );

		clock.advance(clock.getStepSize());
        if (stopSignal) {
        	forceResetEngine();
        	stopSignal = false;
        	return (state_ = Constants.STATE_QUIT);
		}
		if (now > clock.getStopTime() + epsilon) {
			if (infoOn)
				infoWriter.writeNFlush("��������ʵ�������⳵��" + (mlpNetwork.getNewVehID()-1)+"\r\n");
			if (rawRecOn)
				loopRecWriter.closeWriter();
			if (trackOn)
				trackWriter.closeWriter();
			if (infoOn)
				infoWriter.closeWriter();
			if(statRecordOn) {
//				String statFileOut = "src/main/resources/output/loop" + Thread.currentThread().getName() + "_" + mod + ".csv";
//				mlpNetwork.writeStat(statFileOut);
				String outputFileName = runProperties.get("outputPath") + "/LoopRec_" + fileOutTag + Thread.currentThread().getName() +"_"+ mod + ".csv";
				mlpNetwork.writeStat(outputFileName);
			}
			//�������д���+1������firstEntryΪ�棬�Ա��´���ִ��SimLoopʱ�������ò�����
			mod += 1;
			firstEntry = true;
			return (state_ = Constants.STATE_DONE);// STATE_DONE�궨�� simulation
													// is done
		}
		else {
			/*System.out.println(String.valueOf(now/3600));
			if(Math.abs(now/3600-8)<0.001)
				System.out.println("BUG");*/
			if (rawRecOn)
				loopRecWriter.flushBuffer();
			if (trackOn)
				trackWriter.flushBuffer();
			if (infoOn)
				infoWriter.flushBuffer();
			return state_ = Constants.STATE_OK;// STATE_OK�궨��
		}			
	}

	/**
	 * ������������ļ������������ʼ��
	 */
	@Override
	public void loadFiles() {
		//��������ļ�
		loadSimulationFiles();
		//����ʵ���������ڼ���fitness
		readEmpData(runProperties.get("empDataPath"));
		readEmpMicroData(runProperties.get("empMicroDataPath"));
		//�����ʼ��
		initEngine();
	}

	private int loadSimulationFiles(){
		
		//load xml
		//parse xml into parameter & network

		// ��ȡ·��xml
		//XmlParser.parseNetwork(mlpNetwork, runProperties.get("roadNetworkPath"));
		try {
			// �����ݿ����ȫ������
//			NetworkCreator.readDataFromDB(mlpNetwork, "4927,4893,4944,4808,4806,4835,4694,4697,4706", true);
			NetworkCreator.readDataFromDB(mlpNetwork, null, false);
			// ����·�����ݺ���֯·����ͬҪ�صĹ�ϵ
			mlpNetwork.calcDbStaticInfo();
//			// ����·�����ݺ���֯·����ͬҪ�صĹ�ϵ
//			mlpNetwork.calcStaticInfo();
			//������ʱ����
			if (!(config.getString("signalPlan") == null || config.getString("signalPlan").equals("")))
				readSignalPlan(runProperties.get("signalPlan"));
			// ������������
			if (!(config.getString("sensorPath") == null || config.getString("sensorPath").equals("")))
				XmlParser.parseSensors(mlpNetwork, runProperties.get("sensorPath"));
			// ����·���������
			mlpNetwork.initLinkStatMap(runProperties.get("statLinkIds"));
			mlpNetwork.initSectionStatMap(runProperties.get("statDetNames"));
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void readEmpData(String fileName) {
		if (fileName==null){
//			System.out.println("warning: have no empirical data read in");
			return;
		}
		empMap = new HashMap<>();
		String[] headers = {"NAME","FLOW_RATE","SPEED","DENSITY","TRAVEL_TIME"};
		List<CSVRecord> results = null;
		try {
			 results = CSVUtils.readCSV(fileName, headers);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(int i=1;i<results.size();i++){
			String detName = results.get(i).get(0);
			double flow = Double.parseDouble(results.get(i).get(1));
			double speed = Double.parseDouble(results.get(i).get(2));
			double density = Double.parseDouble(results.get(i).get(3));
			double travelTime = Double.parseDouble(results.get(i).get(4));
			List<MacroCharacter> records = empMap.get(detName);
			if (records == null) {
				records = new ArrayList<>();
				empMap.put(detName, records);
			}
			records.add(new MacroCharacter(flow,speed,density,travelTime));
		}
		//readFromLoop(MLPSetup.getLoopData_fileName());
	}
	private void readEmpMicroData(String fileName) {
		if (fileName==null){
//			System.out.println("warning: have no empirical microscopic data read in");
			return;
		}
		empMicroMap = new HashMap<>();
		String[] headers = {"NAME","LANEID","DETTIME","SPEED","HEADWAY"};
		List<CSVRecord> results = null;
		try {
			results = CSVUtils.readCSV(fileName, headers);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(int i=1;i<results.size();i++){
			String detName = results.get(i).get(0);
			int laneId = Integer.parseInt(results.get(i).get(1));
			double detTime = Double.parseDouble(results.get(i).get(2));
			double speed = Double.parseDouble(results.get(i).get(3));
			double headway = Double.parseDouble(results.get(i).get(4));
			List<MicroCharacter> records = empMicroMap.get(detName);
			if (records == null) {
				records = new ArrayList<>();
				empMicroMap.put(detName, records);
			}
			records.add(new MicroCharacter(laneId,detTime,speed,headway));
		}
	}

	private void initEngine(){
		getSimParameter().setSimStepSize(timeStep);
		SimulationClock clock = mlpNetwork.getSimClock();
		clock.init(timeStart, timeEnd, timeStep);

		//applying
		((MLPParameter) mlpNetwork.getSimParameter()).setLCDStepSize(Double.parseDouble(runProperties.get("lcdStepSize")));
		((MLPParameter) mlpNetwork.getSimParameter()).setLCBuffTime(Double.parseDouble(runProperties.get("lcBufferTime")));
		((MLPParameter) mlpNetwork.getSimParameter()).setLCSensitivity(Double.parseDouble(runProperties.get("lcSensitivity")));

		readInTrafficParameters();
	}

	/**
	 * ��������ʱ�� ʱ����صĲ��� ��·��״̬
	 * ע�⣺��Ҫ�������ӡ���������·��״̬���������صĲ�������Ҫ��ִ�д˺���ǰ���С�
	 */
	private void resetBeforeSimLoop() {
		SimulationClock clock = mlpNetwork.getSimClock();
		clock.resetTimer();
		double now = clock.getCurrentTime();
		updateTime_ = now;
		LCDTime_ = now;
		statTime_ = now + getSimParameter().statWarmUp + getSimParameter().statStepSize; //��һ��ͳ��ʱ��Ϊ������ʱ��+warmUp+ͳ�Ƽ��
		loadTime = now;

		//Network״̬���貢׼��������
		if (!seedFixed)
			runningSeed = System.currentTimeMillis();
		mlpNetwork.resetNetwork(runningSeed);

		//establish writers
		String threadName = Thread.currentThread().getName();
		if (rawRecOn) {
			loopRecWriter = new TXTUtils(runProperties.get("outputPath") + "/" + "loop" + fileOutTag + threadName + "_" + mod + ".csv");
			loopRecWriter.write("DETNAME,TIME,VID,VIRTYPE,SPD,POS,LINK,LOCATION\r\n");
		}
		if (trackOn) {
			trackWriter = /*new DBWriter("insert into simtrack(time, rvid, vid, virtualIdx, buff, lanePos, segment, link, displacement, speed, lead, trail, tag, create_time) " +
					"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")*/
					new TXTUtils(runProperties.get("outputPath") + "/" + "track" + fileOutTag + threadName + "_" + mod + ".csv");
			trackWriter.write("TIME,RVID,VID,VIRTUALIDX,BUFF,LANEPOS,SEGMENT,LINK,DISPLACEMENT,SPEED,LEAD,FOLLOWER,IN_PLATOON,DI,P_MLC,TNODE\r\n");
		}
		if (infoOn)
			infoWriter = new TXTUtils(runProperties.get("outputPath") + "/" + "info" + fileOutTag + threadName + "_" + mod + ".txt");
		//��Ϣͳ�ƣ�������
		if (infoOn) {
			int total = 0;
			for (int i = 0; i < mlpNetwork.nLinks(); i++) {
				List<Inflow> IFList = mlpNetwork.mlpLink(i).getInflow();
				int tmp = IFList.size();
				total += tmp;
				for (int j = 0; j < tmp; j++)
					infoWriter.write(IFList.get(j).time + ",1\r\n");
			}
			infoWriter.writeNFlush("���������ʵ���� " + total + "\r\n");
		}
	}

	public void forceResetEngine() {
		firstEntry = true;
		resetBeforeSimLoop();
	}

	private void setObservedParas (double qm, double vf_CF, double vf_SD, double vp, double kJLower, double kJUpper){

		MLPParameter allParas = (MLPParameter) mlpNetwork.getSimParameter();

		//����ͨ������������parameter�е�capacity
		allParas.capacity = qm;
		mlpNetwork.setOverallCapacity(qm);//·�ε�����ÿ��ͨ������

		//·���˶�����
		int mask = 0;
		mask |= 1<<(1-1);//vf_SD
		mask |= 1<<(6-1);//vf_CF
		mlpNetwork.setOverallSDParas(new double[] {vf_SD, vf_CF}, mask);

		//������������
		allParas.setPhyLim(vp);

		interConstraints = new InterConstraints(kJUpper,kJLower,qm,vf_CF,vf_SD);
	}

	/**
	 * ���÷ǹ۲�������ڲ�����
	 * ���������������������ȫ�����ġ����@setParas
	 */
	private void setOptParas(double kJam, double ts, double xc, double alpha, double beta, double gamma1, double gamma2) {

		MLPParameter allParas = (MLPParameter) mlpNetwork.getSimParameter();
		allParas.limitingParam_[1] = (float) ts;
		allParas.CF_FAR = (float) xc;
		int mask = 0;
		mask |= 1<<(3-1);//kJam
		mask |= 1<<(4-1);//alpha
		mask |= 1<<(5-1);//beta
		mlpNetwork.setOverallSDParas(new double[] {kJam, alpha, beta}, mask);//kJam, alpha, beta

		//����Kjam ���� leff �� CF_near ��һ���� (CF �� MS model �Ĳ���һ����)
		allParas.limitingParam_[0] = (float) (1.0/kJam - MLPParameter.VEHICLE_LENGTH);
		allParas.CF_NEAR = allParas.limitingParam_[0];//������kjam�Ǻ�

		//��ʱ�޸� tmp alter
		if (!Double.isNaN(gamma1) && !Double.isNaN(gamma2))
			allParas.setLCPara(new double[] {gamma1, gamma2});
	}

    /**
     * ���ù۲���������ɲ���
     * @param ob_paras [0]qm, [1]vf_CF, [2]vf_SD, [3]vp, [4]kJamLower, [5]kJamUpper
     * @param varying_paras [0]kJam, [1]r, [2]gamma1, [3]gamma2.
     */
    private void setParas(double[] ob_paras, double[] varying_paras) {
        //���ȼ��
        if (ob_paras.length != 6 || varying_paras.length != 4) {
            System.err.println("parameters' length does not match");
            return;
        }

        //���͹۲����
        double qm = ob_paras[0];
        double vf_CF = ob_paras[1];
        double vf_SD = ob_paras[2];
        double vp = ob_paras[3];
        double kJLower = ob_paras[4];
        double kJUpper = ob_paras[5];

        //���ù۲����
        setObservedParas(qm,vf_CF,vf_SD,vp,kJLower,kJUpper);

        //�������ɲ���
        double kJam = varying_paras[0];
        double r = varying_paras[1];
        double gamma1 = varying_paras[2];
        double gamma2 = varying_paras[3];
        //��ȡ��������
        double deltaT = getSimClock().getStepSize();
        //��������optPara
        double ts = interConstraints.calTs(kJam, deltaT);
        double xc = interConstraints.calXc();
        double beta = interConstraints.calBeta(r,kJam);

        setOptParas(kJam, ts, xc, r/beta, beta, gamma1, gamma2);
    }

	/**
	 * ����ȫ���˶�����
	 * @param fullParas [0]qm, [1]vf_CF, [2]vf_SD, [3]vp, [4]kJamLower,
	 *                  [5]kJamUpper, [6]kJam, [7]alpha, [8]gamma1, [9]gamma2.
	 */
	private void setParas(double[] fullParas) {
		if (fullParas.length != 10) {
			System.err.println("length does not match");
			return;
		}
		double[] ob = new double[4];
		double[] varying = new double[4];
		System.arraycopy(fullParas,0,ob,0,4);
		System.arraycopy(fullParas,6,varying,0,4);
		setParas(ob,varying);
	}

	protected boolean violateConstraints(double[] fullParas) {
		return violateConstraints(Arrays.copyOfRange(fullParas,0,6),
				                  Arrays.copyOfRange(fullParas,6,10));
	}

    protected boolean violateConstraints(double[] obsParas, double[]varyingParas) {
        //���͹۲����
        double qm = obsParas[0];
        double vf_CF = obsParas[1];
        double vf_SD = obsParas[2];
        double vp = obsParas[3];
        double kJLower = obsParas[4];
        double kJUpper = obsParas[5];

        //�������ɲ���
        double kJam = varyingParas[0];
        double alpha = varyingParas[1];
        double gamma1 = varyingParas[2];
        double gamma2 = varyingParas[3];

        //��ȡdeltaT
        double deltaT = getSimClock().getStepSize();

        return (interConstraints.getConstraint("kj").checkViolated(kJam,null) |
                interConstraints.getConstraint("vp").checkViolated(vp,null) |
                interConstraints.getConstraint("deltaT").checkViolated(deltaT,new double[] {kJam}));
    }

	public MLPParameter getSimParameter() {
		return (MLPParameter) mlpNetwork.getSimParameter();
	}

	public SimulationClock getSimClock() {
		return mlpNetwork.getSimClock();
	}

	@Override
	public MLPNetwork getNetwork() {
		return mlpNetwork;
	}

	/**
	 * ����det2�ĳ�������
	 * @return
	 */
	//TODO: ��ɾ�� ĿǰΪ�˽��;ɴ��뱣��
	public double[] getEmpData() {
		return MacroCharacter.getKmSpeed(
				MacroCharacter.select(empMap.get("det2"),MacroCharacter.SELECT_SPEED));
	}

	public HashMap<String, List<MacroCharacter>> getEmpMap(){
		return empMap;
	}

	@Override
	public HashMap<String, List<MacroCharacter>> getSimMap() {
		return simMap;
	}
	public HashMap<String, List<MicroCharacter>> getEmpMicroMap() {
		return empMicroMap;
	}

    public int repeatRun() {

        //simMap�ÿգ�����״̬�������ظ�ִ��repeatRun()ʱ����
        resetSimMap();

        //�����ظ����������������н���ƽ��
		for(int i = 0; i < Math.max(repeatTimes,1); i++){
			run();
			sumStat(mlpNetwork.exportStat());
		}

		//ͳ������ƽ��
		if (repeatTimes > 1)
			averageStat(simMap);

		status = Constants.STATE_DONE;

        return status;
    }

	/**
	 * ���ڿ��ӻ�debug���������
	 */
	@Override
	public void run() {
		if (config.getBoolean("engineBroadcast")) {
			double count = 0.0;
			long t0 = System.currentTimeMillis();

			EngineEvent engineEvent = new EngineEvent(this, EngineEvent.BROADCAST);

			while (simulationLoop()>=0) {
				count++;
				engineEvent.setMsg("sim time: " + getSimClock().getCurrentTime() + " s.");
				informEngineListeners(engineEvent);
			}

			String msg = "runtime: " + (System.currentTimeMillis()-t0) + " ms." + "\n" +
					"sim rate: " + String.format("%.2f",count/((double)System.currentTimeMillis()-t0)) + " kHz.";

			System.out.println(msg);
			engineEvent.setMsg(msg);
			informEngineListeners(engineEvent);
		}
		else
			while (simulationLoop()>=0);
	}

	@Override
	public void close() {
		//�ر����ݿ�����
		JdbcUtils.close();
	}

	public int countOnHoldVeh() {
		int vehHoldCount = 0;
		for (int k = 0; k<mlpNetwork.nLinks(); k++) {
			vehHoldCount += ((MLPLink) mlpNetwork.getLink(k)).countHoldingInflow();
		}
		return vehHoldCount;
	}

	private void sumStat(Map<String, List<MacroCharacter>> addingMap) {
		if (simMap == null) {
			simMap = new HashMap<>();
			addingMap.forEach((k,v) -> {
				List<MacroCharacter> newList = new ArrayList<>();
				v.stream().forEach(l -> newList.add(l.copy()));
				simMap.put(k,newList);
			});
		return;
		}

		this.simMap.forEach((k,v) -> {
			List<MacroCharacter> addingRecords = addingMap.get(k);
			for (int i = 0; i < v.size(); i++) {
				MacroCharacter srcRecord = v.get(i);
				MacroCharacter addingRecord = addingRecords.get(i);
				if (srcRecord.flow>0.0) {
					//ƽ������
					srcRecord.speed = (srcRecord.flow*srcRecord.speed + addingRecord.flow*addingRecord.speed) / (srcRecord.flow + addingRecord.flow);
					//�г�ʱ��ƽ��
					srcRecord.travelTime = (srcRecord.flow*srcRecord.travelTime + addingRecord.flow*addingRecord.travelTime) / (srcRecord.flow + addingRecord.flow);
					//�����ۼ�
					srcRecord.flow += addingRecord.flow;
					//�ܶ��ۼ�
					srcRecord.density = srcRecord.flow / srcRecord.speed;
				}
			}
		});
	}

	private void averageStat(Map<String, List<MacroCharacter>> srcMap) {
		srcMap.forEach((k,v) -> v.forEach(e -> {
			e.flow = e.flow / ((double) repeatTimes);
			e.density = e.density / ((double) repeatTimes);
		}));
	}

	public int countRunTimes(){
		return mod;
	}

	public MLPEngine setShortTermParas(double[] args) {
		setParameter("kj", args[0]);
		double ts = InterConstraints.calTs(args[0],
				config.getDouble("vf_cf"),
				getSimClock().getStepSize(),
				config.getDouble("xc"));
		setParameter("ts",ts);
		double beta = InterConstraints.calBeta(args[1],
				config.getDouble("kj"),
				config.getDouble("qm"),
				config.getDouble("vf_sd"));
		setParameter("beta", beta);
		double alpha = args[1]/beta;
		setParameter("alpha",alpha);
		setParameter("gamma1",args[2]);
		setParameter("gamma2",args[3]);
		return this;
	}

	protected void resetSimMap() {
		simMap=null;
	}

	/*public void transferParas(double qm, double kj, double r, double vf_SD, double vf_CF, double km_CF, double gamma1, double gamma2) {
		//cal beta
		double beta = Math.log(qm/kj/vf_SD) / (Math.log(r/(r+1)) - Math.pow(r,-1.0)*Math.log(r+1));
		//cal alpha
		double alpha = r / beta;
		//cal xc
		double xc = 1/km_CF;
		//cal ts
		double ts = vf_CF/(xc-1/kj) - getSimClock().getStepSize();
		setObservedParas(qm,vf_CF,vf_SD,120.0/3.6,0.12,0.2);
		setOptParas(kj,ts,xc,alpha,beta,gamma1,gamma2);//gamma ��������
	}*/

	private void readInTrafficParameters(){
		double qm = config.getDouble("qm");
		double vf_cf = config.getDouble("vf_cf");
		double vf_sd = config.getDouble("vf_sd");
		double kj = config.getDouble("kj");
		double ts = config.getDouble("ts");
		double xc = config.getDouble("xc");
		double alpha = config.getDouble("alpha");
		double beta = config.getDouble("beta");
		double vp = config.getDouble("vp");
		double gamma1 = config.getDouble("gamma1");
		double gamma2 = config.getDouble("gamma2");


		MLPParameter allParas = (MLPParameter) mlpNetwork.getSimParameter();

		//����ͨ������������parameter�е�capacity
		allParas.capacity = qm;
		mlpNetwork.setOverallCapacity(qm);//·�ε�����ÿ��ͨ������

		//������������
		allParas.setPhyLim(vp);

		allParas.limitingParam_[1] = (float) ts;
		allParas.CF_FAR = (float) xc;

		//·���˶�����
		int mask = 0;
		mask |= 1<<(1-1);//vf_SD
		mask |= 1<<(3-1);//kJam
		mask |= 1<<(4-1);//alpha
		mask |= 1<<(5-1);//beta
		mask |= 1<<(6-1);//vf_CF
		mlpNetwork.setOverallSDParas(new double[] {vf_sd, kj, alpha, beta, vf_cf}, mask);//kJam, alpha, beta

		//����Kjam ���� leff �� CF_near ��һ���� (CF �� MS model �Ĳ���һ����)
		allParas.limitingParam_[0] = (float) (1.0/kj - MLPParameter.VEHICLE_LENGTH);
		allParas.CF_NEAR = allParas.limitingParam_[0];//������kjam�Ǻ�

		allParas.setLCPara(new double[] {gamma1, gamma2});
	}

	public void setParameter(String parameterName, double value) {
		MLPParameter allParas = getSimParameter();
		switch (parameterName) {

			case "lcdStepSize"	: allParas.setLCDStepSize(value);
			break;
			case "lcBufferTime"	: allParas.setLCBuffTime(value);
			break;
			case "lcSensitivity": allParas.setLCSensitivity(value);
			break;

			case "runningSeed"	: runningSeed = (long)value;
			break;

			case "qm": {allParas.capacity = value; mlpNetwork.setOverallCapacity(value);}
			break;
			case "ts": allParas.limitingParam_[1] = (float) value;
			break;
			case "xc": allParas.CF_FAR = (float) value;
			break;

			case "vf_sd": mlpNetwork.setOverallSDParas(new double[] {value}, 1);
			break;
			case "kj": {mlpNetwork.setOverallSDParas(new double[] {value}, 4);
						allParas.limitingParam_[0] = (float) (1.0/value - MLPParameter.VEHICLE_LENGTH);
						allParas.CF_NEAR = allParas.limitingParam_[0];}
			break;
			case "alpha": mlpNetwork.setOverallSDParas(new double[] {value}, 8);
			break;
			case "beta": mlpNetwork.setOverallSDParas(new double[] {value}, 16);
			break;
			case "vf_cf": mlpNetwork.setOverallSDParas(new double[] {value}, 32);
			break;

			case "gamma1": allParas.setLCPara(new double[]{value, allParas.getLCPara()[1]});
			break;
			case "gamma2": allParas.setLCPara(new double[]{allParas.getLCPara()[0], value});
			break;

			case "vp": allParas.setPhyLim(value);
			break;

			default: System.err.println("Wrong ParameterName");
		}
	}

	public void stop(){
		stopSignal = true;
	}

	public void informEngineListeners(EngineEvent e) {
		Object actionListeners[] = listenerList.getListeners(ActionListener.class);
		for (Object listener : actionListeners) {
			((ActionListener) listener).actionPerformed(e);
		}
	}
}
