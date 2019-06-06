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


public class ExpSwitch {
	public final static boolean DOUBLE_LOOP = false;
	public final static boolean CAP_CTRL = false;
	public final static boolean MAX_ACC_CTRL = true;
	public final static boolean ACC_SMOOTH = false;//Ä¿Ç°ÓÐBUG
	public final static boolean APPROACH_CTRL = true;
	public final static boolean CF_CURVE = false;
	public final static boolean SPD_BUFFER = false;
	public final static boolean VIRTUAL_RELEASE = false;

	public final static double MAX_ACC = 4.0;
	public final static double MAX_DEC = -7.0;
	public final static double APPROACH_SPD = 60.0/3.6;
	public final static int SPD_BUFFER_VAL = 5;
	public final static double CF_VT_END = 200.0;
	public final static double CF_VT = 120.0/3.6;
}
