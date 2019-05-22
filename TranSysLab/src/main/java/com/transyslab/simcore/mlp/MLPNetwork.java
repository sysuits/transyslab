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

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.transyslab.commons.io.*;
import com.transyslab.commons.renderer.AnimationFrame;
import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.roadnetwork.*;
import org.apache.commons.csv.CSVRecord;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

public class MLPNetwork extends RoadNetwork {
	private int newVehID_;
	public List<MLPVehicle> veh_list;
	protected LinkedList<MLPVehicle> vehPool;
	BufferedReader bReader;
//	public List<MLPLoop> sensors;

	//引擎输出变量
	protected HashMap<MLPLink, List<MacroCharacter>> linkStatMap;
	protected HashMap<String, List<MacroCharacter>> sectionStatMap;
	protected HashMap<MLPLoop, List<MacroCharacter>> laneSecMap;

	public MLPNetwork() {
		simParameter = new MLPParameter();//需要在RoadNetwork子类初始化

		newVehID_ = 0;
		veh_list = new ArrayList<>();
		vehPool = new LinkedList<>();

		linkStatMap = new HashMap<>();
		sectionStatMap = new HashMap<>();
		laneSecMap = new HashMap<>();

	}

	@Override
	public Node createNode(long id, int type, String name, GeoPoint posPoint) {
		MLPNode newNode = new MLPNode();
		newNode.init(id, type, nNodes() ,name, posPoint);
		worldSpace.recordExtremePoints(newNode.getPosPoint());
		this.nodes.add(newNode);
		this.addVertex(newNode);
		return newNode;
	}

	@Override
	public Link createLink(long id, int type, String name,long upNodeId, long dnNodeId) {
		MLPLink newLink = new MLPLink();
		newLink.init(id,type,name,nLinks(),findNode(upNodeId),findNode(dnNodeId),this);
		links.add(newLink);
		return newLink;
	}
	@Override
	public Segment createSegment(long id, int speedLimit, double freeSpeed, double grd, List<GeoPoint> ctrlPoint) {
		MLPSegment newSegment = new MLPSegment();
		newSegment.init(id,speedLimit,nSegments(),freeSpeed,grd,ctrlPoint,links.get(nLinks()-1));
		worldSpace.recordExtremePoints(ctrlPoint);
		segments.add(newSegment);
		return newSegment;
	}

	@Override
	public Lane createLane(long id, int rule, int orderNum, double width ,String direction,List<GeoPoint> ctrlPoints) {
		MLPLane newLane = new MLPLane();
		newLane.init(id,rule,nLanes(),orderNum,width,direction,ctrlPoints,segments.get(nSegments()-1));
		worldSpace.recordExtremePoints(ctrlPoints);
		lanes.add(newLane);
		return newLane;
	}

	@Override
	public Sensor createSensor(long id, int type, String detName, long segId, double pos, double zone, double interval) {
		MLPSegment seg = (MLPSegment) findSegment(segId);
		MLPLink lnk = (MLPLink) seg.getLink();
		double dsp = seg.startDSP + seg.getLength()*pos;
		for (int i = 0; i < seg.nLanes(); i++) {
			MLPLane ln = seg.getLane(i);
			MLPLoop loop = new MLPLoop(ln, seg, lnk, detName, dsp, pos);
			sensors.add(loop);
		}
		return null;
	}



	public MLPNode mlpNode(int i) {
		return (MLPNode) getNode(i);
	}
	public MLPLink mlpLink(int i) {
		return (MLPLink) getLink(i);
	}
	public MLPLane mlpLane(int i){
		return (MLPLane) getLane(i);
	}

	@Override
	public MLPLink findLink(long id) {
		return (MLPLink) super.findLink(id);
	}

	public void calcStaticInfo() {
		super.calcStaticInfo();
		organize();
	}
	public void calcDbStaticInfo() {
		super.calcDbStaticInfo();
		organize();
	}

	public void organize() {
		//补充车道编号的信息
		for (Lane l: lanes){
			((MLPLane) l).calLnPos();
		}
		
		for (Segment seg: segments){
			Segment tmpseg = seg;
			while (tmpseg.getUpSegment() != null) {
				tmpseg = tmpseg.getUpSegment();
				((MLPSegment) seg).startDSP += tmpseg.getLength();
			}
			((MLPSegment) seg).endDSP = ((MLPSegment) seg).startDSP + seg.getLength();
		}

		for (Segment seg: segments) {
			((MLPSegment) seg).setSucessiveLanes();
		}

		for (Lane l: lanes){
			((MLPLane) l).checkConectedLane();
		}

		int li = 0;
		for (Link l: links){
			//预留
			((MLPLink) l).checkConnectivity();
			//networkGraph
			this.addEdge(l.getUpNode(),l.getDnNode(),l);
			this.setEdgeWeight(l,Double.POSITIVE_INFINITY);
			//组织laneGraph
			segments.forEach(segment -> {
				lanes.forEach(lane -> {
					((MLPLink) l).addLaneGraphVertex((MLPLane) lane);
				});
			});
			for (Segment seg: segments) {
				for (int i = 0; i < seg.nLanes(); i++) {
					MLPLane mlpLane = (MLPLane) seg.getLane(i);
					if (i<seg.nLanes()-1)//可叠加实线判断
						((MLPLink)l).addLaneGraphEdge(mlpLane, (MLPLane) mlpLane.getRightLane(), 1.0);
					if (i>0)//可叠加实线判断
						((MLPLink)l).addLaneGraphEdge(mlpLane, (MLPLane) mlpLane.getLeftLane(),1.0);
					if (!((MLPSegment) seg).isEndSeg())
						mlpLane.successiveDnLanes.forEach(suDnLane ->
								((MLPLink)l).addLaneGraphEdge(mlpLane, (MLPLane) suDnLane, 0.0));//可叠加封路判断
				}
			}
			//将jointLane信息装入Link中
			((MLPLink) l).addLnPosInfo();
			((MLPLink) l).organizeTurnableDnLinks();
			li += 1;
			System.out.println("DEBUG message: link " + l.getId() + " " + li + "th of " + links.size());
		}
	}

	public void buildEmitTable(boolean needRET, String odFileDir, String emitFileDir){
		if (needRET) {
			createRndETables(odFileDir);
			for (int i = 0; i < nLinks(); i++) {
				Collections.sort(mlpLink(i).getInflow(), (a,b) -> a.time < b.time ? -1 : a.time > b.time ? 1 : 0);
			}
		}
		else {
			readETablesFrom(emitFileDir);
		}
	}

	public void resetReleaseTime(){
		for (int i = 0; i<nLanes(); i++){
			mlpLane(i).resetReleaseTime();
		}
	}
	
	public int getNewVehID(){
		newVehID_ += 1;	
		return newVehID_ ;
	}
	
	public void loadEmtTable(){
		for (int i = 0; i<nLinks(); i++){
			MLPLink launchingLink = mlpLink(i);
			while (launchingLink.checkFirstEmtTableRec()){
				Inflow emitVeh = launchingLink.pollInflow();
//				System.out.println("DEBUG " + emitVeh.time + " " + emitVeh.laneIdx + " " + emitVeh.realVID);
				MLPVehicle newVeh = generateVeh();
				newVeh.initInfo(0,launchingLink,mlpLane(emitVeh.laneIdx).getSegment(),mlpLane(emitVeh.laneIdx),emitVeh.realVID);
				newVeh.init(getNewVehID(), MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) emitVeh.speed);
				MLPNode upNode = (MLPNode) launchingLink.getUpNode();
				MLPNode dnNode = emitVeh.tLinkID==0 ? null : (MLPNode) findLink(emitVeh.tLinkID).getDnNode();
				assignPath(newVeh, upNode, dnNode, false);
				//todo 调试阶段暂时固定路径
				newVeh.fixPath();
				newVeh.initNetworkEntrance(simClock.getCurrentTime(), mlpLane(emitVeh.laneIdx).getLength()-emitVeh.dis);
				//进入路网，初始化强制换道参考值di
				newVeh.updateDi();
				if (ExpSwitch.SPD_BUFFER)
					newVeh.spdBuffer = ExpSwitch.SPD_BUFFER_VAL;
				//newVeh.init(getNewVehID(), 1, MLPParameter.VEHICLE_LENGTH, (float) emitVeh.dis, (float) now);
				mlpLane(emitVeh.laneIdx).appendVeh(newVeh);
			}
		}
	}
	
	public void platoonRecognize() {
		for (MLPVehicle mlpv : veh_list){
			mlpv.calState();
			if (mlpv.cfState && mlpv.speedLevel ==mlpv.leading.speedLevel) {
				mlpv.resemblance = true;
			}
			else {
				mlpv.resemblance = false;
			}
			mlpv.resetPlatoonCode();
		}
	}
	
	public void setOverallCapacity(double arg) {
		for (int i = 0; i < nLanes(); i++) {
			mlpLane(i).setCapacity(arg);
		}
	}	
	public void setCapacity(int laneIdx, double capacity) {
		mlpLane(laneIdx).setCapacity(capacity);
	}

	public void setOverallSDParas(double[] args, int mask) {
		for (int i = 0; i < nLinks(); i++) {
			mlpLink(i).dynaFun.setPartialCharacteristics(args, mask);
		}
	}
	public void setSDParas(int linkIdx, double[] args, int mask) {
		mlpLink(linkIdx).dynaFun.setPartialCharacteristics(args, mask);
	}
	
	public void setLoopsOnLink(String name, long linkID, double p) {
		MLPLink theLink = findLink(linkID);
		MLPSegment theSeg = null;
		MLPLane theLane = null;
		//这里的p是自Link起点的百分位位置
		double dsp = ((MLPSegment) theLink.getEndSegment()).endDSP * p;
		for (int i = 0; i<theLink.nSegments(); i++) {
			theSeg = (MLPSegment) theLink.getSegment(i);
			if (theSeg.startDSP<dsp && theSeg.endDSP>=dsp) 
				break;
		}
		if (theSeg != null) {
			MLPSegment endSeg = (MLPSegment) theLink.getEndSegment();
			double portion = (dsp - endSeg.startDSP) / endSeg.getLength();
			for (int k = 0; k<theSeg.nLanes(); k++) {
				theLane = theSeg.getLane(k);
				sensors.add(new MLPLoop(theLane, theSeg, theLink, name, dsp, portion));
			}
		}
		
	}

	public void setLoopsOnSeg(String name, int segId, double portion) {
		//这里的portion是自seg起点的百分比
		MLPSegment theSeg = (MLPSegment) findSegment(segId);
		MLPLane theLane = null;
		double dsp = theSeg.startDSP + theSeg.getLength() * portion;
		for (int k = 0; k<theSeg.nLanes(); k++) {
			theLane = theSeg.getLane(k);
			sensors.add(new MLPLoop(theLane, theSeg, (MLPLink) theSeg.getLink(), name, dsp, portion));
		}
	}

	public void sectionStatistics(double fTime, double tTime, int avgMode) {
		List<Double> spdRecords = new ArrayList<>();
		sectionStatMap.forEach((detName,aggRec) -> {
			spdRecords.clear();
			laneSecMap.forEach((loop,laneAggRec) -> {
				if (loop.getName().equals(detName)) {
					List<Double> laneRawRec = loop.getPeriodSpds(fTime,tTime,false);
					spdRecords.addAll(laneRawRec);
					//cal lane flow
					double flow = laneRawRec.size();
					//cal lane meanSpd
					double meanSpd = flow <= 0 ? 0.0 :
							avgMode == Constants.ARITHMETIC_MEAN ? laneRawRec.stream().mapToDouble(d->d).sum() / flow :
									avgMode == Constants.HARMONIC_MEAN ? flow / laneRawRec.stream().mapToDouble(d -> 1/d).sum() :
											0.0;
					flow = flow / (tTime-fTime);
					laneAggRec.add(new MacroCharacter(flow, meanSpd, flow <= 0 ? 0.0 : flow / meanSpd, Double.NaN));
				}
			});
			//calculate section flow
			double secVol = spdRecords.size();
			//calculate section mean speed
			double secMeanSpd = secVol <= 0 ? 0.0 :
					avgMode == Constants.ARITHMETIC_MEAN ? spdRecords.stream().mapToDouble(d->d).sum() / secVol :
							avgMode == Constants.HARMONIC_MEAN ? secVol / spdRecords.stream().mapToDouble(d -> 1/d).sum() :
									0.0;
			double secFlow = secVol / (tTime-fTime);
			aggRec.add(new MacroCharacter(secFlow, secMeanSpd, secFlow<=0.0? 0.0 : secFlow/secMeanSpd, Double.NaN));
		});
	}

	public void linkStatistics(double fTime, double tTime) {
		/*flow为link在时间段内服务的完整的trip的个数；speed为总服务里程/总服务时间； TrT为前两者的商*/
		double now = getSimClock().getCurrentTime();
		for (MLPLink mlpLink : linkStatMap.keySet()) {
			double linkLen = mlpLink.length();

			if (simClock.getCurrentTime()<=tTime) { //当前时间正在服务的车辆也计算在内
				Object[] servingVehsObj = veh_list.stream().filter(v ->
						v.virtualType>0
								&& v.getLink().equals(mlpLink)
								&& v.timeEntersLink()<now).toArray();
				MLPVehicle[] servingVehs = Arrays.copyOf(servingVehsObj,servingVehsObj.length,MLPVehicle[].class);
				double onLinkTripSum = Arrays.stream(servingVehs).mapToDouble(v -> (v.Displacement()-v.dspLinkEntrance)/linkLen).sum();
				double onLinkTrTSum = Arrays.stream(servingVehs).mapToDouble(v -> (now - v.timeEntersLink())).sum();
			}

//			Object[] servedRecordsObj = mlpLink.tripTime.stream().filter(trT -> trT[MLPLink.TIMEIN_MASK]>fTime && trT[MLPLink.TIMEOUT_MASK]<=tTime).toArray();
//			double[][] servedRecords = Arrays.copyOf(servedRecordsObj,servedRecordsObj.length,double[][].class);
			List<double[]> servedRecords = mlpLink.getServedVehRecs(fTime,tTime);
			double servedTripSum = servedRecords.stream().mapToDouble(r -> (linkLen-r[MLPLink.DSPIN_MASK])/linkLen).sum();
			double servedTrTSum = servedRecords.stream().mapToDouble(r -> r[MLPLink.TIMEOUT_MASK] - r[MLPLink.TIMEIN_MASK]).sum();

			/*double flow = onLinkTripSum + servedTripSum;
			double meanSpd = flow<=0.0 ? 0.0 : linkLen*flow/(onLinkTrTSum+servedTrTSum);*/
			double flowSum = servedTripSum;
			double meanSpd = flowSum<=0.0 ? 0.0 : linkLen*flowSum/servedTrTSum;
			double trT = meanSpd<=0.0 ? 0.0 : linkLen / meanSpd;

			double flow = flowSum / (tTime-fTime);
			linkStatMap.get(mlpLink).add(new MacroCharacter(flow, meanSpd, flow <= 0 ? 0.0 : flow/meanSpd, trT));
		}
	}

	public void linkStatistics_Old(double fTime, double tTime) {
		double now = getSimClock().getCurrentTime();
		for (MLPLink mlpLink : linkStatMap.keySet()) {
			double[][] servedRecords = (double[][]) mlpLink.tripTime.stream().filter(trT -> trT[0]>fTime && trT[0]<=tTime).toArray();
			if (servedRecords.length > 0) {
				double flow = servedRecords.length;
				double trT = Arrays.stream(servedRecords).mapToDouble(r -> r[1]).sum()/flow;
				double meanSpd = flow * mlpLink.length() / trT;
				double density = flow/meanSpd;
				flow = flow / (tTime-fTime);
				linkStatMap.get(mlpLink).add(new MacroCharacter(flow, meanSpd, density, trT));
			}
			else
				linkStatMap.get(mlpLink).add(new MacroCharacter(0,0,0,0));
		}
	}
	
	public void resetNetwork(long seed) {
		sysRand.setSeed(seed);//重置系统种子
		newVehID_ = 0;//重置车辆编号
		resetReleaseTime();//重置capacity控制周期时间
		for (int i = 0; i < nLinks(); i++) {
			MLPLink LNK = mlpLink(i);
			LNK.clearInflow();//未发出的车从emtTable中移除
			LNK.tripTime.clear();//重置已记录的Trip Time
			LNK.resetEmitCount();//重置发车计数
			Collections.sort(LNK.jointLanes, (a,b) -> a.jlNum <b.jlNum ? -1 : a.jlNum ==b.jlNum ? 0 : 1);//车道排列顺序复位
		}
		for (int i = 0; i < nLanes(); i++) {
			mlpLane(i).vehsOnLn.clear();//从lane上移除在网车辆
		}
		connectors.forEach(conn -> ((MLPConnector)conn).clearVehsOnConn());//remove vehicles on lane connector
		for (int i = 0; i < nNodes(); i++) {
			mlpNode(i).clearStatedVehs();//从Node上清除未加入路段的车辆
		}
		for (int i = 0; i < sensors.size(); i++) {
			((MLPLoop) sensors.get(i)).clearRecords();//清除检测器记录结果
		}
		recycleAllVehs();//回收所有在网车辆
//		buildEmitTable(needRET, odFileDir, emitFileDir);//重新建立发车(旧设计 每次完整load进所有的发车信息)
		bReader = null; //新设计，重置缓冲区。

		//重置输出参数
		clearSecStat();
		clearLinkStat();
	}

	public void transformVehData(VehicleData vd, MLPVehicle veh) {
		vd.init(veh,false,(veh.resemblance ? Constants.FOLLOWING : 0) + Math.min(1,veh.virtualType), veh.getInfo());
//		vd.setPathInfo(veh.getPath());
		//todo: 过渡方案
		//计算翻译线性参考
		if (veh.conn==null){
			double l0 = veh.segment.getLength();
			double lr = Math.max(0.0,l0-veh.getDistance()) / l0;
			double l1 = veh.lane.getGeoLength();
			if (!veh.segment.isEndSeg()){
				if (veh.link.getUpNode().type(Constants.NODE_TYPE_INTERSECTION)==0 &&
						veh.segment.isStartSeg() &&
						veh.getPath().index()>1) {
					MLPConnector conn_ = veh.lane.upStrmConns.stream().filter(c->c.upLane.getId()==veh.shownUpLane.getId()).findFirst().orElse(null);
					double l_ = conn_.getLength();

					int sDnLaneNum = veh.lane.successiveDnLanes.size();
					MLPLane successiveDnLane = veh.lane.dnStrmConns.size()==1 ?
							(MLPLane) veh.lane.dnLane(0) :
							sDnLaneNum==1 ?
									veh.lane.successiveDnLanes.get(0) :
									null;
					if (successiveDnLane==null)
						System.err.println("wrong successiveDnNum");
					MLPConnector conn = (MLPConnector) veh.lane.dnStrmConns.stream().filter(c->c.dnLane.equals(successiveDnLane)).findFirst().orElse(null);
					if (conn==null)
						System.err.println("can not find a conn");
					double l2 = conn.getLength();

					double cr1 = l_ / (l_+l1+l2);
					double cr2 = (l_+l1) / (l_+l1+l2);
					if (lr<=cr1)
						vd.init(String.valueOf(veh.getId()), conn_, veh.getLength(), Math.min(l_-1e-5,lr*(l_+l1+l2)), veh.getCurrentSpeed(),"B", Math.abs(veh.getCurrentSpeed())<1.0/3.6, true);
					else if (lr<=cr2)
						vd.init(String.valueOf(veh.getId()), veh.lane, veh.getLength(), Math.min(l1-1e-5,lr*(l_+l1+l2)-l_), veh.getCurrentSpeed(),"B", Math.abs(veh.getCurrentSpeed())<1.0/3.6, true);
					else
						vd.init(String.valueOf(veh.getId()), conn, veh.getLength(), Math.min(l2-1e-5,lr*(l_+l1+l2)-l_-l1), veh.getCurrentSpeed(),"B", Math.abs(veh.getCurrentSpeed())<1.0/3.6, true);
				}
				else {
					//on the road
					//find the connector
					int sDnLaneNum = veh.lane.successiveDnLanes.size();
					MLPLane successiveDnLane = veh.lane.dnStrmConns.size()==1 ?
							(MLPLane) veh.lane.dnLane(0) :
							sDnLaneNum==1 ?
									veh.lane.successiveDnLanes.get(0) :
									null;
					if (successiveDnLane==null)
						System.err.println("wrong successiveDnNum");
					MLPConnector conn = (MLPConnector) veh.lane.dnStrmConns.stream().filter(c->c.dnLane.equals(successiveDnLane)).findFirst().orElse(null);
					if (conn==null)
						System.err.println("can not find a conn");

					double l2 = conn.getLength();
					double cr = l1 / (l1+l2);
					if (lr<=cr)
						vd.init(String.valueOf(veh.getId()), veh.lane, veh.getLength(), Math.min(l1-1e-5,lr*(l1+l2)), veh.getCurrentSpeed(),"B", Math.abs(veh.getCurrentSpeed())<1.0/3.6, true);
					else
						vd.init(String.valueOf(veh.getId()), conn, veh.getLength(), Math.min(l2-1e-5,lr*(l1+l2)-l1), veh.getCurrentSpeed(),"B", Math.abs(veh.getCurrentSpeed())<1.0/3.6, true);
				}
			}
			else {
				if (veh.link.getDnNode().type(Constants.NODE_TYPE_INTERSECTION)==0) {

				}
				else
					vd.init(String.valueOf(veh.getId()), veh.lane, veh.getLength(), Math.min(l1-1e-5,lr*l1), veh.getCurrentSpeed(),"B", Math.abs(veh.getCurrentSpeed())<1.0/3.6, true);
			}

		}
		else {
			//in an intersection
			vd.init(String.valueOf(veh.getId()), veh.conn, veh.getLength(), Math.min(veh.conn.getLength()-1e-5,veh.conn.getLength()-veh.getDistance()), veh.getCurrentSpeed(),"B", Math.abs(veh.getCurrentSpeed())<1.0/3.6, true);
		}
	}
	
	public void recordVehicleData(){
		VehicleData vd;
		AnimationFrame af;
		if (!veh_list.isEmpty()) {
			af = new AnimationFrame();
			//遍历vehicle
			for (MLPVehicle v : veh_list) {
				//从对象池获取vehicledata对象
				vd = VehicleDataPool.getInstance().newData();
				//记录车辆信息
				transformVehData(vd, v);
				//将vehicledata插入frame
				af.addVehicleData(vd);
			}
			//添加额外信息(过车数)
			int count = 0;
			for(int i = 0; i< nSensors(); i++){
				MLPLoop tmpSensor = (MLPLoop) getSensor(i);
				count = count + tmpSensor.getRecords().size();
			}
			af.setInfo("Count",count);
			//添加时间信息
			af.setSimTimeInSeconds(getSimClock().getCurrentTime());
			//记录信控灯色
			setArrowColor(getSimClock().getCurrentTime(), af.getSignalColors());
			try {
				FrameQueue.getInstance().offer(af);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void assignPath(MLPVehicle mlpVeh, MLPNode oriNode, MLPNode desNode, boolean isImported) {
		int maxRandWalkStep = 20;
		if (isImported) {
			// todo 完善导入路径的逻辑
			mlpVeh.fixPath();
		}
		else {
			if (desNode!=null) {
				ODPair thePair = findODPair(oriNode, desNode);
				mlpVeh.setPath(thePair.assignRoute(mlpVeh, getSysRand().nextDouble()),1);
			}
			else {
				List<Link> links = new ArrayList<>();
				Node n;
				Link l;
				int stepIndex = 1;
				if (mlpVeh.link!=null) {
					//make sure path begins with the link on which the vehicle is running
					l = mlpVeh.getLink();
					links.add(l);
					n = mlpVeh.getLink().getDnNode();
				}
				else {
					l = oriNode.getDnLink(getSysRand().nextInt(oriNode.nDnLinks()));
					links.add(l);
					n = l.getDnNode();
				}
				while (stepIndex<maxRandWalkStep) {
					List<MLPLink> dnLinks = ((MLPLink)l).getTurnableDnLinks();
					if (dnLinks.size()<=0)
						break;
					l = dnLinks.get(getSysRand().nextInt(dnLinks.size()));
					links.add(l);
					n = l.getDnNode();
					stepIndex += 1;
				}
				Path randPath = new Path(oriNode,n,links);
				mlpVeh.setPath(randPath,1);
			}
		}
	}

	@Override
	public ODPair findODPair(Node oriNode, Node desNode) {
		ODPair thePair = super.findODPair(oriNode, desNode);
		if (thePair == null) {
			// todo 应加入所有可行路径，非最短路
//			GraphPath<Node, Link> gpath = DijkstraShortestPath.findPathBetween(this, oriNode, desNode);
			//临时修改 wym
			List<GraphPath<Node, Link>> gpaths = (List<GraphPath<Node, Link>>) new AllDirectedPaths(this).getAllPaths(oriNode,desNode,true,Path.MAX_PATH_LENGTH);
			gpaths.sort(new Comparator<GraphPath<Node, Link>>() {
				@Override
				public int compare(GraphPath<Node, Link> o1, GraphPath<Node, Link> o2) {
					int n1 = o1.getEdgeList().size();
					int n2 = o2.getEdgeList().size();
					return n1<n2 ? -1 : n1>n2 ? 1 : 0;
				}
			});
			oriNode.setType(oriNode.getType() | Constants.NODE_TYPE_ORI);
			desNode.setType(desNode.getType() | Constants.NODE_TYPE_DES);
			ODPair newPair = new ODPair(oriNode, desNode);
			for (GraphPath gpath: gpaths) {
				newPair.addPath(new Path(gpath));
			}
			odPairs.add(newPair);
			return newPair;
		}
		else {
			return thePair;
		}
	}

	//MLPVehicle recycle operations
	public MLPVehicle generateVeh() {
		MLPVehicle newVeh;
		if (!vehPool.isEmpty())
			newVeh = vehPool.poll();
		else
			newVeh = new MLPVehicle((MLPParameter) simParameter);
		newVeh.updateUsage();
		veh_list.add(newVeh);
		return newVeh;
	}

	public void recycleVeh(MLPVehicle v) {
		veh_list.remove(v);
		if (vehPool.size()<100) {
			v.clearMLPProperties();
			vehPool.offer(v);
		}
		else v = null;
	}

	public void recycleAllVehs() {
		if (!veh_list.isEmpty()) {
			for (MLPVehicle veh : veh_list) {
				veh.clearMLPProperties();
				vehPool.offer(veh);
			}
			veh_list.clear();
		}
	}

	public int countIdleVeh(){
		return vehPool.size();
	}

	private void createRndETables(String odFileDir){
		if (odFileDir==null || odFileDir.equals(""))
			return;
		String[] header = {"fLinkID","tLinkID","demand",
				"fTime","tTime",
				"mean","sd","vlim"};
		try {
			List<CSVRecord> rows = CSVUtils.readCSV(odFileDir,header);
			for(int i = 1; i<rows.size(); i++){
				CSVRecord r = rows.get(i);
				int fLinkID = Integer.parseInt(r.get(0));
				int tLinkID = Integer.parseInt(r.get(1));
				int demand = Integer.parseInt(r.get(2));
				double [] time = {Double.parseDouble(r.get(3)),
								  Double.parseDouble(r.get(4))};
				double [] speed = {Double.parseDouble(r.get(5)),
								   Double.parseDouble(r.get(6)),
								   Double.parseDouble(r.get(7))};
				MLPLink theLink = findLink(fLinkID);
				List<Lane> lanes = theLink.getStartSegment().getLanes();
				theLink.generateInflow(demand, speed, time, lanes, tLinkID);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readETablesFrom(String filePath){
		if (filePath==null || filePath.equals(""))
			return;
		String[] header = {"laneID", "tLinkID", "time", "speed", "dis", "rvId"};
		try {
			List<CSVRecord> rows = CSVUtils.readCSV(filePath,header);
			int theLNID = Integer.MIN_VALUE;
			MLPLane theLN = null;
			MLPLink theLNK = null;
			for(int i = 1; i<rows.size(); i++){
				CSVRecord r = rows.get(i);
				int LNID = Integer.parseInt(r.get(0));
				if (theLNID != LNID) {
					theLN = (MLPLane) findLane(LNID);
					theLNK = (MLPLink) theLN.getLink();
					theLNID = LNID;
				}
				theLNK.appendInflowFromCSV(LNID,
						Integer.parseInt(r.get(1)),
						Double.parseDouble(r.get(2)),
						Double.parseDouble(r.get(3)),
						Double.parseDouble(r.get(4)),
						Integer.parseInt(r.get(5)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadInflowFromSQL(String tableName, double fTime, double tTime){
		try {
			Connection con = JdbcUtils.getConnection();
			String sql = "SELECT * FROM public." + tableName +
					" WHERE emitTime >= " + fTime +
					" AND emitTime < " + tTime +
					" ORDER BY rvid";
			PreparedStatement ps = con.prepareStatement(sql);
			ResultSet result = ps.executeQuery();
			int theLNID = Integer.MIN_VALUE;
			MLPLink theLNK = null;
			while (result.next()) {
				int LNID = result.getInt(1);
				if (theLNID != LNID) {
					MLPLane theLN = (MLPLane) findLane(LNID);
					theLNK = (MLPLink) theLN.getLink();
					theLNID = LNID;
				}
				theLNK.appendInflowFromCSV(LNID,
						result.getInt(2),
						result.getDouble(3),
						result.getDouble(4),
						result.getDouble(5),
						result.getInt(6));
			}
			JdbcUtils.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadInflowFromFile(String fileName, double tTime){
		try {

			if (bReader == null) {
				File f = new File(fileName);
				bReader = new BufferedReader(new FileReader(f));
				bReader.readLine();//table heading
			}

			String readLine = "";
			MLPLink theLNK = null;
			int theLNID = Integer.MIN_VALUE;
			double emitTime = 0.0;
			while ((readLine = bReader.readLine()) != null && emitTime <= tTime) {
				String[] items = readLine.split(",");
				emitTime = Double.parseDouble(items[2]);
				int LNID = Integer.parseInt(items[0]);
				if (theLNID != LNID) {
					MLPLane theLN = (MLPLane) findLane(LNID);
					theLNK = (MLPLink) theLN.getLink();
					theLNID = LNID;
				}
				theLNK.appendInflowFromCSV(LNID,
						Integer.parseInt(items[1]),
						emitTime,
						Double.parseDouble(items[3]),
						Double.parseDouble(items[4]),
						Integer.parseInt(items[5]));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void genInflowFromFile(String fileName, double tTime){
		try {

			if (bReader == null) {
				File f = new File(fileName);
				bReader = new BufferedReader(new FileReader(f));
				bReader.readLine();//table heading
			}

			String readLine;
			double emitTime = 0.0;
			while ((readLine = bReader.readLine()) != null && emitTime <= tTime) {
				String[] items = readLine.split(",");
				int demand = Integer.parseInt(items[2]);
				double [] time = {Double.parseDouble(items[3]),
						Double.parseDouble(items[4])};
				emitTime = Double.parseDouble(items[4]);
				double [] speed = {Double.parseDouble(items[5]),
						Double.parseDouble(items[6]),
						Double.parseDouble(items[7])};
				if (items[0]==null || items[0].equals(""))
					leafEmit(demand, speed, time);
				else {
					MLPLink fLink = findLink(Long.parseLong(items[0]));
					List<Lane> fLanes = fLink.getStartSegment().getLanes();
					if (items[1]==null || items[1].equals(""))
						fLink.generateInflow(demand, speed, time, fLanes, 0);
					else
						fLink.generateInflow(demand, speed, time, fLanes, Long.parseLong(items[1]));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void leafEmit(int demand, double[] speed, double[] time) {
		List<Link> fLinks = links.stream().filter(l->l.nUpLinks()==0).collect(Collectors.toList());
		List<Link> tLinks = links.stream().filter(l->l.nDnLinks()==0).collect(Collectors.toList());
		if (fLinks.size()>0 && tLinks.size()>0) {
			fLinks.forEach(fLink->{
				List<Lane> lanes = fLink.getStartSegment().getLanes();
				tLinks.forEach(tLink -> ((MLPLink)fLink).generateInflow(demand, speed, time, lanes, tLink.getId()));
			});
		}
	}

	public void clearInflows() {
		links.stream().forEach(l -> ((MLPLink) l).clearInflow());
	}
	public void initLinkStatMap(String linkIdStr) {
		String[] parts = linkIdStr.split(",");
		if (parts.length<=0 || parts[0].equals("")) return;
		Arrays.stream(parts).forEach(p -> linkStatMap.put(findLink(Integer.parseInt(p)), new ArrayList<>()));
	}
	public void initSectionStatMap(String detNameStr) {
		String[] parts = detNameStr.split(",");
		if (parts.length<=0 || parts[0].equals("")) return;
		for (String p : parts) {
			/*Object[] secObj = sensors.stream().filter(l -> ((MLPLoop) l).detName.equals(p)).toArray();
			MLPLoop[] sec = Arrays.copyOf(secObj, secObj.length, MLPLoop[].class);*/
			List<Sensor> laneSensors = sensors.stream().filter(l -> ((MLPLoop) l).detName.equals(p)).collect(Collectors.toList());
			laneSensors.forEach(s -> laneSecMap.put((MLPLoop) s, new ArrayList<>()));
			sectionStatMap.put(detNameStr, new ArrayList<>());
		}
	}
	public List<MacroCharacter> getSecStatRecords(String detName) {
		return sectionStatMap.get(detName);
	}
	public List<MacroCharacter> getLinkStatRecords(int LinkId) {
		MLPLink theLink = linkStatMap.keySet().stream().
				filter(link -> link.getId()==LinkId).
				findFirst().
				orElse(null);
		return theLink == null ? null : linkStatMap.get(theLink);
	}
	public void	writeStat(String filename){
		TXTUtils writer = new TXTUtils(filename);
		writer.writeNFlush("DET,TIME_PERIOD,FLOW,SPEED,DENSITY,TRAVEL_TIME\r\n");
		sectionStatMap.forEach((k,v) -> {
			String det = k;
			for (int i = 0; i<v.size(); i++) {
				MacroCharacter r = v.get(i);
				writer.write(det + "," +
						(i+1) + "," +
						r.getHourFlow() + "," +
						r.getKmSpeed() + "," +
						r.getKmDensity() + "," +
						r.travelTime + "\r\n");
			}
		});
		writer.flushBuffer();
		laneSecMap.forEach((k,v) -> {
			String det = k.detName + "_" + k.getLane().getId();
			for (int i = 0; i<v.size(); i++) {
				MacroCharacter r = v.get(i);
				writer.write(det + "," +
						(i+1) + "," +
						r.getHourFlow() + "," +
						r.getKmSpeed() + "," +
						r.getKmDensity() + "," +
						r.travelTime + "\r\n");
			}
		});
		writer.flushBuffer();
		linkStatMap.forEach((k,v) -> {
			String det = "Link" + k.getId();
			for (int i = 0; i<v.size(); i++) {
				MacroCharacter r = v.get(i);
				writer.write(det + "," +
						(i+1) + "," +
						r.getHourFlow() + "," +
						r.getKmSpeed() + "," +
						r.getKmDensity() + "," +
						r.travelTime + "\r\n");
			}
		});
		writer.flushBuffer();
		writer.closeWriter();
	}

	public void writeStat2Db(String tag, LocalDateTime dt) {
		DBWriter loopWriter = new DBWriter("insert into simloop(det, time_period, flow, speed, density, travel_time, tag, create_time) values(?,?,?,?,?,?,?,?)");
		sectionStatMap.forEach((k,v) -> {
			String det = k;
			for (int i = 0; i<v.size(); i++) {
				MacroCharacter r = v.get(i);
				loopWriter.write(new Object[] {det, (i+1), r.flow, r.speed, r.density, r.travelTime, tag, dt});
			}
		});
		laneSecMap.forEach((k,v) -> {
			String det = k.detName + "_" + k.getLane().getId();
			for (int i = 0; i<v.size(); i++) {
				MacroCharacter r = v.get(i);
				loopWriter.write(new Object[] {det, (i+1), r.flow, r.speed, r.density, r.travelTime, tag, dt});
			}
		});
		linkStatMap.forEach((k,v) -> {
			String det = "Link" + k.getId();
			for (int i = 0; i<v.size(); i++) {
				MacroCharacter r = v.get(i);
				loopWriter.write(new Object[] {det, (i+1), r.flow, r.speed, r.density, r.travelTime, tag, dt});
			}
		});
		loopWriter.flush();
		loopWriter.beforeClose();
		try {
			loopWriter.getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void clearSecStat() {
		sectionStatMap.forEach((k,v) -> v.clear());
		laneSecMap.forEach((k,v) -> v.clear());
	}

	public void clearLinkStat() {
		linkStatMap.forEach((k,v) -> v.clear());
	}

	public HashMap<String, List<MacroCharacter>> exportStat() {
		HashMap<String, List<MacroCharacter>> statMap = new HashMap<>();
		linkStatMap.forEach((k,v) -> statMap.put("link"+k.getId(),v));
		sectionStatMap.forEach((k,v) -> statMap.put(k,v));
		laneSecMap.forEach((k,v) -> statMap.put("lane"+k.getLane().getId(),v));
		return statMap;
	}

	@Override
	public int addLaneConnector(long id, long up, long dn, int successiveFlag, List<GeoPoint> polyline) {
		int ans = super.addLaneConnector(id, up, dn, successiveFlag,polyline);
		MLPLane upLane = (MLPLane) findLane(up);
		MLPLane dnLane = (MLPLane) findLane(dn);
//		try {
//			if(upLane.getSegment().isEndSeg() && (upLane.getSegment().getLink().getDnNode().getType()&Constants.NODE_TYPE_INTERSECTION) !=0)
//				createConnector(id,polyline,up,dn);
//		}
//		catch (java.lang.NullPointerException e) {
//			System.out.println(e.getStackTrace());
//		}
		MLPConnector mlpConn = (MLPConnector) createConnector(id,polyline,upLane,dnLane);
		upLane.dnStrmConns.add(mlpConn);
		dnLane.upStrmConns.add(mlpConn);
		if(upLane.getSegment().isEndSeg() && (upLane.getSegment().getLink().getDnNode().getType()&Constants.NODE_TYPE_INTERSECTION) !=0) {
			MLPNode theNode = (MLPNode) upLane.getLink().getDnNode();
			mlpConn.setNode(theNode);
			theNode.addLC(mlpConn);
		}
		if (successiveFlag == Constants.SUCCESSIVE_LANE) {
			upLane.successiveDnLanes.add(dnLane);
			dnLane.successiveUpLanes.add(upLane);
		}
		return ans;
	}

	@Override
	public Connector createConnector(long id, long upLaneId, long dnLaneId, List<GeoPoint> shapePoints) {
		updateRelation(upLaneId,dnLaneId);
		MLPLane upLane = (MLPLane) findLane(upLaneId);
		MLPLane dnLane = (MLPLane) findLane(dnLaneId);
		MLPConnector mlpConn = new MLPConnector(id,shapePoints,upLane,dnLane);
		upLane.dnStrmConns.add(mlpConn);
		dnLane.upStrmConns.add(mlpConn);
		if(upLane.getSegment().isEndSeg() && (upLane.getSegment().getLink().getDnNode().getType()&Constants.NODE_TYPE_INTERSECTION) !=0) {
			MLPNode theNode = (MLPNode) upLane.getLink().getDnNode();
			mlpConn.setNode(theNode);
			theNode.addLC(mlpConn);
		}
		this.connectors.add(mlpConn);
		return mlpConn;
	}

	public Connector createConnector(long id, List<GeoPoint> shapePoints, MLPLane upLane, MLPLane dnLane) {
		MLPConnector mlpConn = new MLPConnector(id, shapePoints, upLane, dnLane);
		this.connectors.add(mlpConn);
		return mlpConn;
	}

	public List<Double> rawSectionDataFilter(String detName, double fTime, double tTime, int dataType) {
		List<Double> ans = new ArrayList<>();
		sensors.stream()
				.filter(sensor -> sensor.getName().equals(detName))
				.forEach(sensor -> ans.addAll(((MLPLoop)sensor).getPeriod(fTime,tTime,dataType)));
		return ans;
	}

	public void eliminateLCDeadLock(){
		List<Integer> dealedVehID = new ArrayList<>();
		for (MLPVehicle veh: veh_list) {
			if (!dealedVehID.contains(veh.getId())){
				dealedVehID.add(veh.getId());
				MLPVehicle blocking = veh.getLCBlockingVeh();
				if (blocking==null)
					continue;
				MLPVehicle bblocking = blocking.getLCBlockingVeh();
				if (bblocking!=null && bblocking.equals(veh)) {
					if (blocking.getLength()==veh.getLength()){
						//switch lane
						veh.switchLane(blocking);
						int buffer = ((MLPParameter)getSimParameter()).getLCBuff();
						blocking.buffer = buffer;
						veh.buffer = buffer;
					}
					else {
						//todo: two vehicles with different length should be careful about front & back gap.
						blocking.setNextLink(null);
						veh.setNextLink(null);
					}
					dealedVehID.add(blocking.getId());
				}
			}
		}
	}

}
