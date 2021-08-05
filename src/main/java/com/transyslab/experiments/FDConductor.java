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

import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.mutitask.SimulationConductor;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.mlp.MacroCharacter;

import java.util.ArrayList;
import java.util.List;


public class FDConductor implements SimulationConductor {
	@Override
	public void modifyEngineBeforeStart(SimulationEngine engine, SimSolution simSolution) {
		((MLPEngine) engine).setParameter("gamma1", simSolution.getVariableValue(0));
		((MLPEngine) engine).setParameter("gamma2", simSolution.getVariableValue(1));
		((MLPEngine) engine).setParameter("lcBufferTime", simSolution.getVariableValue(2));
		((MLPEngine) engine).setParameter("lcSensitivity", simSolution.getVariableValue(3));
	}

	@Override
	public double[] evaluateFitness(SimulationEngine engine) {
		double binStart = 0.001;
		double binStep = 0.002;
		double nBins = 40;

		List<MacroCharacter> det2Sim = engine.getSimMap().get("lane18101");
		det2Sim.addAll(engine.getSimMap().get("lane18102"));
		det2Sim.addAll(engine.getSimMap().get("lane18103"));
		List<double[]> simQList = new ArrayList<>();
		for (double b = binStart; b < binStart + binStep*nBins; b += binStep) {
			double lower = b - 0.5*binStep;
			double upper = b + 0.5*binStep;
			double[] qs = det2Sim.stream()
					.filter(r -> r.getKmDensity()/1000.0 > lower && r.getKmDensity()/1000.0 <= upper)
					.mapToDouble(r -> r.getHourFlow()/3600.0)
					.toArray();
			simQList.add(qs);
		}

		List<MacroCharacter> det2Emp = engine.getEmpMap().get("det2");
		List<double[]> empQList = new ArrayList<>();
		for (double b = binStart; b < binStart + binStep*nBins; b += binStep) {
			double lower = b - 0.5*binStep;
			double upper = b + 0.5*binStep;
			double[] qs = det2Emp.stream()
					.filter(r -> r.getKmDensity()/1000.0 > lower && r.getKmDensity()/1000.0 <= upper)
					.mapToDouble(r -> r.getHourFlow()/3600.0)
					.toArray();
			empQList.add(qs);
		}

		double nSamples = empQList.stream().mapToDouble(a -> a.length).sum();
		double fitness = 0.0;
		for (int i = 0; i < nBins; i++) {
			double[] simArry = simQList.get(i);
			double[] empArry = empQList.get(i);
			if (simArry.length == 0)
				fitness += empArry.length / nSamples * 1.0;
			else if (empArry.length == 0)
				fitness += 0.0;
			else
				fitness += empArry.length / nSamples * FitnessFunction.evaKSDistance(simArry, empArry);
		}
		System.out.println(Thread.currentThread().getName() + " returned " + fitness);
		return new double[] {fitness};
	}
}
