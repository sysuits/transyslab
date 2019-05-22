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


package com.transyslab.simcore.mesots;

import java.util.HashMap;
import java.util.Vector;

import com.transyslab.roadnetwork.RoadNetworkPool;


public class MesoRandom {
	protected static int flags = 0;
	protected int signature;
	// c++ long 对应4字节,修改randomize，setSeed方法
	protected int seed;
	protected static int counter = 0;
	public final static int Misc = 0;
	public final static int Departure = 1;
	public final static int Routing = 2;
	public final static int Behavior = 3;

	// create random numbers
	public static MesoRandom[] create(int n){
		MesoRandom[] theRandoms = new MesoRandom[n];
		for (int i = 0; i < n; i++) {
			theRandoms[i] = new MesoRandom();
			theRandoms[i].randomize();
		}
		return theRandoms;
	}

	public MesoRandom() {
		signature = counter;
		counter++;
		seed = 0;
	}

	public static int getFlags() {
		return flags;
	}
	public static void setFlags(int s) {
		flags = s;
	}

	public int getSeed() {
		return seed;
	}
	public void setSeed(int s) {
		seed = s;
	}
	public void resetSeed(){
		seed = 1468288583;
	}
	// Set random seed
	public int randomize() {
		int s = 0xFF << (signature * 8);
		if (!((seed = (flags & s)) > 0)) {
			long ct = System.currentTimeMillis();
			// long E9 = MyMath.myPow(10, 9);
			// 取long型后九位
			// seed = (int) (ct%(ct/E9*E9));
			// seed = (int) System.currentTimeMillis();
			// 校对输出结果
			seed = 1468288583;
		}
		return seed;
	}

	// uniform (0, 1]

	public double urandom() {
		// Constants for linear congruential random number generator.
		final int M = 2147483647; // M = modulus (2^31)
		final int A = 48271; // A = multiplier (was 16807)
		final int Q = M / A;
		final int R = M % A;

		seed = A * (seed % Q) - R * (seed / Q);
		seed = (seed > 0) ? (seed) : (seed + M);
//		System.out.println((double) seed / (double) M);
		return (double) seed / (double) M;
	}

	// uniform [0, n)

	public int urandom(int n) {
		return ((int) (urandom() * n));
	}

	// uniform (a, b]

	public double urandom(double a, double b) {
		return a + (b - a) * urandom();
	}

	// returns 1 with probability p

	public int brandom(double prob) {
		if (urandom() < prob)
			return (1);
		else
			return 0;
	}
	public int brandom(float prob) {
		if (urandom() < prob)
			return (1);
		else
			return 0;
	}

	// exponential with parameter r

	public double erandom(double lambda) {
		return -Math.log(urandom()) / lambda;
	}
	public double rrandom(double one_by_lambda) {
		return -Math.log(urandom()) * one_by_lambda;
	}

	// normal with mean m and stddev v

	public double nrandom() {
		double r1 = urandom(), r2 = urandom();
		double r = -2.0 * Math.log(r1);
		if (r > 0.0)
			return Math.sqrt(r) * Math.sin(2 * Math.PI * r2);
		else
			return 0.0;
	}
	public double nrandom_trunc(double r) {
		double x = nrandom();
		if (x >= -r && x < r)
			return x;
		else
			return urandom(-r, r);
	}
	public double nrandom(double mean, double stddev) {
		double r1 = urandom(), r2 = urandom();
		double r = -2.0 * Math.log(r1);
		if (r > 0.0)
			return (mean + stddev * Math.sqrt(r) * Math.sin(2 * Math.PI * r2));
		else
			return (mean);
	}
	public double nrandom_trunc(double mean, double stddev, double r) {
		double x = nrandom(mean, stddev);
		double dx = r * stddev;
		if (x >= mean - dx && x <= mean + dx) {
			return x;
		}
		else {
			return mean + urandom(-dx, dx);
		}
	}

	// discrete random number in [0, n) with given CDF

	public int drandom(int n, double cdf[]) {
		int i;
		double r = urandom();
		for (n = n - 1, i = 0; i < n && r > cdf[i]; i++);
		return i;
	}

	public int drandom(int n, float cdf[]) {
		int i;
		double r = urandom();
		for (n = n - 1, i = 0; i < n && r > cdf[i]; i++);
		return (i);
	}

	// randomly permute an array

	// Given as input the numbers: 0..N-1 it returns a random permutation
	// of those numbers This is achieved in a single pass
	public void permute(int n, int perm[]) {
		int i;
		int r, tmp;

		for (i = 0; i < n; i++)
			perm[i] = i;

		for (i = n - 1; i >= 0; i--) {
			r = urandom(i);
			tmp = perm[i];
			perm[i] = perm[r];
			perm[r] = tmp;
		}
	}

}

