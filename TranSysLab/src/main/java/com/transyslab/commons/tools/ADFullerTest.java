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

import java.util.HashMap;


public class ADFullerTest {
	private double pValue;
	private String autoLag;
	private int maxLag;
	private double[] timeSeries;
	private final HashMap<String, Integer> regression = new HashMap<>();
	public enum RegressionModel{
		NC, C, CT, CTT
	}
	/*
	public ADFullerTestPy(double[] timeSeries){
		this.timeSeries = new double[timeSeries.length];
		System.arraycopy(timeSeries, 0, this.timeSeries, 0, timeSeries.length);
	}*/
	public static boolean stationaryTest(double[] timeSeries){
		int nObs = timeSeries.length;
		double[] cpTimeSeries = new double[nObs];
		System.arraycopy(timeSeries, 0, cpTimeSeries, 0, nObs);
		// 1. 选择regressionmodel，检查是否在集合内
		// 2. Maximum lag which is included in test, default 12*(nobs/100)^{1/4}
		// 3. 一阶差分
		int maxLag = (int) Math.ceil(12 * Math.pow(nObs/100.0, 1/4.0));
		double[] tsDiff = seriesDiff(cpTimeSeries, 1);
		int nTsDiff = tsDiff.length;
		double[][] tsAll = lagMatrix(tsDiff,maxLag,"both");
		int tmpObs = nObs;
		nObs = tsAll.length;
		for(int i=0;i<nObs;i++){
			tsAll[i][0] = timeSeries[(tmpObs-nObs-1) + i];
		}
		double[] tsShort = new double[nObs];
		for(int i=0;i<nObs;i++){
			tsShort[i] = tsDiff[(nTsDiff-nObs)+i];
		}
		// if autolag = 'AIC','BIC',''...
		// if regression != 'nc'

		// else
		return false;

	}
	public static double[] seriesDiff(double[] x, int nOrder){
		double[] result;
		if(nOrder == 0){
			result = new double[x.length];
			System.arraycopy(x, 0, result, 0, x.length);
			return result;
		}
		if(nOrder < 0)
			System.out.println("order must be non-negative but got" + String.valueOf(nOrder));
		result = new double[x.length-1];
		// 被减数的索引从0至length-2
		System.arraycopy(x, 0, result, 0, x.length -1);
		for(int i=0; i < x.length-1; i++){
			result[i] = x[i+1] - result[i];
		}
		if(nOrder > 1)
			return seriesDiff(result, nOrder-1);
		else
			return result;
	}
	public static double[][] lagMatrix(double[] x, int maxLag, String trim){
		int nObs = x.length;
		// 只有一个变量
		int nVar = 1;
		if(maxLag >= nObs)
			System.out.println("maxlag should be < nobs");
		// lagmatrix
		double[][] lm = new double[nObs+maxLag][nVar*(maxLag+1)];
		/*double[] cpx = new double[nObs];
		System.arraycopy(x, 0, cpx, 0, nObs);*/
		for(int j=0;j<nVar*(maxLag+1);j++){
			for(int i=j;i<nObs+j;i++){
				lm[i][j] = x[i];
			}
		}
		int startObs,stopObs;
		switch (trim) {
			case "none":
				startObs = 0;
				stopObs = nObs+maxLag;
				break;
			case "forward":
				startObs = 0;
				stopObs = nObs;
				break;
			case "both":
				startObs = maxLag;
				stopObs = nObs;
				break;
			case "backward":
				startObs = maxLag;
				stopObs = nObs+maxLag;
				break;
			default:
				System.out.println("trim option not valid");
				return null;
		}
		double[][] lmResult = new double[stopObs - startObs][nVar*(maxLag+1)];
		for(int j=0;j<nVar*(maxLag+1);j++){
			for(int i=startObs;i<stopObs-startObs;i++){
				lmResult[i-startObs][j] = lm[i][j];
			}
		}
		return lmResult;
	}
	public double[][] addTrend(double[][] x, String regression, String trend/*prepend = true*/){
		double[][] result = null;
		switch (trend) {
			case "c":
				result = addConstant(x);
				break;

			default:
				break;
		}
		return result;
	}
	// 矩阵最左加一列全为1的向量
	public double[][] addConstant(double[][] x){
		double[][] ans = new double[x[0].length+1][x.length];
		for (int i = 0; i < ans.length; i++) {
			for (int j = 0; j < ans[0].length; j++) {
				ans[i][j]= i==0 ? 1 : x[i-1][j];
			}
		}
		return ans;
	}
}
