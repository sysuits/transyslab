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

import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.SchedulerThread;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPLink;
import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ODEngThread extends EngThread {

	double periodEndTime;

	public ODEngThread(String thread_name, TaskCenter task_center, String masterFileDir) {
		super(thread_name, masterFileDir, task_center);
	}

	//worker�̵߳�fitness����
	@Override
	public double[] worksWith(Task task) {
		MLPEngine mlpEngine = (MLPEngine) getEngine();

		//����������Լ�ͳ�ƽ��������״̬����
		mlpEngine.getNetwork().clearInflows();
		mlpEngine.getNetwork().clearSecStat();
		mlpEngine.getNetwork().clearLinkStat();

		//���������������ʱ��+OD
		processParas(task.getInputVariables());

		//�ⲿ����simulationLoop��ִ��
		double now = mlpEngine.getSimClock().getCurrentTime();
		while (now <= periodEndTime) {
			mlpEngine.simulationLoop();
			now = mlpEngine.getSimClock().getCurrentTime();
		}

		//TODO: ���fitness��� ����ʱ�����ͳ�Ƽ����û�����
		List<MacroCharacter> records = mlpEngine.getNetwork().getSecStatRecords("det2");
		return records==null ? null : records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
	}

	@Override
	public void onDismiss() {
		((MLPEngine) getEngine()).close();
	}

	public void processParas(double[] paras) {

		//����������ʽ
		//paras = {periodEndTime, fLinkId_1, tLinkId_1, demand_1, ... fLinkId_n, tLinkId_n, demand_n }

		MLPNetwork mlpNetwork = ((MLPEngine) getEngine()).getNetwork();
		double[] speed = {15, 2, 20};//�ѱ궨Ĭ��ֵ
		double[] time = {periodEndTime, paras[0]};
		periodEndTime = paras[0];
		for (int i = 1; i < paras.length - 2; i++) {
			MLPLink launchLink = mlpNetwork.findLink((int)paras[i]);
			List<Lane> lanes = launchLink.getStartSegment().getLanes();
			launchLink.generateInflow((int) paras[i+2], speed, time, lanes, (int) paras[i+1]);
		}
	}

	public static void main(String[] args) {
		//ʵ����TC
		TaskCenter taskCenter = new TaskCenter();

		//TODO: ����߼����������齫�ⲿ�ִ����ƶ���ODEstimator extends Scheduler�����ֻ��Ϊ��ʾ
		//����ʵ����̣�ʵ���������������߳�
		SchedulerThread scheduler = new SchedulerThread("scheduler",taskCenter) {
			//ʵ����̵Ķ���
			@Override
			public void run() {
				List<Task> taskList = new ArrayList<>();

				//TODO: OD���ƹ���
				//��һ�׶�
				taskList.clear();
				//�ɷ�����
				taskList.add(dispatch(new double[]{60, 162, 162, 60}, "Eng1"));
				taskList.add(dispatch(new double[]{60, 162, 162, 60}, "Eng2"));
				taskList.add(dispatch(new double[]{60, 162, 162, 60}, "Eng3"));
				//ȡ�ؽ��
				for (int i = 0; i < 3; i++) {
					System.out.println(Arrays.toString(taskList.get(i).getObjectiveValues()));
				}
				//�ڶ��׶�
				taskList.clear();
				taskList.add(dispatch(new double[]{120, 162, 162, 60}, "Eng1"));
				taskList.add(dispatch(new double[]{120, 162, 162, 60}, "Eng2"));
				taskList.add(dispatch(new double[]{120, 162, 162, 60}, "Eng3"));
				for (int i = 0; i < 3; i++) {
					System.out.println(Arrays.toString(taskList.get(i).getObjectiveValues()));
				}

				//��ɢ���й����߳�
				dismissAllWorkingThreads();
			}
		};
		scheduler.start();

		//ʵ���������������߳�
		//TODO: ��ʵ����Ҫȷ���������߳�������
		for (int i = 0; i < 4; i++) {
			new ODEngThread("Eng" + (i+1), taskCenter, "src/main/resources/demo_neihuan/scenario2/neverEnd.properties")
					.setTaskSpecified()
					.start();
		}
	}
}
