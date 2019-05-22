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

import com.transyslab.roadnetwork.Segment;


public class RTSegment extends Segment {
	private double startDSP;//�ڵ�ǰlink�е�������
	private double endDSP;//��ǰlink�е�segment�յ����
	public void setStartDSP(double startDSP){
		this.startDSP = startDSP;
	}
	public void setEndDSP(double endDSP){
		this.endDSP = endDSP;
	}
	public double getStartDSP(){
		return this.startDSP;
	}
	public double getEndDSP(){
		return this.endDSP;
	}
	@Override
	public RTSegment getUpSegment() {
		return (RTSegment) super.getUpSegment();
	}

	@Override
	public RTSegment getDnSegment(){
		return (RTSegment) super.getDnSegment();
	}
	@Override
	public RTLane getLane(int index) {
		return (RTLane) super.getLane(index);
	}
	public boolean isEndSeg() {
		return link.getEndSegment().equals(this);
	}

	public boolean isStartSeg() {
		return link.getStartSegment().equals(this);
	}



}
