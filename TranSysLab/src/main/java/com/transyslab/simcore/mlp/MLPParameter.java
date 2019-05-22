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

import com.transyslab.commons.tools.NewtonFunction;
import com.transyslab.roadnetwork.Parameter;
import com.transyslab.simcore.mlp.Functions.FunsCombination1;
import com.transyslab.simcore.mlp.Functions.FunsCombination2;
import com.transyslab.simcore.mlp.Functions.TSFun;
import org.apache.commons.math3.special.Gamma;

public class MLPParameter extends Parameter {
	public static final double[] DEFAULT_PARAMETERS = new double[] {0.4633,21.7950,0.1765,33.333,32,0.42,7.92,5.25};
	private double SegLenBuff_;//ǿ�ƻ����߽磨segmentʵ�߳��ȣ�meter
	private double LCBuffTime_;//Lane ChangingӰ��ʱ�� / �������������ȴʱ��
	private int LCBuff_;//LCBuffTmieת��Ϊ֡��Frame or fin
	protected double updateStepSize_;//update�׶�ʱ��
	protected double LCDStepSize_;//Lane changing decision ��������ʱ����
	protected double capacity;// veh/s/lane
	protected float CELL_RSP_LOWER; // ��λ�ף�about 200 feet 30.48f
	protected float CELL_RSP_UPPER; // ��λ�ף�about 500 feet 91.44f
	protected float CF_FAR;
	protected float CF_NEAR;
	protected double PHYSICAL_SPD_LIM;
	private double [] SDPara_;//[0]VMax m/s; [1]VMin m/s; [2]KJam veh/m; [3]Alpha a.u.; [4]Beta a.u.;
	private double [] LCPara_;//[0]gamma1 a.u.; [1]gamma2 a.u.;	
	protected float[] limitingParam_; // [0] stopping gap (m); [1] moving time gap (t); [2] ?
//	protected float[] queueParam; // max speed for queue releasing
	final static float VEHICLE_LENGTH = 4.6f; // ��λ�ף� 20 feet //wym ��Ϊ4.6��
//	final static double CF_NEAR = 0.1;//meter
	final static double SEG_NEAR = 1.0;//meter
	final static double LC_Lambda1 = 18.4204;//����logitģ�ͳ�����
	final static double LC_Lambda2 = -9.2102;//����logitģ�ͳ�����
//	final static double PHYSICAL_SPD_LIM = 120/3.6; // meter/s
	private double simStepSize;
	private double lc_sensitivity;

	//���ʱ������
	protected double statWarmUp;
	protected double statStepSize;//stat(ͳ��)�׶�ʱ������λ����

	public MLPParameter() {
		SegLenBuff_ = 10.0;
		LCBuffTime_ = 2.0;
		updateStepSize_ = 10.0;
		LCDStepSize_ = 2.0;//wym Ϊ�˱�֤�����ٶȣ�Ĭ��Ϊ2.0s
		statStepSize = 300.0;//Ĭ��5���ӽ���ͳ�ƣ������ʼ��ʱ��ȡmaster�ļ��Ḳ�����ֵ��
		statWarmUp = 300.0;//Ĭ��5���ӽ���Ԥ�ȣ������ʼ��ʱ��ȡmaster�ļ��Ḳ�����ֵ��
		capacity = 0.5;//default 0.5
		CELL_RSP_LOWER = 30.87f;
		CELL_RSP_UPPER = 91.58f;
		CF_FAR = 91.58f;
		CF_NEAR = (float) (5.0 * lengthFactor);
		PHYSICAL_SPD_LIM = 120/3.6; // meter/s
		SDPara_ = new double [] {16.67, 0.0, 0.180, 1.8, 5.0};//ԭ{16.67, 0.0, 0.180, 5.0, 1.8}{19.76, 0.0, 0.15875, 2.04, 5.35}
		LCPara_ = new double [] {20.0, 20.0};
		limitingParam_ = new float[3];
//		queueParam = new float[3];
		//��mesolib�ļ������Ĭ�ϲ���ֵ
		limitingParam_[0] = (float) (5.0 * lengthFactor);// turn to meter
		limitingParam_[1] = 1.36f;
		limitingParam_[2] = (float) (5.0 * speedFactor);// turn to km/hour
//		SEG_NEAR = SDPara_[0]*SimulationClock.getInstance().getStepSize();
//		queueParam[0] = -0.001f;
//		queueParam[1] = (float) (25.0 * speedFactor);
//		queueParam[2] = 100.0f;// seconds
		simStepSize = 0.0;
		lc_sensitivity = 1.0;
	}
	public double getUpdateStepSize() {
		return updateStepSize_;
	}
	public void setUpdateStepSize(double uss) {
		updateStepSize_ = uss;
	}
	public double getLCDStepSize() {
		return LCDStepSize_;
	}
	public void setLCDStepSize(double lcd_ss){
		LCDStepSize_ = lcd_ss;
	}

	// This returns the minimum distance gap for a given speed
	public double minGap(double speed) {
		return minHeadwayGap() + headwaySpeedSlope() * speed;
	}

	public double minLCAcceptedGap(double speed, double rate) {
		return minHeadwayGap() + rate * headwaySpeedSlope() * speed;
	}
	
	//This returns a maximum speed for a give gap, in specific MLPlane, provide for MLP model
	public double maxSpeed(double gap) {
		double dt = simStepSize + headwaySpeedSlope();
		double dx = gap - minHeadwayGap();
		return Math.max(0.0, Math.min(PHYSICAL_SPD_LIM, dx/dt));
	}
	
	public float minHeadwayGap() {
		return limitingParam_[0];
	}
	public float headwaySpeedSlope() {
		return limitingParam_[1];
	}
	public float minSpeed() {
		return limitingParam_[2];
	}

/*	public float queueReleasingSpeed(float t, float v_f) {
		if (t > queueParam[2])
			return v_f;
		float r = 1.0f - (float) Math.exp(queueParam[0] * t * t);
		return queueParam[1] + (v_f - queueParam[1]) * r;
	}*/
	
	public void setSegLenBuff(double val){
		SegLenBuff_ = val;
	}
	public double getSegLenBuff() {
		return SegLenBuff_;
	}
	
	public void setLCBuffTime(double val){
		LCBuffTime_ = val;
		LCBuff_ = (int) Math.floor(LCBuffTime_ / simStepSize);
	}
	public double getLCBuffTime() {
		return LCBuffTime_;
	}
	public int getLCBuff() {
		if (LCBuff_ == 0) 
			LCBuff_ = (int) Math.floor(LCBuffTime_ / simStepSize);
		return LCBuff_;
	}
	
	public void setSDPara(double [] val){
		if (val.length != 5) {
			System.out.println("fail setting SDPara: Length error");
			return;
		}
		SDPara_ = val;
	}	
	public double [] getSDPara(){
		return (SDPara_);
	}
	
	public void setLCPara(double [] val){
		if (val.length != 2) {
			System.out.println("fail setting LCPara: Length error");
			return;
		}
		LCPara_ = val;
	}
	public double [] getLCPara(){
		return LCPara_;
	}
	public void setDUpper(float arg) {
		CELL_RSP_UPPER = arg;
	}
	public void setDLower(float arg) {
		CELL_RSP_LOWER = arg;
	}

	public void setSimStepSize(double arg) {
		simStepSize = arg;
	}

	public void setPhyLim(double arg) {
		PHYSICAL_SPD_LIM = arg;
	}

	//�����жϺ���
	public static boolean inPlatoon(double headway) {
		//calibrated paras
		double alpha = 3.7515;
		double lamda0 = 1.6480;
		double lamda1 = 0.5086;
		//beta distribution

		double gammaFunVal = Math.exp(Gamma.logGamma(alpha));
		double g0 = Math.pow(lamda0,alpha) / gammaFunVal * Math.pow(headway, alpha-1) * Math.exp(-lamda0*headway);
		double g1 = Math.pow(lamda1,alpha) / gammaFunVal * Math.pow(headway, alpha-1) * Math.exp(-lamda1*headway);
		return (g0 > g1);
	}

	public double getLCSensitivity() {
		return lc_sensitivity;
	}

	public void setLCSensitivity(double arg) {
		lc_sensitivity = arg;
	}
}
