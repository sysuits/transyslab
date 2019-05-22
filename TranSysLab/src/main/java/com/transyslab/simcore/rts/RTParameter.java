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

import com.transyslab.roadnetwork.Parameter;


public class RTParameter extends Parameter{
    private int normHeadway = 6; //��׼С�����ı��ͳ�ͷ�����Ϊ6m
    private int gapSpeed = 30; //�����Ŷ��볩�е��ٶ���ֵ��km/h
    private int dischargeSpeed = 20; //��ɢ���٣�km/h
    private int dischargeSecond = 1; //����ʱ�࣬sec
    private int arriveSecond = 1; //����ʱ�࣬sec
    private int saturateSecond = 2; //���͹�����ͷʱ�࣬sec
    private int expSpeed = 35; //���������ٶȣ�km/h
    private int maxSpeed = 60; //һ����ٶ����ޣ�km/h

}
