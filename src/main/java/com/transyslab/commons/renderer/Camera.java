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

import com.jogamp.opengl.math.VectorUtil;
import com.transyslab.roadnetwork.GeoPoint;

public abstract class Camera {
	

	protected float[] eyeLocation = new float[3];
	protected float[] tarLocation = new float[3];
	protected float[] eyeMotion = new float[3];
	protected float[] tarMotion = new float[3];
	protected float[] camUp = new float[]{0.0f, 1.0f, 0.0f};
	
	public Camera(){
		
	}
	public float[] getEyeLocation(){
		return eyeLocation;
	}
	public float[] getTarLocation(){
		return tarLocation;
	}
	public void initFirstLookAt(final GeoPoint eyePoint, final GeoPoint tarPoint, final float[] camUp){
		final double[] eyeLocation = eyePoint.getLocCoods();
		final double[] tarLocation = tarPoint.getLocCoods();
		for(int i=0; i<3; i++){
			this.tarLocation[i] = (float) tarLocation[i];
			this.eyeLocation[i] = (float) eyeLocation[i];
			this.camUp[i] = camUp[i];
		}
	}
	public void initFirstLookAt(final float[] eyeLocation, final float[] tarLocation, final float[] camUp){
		for(int i=0; i<3; i++){
			this.tarLocation[i] = (float) tarLocation[i];
			this.eyeLocation[i] = (float) eyeLocation[i];
			this.camUp[i] = camUp[i];
		}
	}
	public abstract void calcMouseMotion(final int deltaWinX, final int deltaWinY, final int mouseButton);
	
	public abstract void calcMouseWheelMotion(final int wheelRotation);
	
	public abstract void calcKeyMotion(final int keyCode);
	
	protected void update(){
		VectorUtil.addVec3(eyeLocation, eyeLocation, eyeMotion);
		VectorUtil.addVec3(tarLocation, tarLocation, tarMotion);
		// reset motion
		for(int i=0;i<3;i++){
			eyeMotion[i] = 0;
			tarMotion[i] = 0;
		}
	}
}
