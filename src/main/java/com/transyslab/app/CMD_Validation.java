package com.transyslab.app;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import org.apache.commons.configuration2.Configuration;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.ProblemUtils;

import java.util.Arrays;

public class CMD_Validation {
    public static void main(String[] args) {
        String masterFileName = args[0];
        System.out.println("using: " + masterFileName.substring(masterFileName.lastIndexOf('/') + 1));
        Configuration config = ConfigUtils.createConfig(masterFileName);
        String problemName = config.getString("problemName");
        System.out.println("problem validation: " + problemName);

        SimProblem problem = (SimProblem) ProblemUtils.<DoubleSolution> loadProblem(problemName);
        problem.initWithoutEngThread(masterFileName);
        String solutionStr = config.getString("solution");

        for (int i = 0; i < 1; i++) {
            SimSolution solution = problem.validate(solutionStr);
            System.out.println("result: " + Arrays.toString(solution.getObjectiveValues()));
        }
    }
}
