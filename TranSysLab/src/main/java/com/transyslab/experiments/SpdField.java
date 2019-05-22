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

package com.transyslab.experiments;

import com.transyslab.simcore.mlp.*;


public class SpdField {

    public static void addLoop(MLPLink mlpLink, double distanceInterval) {
        MLPNetwork mlpNetwork = (MLPNetwork) mlpLink.getNetwork();
        double currentP = 0;
        double lnkLen = mlpLink.length();
        int n = 0;
        while (currentP <= lnkLen) {
            mlpNetwork.setLoopsOnLink(String.valueOf(n), mlpLink.getId(), currentP / lnkLen);
            currentP += distanceInterval;
            n += 1;
        }
        mlpNetwork.setLoopsOnLink("det_" + mlpLink.getId() + "_" + n,
                mlpLink.getId(),
                1.0);
    }

    public static void main(String[] args) {
        //�ƶ�·��+��ȡ�ļ�
        MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/�ٶȳ�����.properties");
        mlpEngine.loadFiles();

        //���в�������
        mlpEngine.seedFixed = true;//ǿ��
        mlpEngine.runningSeed = 1500613842660l;

        //����·��������Ȧ
        SpdField.addLoop(mlpEngine.getNetwork().findLink(111), 50);
        SpdField.addLoop(mlpEngine.getNetwork().findLink(112), 50);
        SpdField.addLoop(mlpEngine.getNetwork().findLink(26), 50);
        SpdField.addLoop(mlpEngine.getNetwork().findLink(113), 50);
        SpdField.addLoop(mlpEngine.getNetwork().findLink(25), 50);

        //��ʼ����ʱ��
        long t_begin = System.currentTimeMillis();

        //��������
        mlpEngine.repeatRun();

        //ͳ�Ʒ���
        System.out.println("δ����������" + mlpEngine.countOnHoldVeh() + "��");
        System.out.println("time " + (System.currentTimeMillis()-t_begin) + " ms");

        //�ر�����
        mlpEngine.close();
    }
}
