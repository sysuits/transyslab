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

import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import com.transyslab.commons.tools.optimizer.DifferentialEvolution;
import com.transyslab.commons.tools.optimizer.DominanceComparator;
import com.transyslab.simcore.mlp.ExpSwitch;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.Arrays;

public class OptmKSProblemIdvd {
    public static void main(String[] args) {
        //重现校准过程
        //JMetalRandom.getInstance().setSeed(1806);
        int popSize = 20;
        int maxGeneration = 1000;
        double crossOver_cr = 0.5;
        double crossOver_f = 0.5;
        String crossOver_variant = "rand/1/bin";
        String simMasterFileName = "src/main/resources/optmksidvd.properties";

        SimProblem problem = new KSIdvdProblem(simMasterFileName);
        DifferentialEvolution algorithm;
        DifferentialEvolutionSelection selection = new DifferentialEvolutionSelection();
        DifferentialEvolutionCrossover crossover = new DifferentialEvolutionCrossover(crossOver_cr, crossOver_f, crossOver_variant) ;

        algorithm = new DifferentialEvolution(problem,maxGeneration*popSize,popSize,
                crossover,selection,new SequentialSolutionListEvaluator<>());
//        algorithm.setComparator(new DominanceComparator<>());
        algorithm.setSolutionWriter(new TXTUtils("src/main/resources/ksIdvd.csv"));
        algorithm.run();
        SimSolution bestSolution = (SimSolution) algorithm.getResult();
        System.out.println("BestFitness: " + Arrays.toString(bestSolution.getObjectiveValues()));
        System.out.println("BestSolution: " + Arrays.toString(bestSolution.getInputVariables()));
        System.out.println("SimSeed: " + bestSolution.getAttribute("SimSeed"));
        System.out.println("AlgSeed: " + JMetalRandom.getInstance().getSeed());
        problem.closeProblem();
    }
}

