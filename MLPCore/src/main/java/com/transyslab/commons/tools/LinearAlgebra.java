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

public class LinearAlgebra {
    public static double[][] minus(double[][]a, double[][]b){
        Matrix ma = new Matrix(a);
        Matrix mb = new Matrix(b);
        return ma.minus(mb).getArray();
    }
    public static double[] minus(double[]a, double[]b){
        Matrix va = new Matrix(a,1);
        Matrix vb = new Matrix(b,1);
        return va.minus(vb).getArray()[0];
    }
    public static double[][] times(double[][]a, double[][]b){
        Matrix ma = new Matrix(a);
        Matrix mb = new Matrix(b);
        return ma.times(mb).getArray();
    }
    public static double[] times(double[]a, double b){
        Matrix ma = new Matrix(a,1);
        return ma.times(b).getArray()[0];
    }
    public static double[] plus(double[]a, double[]b){
        Matrix ma = new Matrix(a,1);
        Matrix mb = new Matrix(b,1);
        return ma.plus(mb).getArray()[0];
    }
    public static double[][] transpose(double[][] M) {
        double[][] tM = new double[M[0].length][M.length];

        for(int i = 0; i < tM.length; ++i) {
            for(int j = 0; j < tM[0].length; ++j) {
                tM[i][j] = M[j][i];
            }
        }

        return tM;
    }
    public static int rank(double[][] a){
        return new Matrix(a).rank();
    }
    public static double[][] solve(double[][]coef, double[][]res){
        Matrix m_coef = new Matrix(coef);
        Matrix m_res = new Matrix(res);
        return m_coef.solve(m_res).getArray();
    }
    public static double[] divide(double[] v1, double v) {
        double[] array = new double[v1.length];

        for(int i = 0; i < v1.length; ++i) {
            array[i] = v1[i] / v;
        }
        return array;
    }
}
