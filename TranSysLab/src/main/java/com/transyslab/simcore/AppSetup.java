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

package com.transyslab.simcore;

import java.util.HashMap;
import java.util.Map;

public class AppSetup {
	
	public static Map<String, String> setupParameter = new HashMap<String, String>();
	public static int modelType;
	public static double startTime;
	public static double endTime;
	public static double timeStep;
	public static int simMode;
	public static boolean displayOn;
	public static String masterFileName;
}
