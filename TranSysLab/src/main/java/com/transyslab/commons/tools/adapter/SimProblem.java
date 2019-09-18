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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.mutitask.EngThread;
import com.transyslab.commons.tools.mutitask.SimController;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskGiver;
import org.apache.commons.configuration2.Configuration;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

public abstract class SimProblem extends AbstractDoubleProblem implements TaskGiver {
    protected Configuration config;
    private static Injector injector;
    private TaskCenter taskCenter;

    public SimProblem() {
        taskCenter = new TaskCenter();
    }

    public void initProblem(String masterFileName){
        this.config = ConfigUtils.createConfig(masterFileName);
        int numOfEngines = config.getInt("numOfEngines");
        for (int i = 0; i < numOfEngines; i++) {
            //标准引擎的初始化与参数的设置
            EngThread engThread = createEngThread();
            engThread.config(masterFileName);
            engThread.setName("eng" + i);
            engThread.assignTo(this);
            engThread.start();
        }
    }

    protected EngThread createEngThread(){
        if (injector==null){
            injector = Guice.createInjector(getModule());
        }
        return injector.getInstance(EngThread.class);
    }

    protected abstract AbstractModule getModule();

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

    public void closeProblem() {
        dismissAllWorkingThreads();
    }

    public Configuration getConfig(){
        checkConfig();
        return config;
    }

    public void checkConfig(){
        if (config==null)
            System.err.println("this problem has no config info.");
    }
}
