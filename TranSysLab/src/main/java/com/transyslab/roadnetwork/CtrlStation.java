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


package com.transyslab.roadnetwork;


import java.util.*;

/**
 * //-------------------------------------------------------------------- //
 * CLASS NAME: RN_CtrlStation -- a surveillance station consists of // one or
 * more signals. Ctrlerllace station is stored in a sorted // list in each link
 * according to their getDistance from the end of the // link. The sorting is in
 * descending order (Upstream = // LongerDistance = Top) //------
 *
 */
public class CtrlStation {
	protected static float maxVisibility_;
	protected int type_; // signal type
	protected Segment segment_; // pointer to segment
	protected float distance_; // getDistance from the link end
	protected float visibility_; // length of detection zone
	protected float position_; // position in % from segment end
	protected Vector<Signal> signals_; // array of pointers to signals
	protected String objInfo;
	protected int id;

	public CtrlStation() {

	}
	public int getId(){
		return this.id;
	}
	public String getObjInfo(){
		return this.objInfo;
	}
	/*
		 * public int type() { return (CTRL_SIGNAL_TYPE & type); } public int
		 * isLinkWide() { return (CTRL_LINKWIDE & type); }
		 *
		 * public RN_Segment segment() { return segment; } public RN_Link
		 * getLink(){ return segment.getLink(); }
		 *
		 * public int nLanes(){ return segment.nLanes(); }
		 *
		 * // Returns pointer to the signal in ith lane. It may be NULL if //
		 * there is no signal in the ith lane.
		 *
		 * public RN_Signal signal(int i){ //Î´´¦Àí if (isLinkWide()>0) return
		 * signals.get(0); else return signals.get(i); }
		 *
		 * // Connect ith point to the signal
		 *
		 * public void signal(int i, RN_Signal s){ if (isLinkWide()>0) i = 0;
		 * signals.add(i,s); }
		 *
		 * public float getDistance() { return getDistance; } public float
		 * visibility() { return visibility; } public void visibility(float d){
		 * visibility = d; if (visibility > maxVisibility_) { maxVisibility_ =
		 * visibility; } } public float position() { return position; }
		 *
		 * public int initWithoutInsert(int ty, float vis, int seg, float pos){
		 * switch (ty) { case CTRL_PS: case CTRL_TS: case CTRL_VSLS: case
		 * CTRL_VMS: { type = (ty | CTRL_LINKWIDE); break; } default: { type =
		 * ty; break; } }
		 *
		 * if (!(segment = theNetwork.findSegment(seg))) { // cerr <<
		 * "Error:: Unknown segment <" << seg << ">. "; return -1; }
		 *
		 * vis *= theBaseParameter.lengthFactor(); vis *=
		 * theBaseParameter.visibilityScaler(); visibility(vis);
		 *
		 * position = (float) (1.0 - pos); // position in % from segment end
		 *
		 * getDistance = segment.getDistance() + position *
		 * segment.getLength();
		 *
		 * if (isLinkWide()>0) { // signals.reserve(1); signals.add(0,null); }
		 * else { int n = segment.nLanes(); // signals.reserve(n); while (n >
		 * 0) { n --; signals.add(n,null); } }
		 *
		 * return 0; }
		 */
	/*
	 * public int init(int ty, float vis, int seg, float pos){ // This function
	 * is called by RN_Parser to set information of a // ctrleillance station
	 * from network database. It returns zero if no // error, -1 if fatal error
	 * or a positive number if warning error. /* if (ToolKit::debug()) { cout <<
	 * indent << "<" << ty << endc << vis << endc << seg << endc << pos << ">"
	 * << endl; }
	 *
	 * int err = initWithoutInsert(ty, vis, seg, pos); if (err < 0) return err;
	 *
	 * addIntoNetwork();
	 *
	 * return err; }
	 */
	/*
	 * public void addIntoNetwork(){ /*static int serial_no = 0;
	 * getLink().getCtrlStationList().add(this);
	 * theNetwork.lastCtrlStation(this); code_ = ++serial_no; }
	 */
/*
	@Override
	public void print() {

	}

	@Override
	public int cmp(CodedObject other) {
		CtrlStation ctrl = (CtrlStation) other;
		if (getDistance < ctrl.getDistance)
			return 1;
		else if (getDistance > ctrl.getDistance)
			return -1;
		else
			return 0;
	}
	@Override
	public int cmp(int c) {
		return this.cmp(c);
	}*/
}
