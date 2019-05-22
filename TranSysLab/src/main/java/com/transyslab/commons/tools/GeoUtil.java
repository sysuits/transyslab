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

package com.transyslab.commons.tools;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.math.VectorUtil;
import com.transyslab.roadnetwork.Boundary;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.GeoPoint;
import com.transyslab.roadnetwork.GeoSurface;

import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;

public class GeoUtil {
	public static final double EPSILON = 0.000001;
	public static GeoPoint intersect(Boundary upBound, Boundary dnBound) {
		double[][] coef = new double[][] { upBound.getDelta(), LinearAlgebra.times(dnBound.getDelta(), -1)};
		coef = LinearAlgebra.transpose(coef);
		double[][] b = new double[][] {LinearAlgebra.minus(dnBound.getStartPnt().getLocCoods(),
				upBound.getStartPnt().getLocCoods())};
		b = LinearAlgebra.transpose(b);
		int r = LinearAlgebra.rank(coef);
		switch (r) {
			case 0:
				return null;
			case 1:
				return dnBound.getStartPnt();
			case 2:
				double[][] ans = LinearAlgebra.solve(coef, b);
				double[] p = LinearAlgebra.plus(LinearAlgebra.times(upBound.getDelta(), ans[0][0]), upBound.getStartPnt().getLocCoods());
				return new GeoPoint(p);
			case 3:
				return null;
			default:
				return null;
		}
	}
	public static GeoSurface lineToRectangle(final GeoPoint fPoint, final GeoPoint tPoint, final double width, final boolean bothSide){
		GeoSurface sf = new GeoSurface();
		double[] vecDir = LinearAlgebra.minus(tPoint.getLocCoods(), fPoint.getLocCoods());
		double distance;
		if(bothSide)// 从中心线扩展成矩形面
			distance = width/2.0f;
		else // 从中心线向右拓宽
			distance = width;
		double[] translation = new double[3];
		if(vecDir[0] == 0.0){
			if(vecDir[1]!=0.0){
				// coods.X
				translation[0] = distance;
				// coods.Y
				translation[1] = 0.0;
			}
			else{
				System.out.println("Error: Can't not expand");
			}
		}
		else{
			// coods.Y
			translation[1] = distance/Math.sqrt(1.0 + Math.pow(vecDir[1]/vecDir[0],2));
			// coods.X
			translation[0] = (-vecDir[1]/vecDir[0]) * translation[1];
		}
		Vector3D normVec = new Vector3D(translation[0],translation[1],translation[2]);
		Vector3D dirVec = new Vector3D(vecDir[0],vecDir[1],vecDir[2]);
		//注意叉乘次序，a x b ！= b x a
		normVec = Vector3D.crossProduct(normVec, dirVec);
		if(normVec.getZ()<0)
			// z>=0的面朝上，适应opengl右手坐标系
			translation = LinearAlgebra.divide(translation, -1);
		// 矩形四个顶点，按逆时针顺序存储
		if(bothSide){
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(fPoint.getLocCoods(),translation)));
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(fPoint.getLocCoods(),translation)));
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(tPoint.getLocCoods(),translation)));
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(tPoint.getLocCoods(),translation)));
		}
		else{
			sf.addKerbPoint(new GeoPoint(fPoint));
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(fPoint.getLocCoods(),translation)));
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(tPoint.getLocCoods(),translation)));
			sf.addKerbPoint(new GeoPoint(tPoint));
		}
		return sf;
	}

	public static GeoPoint calcRotation(GeoPoint fPoint, GeoPoint tPoint, double theta, Vector3D rotateDir){
		// 四元数法计算旋转矩阵，旋转角为theta,绕轴rotateDir（单位向量）旋转
		if(rotateDir.getNorm() - 1.0 > Constants.RATE_EPSILON)
			rotateDir = rotateDir.normalize();
		double q0 = Math.cos(theta/2.0);
		double q1 = rotateDir.getX()*Math.sin(theta/2.0);
		double q2 = rotateDir.getY()*Math.sin(theta/2.0);
		double q3 = rotateDir.getZ()*Math.sin(theta/2.0);
		Array2DRowRealMatrix rotateMatrix = new Array2DRowRealMatrix(new double[][]{{1-2*q2*q2-2*q3*q3, 2*q1*q2-2*q3*q0,   2*q1*q3+2*q2*q0},
				{2*q1*q2+2*q3*q0,   1-2*q1*q1-2*q3*q3, 2*q2*q3-2*q1*q0},
				{2*q1*q3-2*q2*q0,   2*q2*q3+2*q1*q0,   1-2*q1*q1-2*q2*q2}});
		Array2DRowRealMatrix vecMatirx = new Array2DRowRealMatrix(new double[]{tPoint.getLocationX() - fPoint.getLocationX(),
				tPoint.getLocationY() - fPoint.getLocationY(),
				tPoint.getLocationZ() - fPoint.getLocationZ()});
		double[] result;
		// 左乘
		vecMatirx = rotateMatrix.multiply(vecMatirx);
		result = LinearAlgebra.plus(vecMatirx.getColumn(0),fPoint.getLocCoods());
		return new GeoPoint(result);

	}
	public static void calcTranslation(GeoPoint[] points,Vector3D dir){
		for(int i=0;i<points.length;i++){
			points[i].setLocCoods(points[i].getLocationX() + dir.getX(),
					points[i].getLocationY() + dir.getY(),
					points[i].getLocationZ() + dir.getZ());
		}
	}
	public static boolean isIntersect(Ray ray, GeoSurface surf) {

		boolean Intersect = true;

		List<GeoPoint> geoPointSet = surf.getKerbList();
		float[] x = new float[geoPointSet.size() + 1];
		float[] y = new float[geoPointSet.size() + 1];
		float[] z = new float[geoPointSet.size() + 1];

		for (int i = 0; i < geoPointSet.size(); i++) {

			x[i] = geoPointSet.get(i).getLocCoodsf()[0];
			y[i] = geoPointSet.get(i).getLocCoodsf()[1];
			z[i] = geoPointSet.get(i).getLocCoodsf()[2];

		}
		float[] v1 = { x[1] - x[0], y[1] - y[0], z[1] - z[0] };

		// 不共线的三点组成一个平面，根据两向量叉乘判断，a x b=0->a与b共线
		float[] normVector = new float[3];
		int k = 0;
		for (int i = 2; i < geoPointSet.size(); i++) {
			float[] v2 = { x[i] - x[0], y[i] - y[0], z[i] - z[0] };

			float[] result = { 0, 0, 0 };
			VectorUtil.crossVec3(result, v1, v2);
			if (!(result[0] == 0 && result[1] == 0 && result[2] == 0)) {
				// normal vector
				normVector = result;
				k = i;
				break;
			}
		}

		// 求平面的单位法向量
		VectorUtil.normalizeVec3(normVector,normVector);

		if (VectorUtil.dotVec3(normVector, ray.dir) == 0) // when ray is orthogonal with normal vector, there
		// is no intersection
		{
			Intersect = false;
		}

		else // if there is a intersection, find out whether it is within the lane
		{
			// find out the scale to intersect

			float scale = -(normVector[0] * (ray.orig[0] - x[k]) + normVector[1] * (ray.orig[1] - y[k])
					+ normVector[2] * (ray.orig[2] - z[k]))
					/ (normVector[0] * ray.dir[0] + normVector[1] * ray.dir[1] + normVector[2] * ray.dir[2]);
			GeoPoint intersection = new GeoPoint( ray.orig[0] + scale * ray.dir[0], ray.orig[1] + scale * ray.dir[1],
					ray.orig[2] + scale * ray.dir[2] );
			if(surf.getType() == GeoSurface.MULTI_POLYGONS){
				// polygon个数
				int nPolygon = geoPointSet.size()/2-1;
				for(int i=0;i<nPolygon;i++){
					List<GeoPoint> kerbs = new ArrayList<>();
					int index = i*2;
					kerbs.add(geoPointSet.get(index));
					kerbs.add(geoPointSet.get(index+1));
					kerbs.add(geoPointSet.get(index+3));
					kerbs.add(geoPointSet.get(index+2));
					if(Intersect = isInPolygon(intersection,kerbs)){
						break;
					}
				}
			}
			else{// 顶点已按逆时针顺序存储的凸边形
				Intersect = isInPolygon(intersection,geoPointSet);
			}




		}

		return Intersect;
	}
	public static boolean isInPolygon(GeoPoint point,List<GeoPoint> vertices){
		// 判断点是否在多边形内部，设多边形由点n1、n2、n3...组成，交点为no，依次求 ni->ni+1 与 ni->no 的叉乘，判断叉乘结果是否都在一个方向，
		// 若是则在平面内，反之在平面外
		Vector3D c1 = new Vector3D(0,0,0),c2 = new Vector3D(0,0,0);
		double temp = 1;
		int nPoint = vertices.size();
		double x[] = new double[nPoint];
		double y[] = new double[nPoint];
		double z[] = new double[nPoint];
		for(int i=0; i< nPoint;i++){
			x[i] = vertices.get(i).getLocationX();
			y[i] = vertices.get(i).getLocationY();
			z[i] = vertices.get(i).getLocationZ();
		}
		for (int i = 0; i < nPoint - 2; i++) {
			double[] va = { x[i + 1] - x[i], y[i + 1] - y[i], z[i + 1] - z[i] };
			double[] vai = { point.getLocationX() - x[i], point.getLocationY() - y[i], point.getLocationZ() - z[i] };
			double[] vb = { x[i + 2] - x[i + 1], y[i + 2] - y[i + 1], z[i + 2] - z[i + 1] };
			double[] vbi = { point.getLocationX() - x[i + 1], point.getLocationY() - y[i + 1], point.getLocationZ() - z[i + 1] };
			c1 = new Vector3D(va).crossProduct(new Vector3D(vai));
			c2 = new Vector3D(vb).crossProduct(new Vector3D(vbi));

			temp = temp * c1.dotProduct(c2); // a.b=|a||b|cos<a,b>

			if (temp < 0) {
				break;
			}
		}

		if(temp >= 0)
		{
			double[] vd = { x[0] - x[nPoint - 1], y[0] - y[nPoint - 1], z[0] - z[nPoint - 1] };
			double[] vdi = { point.getLocationX() - x[nPoint - 1], point.getLocationY() - y[nPoint - 1],
					point.getLocationZ() - z[nPoint - 1] };
			c1 = new Vector3D(vd).crossProduct(new Vector3D(vdi));
			temp = temp * c1.dotProduct(c2);
		}


		if (temp >= 0) {
			return true;
		} else {
			return  false;
		}
	}
	// 判断空间直线是否有交点，有则返回交点，否则返回null
	public static GeoPoint calcLineLineIntersection(GeoPoint l1p1, GeoPoint l1p2, GeoPoint l2p1, GeoPoint l2p2){
		// Paul Bourke at http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline3d/
		//GeoPoint rstp1 = new GeoPoint();
		//GeoPoint rstp2 = new GeoPoint();
		Vector3D p1 = new Vector3D(l1p1.getLocCoods());
		Vector3D p2 = new Vector3D(l1p2.getLocCoods());
		Vector3D p3 = new Vector3D(l2p1.getLocCoods());
		Vector3D p4 = new Vector3D(l2p2.getLocCoods());
		Vector3D p13 = p1.subtract(p3);
		Vector3D p43 = p4.subtract(p3);
		// 非平行或共线
		if ( p13.getNormSq()< EPSILON) {
			return null;
		}
		Vector3D p21 = p2.subtract(p1);
		if (p21.getNormSq() < EPSILON) {
			return null;
		}

		double d1343 = p13.getX() * p43.getX() + p13.getY() * p43.getY() + p13.getZ() * p43.getZ();
		double d4321 = p43.getX() * p21.getX() + p43.getY() * p21.getY() + p43.getZ() * p21.getZ();
		double d1321 = p13.getX() * p21.getX() + p13.getY() * p21.getY() + p13.getZ() * p21.getZ();
		double d4343 = p43.getX() * p43.getX() + p43.getY() * p43.getY() + p43.getZ() * p43.getZ();
		double d2121 = p21.getX() * p21.getX() + p21.getY() * p21.getY() + p21.getZ() * p21.getZ();

		double denom = d2121 * d4343 - d4321 * d4321;
		if (Math.abs(denom) < EPSILON) {
			return null;
		}
		double numer = d1343 * d4321 - d1321 * d4343;

		double mua = numer / denom;
		double mub = (d1343 + d4321 * (mua)) / d4343;

		//构成两直线最短距离的点，若相交则两点重合
		Vector3D v1 = p3.add(p43.scalarMultiply(mub));
		Vector3D v2 = p1.add(p21.scalarMultiply(mua));
		double distance = (v1.subtract(v2)).getNorm();
		if(distance<EPSILON){
			//交点在线段1末端和线段2始端之间的
			if(mua>1 && mub<0)
				return new GeoPoint(v1.getX(),v1.getY(),v1.getZ());
			else
				return null;
		}
		return null;
		//rstp1.setLocCoods(p1.getX()+mua * p21.getX(),p1.getY() + mua * p21.getY(),p1.getZ() + mua * p21.getZ());
		//rstp2.setLocCoods(p3.getX()+mub * p43.getX(),p3.getY() + mub * p43.getY(),p3.getZ() + mub * p43.getZ());
	}
	// 凸边形三角剖分算法，未实现
	public static GeoSurface multiLine2Triangles(List<GeoPoint> points,final double width, final boolean bothSide){
		int nPoints = points.size();
		if(nPoints < 2){
			System.out.println("Error");
			return null;
		}
		else if(nPoints<3) {
			return lineToRectangle(points.get(0),points.get(1),width,bothSide);
		}
		else{
			return new GeoSurface();
		}
	}
	public static GeoSurface multiLines2Rectangles(List<GeoPoint> points,final double width, final boolean bothSide){
		GeoSurface sf = new GeoSurface(GeoSurface.MULTI_POLYGONS);
		GeoPoint tPoint,fPoint;
		int nPolygons = points.size()-1;
		for(int i=0;i<nPolygons;i++){
			tPoint = points.get(i+1);
			fPoint = points.get(i);
			double[] vecDir = LinearAlgebra.minus(tPoint.getLocCoods(), fPoint.getLocCoods());
			double distance;
			if(bothSide)// 从中心线扩展成矩形面
				distance = width/2.0f;
			else // 从中心线向右拓宽
				distance = width;
			double[] translation = new double[3];
			if(vecDir[0] == 0.0){
				if(vecDir[1]!=0.0){
					// coods.X
					translation[0] = distance;
					// coods.Y
					translation[1] = 0.0;
				}
				else{
					System.out.println("Error: Can't not expand");
				}
			}
			else{
				// coods.Y
				translation[1] = distance/Math.sqrt(1.0 + Math.pow(vecDir[1]/vecDir[0],2));
				// coods.X
				translation[0] = (-vecDir[1]/vecDir[0]) * translation[1];
			}
			Vector3D normVec = new Vector3D(translation[0],translation[1],translation[2]);
			Vector3D dirVec = new Vector3D(vecDir[0],vecDir[1],vecDir[2]);
			//注意叉乘次序，a x b ！= b x a
			normVec = Vector3D.crossProduct(normVec, dirVec);
			if(normVec.getZ()<0)
				// z>=0的面朝上，适应opengl右手坐标系
				translation = LinearAlgebra.divide(translation, -1);
			// 矩形四个顶点，按逆时针顺序存储
			if(bothSide){
				if(i==nPolygons-1){//最后一个矩形
					sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(fPoint.getLocCoods(),translation)));
					sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(fPoint.getLocCoods(),translation)));
					sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(tPoint.getLocCoods(),translation)));
					sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(tPoint.getLocCoods(),translation)));
				}
				else{
					sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(fPoint.getLocCoods(),translation)));
					sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(fPoint.getLocCoods(),translation)));
				}

			}
			else{
				if(i==nPolygons-1){//最后一个矩形
					sf.addKerbPoint(new GeoPoint(fPoint));
					sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(fPoint.getLocCoods(),translation)));
					sf.addKerbPoint(new GeoPoint(tPoint));
					sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(tPoint.getLocCoods(),translation)));
				}
				else {
					sf.addKerbPoint(new GeoPoint(fPoint));
					sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(fPoint.getLocCoods(),translation)));
				}
			}
		}

		return sf;
	}
	// 判断折线是否相交（两折线中的任意线段跨立）
	public static boolean isCross(List<GeoPoint> ploylineP, List<GeoPoint> polyLineQ) {
		//todo: 实现几何判断逻辑
		return false;
	}
}
