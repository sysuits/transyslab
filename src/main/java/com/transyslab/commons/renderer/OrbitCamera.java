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


import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.math.VectorUtil;
import com.transyslab.roadnetwork.GeoPoint;

import java.awt.event.KeyEvent;

public class OrbitCamera extends Camera{
	private float zoomSpeed, rotateSpeed, panSpeed;
	private float minDistance;
	private float maxDistance;
	private float minPolarAngle;
	private float maxPolarAngle;
	private final static float EPS = 0.00001f;
	private final static float PIXELS_PER_ROUND = 1800;
	//球坐标系
	private double theta, thetaDelta;
	private double phi, phiDelta;
	private double radius,scale;
	public OrbitCamera(){
		scale = 1.0;
		zoomSpeed = 1.0f;
		rotateSpeed = 1.0f;
		panSpeed = 1.0f;
		minDistance = Float.NEGATIVE_INFINITY;
		maxDistance = Float.MAX_VALUE;
		
		maxPolarAngle = (float) Math.PI;
		minPolarAngle = 0;
	}
	public void initFirstLookAt(final GeoPoint eyePoint, final GeoPoint tarPoint, final float[] camUp){
		super.initFirstLookAt(eyePoint, tarPoint, camUp);
		float[] offset = new float[3];
		VectorUtil.subVec3(offset, eyeLocation, tarLocation);
		this.theta =  Math.atan2(offset[0], offset[2]);
		this.phi = Math.atan2(Math.sqrt(offset[0] * offset[0] + offset[2] * offset[2]), offset[1]);
		this.radius =  VectorUtil.normVec3(offset) *this.scale;
	}
	public void calcMouseMotion(final int deltaWinX, final int deltaWinY, final int mouseButton){

		if( mouseButton == MouseEvent.BUTTON2){
			//关于高度radius自适应调整旋转速度
			//TODO 改成与画布高宽相关
			rotateSpeed = (float) (rotateSpeed*radius/500.0f);
			this.thetaDelta -= (2.0*Math.PI*(deltaWinX)/PIXELS_PER_ROUND * rotateSpeed);
			this.phiDelta -= (2.0*Math.PI*(deltaWinY)/PIXELS_PER_ROUND * rotateSpeed);
			float[] offset = new float[3];
			this.theta += this.thetaDelta;
			this.phi += this.phiDelta;
			
			this.phi = Math.max(this.minPolarAngle, Math.min(this.maxPolarAngle, phi));
			this.phi = Math.max( EPS, Math.min( Math.PI - EPS, phi ) );
			
			offset[0] = (float) (radius * Math.sin(phi) * Math.sin(theta));
			offset[1] = (float) (radius * Math.cos(phi));
			offset[2] = (float) (radius * Math.sin(phi) * Math.cos(theta));
			
			VectorUtil.addVec3(eyeLocation, tarLocation, offset);
			this.thetaDelta = 0.0;
			this.phiDelta = 0.0;
			this.rotateSpeed = 1.0f;
		}
		else if(mouseButton == MouseEvent.BUTTON3){
			float[] dir = new float[3];
			//关于高度radius自适应调整平移速度
			//TODO 改成与画布高宽相关
			panSpeed = (float) (panSpeed*radius/1000.0f);
			VectorUtil.subVec3(dir, eyeLocation, tarLocation);
			float[] right = new float[3];
			VectorUtil.crossVec3(right, dir, this.camUp);
			float[] up = new float[3];
			VectorUtil.crossVec3(up, right, dir);
			VectorUtil.normalizeVec3(up);
			VectorUtil.scaleVec3(up, up, deltaWinY*panSpeed);
			VectorUtil.normalizeVec3(right);
			VectorUtil.scaleVec3(right, right, deltaWinX*panSpeed);
			VectorUtil.addVec3(eyeMotion, right, up);
			VectorUtil.addVec3(tarMotion, right, up);
			panSpeed = 1.0f;
			update();
			
		}
		
	}

	public void calcMouseWheelMotion(final int wheelRotation){
		if(wheelRotation>0){
			this.scale /= Math.pow(0.95, zoomSpeed);
		}
		else{
			this.scale *= Math.pow(0.95, zoomSpeed);
		}
		float[] offset = new float[3];	
		this.radius *= this.scale;
		offset[0] = (float) (radius * Math.sin(phi) * Math.sin(theta));
		offset[1] = (float) (radius * Math.cos(phi));
		offset[2] = (float) (radius * Math.sin(phi) * Math.cos(theta));
		
		VectorUtil.addVec3(eyeLocation, tarLocation, offset);
		scale = 1.0;
		
	}
	public void calcKeyMotion(final int keyCode){

		switch (keyCode) {
			case KeyEvent.VK_SPACE :
				phi = Math.PI /2 - EPS;
				theta =  0.0;
				float[] offset = new float[3];
				offset[0] = (float) (radius * Math.sin(phi) * Math.sin(theta));
				offset[1] = (float) (radius * Math.cos(phi));
				offset[2] = (float) (radius * Math.sin(phi) * Math.cos(theta));
				VectorUtil.addVec3(eyeLocation, tarLocation, offset);
				break;
			case KeyEvent.VK_RIGHT :
				break;
			default:
				break;
		}
		/*
		switch (keyCode) {
			case KeyEvent.VK_PAGE_UP :
				keyStep += keySensitive;
				break;
			case KeyEvent.VK_PAGE_DOWN :
				keyStep -= keySensitive;
				break;
			case KeyEvent.VK_UP :
				eyeMotion[1] += keyStep;
				tarMotion[1] += keyStep;
				break;
			case KeyEvent.VK_DOWN :
				eyeMotion[1] -= keyStep;
				tarMotion[1] -= keyStep;
				break;
			case KeyEvent.VK_RIGHT :
				eyeMotion[0] += keyStep;
				tarMotion[0] += keyStep;
				break;
			case KeyEvent.VK_LEFT :
				eyeMotion[0] -= keyStep;
				tarMotion[0] -= keyStep;
				break;
		}*/
	}
	
}
