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

public class Constants {
	public static final double DEFAULT_VEHICLE_WIDTH = 1.8; /* Camry's width */
	public static final double DEFAULT_VEHICLE_LENGTH = 4.0; /* meters */
	public static final int ATTR_FAMILIARITY = 0x00000001;
	public static final int ATTR_TS_COMPLY = 0x00000010;
	public static final int ATTR_PS_COMPLY = 0x00000020;
	public static final int ATTR_RAMPMETER_COMPLY = 0x00000040;
	public static final int ATTR_LUS_COMPLY = 0x00000300; // sum
	public static final int ATTR_LUS_COMPLY_RED = 0x00000100;
	public static final int ATTR_LUS_COMPLY_YELLOW = 0x00000200;

	public static final int ATTR_GLU_RULE_COMPLY = 0x00000400;
	public static final int ATTR_GLC_RULE_COMPLY = 0x00000800;

	public static final int ATTR_MSG_TYPE_LU_COMPLY = 0x00001000;
	public static final int ATTR_MSG_PATH_LU_COMPLY = 0x00002000;
	public static final int ATTR_MSG_GUIDE_COMPLY = 0x00004000;

	public static final int ATTR_ACCESSED_INFO = 0x00010000;

	public static final int NO_EXPLICIT_PATH = -1;
	public static final int NO_EXPLICIT_LANE = -1;

	public static final char OVER_HEIGHT_TRUE = 1;
	public static final char OVER_HEIGHT_FALSE = 0;

	public final static int NUM_RESOLUTIONS = 4;
	public final static float DEFAULT_VISIBILITY = 80; // meters
	public final static float DEFAULT_BUSLC_VISIBILITY = 300; // meters
	public final static float DEFAULT_SQUEEZE_FACTOR = 1.0f;
	public static final double POINT_EPSILON = 0.1;
	public static final double BULGE_EPSILON = 1.0e-5;
	public final static int LANE_CHANGE_RIGHT = 0x00000001;//说明：用于rules
	public final static int LANE_CHANGE_LEFT = 0x00000002;//说明：用于rules
	public final static int LANE_CHANGE = 0x00000003;//说明：用于rules
	public final static int LANE_RIGHTTURN_FREE = 0x00000010;//说明：用于rules，右转不受灯控

	public final static int LANE_CHANGE_RIGHT_REQUIRED = 0x00000004;//0100
	public final static int LANE_CHANGE_LEFT_REQUIRED = 0x00000008;//1000
	public final static int LANE_CHANGE_REQUIRED = 0x0000000C;//1100

	public final static int LANE_TYPE_SHOULDER = 0x00000003;//0011
	public final static int LANE_TYPE_RIGHT_MOST = 0x00000001;//0001
	public final static int LANE_TYPE_LEFT_MOST = 0x00000002;//0010

	public final static int LANE_TYPE_RAMP = 0x000000F0;//1111,0000
	public final static int LANE_TYPE_UP_RAMP = 0x00000030;//0011,0000
	public final static int LANE_TYPE_UP_ONRAMP = 0x00000010;//0001,0000
	public final static int LANE_TYPE_UP_OFFRAMP = 0x00000020;//0010,0000

	public final static int LANE_TYPE_DN_RAMP = 0x000000C0;//1100,0000
	public final static int LANE_TYPE_DN_ONRAMP = 0x00000040;//0100,0000
	public final static int LANE_TYPE_DN_OFFRAMP = 0x00000080;//1000,0000

	public final static int LANE_TYPE_BOUNDARY = 0x00000200;
	public final static int LANE_TYPE_DROPPED = 0x00000100;

	public final static int LANE_TYPE_SIGNAL_ARROW = 0x00001000;
	// 车道宽度
	public static float LANE_WIDTH = 3.75f;
	public static int LINK_TYPE_FREEWAY = 1;
	public static int LINK_TYPE_RAMP = 2;
	public static int LINK_TYPE_URBAN = 3;
	// When an angle between two link is less that 1/128 PI, we do not
	// care their intersection anymore because they are almost in parallel
	public final static double U_ANGLE = Math.PI / 4096.0;
	public final static double V_ANGLE = 2 * Math.PI - U_ANGLE;
	// The angle a link enters and leaves an intersection is defined by
	// the angle of the line connecting end point and a point with a given
	// getDistance (ANGLE_DISTANCE) along the arc. However, this point
	// should also be wiithin a given relative getDistance (ANGLE_POSITION)
	// of the length of the last/first segment.
	public static final double ANGLE_DISTANCE = 10.0;
	public static final double ANGLE_POSITION = 0.25;
	public static final int NODE_TYPE_GEOMETRY = 15;
	public static final int NODE_TYPE_CENTRIOD = 0;
	public static final int NODE_TYPE_EXTERNAL = 1;
	//wym
	public static final int NODE_TYPE_INTERSECTION = 6; // changed from 2
	public static final int NODE_TYPE_NONSIGNALIZED_INTERSECTION = 2;
	public static final int NODE_TYPE_SIGNALIZED_INTERSECTION = 4;

	public static final int NODE_TYPE_OD = 48;
	public static final int NODE_TYPE_ORI = 16;
	public static final int NODE_TYPE_DES = 32;

	public static final int NUM_LABELS = 20;
	public static final int NUM_NODES = 100;
	public static final int NUM_LINKS = 200;
	public static final int NUM_SEGMENTS = 400;
	public static final int NUM_LANES = 1600;
	public static final int NUM_SENSORS = 500;
	public static final int NUM_SIGNALS = 500;
	public static final int NUM_SDFUNCTIONS = 5;

	// tomer

	public static final int NUM_IS = 200;
	public static final int STATE_OK = 0;// a step is done (clock advanced)
	public static final int STATE_DONE = -1;// simulation is done
	public static final int STATE_WAITING = 1;// waiting for other process
	public static final int STATE_ERROR_QUIT = -3;// end because of error
	public static final int STATE_QUIT_CALLED = -8; // already quit
	public static final int STATE_QUIT = -2;
	// SimulationEngine
	public static final int OUTPUT_VEHICLE_LOG = 0x00000001;

	// const unsigned int OUTPUT_SENSOR = 0x0000000E; // sum (removed by Angus)
	public static final int OUTPUT_SENSOR_READINGS = 0x00000002;
	public static final int OUTPUT_VRC_READINGS = 0x00000004;
	public static final int OUTPUT_ASSIGNMENT_MATRIX = 0x00000008; // (Angus)

	public static final int OUTPUT_LINK_TRAVEL_TIMES = 0x00000010; // 16
	public static final int OUTPUT_SEGMENT_TRAVEL_TIMES = 0x00000020; // 32
	public static final int OUTPUT_SEGMENT_STATISTICS = 0x00000040; // 64
	public static final int OUTPUT_QUEUE_STATISTICS = 0x00000080; // 128
	public static final int OUTPUT_TRAVEL_TIMES_TABLE = 0x00000100; // 256
	public static final int OUTPUT_VEHICLE_PATH_RECORDS = 0x00000200; // 512
	public static final int OUTPUT_VEHICLE_DEP_RECORDS = 0x00000400; // 1024
	public static final int OUTPUT_VEHICLE_TRAJECTORY = 0x00000800; // 2048
	public static final int OUTPUT_RECT_TEXT = 0x00001000; // 4096
	public static final int OUTPUT_SKIP_COMMENT = 0x00002000; // 8192
	public static final int OUTPUT_TRANSIT_TRAJECTORY = 0x00004000; // 16384
	public static final int OUTPUT_STOP_ARRIVAL = 0x00008000; // 32768
	public static final int OUTPUT_STATE_3D = 0x00010000; //
	public static final int OUTPUT_SIGNAL_PRIORITY = 0x00020000; //
	// Tomer to have the lane changing locations
	public static final int OUTPUT_LANE_CHANGING = 0x00040000; //

	public static final int OUTPUT_SYSTEM_MOE = 0x10000000;

	public static final int STATE_COMMUNICATING = 2; // send or receiving data
	public static final int STATE_NOT_STARTED = 9; // initial value
	public static final int ONE_DAY = 86400;
	public static final float FLT_INF = (float) (3.40282e+038 / 3.0);
	public static final int INT_INF = 2147483647 / 3;
	public static final double DBL_INF = 1.79769e+308 / 3.0;

	/*--------------------------------------------------------------------
	 * These constants are used to mask the vehicle types in terms of
	 * acceleration/deceleration profile, lane use privilege, and
	 * information availability, etc.
	 */

	public static final int VEHICLE_CLASS = 0x0000000F; // 15 //wym 1111 in binary
	public static final int VEHICLE_GROUP = 0x0000FFF0;//  wym 65520 1111,1111,1111,0000 in binary

	public static final int VEHICLE_LANE_USE = 0x000007F0; // 2032 sum //wym 111,1111,0000 in binary

	public static final int VEHICLE_SMALL = 0x00000010; // 16
	public static final int VEHICLE_LOW = 0x00000020; // 32

	public static final int VEHICLE_ETC = 0x00000040; // 64
	public static final int VEHICLE_HOV = 0x00000080; // 128
	public static final int VEHICLE_COMMERCIAL = 0x00000100; // 256 // Dan -
																// eligible for
																// bus lane
	public static final int VEHICLE_BUS_RAPID = 0x00000200; // 512 // Dan - bus
															// rapid transit bus
	public static final int VEHICLE_EMERGENCY = 0x00000400; // 1024 // Dan - not
															// used (4/9/02)
	public static final int VEHICLE_GUIDED = 0x00000800; // 2048
	public static final int VEHICLE_FIXEDPATH = 0x00001000; // 4096
	public static final int VEHICLE_PROBE = 0x00002000; // 8192
	public static final int VEHICLE_CELLULAR = 0x00004000; // 16384
	public static final int MAX_NUM_OF_IN_LINKS = 6; // it could be upto 8
	public static final int MAX_NUM_OF_OUT_LINKS = 6; // it could be upto 8
	public static final double SPEED_EPSILON = 1.0E-3; /* meter/sec */
	public static final double ACC_EPSILON = 1.0E-3; /* meter/sec2 */
	public static final double DIS_EPSILON = 1.0E-0; /* meter */
	public static final double RATE_EPSILON = 1.0E-4;
	public static final int STATE_MARKED = 0x00008000;
	public static final int LINK_TYPE_MASK = 7;
	public static final int IN_TUNNEL_MASK = 8;
	public static final double DBL_EPSILON = 1.0 / DBL_INF;
	public static final int INFO_FLAG_DYNAMIC = 0x0001; // time variant
	public static final int INFO_FLAG_UPDATE = 0x0006; // sum
	public static final int INFO_FLAG_UPDATE_TREES = 0x0002; // calculate
																// shortest getPath
																// trees
	public static final int INFO_FLAG_UPDATE_PATHS = 0x0004; // prespecified
																// getPath only
	public static final int INFO_FLAG_USE_EXISTING_TABLES = 0x0008;// do not
																	// update
																	// initial
																	// SP
	public static final int INFO_FLAG_AVAILABILITY = 0x0100;
	public static final int THREAD_NUM = 1;
	public static final double APROXEPSILON = 1.0E-5;
	public static final float[] COLOR_RED = {1.0f, 0.0f, 0.0f};
	public static final float[] COLOR_BLUE = {0.0f, 0.0f, 1.0f};
	public static final float[] COLOR_GREEN = {0.0f, 1.0f, 0.0f};
	public static final float[] COLOR_WHITE = {1.0f, 1.0f, 1.0f};
	public static final float[] COLOR_GREY_WHITE = {0.64f, 0.64f, 0.64f};
	public static final float[] COLOR_GREY = {0.21f, 0.21f, 0.21f};
	public static final float[] COLOR_LITEBLUE = new float[]{0.0f,0.75f,1.0f};
	public static final float[] COLOR_AMBER = {0.9f, 0.7f, 0.09f};
	public static final float[] COLOR_GOLDEN = {0.98f, 0.72f, 0.35f};
	// =0:非snapshot启动，按OD流量随机发车；
    // =1:非snapshot启动，按过车记录定时发车;
	// =2:snapshot启动，按OD流量随机发车；
    // =3:snapshot启动，按过车记录定时发车；
	public static final int SIM_MODE = 1;

	//wym
	public static final int ARITHMETIC_MEAN = 0;
	public static final int HARMONIC_MEAN = 1;
	public static final int VEHICLE_RECYCLE = 1;
	public static final int VEHICLE_NOT_RECYCLE = 0;
	//车辆着色
	public static final int VIRTUAL_VEHICLE = 1;
	public static final int FOLLOWING = 2;
	//模型类别
	public static final int MODEL_TYPE_MESO = 1;
	public static final int MODEL_TYPE_MLP = 2;
	public static final int MODEL_TYPE_RT = 3;
	//连接车道
	public static final	int SUCCESSIVE_LANE = 1;

	//参数优化最大检验次数
	public static final int REPEAT_TEST_TIMES = 5;

	//display
	public static final int FPS = 60;
	public static final int FRAME_QUEUE_BUFFER = 60;
}
