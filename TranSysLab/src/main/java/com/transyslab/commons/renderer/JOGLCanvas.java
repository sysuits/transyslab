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

package com.transyslab.commons.renderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.commons.tools.LinearAlgebra;
import com.transyslab.gui.MainWindow;
import com.transyslab.gui.PanelAction;
import com.transyslab.gui.SignalStagePanel;
import com.transyslab.roadnetwork.*;
import com.transyslab.simcore.mlp.MLPConnector;
import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.rts.RTEngine;
import com.transyslab.simcore.rts.RTNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

public class JOGLCanvas extends GLCanvas implements GLEventListener, KeyListener, MouseListener,
		MouseWheelListener, MouseMotionListener {
	//status
	public static final int ANIMATOR_STOP = 0;
	public static final int ANIMATOR_PLAYING = 1;
	public static final int ANIMATOR_PAUSE = 2;
	//mode
	public static final int ANIMATOR_FRAME_ADVANCE = 1;
	//图层
	public static final double LAYER_SURFACE = 0.0;
	public static final double LAYER_NODE = 0.5;
	public static final double LAYER_LINK = 0.5;
	public static final double LAYER_SEGMENT = 0.1;
	public static final double LAYER_LANE = 0.1;
	public static final double LAYER_BOUNDARY = 0.4;
	public static final double LAYER_CONNECTOR = 0.4;
	public static final double LAYER_VEHICLE = 0.5;
	public static final double LAYER_SENSOR = 0.4;
	public static final double LAYER_SIGNALARROW = 0.4;
	private GLU glu; // for the GL Utility
	private RoadNetwork drawableNetwork;
	private Camera cam;
	private java.awt.Point preWinCoods;
	private TextRenderer textRenderer;
	private boolean isMidBtnDragged, isRightBtnDragged;
	private boolean isPicking;
	private boolean isFirstRender;
	private List<NetworkObject> pickedObjects;
	// 临时记录已选择的对象
	private NetworkObject preObject;
	private AnimationFrame curFrame;
	private int status;
	private int mode;
	private int clockCounter;
	private double sliderFTime;

	/** Constructor to setup the GUI for this Component */
	public JOGLCanvas() {
		this.addGLEventListener(this);
		status = 0;
		mode = 0;
		preWinCoods = new java.awt.Point();
		pickedObjects = new ArrayList<>();
		clockCounter = 0;

	}
	public JOGLCanvas(int width, int height) {
		this.addGLEventListener(this);
		preWinCoods = new java.awt.Point();
		pickedObjects = new ArrayList<NetworkObject>();
		// 设置画布大小
		// setPreferredSize 有布局管理器下使用；setSize 无布局管理器下使用
//		this.setPreferredSize(new Dimension(width, height));
	}
	public boolean isNetworkReady(){
		return drawableNetwork != null? true : false;
	}
	public void setDrawableNetwork(RoadNetwork network){
		drawableNetwork = network;
	}
	public void setSliderFTime(double fTime){
		this.sliderFTime = fTime;
	}
	public void setCamera(Camera cam){
		this.cam = cam;
	}
	public void setFirstRender(boolean isFirstRender) {
		this.isFirstRender = isFirstRender;
	}
	public void setStatus(int status){
		this.status = status;
	}
	public int getStatus(){
		return this.status;
	}
	public void setMode(int mode){
		this.mode = mode;
	}
	public AnimationFrame getCurFrame(){
		return this.curFrame;
	}
	// ------ Implement methods declared in GLEventListener ------

	/**
	 * Called back immediately after the OpenGL context is initialized. Can be
	 * used to perform one-time initialization. Run only once.
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL graphics context
		glu = new GLU(); // get GL Utilities
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
		// YYL begin
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		float widthHeightRatio = (float) getWidth() / (float) getHeight();
		glu.gluPerspective(45, widthHeightRatio, 0.1, 10000);
		// YYL end
		gl.glClearDepth(1.0f); // set clear depth value to farthest
		gl.glEnable(GL_DEPTH_TEST); // enables depth testing
		gl.glDepthFunc(GL_LESS); // the type of depth test to do
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best
		// perspective
		// correction
		gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out
		// lighting

		// ----- Your OpenGL initialization code here -----
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.addMouseWheelListener(this);
		this.addMouseMotionListener(this);
		this.textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 100));

	}

	/**
	 * Call-back handler for window re-size event. Also called when the drawable
	 * is first set to visible.
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context

		if (height == 0)
			height = 1; // prevent divide by zero
		float aspect = (float) width / height;

		// Set the view port (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);

		// Setup perspective projection, with aspect ratio matches viewport
		gl.glMatrixMode(GL_PROJECTION); // choose projection matrix
		gl.glLoadIdentity(); // reset projection matrix
		glu.gluPerspective(45.0, aspect, 0.1, 10000.0); // fovy, aspect, zNear,
		// zFar
	}

	/**
	 * Called back by the animator to perform rendering.
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color
		// and depth  buffers

		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity(); // reset the model-view matrix

		// ----- Your OpenGL rendering code here (render a white triangle for
		// testing) -----

		if(drawableNetwork !=null){
			if(isFirstRender){
				GeoPoint center = drawableNetwork.getWorldSpace().getCenter();
				cam.initFirstLookAt(new GeoPoint(center.getLocationX(), center.getLocationY(),1000), center, new float[]{0.0f, 1.0f, 0.0f});
				isFirstRender = false;
			}
			glu.gluLookAt(cam.getEyeLocation()[0], cam.getEyeLocation()[1], cam.getEyeLocation()[2],
					cam.getTarLocation()[0], cam.getTarLocation()[1], cam.getTarLocation()[2], 0, 1, 0);

			scene(gl);
			if(isPicking) {
				isPicking = false;
				selectObject(gl, MainWindow.getInstance().getCurLayerName());
			}

		}
	}

	/**
	 * Called back before the OpenGL context is destroyed. Release resource such
	 * as buffers.
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
	}
	public void scene(GL2 gl) {
		// Node 点
//		for(int i=0; i< drawableNetwork.nNodes();i++){
//			Node itrNode = drawableNetwork.getNode(i);
//			ShapeUtil.drawPoint(gl,itrNode.getPosPoint(),10,new float[]{0,0.69f,0.94f},itrNode.isSelected(),LAYER_NODE);
//		}
		// Boundary 线
		for (int i = 0; i< drawableNetwork.nBoundaries(); i++) {
			Boundary tmpboundary = drawableNetwork.getBoundary(i);
			ShapeUtil.drawSolidLine(gl, tmpboundary.getStartPnt(), tmpboundary.getEndPnt(), 2,
					Constants.COLOR_WHITE, LAYER_BOUNDARY);
		}
		// Segment 线
		for(int i = 0; i< drawableNetwork.nSegments(); i++){
			Segment tmpsegment = drawableNetwork.getSegment(i);
			ShapeUtil.drawPolyline(gl, tmpsegment.getCtrlPoints(), 2,
					Constants.COLOR_BLUE,LAYER_BOUNDARY);
		}
		// Lane 线
		for(Lane itrLane:drawableNetwork.getLanes()){
			ShapeUtil.drawPolyline(gl,itrLane.getCtrlPoints(),2, Constants.COLOR_WHITE,LAYER_LANE);
		}
		// Connector 线

		for(int i=0; i< drawableNetwork.nConnectors();i++){
			Connector tmpConnector = drawableNetwork.getConnector(i);
			if (tmpConnector instanceof MLPConnector && ((MLPConnector)tmpConnector).vehNumOnConn()>0)
				ShapeUtil.drawPolyline(gl,tmpConnector.getShapePoints(),2, Constants.COLOR_GOLDEN, LAYER_CONNECTOR);
			else
				ShapeUtil.drawPolyline(gl,tmpConnector.getShapePoints(),2, Constants.COLOR_GREY_WHITE, LAYER_CONNECTOR);
		}

		boolean isPause = (status == ANIMATOR_PAUSE);
		//暂停时不更新帧索引
		curFrame =FrameQueue.getInstance().poll(isPause);
		//暂停状态下读下一帧
		if (isPause && mode == ANIMATOR_FRAME_ADVANCE){
			curFrame = FrameQueue.getInstance().poll(false);
			mode = 0;
		}

		// SignalArrow 箭头
		for(int i=0; i< drawableNetwork.nLanes();i++){
			float[] saColor;
			Lane itrLane = drawableNetwork.getLane(i);
			for(SignalArrow sa:itrLane.getSignalArrows()){
				if(curFrame ==null || (saColor = curFrame.getSignalColors().get(sa))==null)
					saColor = Constants.COLOR_WHITE;
				ShapeUtil.drawPolyline(gl,sa.getPolyline(),2, saColor,LAYER_SIGNALARROW);
				ShapeUtil.drawPolygon(gl,sa.getArrowTip(),saColor,false,LAYER_SIGNALARROW);
			}
		}

		if(curFrame !=null){
			for(VehicleData vd:curFrame.getVhcDataQueue()){
				if(drawableNetwork instanceof MLPNetwork){
					if ((vd.getSpecialFlag()&Constants.FOLLOWING) == 0){
						ShapeUtil.drawPolygon(gl, vd.getVhcShape().getKerbList(), Constants.COLOR_RED, vd.isSelected(),LAYER_VEHICLE);
					}
					else {
						if((vd.getSpecialFlag()&Constants.VIRTUAL_VEHICLE) != 0)//虚拟车
							ShapeUtil.drawPolygon(gl, vd.getVhcShape().getKerbList(), Constants.COLOR_LITEBLUE, vd.isSelected(),LAYER_VEHICLE);
						else //非虚拟车
							ShapeUtil.drawPolygon(gl, vd.getVhcShape().getKerbList(), Constants.COLOR_BLUE, vd.isSelected(),LAYER_VEHICLE);
					}
				}
				else if(drawableNetwork instanceof RTNetwork){
					switch (vd.getTurnInfo()) {
						// 左转
						case "L":
							ShapeUtil.drawPolygon(gl, vd.getVhcShape().getKerbList(), Constants.COLOR_RED, vd.isSelected(), 1);
							break;
						// 直行
						case "S":
							ShapeUtil.drawPolygon(gl, vd.getVhcShape().getKerbList(), Constants.COLOR_BLUE, vd.isSelected(), 1);
							break;
						// 右转
						case "R":
							ShapeUtil.drawPolygon(gl, vd.getVhcShape().getKerbList(), Constants.COLOR_GREEN, vd.isSelected(), 1);
							break;
						default:
							ShapeUtil.drawPolygon(gl, vd.getVhcShape().getKerbList(), Constants.COLOR_WHITE, vd.isSelected(), 1);
							break;
					}
				}

			}
			// 显示车道状态
			if(RTEngine.isState ){
				for(StateData sd:curFrame.getStateDataQueue()) {
					double avgSpeed = sd.getAvgSpeed();
					Color color;
					if(avgSpeed<15 && avgSpeed>0)
						color = ColorBar.valueToColor(0, 15, sd.getAvgSpeed(), 250);
					else{
						if(avgSpeed == 0)
							color = new Color(255,0,0);
						else
							color = new Color(0,255,0);
					}


					if (color != null && sd.getSurface()!=null) {// 排队长度超出车道长度时surface为空
						float[] colorf = new float[]{color.getRed()/255.0f,color.getGreen()/255.0f,color.getBlue()/255.0f};
						ShapeUtil.drawPolygons(gl, sd.getSurface(), colorf, false, 0.9);
					}
				}
			}
			JSlider slider = MainWindow.getInstance().getSlider();
			slider.setValue((int)Math.floor((curFrame.getSimTimeInSeconds() - drawableNetwork.getSimClock().getStartTime() - 120*clockCounter)));
			if(slider.getValue()>=120) {// 大于120秒(超出时间条长度)
				sliderFTime = drawableNetwork.getSimClock().getCurrentTime();
				MainWindow.getInstance().updateSlider((long)sliderFTime);
				MainWindow.getInstance().updateSignalPanel();
				clockCounter ++;
			}
			//以下过程转移至FrameQueue.queueSwitch()中，在交换队列时统一回收
//			//回收vehicledata
//			if(!isPause)
//				curFrame.clean();
		}

		// Sensor 面
		for(int i = 0; i< drawableNetwork.nSensors(); i++){
			Sensor tmpSensor = drawableNetwork.getSensor(i);
			ShapeUtil.drawPolygon(gl, tmpSensor.getSurface().getKerbList(),Constants.COLOR_GREEN, tmpSensor.isSelected(),LAYER_SENSOR);
		}

		switch (MainWindow.getInstance().getCurLayerName()){
			case "Segment": //Segment 面
				for(int i = 0; i< drawableNetwork.nSegments(); i++){
					Segment tmpSegment = drawableNetwork.getSegment(i);
					if(tmpSegment.isSelected()) {
						ShapeUtil.drawPolygons(gl, tmpSegment.getSurface(), Constants.COLOR_GREY, true, LAYER_SEGMENT);
					}
				}
				break;
			case "Lane":// Lane 面
				for(int i = 0; i< drawableNetwork.nLanes(); i++){
					Lane tmpLane = drawableNetwork.getLane(i);
					if(tmpLane.isSelected())
						ShapeUtil.drawPolygons(gl, tmpLane.getSurface(),Constants.COLOR_GREY, true, LAYER_LANE);
				}
				break;
		}

		// Surface 面
		for(int i=0;i<drawableNetwork.nSurfaces();i++){
			GeoSurface surface = drawableNetwork.getSurface(i);
			ShapeUtil.drawPolygon(gl,surface.getKerbList(),Constants.COLOR_GREY,false,LAYER_SURFACE);
		}


	}
	//计算拾取射线与x/y/z = intersectPlane 平面的交点
	//offset=0,1,2:x,y,z
	public float[] calcRay(final GL2 gl, final float intersectPlane, final int offset, final int winx, final int winy){
		int[] viewport = new int[4];
		float[] projmatrix = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
		float[] mvmatrix = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
		float[] posNear = new float[3];
		float[] posFar = new float[3];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		float winX = winx;
		float winY = viewport[3] - winy;
		if(!glu.gluUnProject(winX, winY, 0.0f, mvmatrix, 0, projmatrix, 0, viewport, 0, posNear, 0) ||
				!glu.gluUnProject(winX, winY, 1.0f, mvmatrix, 0, projmatrix, 0, viewport, 0, posFar, 0))
			System.out.println("The matrix can not be inverted");
		VectorUtil.subVec3(posFar, posFar, posNear);
		VectorUtil.normalizeVec3(posFar);
		float scale = (intersectPlane - posNear[offset])/posFar[offset];
		VectorUtil.scaleVec3(posFar, posFar, scale);
		float[] intersection = new float[3];
		VectorUtil.addVec3(intersection, posNear, posFar);
		return intersection;
	}
	public Ray calcRay(final GL2 gl){
		int[] viewport = new int[4];
		float[] projmatrix = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
		float[] mvmatrix = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
		float[] posNear = new float[3];
		float[] posFar = new float[3];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		float winX = preWinCoods.x;
		float winY = viewport[3] - preWinCoods.y;
		if(!glu.gluUnProject(winX, winY, 0.0f, mvmatrix, 0, projmatrix, 0, viewport, 0, posNear, 0) ||
				!glu.gluUnProject(winX, winY, 1.0f, mvmatrix, 0, projmatrix, 0, viewport, 0, posFar, 0))
			System.out.println("The matrix can not be inverted");
		VectorUtil.subVec3(posFar, posFar, posNear);
		VectorUtil.normalizeVec3(posFar);
		Ray ray = new Ray();
		for(int i=0;i<posFar.length;i++){
			ray.orig[i] = posNear[i];
			ray.dir[i] = posFar[i];
		}
		return ray;
	}
	private void selectObject(GL2 gl, String layerName){

		Ray pickRay = calcRay(gl) ;
		switch (layerName){//被选中对象用黄色渲染
			case "Node":
				//节点选择
				for(int i = 0; i < drawableNetwork.nNodes();i++){
					Node itrNode = drawableNetwork.getNode(i);
					if(GeoUtil.isIntersect(pickRay,itrNode.getBoundBox())){
						itrNode.setSelected(true);
						// TODO checkBox
						double fTime;
						if(!itrNode.getSignalPlans().isEmpty()){
							if(curFrame!=null)
								fTime = sliderFTime;
							else
								fTime = itrNode.getSignalPlans().get(0).getFTime();
							SignalStagePanel.getInstance().setPlans(itrNode.getSignalPlans(), fTime, fTime + 120);
						}
						pickedObjects.add(itrNode);
					}
				}
				break;
			case "Link":
				break;
			case "Segment":
				//路段选择
				for(int i=0;i<drawableNetwork.nSegments();i++){
					Segment tmpSegment = drawableNetwork.getSegment(i);
					if(GeoUtil.isIntersect(pickRay,tmpSegment.getSurface())){
						tmpSegment.setSelected(true);
						pickedObjects.add(tmpSegment);

					}
				}
				break;
			case "Lane":
				//车道选择
				for(int i=0;i<drawableNetwork.nLanes();i++){
					Lane tmpLane = drawableNetwork.getLane(i);
					if(GeoUtil.isIntersect(pickRay,tmpLane.getSurface())){
						tmpLane.setSelected(true);
						pickedObjects.add(tmpLane);
					}
				}
				break;
			case "Vehicle":
				//车辆选择
				if(status==ANIMATOR_PAUSE && curFrame!=null) {
					for (VehicleData vd : curFrame.getVhcDataQueue()) {
						if (GeoUtil.isIntersect(pickRay, vd.getVhcShape())) {
							vd.setSelected(true);
							pickedObjects.add(vd);
						}
					}
				}
				break;
			case "Sensor":
				break;
			default:
				System.out.println("The name of layer might be wrong!");
				break;
		}
		//TODO 写死读取第一个对象
		if(!pickedObjects.isEmpty()){
			((PanelAction)MainWindow.getInstance().getLayerPanel().getLayer(layerName)).writeTxtComponents(pickedObjects.get(0));
		}
	}

	public void deselect(){
		for(NetworkObject no:pickedObjects){
			no.setSelected(false);
			if(no instanceof Node){
				SignalStagePanel.getInstance().clear();
			}
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {

	}
	@Override
	public void mousePressed(MouseEvent e) {
		switch(e.getButton()){
			case MouseEvent.BUTTON1:
				isPicking = true;
				//清空前一帧已选对象
				if(!pickedObjects.isEmpty()){
					deselect();
					pickedObjects.clear();
					((PanelAction)MainWindow.getInstance().getLayerPanel().getLayer(MainWindow.getInstance().getCurLayerName()))
							.resetTxtComponents();
				}
				break;
			case MouseEvent.BUTTON2:
				isMidBtnDragged = true;
				break;
			case MouseEvent.BUTTON3:
				isRightBtnDragged = true;
				break;
		}


	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON2){
			isMidBtnDragged = false;
		}
		else if(e.getButton() == MouseEvent.BUTTON3){
			isRightBtnDragged = false;
		}
	}
	@Override
	public void mouseEntered(MouseEvent e) {

	}
	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(isMidBtnDragged){
			cam.calcMouseMotion(e.getX() - preWinCoods.x, e.getY() - preWinCoods.y, MouseEvent.BUTTON2);
			preWinCoods.setLocation(e.getX(), e.getY());
		}
		else if (isRightBtnDragged){
			cam.calcMouseMotion(e.getX() - preWinCoods.x, e.getY() - preWinCoods.y, MouseEvent.BUTTON3);
			preWinCoods.setLocation(e.getX(), e.getY());
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {

		preWinCoods.setLocation(e.getX(), e.getY());
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		cam.calcMouseWheelMotion(e.getWheelRotation());
	}
	@Override
	public void keyTyped(KeyEvent e) {

	}
	@Override
	public void keyPressed(KeyEvent e) {
		cam.calcKeyMotion(e.getKeyCode());
	}
	@Override
	public void keyReleased(KeyEvent e) {

	}

	//wym
	private List<GeoPoint> platoonBoundGen(VehicleData vd) {
		List<GeoPoint> pts = vd.getVhcShape().getKerbList();
		double[] v1 = LinearAlgebra.minus(pts.get(1).getLocCoods(),pts.get(0).getLocCoods());
		double[] v2 = LinearAlgebra.minus(pts.get(2).getLocCoods(),pts.get(1).getLocCoods());
		double d1 = Math.sqrt(Arrays.stream(v1).map(e -> e*e).sum());
		double d2 = Math.sqrt(Arrays.stream(v2).map(e -> e*e).sum());
		double[] el, et;
		if(d1<d2) {
			el = LinearAlgebra.times(v2, 1/d2);
			et = LinearAlgebra.times(v1, 1/d1);
		}
		else {
			el = LinearAlgebra.times(v1, 1/d1);
			et = LinearAlgebra.times(v2, 1/d2);
		}
		double[] nt = LinearAlgebra.times(et,Constants.LANE_WIDTH);
		double[] nl = LinearAlgebra.times(et,0.5);
		List<GeoPoint> ans = new ArrayList<>();
		double[] newP = LinearAlgebra.minus(vd.getHeadPosition().getLocCoods(),LinearAlgebra.times(nt,0.5));
		ans.add(new GeoPoint(newP));
		newP = LinearAlgebra.plus(newP,nl);
		ans.add(new GeoPoint(newP));
		newP = LinearAlgebra.plus(newP,nt);
		ans.add(new GeoPoint(newP));
		newP = LinearAlgebra.minus(newP,nl);
		ans.add(new GeoPoint(newP));
		return ans;
	}
}
