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

import com.transyslab.roadnetwork.*;

import java.util.*;

public class MLPVehicle extends Vehicle{
	protected MLPVehicle trailing;// upstream vehicle
	protected MLPVehicle leading;// downstream vehicle
	protected MLPLane lane;
	protected MLPSegment segment;
	protected MLPLink link;
	public MLPConnector conn;
	protected int platoonCode;
	protected int virtualType;//0 for real veh; num>0 for virual veh with the connected vheID
	protected int buffer;//lane changing cold down remain frames
	protected int spdBuffer;//lane changing cold down remain frames
	protected int speedLevel;
	protected boolean cfState;
	protected boolean resemblance;
	protected boolean stopFlag;//下版本可去除
	protected double newSpeed;
	protected double newDis;
	protected Map<MLPLane,Double> lcShiftTable;
	protected int usage;
//	public double TimeEntrance;
	protected double dspLinkEntrance;
	public int rvId;
	protected HashMap<MLPLane, Double> diMap;
//	static public TXTUtils fout = new TXTUtils("src/main/resources/output/test.csv");
//	protected double TimeExit;
	//private boolean active_;
	private String license;
	private String licenseType;
	//wym !!parameter在回收过程中不需要重置
	protected MLPParameter mlpParameter;

	private int count;

	public MLPConnector selectedConn;
	
	
	public void setMlpParameter(MLPParameter theParameter){
		trailing = null;
		leading = null;
		platoonCode = 0;
		mlpParameter = theParameter;
		diMap = new HashMap<>();
		lcShiftTable = new HashMap<>();
	}

	public MLPNetwork getMLPNetwork() {
		return link!=null ?
				(MLPNetwork) link.getNetwork() :
				conn!=null ?
				(MLPNetwork) conn.upLane.getLink().getNetwork() :
				null;
	}
	
	@Override
	public MLPLink getLink() {
		return link;
	}
	
	public MLPSegment getSegment(){
		return segment;
	}
	public MLPLane getLane(){
		return lane;
	}
	public void reset(){
		
	}
	
	public void resetPlatoonCode(){
		platoonCode = getId();
	}
	
	public void calState() {
		if (leading != null ) {//&&leading.Displacement() - Displacement() <= mlpParameter.CELL_RSP_LOWER
			double headway = (leading.Displacement() - Displacement()) / Math.max(currentSpeed, 1e-5);
			if (MLPParameter.inPlatoon(headway))
			cfState = true;
			else
				cfState = false;
		}
		else {
			cfState = false;
		}
		speedLevel = 1;
	}
	
	public MLPVehicle getUpStreamVeh() {
		if (trailing != null) {
			return trailing;
		}
		else {
			JointLane jointLane = link.findJointLane(lane);
			int p = jointLane.lanesCompose.indexOf(lane) + 1;
			while (p<jointLane.lanesCompose.size()-1) {
				 if (!jointLane.lanesCompose.get(p).vehsOnLn.isEmpty()) 
					 return  jointLane.lanesCompose.get(p).getHead();
				p += 1;
			}
			return  null;
		}
	}
	
	public double Displacement(){
		return conn == null ?
				Math.max(0.0, segment.endDSP - distance) :
				Math.max(0.0, conn.getLength() - distance);
	}

	public void mandetoryLanchange(){
		if (virtualType == 0 && buffer == 0) {
			for (int turning = 0; turning < 2; turning++) {
				double h = calH(turning);
				if (h>0 && calMLC()>0.95){
					MLPLane tarLane = getLane().getAdjacent(turning);
					if (tarLane != null && //换道检查
							getLane().checkTurn(turning+1) &&
							checkGapAccept(tarLane)){
						getLink().LCOperate(this, turning);
					}
					else {
						System.out.println("veh " + getId() + " mlc blocked on link " + getLink().getId());
					}
					break;
				}
			}
		}
	}

	public boolean checkAheadGapAccept(MLPLane tarLane){
		if (tarLane.vehsOnLn.size()<=0) {
			return true;
		}
		MLPVehicle sideVeh = tarLane.vehsOnLn.getLast();
		double maxShift = mlpParameter.alertDis();
		if (getDistance() + getLength() - maxShift > sideVeh.getDistance()){
			return (sideVeh.Displacement() - sideVeh.getLength() - Displacement() >= 0);
		}
		Object[] targetVehs = tarLane.vehsOnLn.stream()
				.filter(v -> getDistance()+getLength()-maxShift<=v.getDistance())
				.filter(v->
					((v.leading==null || !v.leading.getLane().equals(v.getLane())
						&& v.getDistance() - mlpParameter.minLCAcceptedGap(v.getCurrentSpeed(),mlpParameter.getLCSensitivity()) - getLength() >= 0)
							|| (getDistance()-v.leading.getDistance()-v.leading.getLength()>=0
									&& v.getDistance() - v.leading.getDistance() - v.leading.getLength()>=getLength()))
				)
				.sorted(Comparator.comparing(MLPVehicle::getDistance).reversed())
				.toArray();
		if (targetVehs.length>0){
			MLPVehicle target = (MLPVehicle) targetVehs[0];
			double targetDis = (target.leading!=null && !target.leading.getLane().equals(target.getLane())) ?
					target.getDistance() - (target.getDistance() - target.leading.getLength() - getLength())/2.0 - getLength() :
					target.getDistance() - mlpParameter.minLCAcceptedGap(target.getCurrentSpeed(),mlpParameter.getLCSensitivity()) - getLength();
			lcShiftTable.put(tarLane,targetDis);
			return true;
		}
		else{
			return false;
		}
	}

	public void updateLCDistance(MLPLane tarLane){
		if (lcShiftTable.containsKey(tarLane)){
			distance = lcShiftTable.get(tarLane);
		}
		lcShiftTable.clear();
	}
	
	public boolean checkGapAccept(MLPLane tarLane){
		boolean frontCheck = true;
		boolean backCheck = true;
		MLPVehicle frontVeh = null;
		MLPVehicle backVeh = link.findJointLane(tarLane).getFirstVeh();//效率有待提高
		if (backVeh == null) {//该车道上没有车
			return true;//TODO: 路段太短的情况下也会直接返回true，不合理。待修改。
		}
		else {
			//front = ((MLPSegment) link.getEndSegment()).endDSP;
			while (backVeh != null && backVeh.Displacement()>Displacement()){
				frontVeh = backVeh;
				backVeh = backVeh.trailing;
			}			
			if (frontVeh != null) 
				frontCheck = (frontVeh.Displacement() - frontVeh.getLength() - Displacement() >= mlpParameter.minLCAcceptedGap(currentSpeed,mlpParameter.getLCSensitivity()));//mlpParameter.headwaySpeedSlope() * currentSpeed);
			if (backVeh!=null) 
				backCheck = (Displacement() - length - backVeh.Displacement() >= mlpParameter.minLCAcceptedGap(backVeh.currentSpeed,mlpParameter.getLCSensitivity()));//mlpParameter.headwaySpeedSlope() * backVeh.currentSpeed);//mlpParameter.minGap(backVeh.currentSpeed)//backVeh.getCurrentSpeed
			return (frontCheck && backCheck);
		}
	}
	
	private double calDLC(int turning, double fDSP, double tDSP, double PlatoonCount){
		try {
			//DEBUG please delete later
			count = 0;
			//DEBUG please delete later
			double [] s = sum(turning, segment, fDSP, tDSP, new double []{0.0,0.0});
			return (PlatoonCount/(tDSP - fDSP) - (s[0] + 1.0) /s[1]) / link.dynaFun.linkCharacteristics[2];
		} catch (Exception e) {
			e.printStackTrace();
		}
		//failed	
		return 0.0;		
	}
	
	private double [] sum(int turning, MLPSegment seg, double f, double t, double [] count){
		//DEBUG please delete later
		this.count += 1;
		if (this.count>100)
			System.out.println("DEBUG: LC deadlock warning");
		//DEBUG please delete later
		//double [] answer = {0.0,0.0};
		if (f - seg.startDSP > -0.001)	{
			if (t - seg.endDSP < 0.001) {
				double [] answer = new double [2];
				MLPLane tarlane = lane.getAdjacent(turning).getSamePosLane(seg);
				if (tarlane == null || !tarlane.enterAllowed || !lane.checkTurn(turning+1)) {
					answer[0] = count[0] + (t-f)* link.dynaFun.linkCharacteristics[2];
				}
				else {
					answer[0] = count[0] + tarlane.countVehWhere(f, t);
				}
				//very important
				double[] expandedBound = tarlane==null ? new double[]{f,t} : tarlane.expandBound(f,t);
				answer[1] = count[1] + (expandedBound[1]-expandedBound[0]);
				return answer;
			}
			else {
				return sum(turning, seg.getDnSegment(), seg.endDSP, t, 
									sum(turning, seg, f, seg.endDSP, count));
			}
		}
		else {
			if (t<=seg.endDSP) {
				return sum(turning, seg.getUpSegment(), f, seg.startDSP, 
									sum(turning, seg, seg.startDSP, t, count));
			}
			else{
				return sum(turning, seg.getDnSegment(), seg.endDSP, t, 
									sum(turning, seg.getUpSegment(), f, seg.startDSP, 
											sum(turning, seg, seg.startDSP, seg.endDSP, count)));
			}
		}
	}
	
	protected double calMLC(){
		double effectedLength = 800.0;//m
		double buff = mlpParameter.getSegLenBuff();

		//find out next MANDATORY LANE CHANGING POINT with distance less than effective length.
		MLPLane mlpLane = lane;
		while ((!(mlpLane.getSegment()).isEndSeg()) && !have2ChangeLane() && mlpLane.connectedDnLane!=null) {
			mlpLane = mlpLane.connectedDnLane;
		}

		//calculate
		double maxDsp = mlpLane.getSegment().endDSP;
		if (maxDsp <= buff)
			return 1.0;//too short.
		double L = Math.min(effectedLength, maxDsp-buff);
		return Math.min(1.0, Displacement()/L);
	}
	
	private double calH(int turning){
//		int h = lane.calDi(this) - lane.getAdjacent(turning).calDi(this);//旧方法 重复计算
		try{
			double h = diMap.get(lane) - diMap.get(lane.getAdjacent(turning));
			if (h==0.0){
				return 0.0;
			}
			else{
				if (h>0){
					return 1.0;
				}
				else {
					return -1.0;
				}
			}
		}
		catch (NullPointerException e){
//			System.out.println("No diMap Exception ");
			return Double.NEGATIVE_INFINITY;
		}
	}
	
	public double calLCProbability(int turning, double fDSP, double tDSP, double PlatoonCount){
		double [] gamma = mlpParameter.getLCPara();
		double lambda1 = MLPParameter.LC_Lambda1;
		double lambda2 = MLPParameter.LC_Lambda2;
		double h = calH(turning);
		double Umlc = h==0.0 ? 0.0 : calMLC();
		double Udlc = calDLC(turning, fDSP, tDSP, PlatoonCount);
		double W = gamma[0]*h*Umlc + gamma[1]*Udlc;// - (gamma[0] + gamma[1])*0.5
		double U = lambda1*W + lambda2;
		//计算边界截断
		double tmp = Math.min(Math.max(Math.exp(U),-1e10),1e10);
		double pr = tmp/(1+tmp);
//		fout.writeNFlush(u + "," + pr + "\r\n");
		return pr;
	}

	public double calLCProbability2(int turning) {
		double [] gamma = mlpParameter.getLCPara();
		double lambda1 = MLPParameter.LC_Lambda1;
		double lambda2 = MLPParameter.LC_Lambda2;
		double h = calH(turning);
		double Umlc = h==0.0 ? 0.0 : calMLC();
		double Udlc = calDLC2(turning);
		double W = gamma[0]*h*Umlc + gamma[1]*Udlc;// - (gamma[0] + gamma[1])*0.5
		double U = lambda1*W + lambda2;
		//计算边界截断
		double tmp = Math.min(Math.max(Math.exp(U),-1e10),1e10);
		double pr = tmp/(1+tmp);
		return pr;
	}

	public double calDLC2(int turning) {
		MLPLane tarLane = lane.getAdjacent(turning);
		double p_dlc = ((double) lane.vehsOnLn.size() - 1 - tarLane.vehsOnLn.size()) / lane.getLength() / link.dynaFun.linkCharacteristics[2];
		return p_dlc;
	}
	
	public void initNetworkEntrance(double time, double dsp) {
		departTime = (float) time;
		initLinkEntrance(time,dsp);
	}

	public void initLinkEntrance(double time, double dsp) {
		timeEntersLink = (float) time;
		dspLinkEntrance = dsp;
	}
	
	public void initInfo(int virType, MLPLink onLink, MLPSegment onSeg, MLPLane onLane, int rvid){
		virtualType = virType;
		link = onLink;
		segment = onSeg;
		lane = onLane;
		rvId = rvid;
		if (!segment.isStartSeg()){
			selectedConn = lane.upStrmConns().get(0);
		}
	}
	
	public void init(int id, double len, double dis, double speed){
		 setId(id);
		 type = 1;
		 length = len;
	     distance = dis;
	     currentSpeed = speed;
	 }
	public void init(int id, double len, double dis, double speed, String license, String licenseType){
		init(id,len,dis,speed);
		this.license = license;
		this.licenseType = licenseType;
	}
	
	public int updateMove() {
//		if ( Math.abs(getDistance - newDis - newSpeed*SimulationClock.getInstance().getStepSize()) > 0.001 )
//			System.out.println("BUG 未在本计算帧内处理此车");
		if (virtualType >0 && buffer == 0) {
			lane.removeVeh(this, true);
			return Constants.VEHICLE_RECYCLE;
		}
		if (newDis < 0.0 && conn==null)
			return dealPassing();//Passing link or seg
		return Constants.VEHICLE_NOT_RECYCLE;
	}
	
	public void advance() {
		updateDynamics();
		buffer = Math.max(0, buffer -1);
	}

	public void updateDynamics() {
		currentSpeed = (float) newSpeed;
		distance = (float) newDis;
	}
	
	public int dealPassing() {//串行

		if (segment.isEndSeg()) {
			if (virtualType != 0) {
				//虚车最多影响到Link末端
				holdAtDnEnd();
				if (ExpSwitch.VIRTUAL_RELEASE) {
					lane.removeVeh(this, true);
					return Constants.VEHICLE_RECYCLE;
				}
				else
					return Constants.VEHICLE_NOT_RECYCLE;
			}
			MLPNode server = (MLPNode) link.getDnNode();
			return server.serve(this);
		}
		else {//deal passing Seg.
			/*if (lane.checkPass()) {
				lane.passVeh2ConnDnLn(this);
				newDis = segment.getLength() + newDis;
				if (newDis < 0.0) {
					dealPassing();
				}
			}
			else {//hold in this seg
				newDis = 0.0;
				if (getCurrentSpeed>0.0)
					newSpeed = (getDistance-newDis)/SimulationClock.getInstance().getStepSize();
			}*/
			MLPLane nextLane = nextLane();
			if (nextLane == null || !nextLane.checkNewVolum(this)) {//has no successive lane
				if (virtualType == 0){
//					System.err.println("Vehicle No. " + getId() +" has no successive lane to go");
					holdAtDnEnd();
				}
				else
					holdAtDnEnd();//lane.removeVeh(this, true);//如果虚车触发此条件（successive lane不可用），则消失
				return Constants.VEHICLE_NOT_RECYCLE;
			}
			selectedConn = getMLPNetwork().findConnector(lane.getId(),nextLane.getId());
			if (selectedConn==null){
				System.out.println("ERROR: NO CONNECTOR FOUND");
				nextLane();
			}
			if (nextLane.equals(lane.connectedDnLane))
				lane.passVeh2ConnDnLn(this);
			else
				lane.passVeh(this,nextLane);
			newDis = segment.getLength() + newDis;
			if (newDis < 0.0) {
				dealPassing();
			}
			else {
				updateLeadNTrail();
				if (leading!=null)
					leading.updateLeadNTrail();
				if (trailing!=null)
					trailing.updateLeadNTrail();
			}
			return Constants.VEHICLE_NOT_RECYCLE;
		}
	}
	
	protected void holdAtDnEnd() {
		newDis = 0.0;
		if (newSpeed >0.0)
			newSpeed = (distance -newDis) / getMLPNetwork().getSimClock().getStepSize();
	}
	
	public void setNewState(double spd) {
		spd *= VehicleType.getPowerRate(getType());
		if (ExpSwitch.SPD_BUFFER) {
			spd = currentSpeed;
			spdBuffer = Math.max(0, spdBuffer-1);
		}
		//最大加速度平滑
		if (ExpSwitch.MAX_ACC_CTRL)
			spd = powerRate(spd);
		if (need2Stop()) {
			newSpeed = 0.0;
			newDis = distance;
			return;
		}
		if (leading != null) {
			double gap = leading.Displacement() - leading.getLength() - Displacement();
			double maxSpd = ((MLPParameter) getMLPNetwork().getSimParameter()).maxSpeed(gap);
			maxSpd *= VehicleType.getPowerRate(getType());
			newSpeed = Math.min(spd,maxSpd);
		}
		else {
			newSpeed = spd;
		}
		//将过程看做匀加速过程，增加平滑度。
		newDis = ExpSwitch.ACC_SMOOTH ?
					getDistance() - (currentSpeed + newSpeed) / 2.0 * getMLPNetwork().getSimClock().getStepSize() :
					getDistance() - newSpeed * getMLPNetwork().getSimClock().getStepSize();
	}
	
	public void clearMLPProperties() {
		type = 0;
		leading = null;
		trailing = null;
		lane = null;
		segment = null;
		link = null;
		conn = null;
		selectedConn = null;
		platoonCode = 0;
		virtualType = 0;
		buffer = 0;
		spdBuffer = 0;
		speedLevel = 0;
		cfState = false;
		resemblance = false;
		newSpeed = 0.0;
		newDis = 0.0;
		lcShiftTable.clear();
		setId(0);
		length = 0.0f;
		distance = 0.0f;
		currentSpeed = 0.0f;
		mileage = 0.0;
		departTime = 0.0f;
		timeEntersLink = 0.0f;
		dspLinkEntrance = 0.0;
		rvId = 0;
		donePathIndex();
//		TimeExit = 0.0;
		diMap.clear();
		license = null;
		licenseType = null;
	}
	
	public void updateUsage() {
		usage += 1;
	}

//	public void updateLeadNTrail() {
//		int p = lane.vehsOnLn.indexOf(this);
//		if (p==0) {
//			leading = null;
//		}
//		else {
//			leading = lane.vehsOnLn.get(p-1);
//		}
//		if (p+1>=lane.vehsOnLn.size()){
//			trailing = null;
//		}
//		else {
//			trailing = lane.vehsOnLn.get(p+1);
//		}
//	}
	
	public void updateLeadNTrail() {
		int p = lane.vehsOnLn.indexOf(this);
		if (p==0) {
			//是lane上的第一辆车，先将前车为null
			leading = (MLPVehicle) null;
			//只要前方存在lane且允许通行，则一直取前方lane，直到前方lane上有车，此时取前方lane的最后一辆作为前车
//			MLPLane thelane = lane.connectedDnLane;
//			while (thelane != null && thelane.enterAllowed) {
//				if (!thelane.vehsOnLn.isEmpty()) {
//					leading = thelane.vehsOnLn.getLast();
//					break;
//				}
//				thelane = thelane.connectedDnLane;
//			}
			MLPSegment theSeg = segment;
			MLPLane theLN = lane;
			while (!theSeg.isEndSeg() && theLN.successiveDnLanes.size()==1){
				//检查下游LN，并推进
				theLN = theLN.successiveDnLanes.get(0);
				theSeg = theLN.getSegment();
				if (!theLN.vehsOnLn.isEmpty()) {
					leading = theLN.vehsOnLn.getLast();
					break;
				}
			}
		}
		else {
			//非lane上第一辆车，可取index-1的车作为前车
			leading = lane.vehsOnLn.get(p-1);
		}
		if (p == lane.vehsOnLn.size() - 1) {
			trailing = (MLPVehicle) null;
//			MLPLane thelane = lane.connectedUpLane;
//			while (thelane != null && thelane.enterAllowed) {
//				if (!thelane.vehsOnLn.isEmpty()) {
//					trailing = thelane.vehsOnLn.getFirst();
//					break;
//				}
//				thelane = thelane.connectedUpLane;
//			}
			MLPSegment theSeg = segment;
			MLPLane theLN = lane;
			while (!theSeg.isStartSeg() && theLN.successiveUpLanes.size()==1){
				//检查上游LN，并推进
				theLN = theLN.successiveUpLanes.get(0);
				theSeg = theLN.getSegment();
				if (!theLN.vehsOnLn.isEmpty()) {
					trailing = theLN.vehsOnLn.getFirst();
					break;
				}
			}
		}
		else
			trailing = lane.vehsOnLn.get(p+1);
	}

	/*public MLPVehicle getLateralLeading(MLPLane tarLN){		
	}
	
	public MLPVehicle getLaterallTrailing(){	
	}*/
	public void updateDi() {
		diMap.clear();
		List<MLPLane> target = link.validEndLanesFor(this);
		link.getSegments().forEach(seg -> {
			seg.getLanes().forEach(fLane -> {
				diMap.put((MLPLane) fLane, link.getLCRouteWeight((MLPLane) fLane,target));
			});
		});
	}
	public String getInfo(){
		StringBuilder sb = new StringBuilder();
		sb.append("DIS: " + getDistance() + "\n");
		sb.append("sim time: " + getMLPNetwork().getSimClock().getCurrentTime() + "\n");
		sb.append("hphm: " + getLicense() + "\n");
		sb.append("hpzl: " + getLicenseType() + "\n");
//		if (getNextLink()!=null)
//			sb.append("NLID: " + getNextLink().getId() + "\n");
//		if (lane!=null)
//			sb.append("lane: " + lane.getId() + "\n"
//			+ "VNum: " + (lane.vehsOnLn.indexOf(this)+1) + "/" + lane.vehsOnLn.size() + " pos: " + String.format("%.2f",(1.0-distance/ lane.getLength())) + "\n");
//		if (conn!=null)
//			sb.append("LC: " + conn.getId() + "\n"
//			+ "Vnum: " + (conn.vehsOnConn.indexOf(this)+1) + "/" +conn.vehsOnConn.size() + " pos: " + String.format("%.2f",(1.0-distance/ conn.getLength())) + "\n");
//		if (leading!=null)
//			sb.append("leading " + leading.getId() + " ahead: " + (this.distance-leading.distance) + "\n" );
//		if (lane!=null){
//			sb.append("buffer: " + buffer + "\n");
//			sb.append("endSeg: " + getSegment().isEndSeg() + "\n");
//			sb.append("di: " + diMap.get(getLane()) + "\n");
//		}
		if (conn!=null){
			sb.append("Km: " + ((MLPLink) conn.dnLane.getLink()).dynaFun.linkCharacteristics[2] + "\n");
			sb.append("k: " + ((double)conn.queueNum())/conn.getLength() + "\n");
		}
		return sb.toString();
	}
	protected double powerRate(double spd) {
		double maxAcc = ExpSwitch.MAX_ACC;
		double maxDec = ExpSwitch.MAX_DEC;
		double deltaT = getMLPNetwork().getSimClock().getStepSize();
		return spd >= currentSpeed ? Math.min(spd, maxAcc*deltaT+currentSpeed) :
				Math.max(spd, Math.max(0.0,maxDec*deltaT+currentSpeed));
	}
	public boolean have2ChangeLane() {
		MLPLane nextLane = nextLane();
		return virtualType==0 &&
				diMap.get(lane) != 0 &&
				(nextLane==null ||
						diMap.get(nextLane)==Double.POSITIVE_INFINITY);
	}
	public boolean need2Stop() {
		return have2ChangeLane() &&
				calMLC()>0.99;
	}

	public boolean isVirtual(){
		return this.virtualType!=0;
	}

	public MLPVehicle getLCBlockingVeh(){
		if (
				getLane().vehsOnLn.contains(this) //not on node
				&& virtualType == 0 && buffer == 0 //换道资格
				&& getCurrentSpeed()==0.0 // 停车等待
				&& getLane()!=null && getSegment().isEndSeg() //道路末端
				&& diMap.get(getLane())!=0 && calMLC()>0.99 //此道不通，强制换道紧急性强
		){
			//选择目标车道
			MLPLane right = getLane().getAdjacent(0);
			MLPLane left = getLane().getAdjacent(1);
			double di = diMap.get(getLane());
			double di_r = right==null ? Double.POSITIVE_INFINITY : diMap.get(right);
			double di_l = left==null ? Double.POSITIVE_INFINITY : diMap.get(left);
			if (di_l>=di && di_r>=di){
				System.err.println("veh no. " + getId() +  " in dead end.");
				if (getMLPNetwork().getSysRand().nextDouble()<0.1){
					lane.removeVeh(this,true);
					return this;
				}
				else
					return null;
			}
			MLPLane targetLane = di_r<=di_l ? right : left;
			//查找阻塞车辆 first overlap veh
			MLPVehicle backVeh = link.findJointLane(targetLane).getFirstVeh();
			while (backVeh != null && backVeh.Displacement()>Displacement() && !overlap(backVeh))
				backVeh = backVeh.trailing;
			if (backVeh!=null && backVeh.getCurrentSpeed()==0.0)
				return backVeh;
			return null;
		}
		return null;
	}

	public boolean overlap(MLPVehicle target){
		return
				target.getSegment().equals(getSegment())
				&& target.Displacement() > Displacement() - getLength()
				&& target.Displacement() - target.getLength() < Displacement();

	}

	public void switchLane(MLPVehicle tarVeh){
		String msg = "warning: vehicle no. " + getId() + " & " + tarVeh.getId() + " lane changing deadlock on link no." + getLink().getId();
		getMLPNetwork().broadcast(msg);
		MLPLane theLane = getLane();
		MLPLane tarLane = tarVeh.getLane();
		theLane.removeVeh(this,false);
		tarLane.removeVeh(tarVeh,false);
		theLane.insertVeh(tarVeh);
		tarLane.insertVeh(this);
	}

	public void setLCPath(MLPLink onLink){
		List<Link> pathLinks = new ArrayList<>();
		pathLinks.add(onLink);
		Path shortPath = new Path(onLink.getUpNode(), onLink.getUpNode(), pathLinks);
		setPath(shortPath,1);
	}

	/**
	 * get 6 columns of vehicle location reference
	 * @return
	 */
	public String getLocationRef(){
		Object[] ref = getLR();
		if (ref[0] instanceof MLPLane){
			MLPLane refLn = (MLPLane) ref[0];
			return (refLn.getLnPosNum() + "," +
					refLn.getId() + "," +
					refLn.getSegment().getId() + "," +
					refLn.getLink().getId() + "," +
					"" + "," +
					ref[1]);
		}
		else {
			MLPConnector refConn = (MLPConnector) ref[0];
			return ("" + "," +
					"" + "," +
					"" + "," +
					"" + "," +
					refConn.getId() + "," +
					ref[1]);
		}
	}

	public String getSegBasedLocationRef(){
		if (getLane().vehsOnLn.contains(this)){
			MLPLane refLn = getLane();
			return (refLn.getLnPosNum() + "," +
					refLn.getId() + "," +
					refLn.getSegment().getId() + "," +
					refLn.getLink().getId() + "," +
					"-1" + "," +
					(refLn.getLength()-getDistance()));
		}
		else if (conn!=null){
			MLPConnector refConn = conn;
			return ("-1" + "," +
					"-1" + "," +
					"-1" + "," +
					"-1" + "," +
					refConn.getId() + "," +
					(refConn.getLength()-getDistance()));
		}
		else
			return ("-1" + "," +
					"-1" + "," +
					"-1" + "," +
					"-1" + "," +
					"-1" + "," +
					"-1");
	}

	public double currentMileage(){
		return Displacement() + mileage();
	}

	public double getPCU(){
		return VehicleType.getType(this.getType()).pcu;
	}

	private Object[] projectSeg(double lr, double l2, MLPConnector dnConn){
		if (dnConn==null)
			return new Object[]{lane, Math.min(l2-1e-5,lr*l2)};
		double l1 =	dnConn.getLength();
		double cr = l1 / (l1+l2);
		if (lr<=cr) {
			return new Object[]{dnConn, Math.min(l1-1e-5,lr*(l1+l2))};
		} else {
			return new Object[]{lane, Math.min(l2-1e-5,lr*(l1+l2)-l1)};
		}
	}

	public Object[] getLR(){
		double l0 = segment.getLength();
		double lr = Math.max(0.0,l0-getDistance()) / l0;
		double l2 = lane.getGeoLength();

		if (getLane().vehsOnLn.contains(this)){//on the road
			if (selectedConn!=null&&selectedConn.getDnSeg().equals(getSegment())){
				return projectSeg(lr, l2, selectedConn);
			}
			else if (getSegment().isStartSeg()){
				return projectSeg(lr, l2, null);
			}
			else {
				System.out.println("ERROR: WRONG TOPOLOGY");
				return null;
			}
		}
		else {//in the node
			if (conn!=null){
				return new Object[]{conn, Math.min(conn.getLength()-1e-5,conn.getLength()-getDistance())};
			}
			else {
				return new Object[]{selectedConn, 1e-5};
			}

		}

//		if(link.getDnNode().type(Constants.NODE_TYPE_INTERSECTION)==0){
//
//		}
//		else if (conn!=null){
//			return new Object[]{conn, Math.min(conn.getLength()-1e-5,conn.getLength()-getDistance())};
//		}
//		else if(!segment.isEndSeg()){
//			return new Object[]{lane, Math.min(l1-1e-5,lr*l1)};
//		}
//		else{
//			MLPConnector dnConn = findDnConn();
//			return projectSeg(lr, l1, dnConn);
//		}
	}

	public List<MLPConnector> selectDnConns(){
		MLPLink nextLink = (MLPLink) getNextLink();
		if (!segment.isEndSeg() || nextLink==null)
			return null;
		List<MLPConnector> conns = lane.connsToDnLink(nextLink.getId());
		if (conns.size()<=0){
			return null;
		}
		conns.sort((o1, o2) -> {
			MLPNode node = (MLPNode) getLink().getDnNode();
			if (!node.isIntersection()){
				double vol1 = node.nVehsOn(o1.dnLane);
				double vol2 = node.nVehsOn(o2.dnLane);
				if (vol1!=vol2){
					return Double.compare(vol1,vol2);
				}
				else {
					double d1 = Math.abs(o1.dnLane.getLnPosNum()-o1.upLane.getLnPosNum());
					double d2 = Math.abs(o2.dnLane.getLnPosNum()-o2.upLane.getLnPosNum());
					return Double.compare(d1,d2);
				}
			}
			else {
				double vol1 = o1.vehsOnConn.size() + o1.dnLane.vehsOnLn.size();
				double vol2 = o2.vehsOnConn.size() + o2.dnLane.vehsOnLn.size();
				if (vol1!=vol2){
					return Double.compare(vol1,vol2);
				}
				else {
					double d1 = Math.abs(o1.dnLane.getLnPosNum()-o1.upLane.getLnPosNum());
					double d2 = Math.abs(o2.dnLane.getLnPosNum()-o2.upLane.getLnPosNum());
					return Double.compare(d1,d2);
				}
			}
		});
		return conns;
	}

	public MLPLane nextLane(){
		if (virtualType!=0){
			// If the virtual type is not 0, check if the segment is an end segment. If it is, return null.
			// Otherwise, return the first non-successive down lane if it exists, else return null.
			return segment.isEndSeg() ?
					null :
					lane.nDnLanes()>0 ?
							(MLPLane) lane.dnLane(0):
							null;
		}
		// If the virtual type is 0, and the segment is an end segment, select the first down connector's lane if it exists, else return null.
		if (segment.isEndSeg()){
			List<MLPConnector> conns = selectDnConns();
			return conns==null ? null :conns.get(0).dnLane;
		}

		// If the virtual type is 0 and the segment is not an end segment, return null if there are no successive down lanes.
		List<MLPLane> candidates = lane.selectDnLane(segment.getDnSegment());
		if (candidates.size() <= 0)
			return null;
		// Pick the minimum distance next lane from the list of down lanes.
		MLPLane nextLane = pickMinDiNextLane(candidates);
		// Avoid the next lane if its distance is infinite, and it's not in the down lanes of the current lane.
		if (diMap.get(nextLane)<Double.POSITIVE_INFINITY && lane.isInDnLanes(nextLane))
			return nextLane;

		// If the above condition is not met, find the next lane with the minimum distance among the down lanes of the current lane.
		nextLane = null;
		double di = Double.POSITIVE_INFINITY;
		for (int i = 0; i < lane.nDnLanes(); i++) {
			if (diMap.get(lane.dnLane(i))<di){
				di = diMap.get(lane.dnLane(i));
				nextLane = (MLPLane) lane.dnLane(i);
			}
		}
		return nextLane;
	}

	public MLPLane pickMinDiNextLane(List<MLPLane> lanes){
		if (lanes.size()>1) {
			lanes.sort(Comparator.comparingDouble(o -> diMap.get(o)));
		}
		return lanes.get(0);
	}

	private MLPConnector findDnConn(){
		MLPNetwork network = (MLPNetwork) getLink().getNetwork();
		MLPLane nextLane = nextLane();
		return nextLane==null ?
				null :
				network.findConnector(lane.getId(),nextLane.getId());
	}

    public String getLicense() {
        return license==null ? String.valueOf(getId()) : license;
    }
    public String getLicenseType(){
	    return licenseType==null ? "0" : licenseType;
    }
}
