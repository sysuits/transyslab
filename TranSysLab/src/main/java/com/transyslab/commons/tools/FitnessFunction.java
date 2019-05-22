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

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class FitnessFunction {
	public static double evaRNSE(double[] sim, double[] obs){
		if(sim.length ==0 || sim.length != obs.length){
			System.out.print("Error:The length of two input arrays are not equal!");
			return Double.NaN;
		}
		double numerator = 0;
		double denominator = 0;
		for(int i=0;i<sim.length;i++){
			numerator += Math.pow(sim[i]-obs[i],2);
			denominator += obs[i];
		}
		return Math.sqrt(numerator*sim.length)/denominator;		
	}
	public static double evaMAPE(double[] sim, double[] obs){
		if (sim.length ==0 || sim.length != obs.length) {
			System.out.print("Error:The length of two input arrays are not equal!");
			return Double.NaN;
		}		
		double sum = 0.0;
		int count = 0;
		for (int i = 0; i < sim.length; i++) {
			double del = Math.abs(sim[i] - obs[i]);
			if (Math.abs(obs[i])>0.0001) {
				sum += del / obs[i];
				count += 1;
			}
		}		
		if (count > 0) 
			return (sum / count);
		else 
			return 0.0;
	}
	public static double evaRMSE(double[] sim, double[] obs){
		if (sim.length ==0 || sim.length != obs.length) {
			System.out.print("Error:The length of two input arrays are not equal!");
			return Double.NaN;
		}
		double sum = 0;
		for(int i=0;i<sim.length;i++){
			sum += (sim[i]-obs[i])*(sim[i]-obs[i]);
		}
		double result = sum/(double)sim.length;
		return Math.sqrt(result);
	}
	public static double evaKSDistance(double[] sim, double[] obs){
		return evaKS(sim,obs,true);
	}
	public static double evaKS(double[] sim, double[] obs, boolean generalize){
		Arrays.sort(sim);
		Arrays.sort(obs);
		int lenSim = sim.length;
		int lenObs = obs.length;
		double[] dataAll = ArrayUtils.addAll(obs, sim);
		double[] simECDF = new double[dataAll.length];
		double[] obsECDF = new double[dataAll.length];
		double maxDistance = 0.0;
		// 二分查找dataAll在sim中位置
		for(int i=0;i<dataAll.length;i++){
			simECDF[i] = binarySearchIndex(sim, dataAll[i]);
			obsECDF[i] = binarySearchIndex(obs, dataAll[i]);
			if (generalize) {
				simECDF[i] = simECDF[i] * 1.0 / lenSim;
				obsECDF[i] = obsECDF[i] *1.0 / lenObs;
			}
			maxDistance = Math.max(Math.abs(simECDF[i]-obsECDF[i]),maxDistance);
		}
		return maxDistance;
	}

	public static int binarySearchIndex(final double[] array,final double data){
		int mid = 0,from = 0,to = array.length-1;
		int tarid = 0;
		if(array[to]<=data)
			return to+1;
		if(array[from]>=data){
			return from+1;
		}
		while(from <= to && tarid ==0) {
			mid = from + (to - from) / 2;
			if (array[mid] < data) {
				if(mid!=array.length-1){
					if(array[mid+1]>data)
						tarid = mid+1;
					else if(array[mid+1]<data)
						from = mid + 1;
					else
						tarid = mid + 2;
				}
				else tarid = mid + 1;

			}else if(array[mid] > data) {
				to = mid - 1;
			}else {
				tarid = mid + 1;
			}
		}
		if(tarid == 0)
			System.out.println("Error:Fail to find the approprate index to insert");
		return tarid;
	}

}
