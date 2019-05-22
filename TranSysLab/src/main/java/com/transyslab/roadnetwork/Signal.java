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


public class Signal {
	protected static int sorted_;
	protected int index_; // index in array
	protected Lane lane_; // pointer to lane
	protected CtrlStation station_; // station
	protected int state_; // current state

	public Signal() {
		lane_ = null;
		station_ = null;
		index_ = -1;
		state_ = 0;
	}/*
		 * public static int sorted() { return sorted; }
		 *
		 * public int clearState(int flag){ return (state &= ~flag); } public
		 * int setState(int flag){ return (state |= flag); } public int state()
		 * { return state; } public void state(int s) { state = s; }
		 *
		 * // These two functions apply to traffic signal only
		 *
		 * public int stateForExit(int i){ return ((state >> (i * SIGNAL_BITS))
		 * & SIGNAL_STATE); } public int stateForExit(int s, int i){ return ((s
		 * >> (i * SIGNAL_BITS)) & SIGNAL_STATE); }
		 *
		 * // Iterator on the global array
		 *
		 * public RN_Signal prev(){ if (index > 0) return
		 * theNetwork.signal(index - 1); else return null; } public RN_Signal
		 * next(){ if (index < (theNetwork.nSignals() - 1)) return
		 * theNetwork.signal(index + 1); else return null; }
		 *
		 * public int index() { return index; } public RN_CtrlStation station()
		 * { return station_;} public RN_Lane lane() { return lane; } public
		 * RN_Segment segment(){ // Segment that contains this signal if
		 * (station_!=null) return station_->segment(); else if (lane!=null)
		 * return lane.segment(); else return null; } public RN_Link link(){
		 * return segment().getLink(); } // Relative getDistance from the end of
		 * the segment public float position(){ return station_.position(); }
		 * public int stateIoToInternal(int s){ if (type() != CTRL_VMS) return
		 * sign; int s = SIGN_TYPE & sign; if (s == SIGN_LANE_USE_PATH || s ==
		 * SIGN_PATH) { // A getPath related message. Use link index instead of id
		 * s = SignSuffix(sign); RN_Link pl = theNetwork.findLink(s); if (pl) {
		 * sign = (SignPrefix(sign) | pl->index()); } else { // cerr <<
		 * "Error:: Unknown link ID <" << s << ">. "; sign = SIGN_ERROR; } }
		 * return sign; } public int stateInternalToIo(int s){ if (type() !=
		 * CTRL_VMS) return sign; int s = SIGN_TYPE & sign; if (s ==
		 * SIGN_LANE_USE_PATH || s == SIGN_PATH) { // A getPath related message.
		 * Convert id to link index s = SignSuffix(sign); RN_Link pl =
		 * theNetwork.link(s); sign = (SignPrefix(sign) | pl.get_code()); }
		 * return sign; } // Returns 1 if the signal is like-wide or 0 if it is
		 * lane specific public int isLinkWide(){ return station_.isLinkWide();
		 * } public int type(){ return station_.type(); } public float
		 * getDistance(){ // Distance from the end of the link return
		 * station_.getDistance(); } // Visibility getDistance public float
		 * visibility(){ return station_.visibility(); }
		 *
		 * public int init(int code, int s, int l){ int last = -1;
		 *
		 * /* if (ToolKit::debug()) { cout << indent << indent << "<" << c <<
		 * endc << hextag << hex << s << dec << endc << l << ">" << endl; }
		 *
		 * if (station_!= null) { // cerr << "Error:: Signal <" << c << "> " //
		 * << "cannot be initialized twice. "; return -1; } else { station_ =
		 * theNetwork.lastCtrlStation(); }
		 *
		 * if (sorted >0&& c <= last) sorted = 0; else last = c; code_ = c;
		 *
		 * state = stateIoToInternal(s); if (state == SIGN_ERROR) return -1;
		 * // error
		 *
		 * if (l < 0) {
		 *
		 * RN_Segment ps = segment();
		 *
		 * // central lane
		 *
		 * lane = theNetwork.lane(ps.leftLaneIndex() + ps->nLanes() / 2);
		 *
		 * } else { if (!(lane = theNetwork.findLane(l)) || (lane.getSegment()
		 * != segment())) { // cerr << "Error:: Unknown lane ID <" << l << ">. "
		 * ; return -1; } }
		 *
		 * addIntoNetwork();
		 *
		 * return 0; } public void addIntoNetwork(){ index =
		 * theNetwork.nSignals(); theNetwork.addSignal(this); if
		 * (isLinkWide()>0) station_.signal(0, this); else
		 * station_.signal(lane.localIndex(), this); } public void print(){
		 *
		 * }
		 *
		 * public float acceleration(TS_Vehicle *, float) { return FLT_INF; }
		 * public float squeeze(TS_Vehicle *, float) { return FLT_INF; }
		 *
		 * // IMPORTANT NOTE!! // ---------------- // These functions are
		 * nonsensical in the context of RN_Signal. // They are here to avoid
		 * casting to derived classes which is // not possible based on the
		 * multiple inheritance / virtual // inheritance we have used for
		 * signals. See Section 10.6c of // The Annotated C++ Reference Manual
		 * (pg 227) by Ellis and // Stroustrup for more info. //
		 * ----------------- // IMPORTANT NOTE!!
		 *
		 * // -- TollBooth Functions --
		 *
		 * public int isEtc() { return 0; } public int speed() { return 0; }
		 * public float delay(TS_Vehicle *) { return 0; }
		 *
		 * // -- BusStop Functions -- // Dan: This function is used for bus
		 * stops, to determine // whether the stop is in the leftmost or
		 * rightmost lane
		 *
		 * public int leftOrRight(){ if ((lane.get_code()) ==
		 * (segment().leftLane().get_code())) { return -1; } else if
		 * ((lane.get_code()) == (segment().rightLane().get_code())) { return
		 * 1; } else return 0; } public int stopLength() { return 0; }
		 *
		 * // -- Drawing Functions -- /* virtual void calcGeometricData() { } //
		 * Should return Pixel* but that requires X stuff virtual unsigned long
		 * *calcColor(unsigned int) { return NULL; } virtual int
		 * draw(DRN_DrawingArea *) { return 0; } virtual WcsPoint& center() {
		 * return theWcsDummyPoint; }
		 */
	// -- Incident Functions --
	/*
	 * public void check() { } public float speedLimit() { return 0; }
	 *
	 * // -- Communication functions -- /* public void send(IOService &) { }
	 * public void receive(IOService &) { }
	 */
}
