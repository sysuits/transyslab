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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Segment;

public class MLPLane extends Lane implements Comparator<MLPLane>{
	private double capacity_;
	private double releaseTime_;
	private int lnPosNum_;
	protected LinkedList<MLPVehicle> vehsOnLn;
//	private MLPVehicle head_;
//	private MLPVehicle tail_;
//	private double emitTime_;
	//private double capacity = 0.5;
//	private MLPLane upConectLane_;
//	private MLPLane dnConectLane_;
//	public int lateralCutInAllowed; // 十位数字0(1)表示(不)允许左侧车道并线；个位数字0(1)表示(不)允许右侧车道并线
//	public boolean LfCutinAllowed;	//left cut in allowed = true=允许左侧车道换道到此车道，
//	public boolean RtCutinAllowed;	//left cut in allowed = true=允许左侧车道换道到此车道，
	public boolean enterAllowed;	//true=允许（后方）车道车辆驶入;false=不允许车道车辆驶入(等于道路封闭)
	public MLPLane connectedDnLane;
	public MLPLane connectedUpLane;
//	public int di;//弃用
	protected List<MLPLane> successiveDnLanes;
	protected List<MLPLane> successiveUpLanes;
	protected List<MLPConnector> upStrmConns;
	protected List<MLPConnector> dnStrmConns;
	
	public MLPLane(){
		lnPosNum_ = 0;
		vehsOnLn = new LinkedList<MLPVehicle>();
//		lateralCutInAllowed = 0;
//		LfCutinAllowed = true;
//		RtCutinAllowed = true;
		enterAllowed = true;
		successiveDnLanes = new ArrayList<>();
		successiveUpLanes = new ArrayList<>();
		upStrmConns = new ArrayList<>();
		dnStrmConns = new ArrayList<>();
	}

	@Override
	public void init(int id, int r, int index, double beginx, double beginy, double endx, double endy, Segment seg) {
		super.init(id, r, index, beginx, beginy, endx, endy, seg);
		capacity_ = ((MLPParameter) getNetwork().getSimParameter()).capacity;
	}

	public boolean checkPass() {
		/*(segment.getLink().getDnNode().getType() & Constants.NODE_TYPE_DES) == 0 ||*/
		return releaseTime_ <= getNetwork().getSimClock().getCurrentTime();
	}
	
	public void resetReleaseTime() {
		releaseTime_ = getNetwork().getSimClock().getCurrentTime();
		scheduleNextEmitTime();
	}
	
	public void scheduleNextEmitTime() {
		if (capacity_ > 1.E-6) {
			releaseTime_ += 1.0 / capacity_;
		}
		else {
			releaseTime_ = Constants.DBL_INF;
		}
	}
	
	public void setCapacity(double val) {
		capacity_ = val;
	}
	
	public void calLnPos() {
		//todo: to delete duplicated filed lnPosNum
		lnPosNum_ = orderNum;
	}
	
	public int getLnPosNum(){
		return lnPosNum_;
	}
	
	@Override
	public MLPSegment getSegment(){
		return (MLPSegment) segment;
	}
	
	public boolean checkVolum(MLPVehicle mlpv) {
		MLPVehicle tail_ = getTail();
		if (tail_ != null &&
			getLength() - tail_.getDistance() <
			(mlpv.getLength() + ((MLPParameter) getNetwork().getSimParameter()).minGap(mlpv.getCurrentSpeed()))) {
			return false;
		}
		else
			return true;
	}
	
	public boolean checkVolum(double vehLen, double vehSpeed) {
		MLPVehicle tail_ = getTail();
		if (tail_ != null &&
			getLength() - tail_.getDistance() - tail_.getLength()<
			(vehLen +  ((MLPParameter) getNetwork().getSimParameter()).minGap(vehSpeed))) {
			return false;
		}
		else
			return true;
	}

	public double spareDis(double vehLen, double vehSpeed) {
		MLPVehicle tail = getTail();
		double minus = tail==null ? 0.0 : tail.getDistance() + tail.getLength();
		return getLength() - minus - vehLen - ((MLPParameter) getNetwork().getSimParameter()).minGap(vehSpeed);
	}
	
	//appendVeh(); removeVeh(); insertVeh(); check list:
	//1. 相关lane.vehsOnLn的挂载更新
	//2. 相关车辆的跟车关系的更新
	//3. Network.veh_list & veh_pool recycle的挂载更新
	//4. 转移车辆(processing veh)的lane, seg, link的挂载关系更新	
	//推荐流程
	//S1 Network.veh_list.add();
	//S2 lane.vehsOnLn.add() & remove();
	//S3 processingVeh.updateLeadNTrail() 
	//     -> if lead or trail not null, lead.updateLeadNTrail() & trail.updateLeadNTrail()
	//S4 veh_pool.recycle() 
	//     -> if no need, processingVeh.lane/seg/link setting
	public void appendVeh(MLPVehicle mlpveh) {
		//处理相关lane的vehsOnLn & network.veh_list generate
		//network.veh_list has been taken care before called
		vehsOnLn.offer(mlpveh);
		//处理相关veh的lead_&trail_
		mlpveh.updateLeadNTrail();
		if (mlpveh.leading != null)
			mlpveh.leading.updateLeadNTrail();
		if (mlpveh.trailing != null)
			mlpveh.trailing.updateLeadNTrail();
		//processing veh的lane, seg, link的注册 也需要在调用本函数之前完成
	}
	
	public void removeVeh(MLPVehicle mlpveh, boolean recycleNeeded){
		//处理相关lane的vehsOnLn
		vehsOnLn.remove(mlpveh);
		//处理与此veh相关veh的lead_&trail_
		if (mlpveh.leading != null)
			mlpveh.leading.updateLeadNTrail();
		if (mlpveh.trailing != null)
			mlpveh.trailing.updateLeadNTrail();
		if (recycleNeeded)
			//处理network.veh_list recycle; 
			((MLPNetwork) getNetwork()).recycleVeh(mlpveh);
		else {
			//若不需要回收，则完成processing Veh的跟车更新及挂载注册
			mlpveh.leading = (MLPVehicle) null;
			mlpveh.trailing = (MLPVehicle) null;
			mlpveh.lane = null;
			mlpveh.segment = null;
			mlpveh.link = null;
		}
	}
	
	public void insertVeh(MLPVehicle mlpveh) {
		//找到插入节点并在vehsOnLn上插入
		if (vehsOnLn.isEmpty()) {
			vehsOnLn.offer(mlpveh);
		}
		else {
			int p = 0;
			while (p<vehsOnLn.size() && vehsOnLn.get(p).getDistance() < mlpveh.getDistance()) {
				p += 1;
			}
			vehsOnLn.add(p, mlpveh);
		}
		//processingVeh.lane/seg/link setting
		mlpveh.lane = this;
		mlpveh.segment = (MLPSegment) segment;
		mlpveh.link = (MLPLink) segment.getLink();
		//updateLeadNTrail()
		mlpveh.updateLeadNTrail();
		if (mlpveh.leading != null)
			mlpveh.leading.updateLeadNTrail();
		if (mlpveh.trailing != null)
			mlpveh.trailing.updateLeadNTrail();
	}
	
	public void insertVeh(MLPVehicle mlpveh, int p) {
		//在vehsOnLn上插入
		vehsOnLn.add(p, mlpveh);
		//updateLeadNTrail()
		mlpveh.updateLeadNTrail();
		if (mlpveh.leading != null)
			mlpveh.leading.updateLeadNTrail();
		if (mlpveh.trailing != null)
			mlpveh.trailing.updateLeadNTrail();
		//processingVeh.lane/seg/link setting
		mlpveh.lane = this;
		mlpveh.segment = (MLPSegment) segment;
		mlpveh.link = (MLPLink) segment.getLink();
	}

	public void substitudeVeh(MLPVehicle rmVeh, MLPVehicle newVeh){
		/*if (!vehsOnLn.contains(rmVeh)) {
			System.err.println("err: rmVeh is not on this lane");
			return;
		}*/
		int p_ = vehsOnLn.indexOf(rmVeh);
		removeVeh(rmVeh, false);
		insertVeh(newVeh, p_);
	}
	
	public void passVeh2ConnDnLn(MLPVehicle theVeh) {
		if (connectedDnLane == null) {
			System.err.println("no connected downstream lane");
		}
		//S2
		vehsOnLn.remove(theVeh);
		connectedDnLane.vehsOnLn.offer(theVeh);
		//S3 NONEED
		//S4 
		theVeh.lane = connectedDnLane;
		theVeh.segment = (MLPSegment) segment.getDnSegment();
	}
	
	public MLPVehicle getHead() {
		if (vehsOnLn.isEmpty()) 
			return (MLPVehicle) null;
		else 
			return vehsOnLn.getFirst();
	}
	
	public MLPVehicle getTail(){
		if (vehsOnLn.isEmpty()) 
			return (MLPVehicle) null;
		else
			return vehsOnLn.getLast();
	}
	
	public MLPLane getAdjacent(int dir){
		if (dir == 0){
			return (MLPLane) getRightLane();
		}
		else {
			if (dir == 1) {
				return (MLPLane) getLeftLane();
			}
			else {
				return (MLPLane) null;
			}
		}
	}
	
	public boolean checkLCAllowen(int turning){
		double a = turning%(Math.pow(10, turning+1));
		a = Math.floor(a / Math.pow(10, turning));
		if (a==0) {
			return true;
		}
		else {
			return false;
		}
		
	}

	//新逻辑 connectedUplane, connectedDnlane新赋值逻辑如successiveUplanes, successiveDnlanes.
	public void checkConectedLane() {
//		connectedUpLane = getSamePosLane(segment.getUpSegment());
//		connectedDnLane = getSamePosLane(segment.getDnSegment());
		if (successiveUpLanes.size()==1)
			connectedUpLane = successiveUpLanes.get(0);
		if (successiveDnLanes.size()==1)
			connectedDnLane = successiveDnLanes.get(0);
	}
	public MLPLane getSamePosLane(Segment seg) {
		if (seg != null && seg.nLanes()>=lnPosNum_) {
			return (MLPLane) seg.getLane(lnPosNum_ - 1);
		}
		else{
			return null;
		}
	}
	
	public int countVehWhere(double fdsp, double tdsp){
		if (vehsOnLn.isEmpty()) {
			return 0;
		}
		else {
			int c = 0;
			for (MLPVehicle veh : vehsOnLn) {
				if (veh.Displacement()>fdsp && veh.Displacement()<=tdsp)
					c += 1;
			}
			return c;
		}
	}

	public double[] expandBound(double fdsp, double tdsp){
		double fBound = this.getSegment().startDSP;
		double tBound = this.getSegment().endDSP;
		if (!vehsOnLn.isEmpty()) {
			for(MLPVehicle veh : vehsOnLn) {
				if (veh.Displacement() <= fdsp){
					fBound = veh.Displacement();
					break;
				}
			}
			for(MLPVehicle veh : vehsOnLn) {
				if (veh.Displacement()-veh.getLength() >= tdsp)
					tBound = veh.Displacement()-veh.getLength();
			}
		}
		return new double[]{fBound, tBound};
	}

	public boolean connect2DnLanes(List<Lane> DnLanes) {
		for (Lane tmpLN: DnLanes){
			if (((MLPLane) tmpLN).upLanes.contains(this))
				return true;
		}
		return false;
	}

	public boolean successivelyConnect2DnLanes(List<Lane> DnLanes) {
		for (Lane tmpLN: DnLanes){
			if (((MLPLane) tmpLN).successiveUpLanes.contains(this))
				return true;
		}
		return false;
	}

	public MLPLane successiveDnLaneInLink(MLPLink arg) {
		for (MLPLane ln : successiveDnLanes) {
			if (ln.getLink().getId() == arg.getId())
				return ln;
		}
		return null;
	}
	
	public int calDi(MLPVehicle theVeh) {
		//last seg of this link
		if (((MLPSegment) segment).isEndSeg()) {
			MLPLink nextLink = (MLPLink) theVeh.getNextLink();

			//on the last link
			if (nextLink == null){
				return 0;
			}

			//this lane connects with next link; find out if nextNode is an intersection

			//next node is an intersection
			if (getLink().getDnNode().type(Constants.NODE_TYPE_INTERSECTION)!=0) {
				List<Lane> nextValidLanes = ((MLPSegment) nextLink.getStartSegment()).getValidLanes(theVeh);
				if (connect2DnLanes(nextValidLanes)) {
					return 0;
				}
				//check neighbor lane
				MLPLane tmpLN = (MLPLane) getLeftLane();
				int count1 = 1;
				while(tmpLN != null && !tmpLN.connect2DnLanes(nextValidLanes)) {
					tmpLN = (MLPLane) tmpLN.getLeftLane();
					count1 += 1;
				}
				if (index - count1 < segment.getLeftLane().getIndex())
					count1 = Integer.MAX_VALUE;
				tmpLN = (MLPLane) getRightLane();
				int count2 = 1;
				while(tmpLN != null && !tmpLN.connect2DnLanes(nextValidLanes)) {
					tmpLN = (MLPLane) tmpLN.getRightLane();
					count2 += 1;
				}
				if (index + count2 > segment.getLeftLane().getIndex() + segment.nLanes() - 1)
					count2 = Integer.MAX_VALUE;
				return Math.min(count1, count2);
			}

			//next node is NOT an intersection
			List<Lane> nextValidLanes = ((MLPSegment) nextLink.getStartSegment()).getValidLanes(theVeh);
			MLPLane theSuDnLane = successiveDnLaneInLink(nextLink);
			if (nextValidLanes.contains(theSuDnLane))
				return 0;
			int count = Integer.MAX_VALUE;
			for (int i = 0; i < segment.nLanes(); i++) {
				MLPLane tmpLN = (MLPLane) segment.getLane(i);
				if ( tmpLN != this && nextValidLanes.contains(tmpLN.successiveDnLaneInLink(nextLink)) ) {
					int tmp = Math.abs(tmpLN.getLnPosNum() - lnPosNum_);
					count = tmp<=count ? tmp : count;
				}
			}
			return count;
		}

		//break point between Segments: (within the link)
		if (connectedDnLane!=null && connectedDnLane.enterAllowed) {
			return 0;
		}
		MLPLane tmp = (MLPLane) getLeftLane();
		int count1 = 1;
		while(tmp != null){
			if (tmp.connectedDnLane!=null && tmp.connectedDnLane.enterAllowed) {
				break;
			}
			count1 += 1;
			tmp = (MLPLane) tmp.getLeftLane();
		}
		if (index - count1 < segment.getLeftLane().getIndex())
			count1 = Integer.MAX_VALUE;
		tmp = (MLPLane) getRightLane();
		int count2 = 1;
		while (tmp != null) {
			if (tmp.connectedDnLane!=null && tmp.connectedDnLane.enterAllowed) {
				break;
			}
			count2 += 1;
			tmp = (MLPLane) tmp.getRightLane();
		}
		if (index + count2 > segment.getLeftLane().getIndex() + segment.nLanes() - 1)
			count2 = Integer.MAX_VALUE;
		return Math.min(count1, count2);
	}

	public boolean diEqualsZero(MLPVehicle theVeh){
		if (((MLPSegment) segment).isEndSeg()) {//last seg of this link
			MLPLink nextLink = (MLPLink) theVeh.getNextLink();
			return ( nextLink == null ||
					 connect2DnLanes( ( (MLPSegment) nextLink.getStartSegment() ).getValidLanes(theVeh) ) );
		}
		return (connectedDnLane!=null && connectedDnLane.enterAllowed);
	}

	public List<MLPLane> selectDnLane(Segment nextSeg) {
		List<MLPLane> tmp = new ArrayList<>();
		for (int i = 0; i < nextSeg.nLanes(); i++) {
			MLPLane theLane = (MLPLane) nextSeg.getLane(i);
			if (dnLanes.contains(theLane)) {
				tmp.add(theLane);
			}
		}
		tmp.sort(this);
		return tmp;
	}

	@Override
	public int compare(MLPLane o1, MLPLane o2) {
		MLPLane tmp = null;
		for (int i = 0; i < successiveDnLanes.size(); i++) {
			tmp = successiveDnLanes.get(i);
			if (tmp.segment.getId() == o1.segment.getId()) {
				break;
			}
		}
		if (tmp == null) 
			return 0;
		int d1 = Math.abs(tmp.lnPosNum_ - lnPosNum_);
		int d2 = Math.abs(tmp.lnPosNum_ - lnPosNum_);
		return ( d1 - d2 );
	}

	public String getSDnLnInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("SuccessiveDnLanes:\r\n");
		successiveDnLanes.stream().forEach(e -> sb.append(e.getId() + ", "));
		return sb.toString();
	}

	public double getCapacity() {
		return capacity_;
	}

	protected MLPConnector pickDnConn(long dnLinkID){
		List<MLPConnector> candidates = dnStrmConns.stream().filter(c -> c.dnLinkID()==dnLinkID).collect(Collectors.toList());
		if (candidates.size()<=0)
			return null;
		else if (candidates.size()==1)
			return candidates.get(0);
		else {
			candidates.sort((o1, o2) -> {
				double q1 = o1.queueNum();
				double q2 = o2.queueNum();
				return Double.compare(q1, q2);
			});
			return candidates.get(0);
		}

	}

	public List<MLPConnector> connsToDnLink(Long linkId){
		return dnStrmConns.stream().filter(c->c.dnLinkID()==linkId).collect(Collectors.toList());
	}
}
