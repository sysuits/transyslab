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
 * 交叉口通行策略
 *
 */
public class CManuver {
	public long _id;

	private Vector _confs;
	private Vector _fifos;
	private float _critical_gap;
	private float _turn_speed;

	// java不存在无符号数据类型
	// 其中 long类型是64位，int是32位，都是带符号的
	// c++中的int是16位，long是32位
	// c++的unsigned long 转换为java的long
	public long getID() {
		return _id;
	}
	public void setCriticalGap(float cg) {
		_critical_gap = cg;
	}
	public void setTurnSpeed(float ts) {
		if (ts > 0) {
			// 转向速度参数
			_turn_speed = Parameter.speedFactor() * ts;
		}
		else {
			_turn_speed = (float) Constants.SPEED_EPSILON;
		}
	}
	public float getTurnSpeed() {
		return _turn_speed;
	}
	public float getCriticalGap() {
		return _critical_gap;
	}
	public long uplane() {
		// 二进制数右移16位
		return (_id >> 16);
	}
	public long dnlane() {
		return (_id & 0xFFFF);
	}
	public long uplane(long id) {
		return (id >> 16);
	}
	public long dnlane(long id) {
		return (id & 0xFFFF);
	}
	public int init(int uplane_, int dnlane_) {
		_id = (uplane_ << 16) | dnlane_;
		return 0;
	}
	public void addConflict(int uplane_, int dnlane_) {
		long c = (uplane_ << 16) | dnlane_;
		_confs.add(c);
	}
	public long getConfs(int n) {
		return (long) _confs.get(n);
	}
	public int NumOfConfs() {
		return _confs.size();
	}
	public void addFifo(int uplane_, int dnlane_) {
		long f = (uplane_ << 16) | dnlane_;
		_fifos.add(f);
	}
}
