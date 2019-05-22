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

import java.util.*;
import java.util.stream.Collectors;

import com.transyslab.roadnetwork.*;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public class MLPLink extends Link {
	private LinkedList<Inflow> inflowList;
	public List<JointLane> jointLanes;
	protected List<MLPVehicle> platoon;//正在处理的车队
	public Dynamics dynaFun;
	public List<double[]> tripTime;//double[] {timeIn, DspIn, timeOut}
	private int emitCount;
	private SimpleDirectedWeightedGraph<MLPLane,DefaultWeightedEdge> laneGraph;
//	private TXTUtils tmpWriter = new TXTUtils("src/main/resources/output/rand.csv");
//	public double capacity;//unit: veh/s/lane
//	private double releaseTime_;
	private Map<Long, MLPLink> turnableNextLinks;
	
	
	public MLPLink(){
		inflowList = new LinkedList<>();
		jointLanes = new ArrayList<JointLane>();
		platoon = new ArrayList<>();
		tripTime = new ArrayList<>();
		emitCount = 0;
//		capacity = MLPParameter.getInstance().capacity;
		laneGraph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		turnableNextLinks = new HashMap<>();
	}

	public void init(long id, int type, String name,int index, Node upNode, Node dnNode, MLPNetwork network) {
		setNetwork(network);
		super.init(id, type, name, index, upNode, dnNode);
		dynaFun = new Dynamics(this);
	}

	public void checkConnectivity(){
		
	}
	
	public boolean checkFirstEmtTableRec() {
		if (inflowList.isEmpty()) {
			return false;
		}
		Inflow rec1 = inflowList.getFirst();
		return (rec1.time<=network.getSimClock().getCurrentTime() &&
				((MLPNetwork) network).mlpLane(rec1.laneIdx).checkVolum(MLPParameter.VEHICLE_LENGTH
						,0.0));//rec1.speed
	}
	
	public JointLane findJointLane(MLPLane ln) {
		if (!jointLanes.isEmpty()) {
			ListIterator<JointLane> lpIterator = jointLanes.listIterator();
			while (lpIterator.hasNext()){
				JointLane candidate = lpIterator.next();
				if (candidate.lanesCompose.contains(ln) ||
					(!ln.getSegment().isEndSeg() && !ln.successiveDnLanes.isEmpty() && candidate.lanesCompose.contains(ln.successiveDnLanes.get(0))) || //非末端Seg的lane只有一个successiveLn
						(!ln.getSegment().isStartSeg() && !ln.successiveUpLanes.isEmpty() && candidate.lanesCompose.contains(ln.successiveUpLanes.get(0)))) //非始端Seg的lane只有一个successiveUpLn
					return candidate;
			}
		}
		return null;
	}
	
	public boolean hasNoVeh(boolean virtualCount) {
//		boolean r = true;
//		for (JointLane jl : jointLanes) {
//			r = r && jl.hasNoVeh();
//		}
//		return r;
		ListIterator<JointLane> JLIterator = jointLanes.listIterator();
		while (JLIterator.hasNext()) {
			if (!JLIterator.next().hasNoVeh(virtualCount)) 
				return false;
		}
		return true;
	}
	
	public void addLnPosInfo() {
		MLPSegment theSeg = (MLPSegment) getEndSegment();
		int JLNUM = 1;
		while (theSeg != null){
			for (int i = 0; i<theSeg.nLanes(); i++){
				MLPLane ln = theSeg.getLane(i);
				JointLane tmpJLn = findJointLane(ln);
				if (tmpJLn == null) {
					JointLane newJLn = new JointLane(JLNUM);
					JLNUM += 1;
					newJLn.lanesCompose.add(ln);
					jointLanes.add(newJLn);
				}
				else {
					tmpJLn.lanesCompose.add(ln);
				}
			}
			theSeg = (MLPSegment) theSeg.getUpSegment();
		}
	}
	
	/*public void resetReleaseTime() {
		releaseTime_ = SimulationClock.getInstance().getCurrentTime();
		scheduleNextEmitTime();
	}
	
	public void scheduleNextEmitTime() {
		if (capacity > 1.E-6) {
			releaseTime_ += 1.0 / capacity;
		}
		else {
			releaseTime_ = Constants.DBL_INF;
		}
	}
	
	public boolean checkPass() {
		if (releaseTime_<=SimulationClock.getInstance().getCurrentTime()) 
			return true;
		else 
			return false;
	}*/
	
	public void lanechange() {
		double platoonhead;
		double platoontail;
		MLPVehicle theveh;//处理中的车辆
		Random r = network.getSysRand();
		Collections.shuffle(jointLanes,r);//任意车道位置排序
		for (JointLane JLn: jointLanes){
			//遍历所有车辆，组成车队以后通过deal来处理
			if (!JLn.hasNoVeh(false)){
				platoon.clear();
				theveh = JLn.getFirstVeh();
				platoon.add(theveh);
//				platoonhead = theveh.Displacement();
//				if (((MLPSegment) getEndSegment()).endDSP  == Double.NaN)
//					System.out.println("DEBUG: NaN Err");
				platoonhead = JLn.getJointLaneEndDSP();//before: //((MLPSegment) getEndSegment()).endDSP;
//				if (Math.max(0.0, theveh.Displacement() - theveh.getLength())==Double.NaN)
//					System.out.println("DEBUG: NaN Err");
				platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
				while (theveh.getUpStreamVeh() != null){
					theveh = theveh.getUpStreamVeh();
					if (theveh.resemblance) {						
						platoon.add(theveh);
//						if (Math.max(0.0, theveh.Displacement() - theveh.getLength())==Double.NaN)
//							System.out.println("DEBUG: NaN Err");
						platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
					}
					else {
						dealLC(platoonhead,platoontail);
						platoon.clear();
						platoon.add(theveh);//新车队
						platoonhead = platoontail;
//						if (Math.max(0.0, theveh.Displacement() - theveh.getLength()) == Double.NaN)
//							System.out.println("DEBUG: NaN Err");
						platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
					}
				}
				dealLC(platoonhead,platoontail);
			}
		}
	}
	
	public void dealLC(double headDsp, double tailDsp) {
//		if (headDsp == Double.NaN || tailDsp == Double.NaN)
//			System.out.println("DEBUG: NaN Err");
		Random r = network.getSysRand();
		Collections.shuffle(platoon,r);
		for (MLPVehicle veh: platoon){
			//虚拟车及冷却中的车不能换道
			if (veh.virtualType == 0 && veh.buffer == 0) {
				//根据acceptance及道路规则，获取可换道车道信息，计算概率并排序
				double [] pr = new double [] {0.0, 0.0};
				int [] turning = new int [] {0,1};
				for (int i = 0; i<2; i++){//i=0右转；i=1左转；
					MLPLane tarLane = veh.lane.getAdjacent(i);
					if (tarLane != null && //换道检查
							tarLane.checkLCAllowen((i+1)%2) &&
							//tarLane.RtCutinAllowed &&
							veh.checkGapAccept(tarLane)) {
						//换道概率计算
						pr[i] = veh.calLCProbability(i, tailDsp, headDsp, (double) platoon.size());
						//简易版换道计算，based on lane
//						pr[i] = veh.calLCProbability2(i);
					}
				}
				//排序
				if (pr[0]<pr[1]){
					turning[0] = 1;
					turning[1] = 0;
					double tmp = pr[0];
					pr[0] = pr[1];
					pr[1] = tmp;
				}
				//按先后顺序做蒙特卡洛，操作成功的进行换道，不成功换道的MLC车进行停车标识计算
				if (r.nextDouble()<pr[0]){
					if (veh.lane.getAdjacent(turning[0]).diEqualsZero(veh)) {
						veh.stopFlag = false;
					}
//					tmpWriter.write(pr[0] + "\r\n");
					LCOperate(veh, turning[0]);
				}
				else{
					if (r.nextDouble()<pr[1]){
						if (veh.lane.getAdjacent(turning[1]).diEqualsZero(veh)) {
							veh.stopFlag = false;
						}
//						tmpWriter.write(pr[1] + "\r\n");
						LCOperate(veh, turning[1]);
					}					
				}
			}
			if ((!veh.lane.diEqualsZero(veh))){
				if (veh.have2ChangeLane() && veh.calMLC()>0.99)
					veh.stopFlag = true;
			}
		}
	}
	
	public void LCOperate(MLPVehicle veh, int turn) {
		MLPLane thisLane = veh.lane;
		MLPLane tarLane = thisLane.getAdjacent(turn);
		//虚拟车(生产->初始化*2->加buff->替换)
		MLPVehicle newVeh = ((MLPNetwork) network).generateVeh();
		newVeh.initInfo(veh.getId(),veh.link,veh.segment,veh.lane,veh.rvId);
		newVeh.init(((MLPNetwork) network).getNewVehID(), MLPParameter.VEHICLE_LENGTH, veh.getDistance(), veh.getCurrentSpeed());
		newVeh.setLCPath(this);
		newVeh.fixPath();
		MLPParameter mlpParameter = (MLPParameter) network.getSimParameter();
		newVeh.buffer = mlpParameter.getLCBuff();
		thisLane.substitudeVeh(veh, newVeh);
		//换道车(加buff->insert)
		veh.buffer = mlpParameter.getLCBuff();
		tarLane.insertVeh(veh);
	}
	
	public void move() {
		double platoonhead;
		double platoontail;
		MLPVehicle theveh;//处理中的车辆
		Random r = network.getSysRand();
		Collections.shuffle(jointLanes,r);//任意车道位置排序
		for (JointLane JLn: jointLanes){
			//遍历所有车辆，组成车队以后通过deal来处理
			if (!JLn.hasNoVeh(true)){
				platoon.clear();
				theveh = JLn.getFirstVeh();
				platoon.add(theveh);
//				platoonhead = theveh.Displacement();
				platoonhead = ((MLPSegment) getEndSegment()).endDSP;//((MLPSegment) getSegment(nSegments -1))
				platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
				while (theveh.getUpStreamVeh() != null){
					theveh = theveh.getUpStreamVeh();
					if (theveh.resemblance) {
						platoon.add(theveh);
						platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
					}
					else {
//						platoontail = theveh.Displacement();//加尾 处理车队
						dealMove(platoonhead,platoontail);
						platoon.clear();
						platoon.add(theveh);//新车队
//						platoonhead = theveh.Displacement();//新头
						platoonhead = platoontail;
						platoontail = Math.max(0.0, theveh.Displacement() - theveh.getLength());
					}
				}
//				platoontail = theveh.Displacement();//余下的尾 处理车队
				dealMove(platoonhead,platoontail);
			}
		}
	}
	
	public void dealMove(double headDsp, double tailDsp){
		double k = ((double)platoon.size())/(headDsp-tailDsp);
		double tailspeed = dynaFun.sdFun(k);
		double headspeed = dynaFun.updateHeadSpd(platoon.get(0));
		if (platoon.size()>1) {
			double headPt = platoon.get(0).Displacement();
			double len = headPt - platoon.get(platoon.size()-1).Displacement();
//			if (len == 0.0){
//				System.out.println("BUG 车队长度为0");
//			}
			for (MLPVehicle veh : platoon) {
				double r = (headPt - veh.Displacement())/len;
				double newspd = (1-r)*headspeed+r*tailspeed;
				veh.setNewState(newspd);
			}
		}
		else {
			MLPVehicle veh = platoon.get(0);
			veh.setNewState(headspeed);
		}
	}

	public void generateInflow(int demand, double[] speed, double[] time, List<Lane> lanes, long tlnkID){
		Random r = getNetwork().getSysRand();
		double mean = speed[0];
		double sd = speed[1];
		double vlim = speed[2];
		double startTime = time[0];
		double endTime = time[1];
		int laneCount = lanes.size();
		double simStep = getNetwork().getSimClock().getStepSize();
		int stepCount = (int) Math.floor((endTime-startTime)/simStep);
		double expect =(double) demand/stepCount;
//		NormalDistribution nd = new NormalDistribution(mean, sd);
		Lane bornLane = lanes.get(r.nextInt(laneCount));
		for (int i = 1; i<=stepCount; i++){
			if (r.nextDouble()<=expect){
				Inflow theinflow = new Inflow(startTime+i*simStep,
						Math.min(vlim, Math.max(0.01,r.nextGaussian()*sd+mean)),
						bornLane.getIndex(),
						tlnkID,
						bornLane.getLength());
				inflowList.offer(theinflow);
			}
		}
	}

	protected void appendInflowFromCSV(int laneId, int tLinkID, double time, double speed, double dis, int realVID){
		//将小于0的dis置为路段起点
		double dis2 = dis < 0.0 ? getNetwork().findLane(laneId).getLength() : dis;
		int laneIdx = getNetwork().findLane(laneId).getIndex();
		Inflow theinflow = new Inflow(time, speed, laneIdx, tLinkID, dis2, realVID);
		inflowList.offer(theinflow);
	}

	protected LinkedList<Inflow> getInflow() {
		return inflowList;
	}

	protected Inflow pollInflow() {
		emitCount++;
		return inflowList.poll();
	}

	public int getEmitNum() {
		return emitCount;
	}

	protected void resetEmitCount() {
		emitCount = 0;
	}

	protected void clearInflow() {
		inflowList.clear();
	}

	public int countHoldingInflow() {
		return inflowList.size();
	}

	public List<double[]> getServedVehRecs(double fTime, double tTime) {
		List<double[]> list = new ArrayList<>();
		tripTime.stream().filter(trT -> trT[MLPLink.TIMEIN_MASK]>fTime && trT[MLPLink.TIMEOUT_MASK]<=tTime).forEach(r -> list.add(r));
		tripTime.removeAll(list);
		return list;
	}

	public static final int TIMEIN_MASK = 0;

	public static final int DSPIN_MASK = 1;

	public static final int TIMEOUT_MASK = 2;

	public GraphPath<MLPLane, DefaultWeightedEdge> findLCRoute(MLPLane fLane, MLPLane tLane) {
		return DijkstraShortestPath.findPathBetween(laneGraph, fLane, tLane);
	}

	public double getLCRouteWeight(MLPLane fLane, MLPLane tLane) {
		GraphPath path = findLCRoute(fLane, tLane);
		return path==null ? Double.POSITIVE_INFINITY : path.getWeight();
	}

	public double getLCRouteWeight(MLPLane fLane, List<MLPLane> tLanes) {
		double ans = Double.POSITIVE_INFINITY;
		for (MLPLane tLane : tLanes)
			ans	= Math.min(ans, getLCRouteWeight(fLane, tLane));
		return ans;
	}

	public List<MLPLane> validEndLanesFor(MLPVehicle veh) {
		List<MLPLane> ans = new ArrayList<>();
		for (int i = 0; i < getEndSegment().nLanes(); i++) {
			ans.add((MLPLane) getEndSegment().getLane(i));
		}
		MLPLink nextLink = (MLPLink) veh.getNextLink();

		//on the last link
		if (nextLink == null){
			return ans;
		}

		List<Lane> dnStreamValidLanes = ((MLPSegment) nextLink.getStartSegment()).getValidLanes(veh);

		//this lane connects with next link; find out if nextNode is an intersection
		if (getDnNode().type(Constants.NODE_TYPE_INTERSECTION)!=0)
			//next node is an intersection
			return ans.stream().filter(l -> l.connect2DnLanes(dnStreamValidLanes)).collect(Collectors.toList());
		else
			//next node is NOT an intersection
			return ans.stream().filter(l -> l.successivelyConnect2DnLanes(dnStreamValidLanes)).collect(Collectors.toList());
	}

	protected void addLaneGraphVertex(MLPLane mlpLane) {
		laneGraph.addVertex(mlpLane);
	}

	protected void addLaneGraphEdge(MLPLane fLane, MLPLane tLane, double weight) {
		DefaultWeightedEdge edge = new DefaultWeightedEdge();
		laneGraph.addEdge(fLane, tLane, edge);
		laneGraph.setEdgeWeight(edge,weight);
	}

	protected void organizeTurnableDnLinks(){
		getEndSegment().getLanes().forEach(lane -> {
			((MLPLane)lane).dnStrmConns.forEach(conn -> {
				if (turnableNextLinks.get(conn.dnLinkID())==null){
					MLPLink tdLink = (MLPLink) conn.dnLane.getLink();
					if (((MLPNode) getDnNode()).dnLinkExist(tdLink))
						turnableNextLinks.put(conn.dnLinkID(),tdLink);
					else
						System.out.println("warning: turning conflicts at downstream of link no. " + getId());
				}
			});
		});
	}

	public List<MLPLink> getTurnableDnLinks(){
		return new ArrayList<>(turnableNextLinks.values());
	}

	public boolean checkTurningTo(Long nextLinkID) {
		return turnableNextLinks.keySet().contains(nextLinkID);
	}
}
