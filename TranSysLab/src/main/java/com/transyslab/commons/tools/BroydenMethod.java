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

import Jama.Matrix;

public class BroydenMethod {
	
	final static double sqrt_eps = 1.4901e-08;
	
	public static double[] solve(Function f, double[] x0) {
		int maxIters = 100;
		int maxBackSteps = 26;
		double tol = 1e-6;
		
		Matrix mat_x0 = new Matrix(x0, x0.length);
		
		double[] fx0 = f.cals(x0);
		Matrix mat_fx0 = new Matrix(fx0,fx0.length);
		double m0 = BroydenMethod.modSquare(fx0);
		Matrix mat_J = BroydenMethod.fjac(f, x0).inverse();
		
		Matrix mat_dx = mat_J.times(mat_fx0).times(-1);
		
		double[] fx = null;
		double m;
		
		for (int iter = 0; iter < maxIters; iter++) {
			Matrix mat_x = mat_x0.plus(mat_dx);
			fx = f.cals(mat_x.getColumnPackedCopy());
			m = BroydenMethod.modSquare(fx);
			if (m>m0) {
				for (int backStep = 0; backStep < maxBackSteps; backStep++) {
					double[] fxtest = f.cals(mat_x0.plus(mat_dx.times(0.5)).getColumnPackedCopy());
					double mtest = BroydenMethod.modSquare(fxtest);
					if (mtest>=m) {
						break;
					}
					mat_dx.timesEquals(0.5);
					fx = fxtest;
					m = mtest;					
				}
				mat_x = mat_x0.plus(mat_dx);
			}
			if (BroydenMethod.maxAbs(fx) < tol 
					&& BroydenMethod.maxAbs(mat_dx.getColumnPackedCopy()) < tol) {
				break;
			}
			if (true){
				mat_J = BroydenMethod.fjac(f, mat_x.getColumnPackedCopy());
				if(mat_J.det()!=0)
					mat_J = mat_J.inverse();
				else{
					break;
				}
					
			}

			else {
				Matrix tmp = mat_J.times(new Matrix(fx, fx.length).minus(mat_x0));
				mat_J.plusEquals(mat_dx.minus(tmp).times(mat_dx.transpose()).times(mat_J).arrayRightDivide(mat_dx.transpose().times(tmp)));
			}
			mat_dx = mat_J.times(mat_fx0).times(-1);
			x0 = mat_x.getColumnPackedCopy();
			mat_x0 = mat_x.copy();
			fx0 = fx;
			mat_fx0 = new Matrix(fx0, fx0.length);
			m0 = m;
		}
		return fx;
	}
	
	public static Matrix fjac(Function f, double[] x) {
		double[] fx = f.cals(x);
		int n = fx.length;
		int d = x.length;
		
		double[] x_abs = new double [d];
		for (int i = 0; i < x_abs.length; i++) {
			x_abs[i] = Math.abs(x[i]);
		}
		
		Matrix mat_x = new Matrix(x, d);
		Matrix mat_x_abs = new Matrix(x_abs, d);
		
		Matrix mat_h = mat_x_abs.times(sqrt_eps);
		Matrix mat_xh = mat_x.plus(mat_h);
		mat_h = mat_xh.minus(mat_x);
		
		double[][] gT = new double[d][n];
		double[] tmp = new double[d];
		System.arraycopy(x, 0, tmp, 0, d);
		for (int i = 0; i < gT.length; i++) {
			tmp[i] = mat_xh.get(i, 0);
			gT[i] = f.cals(tmp);
			tmp[i] = x[i];
		}
		Matrix mat_g = new Matrix(gT);
		mat_g = mat_g.transpose();
		
		double[][] fx_extend = new double[n][d];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < d; j++) {
				fx_extend[i][j] = fx[i];
			}
		}
		Matrix mat_fx_extend = new Matrix(fx_extend);
		
		double[][] tmp2 = new double[n][d];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < d; j++) {
				tmp2[i][j] = mat_h.get(j, 0);
			}
		}
		Matrix mat_tmp2 = new Matrix(tmp2);
		
		return mat_g.minusEquals(mat_fx_extend).arrayRightDivideEquals(mat_tmp2);
	}

	public static double modSquare(double[] args) {
		double sum = 0;
		for (int i = 0; i < args.length; i++) {
			sum += args[i]*args[i];
		}
		return sum;
	}
	
	public static double maxAbs(double[] args) {
		if (args.length < 1) {
			return Double.NaN;
		}
		double ans = Math.abs(args[0]);
		for (int i = 0; i < args.length; i++) {
			ans = ans<Math.abs(args[i]) ? Math.abs(args[i]) : ans;
		}
		return ans;
	}
	
	public static void main(String[] args) {
		Matrix matrix = new Matrix(new double[] {1,2,3,-4},4);
		System.out.println(matrix.norm2());
	}

}