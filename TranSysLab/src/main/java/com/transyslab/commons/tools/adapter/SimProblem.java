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

package com.transyslab.commons.tools.adapter;

import com.transyslab.commons.tools.mutitask.SimulationConductor;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskGiver;
import com.transyslab.commons.tools.mutitask.EngThread;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

public abstract class SimProblem extends AbstractDoubleProblem implements TaskGiver {
    private TaskCenter taskCenter;

    public SimProblem() {
        taskCenter = new TaskCenter();
    }

    public abstract void initProblem(String masterFileName);

    @Override
    public void evaluate(DoubleSolution doubleSolution) {
        dispatch((SimSolution) doubleSolution);
    }

    @Override
    public DoubleSolution createSolution() {
        return new SimSolution(this);
    }

    @Override
    public TaskCenter getTaskCenter() {
        return taskCenter;
    }

    public void prepareEng(String masterFileDir, int numOfEngines) {
        for (int i = 0; i < numOfEngines; i++) {
            //标准引擎的初始化与参数的设置
            EngThread engThread = createEngThread("eng" + i, masterFileDir);
            engThread.setSimConductor(createConductor());
            engThread.assignTo(this);
            engThread.start();
        }

    }

    public void closeProblem() {
        dismissAllWorkingThreads();
    }

    protected abstract EngThread createEngThread(String name, String masterFileDir);

    protected abstract SimulationConductor createConductor();
}
