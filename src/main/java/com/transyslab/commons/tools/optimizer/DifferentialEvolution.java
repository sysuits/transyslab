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

package com.transyslab.commons.tools.optimizer;

import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.adapter.SimProblem;
import com.transyslab.commons.tools.adapter.SimSolution;
import org.uma.jmetal.algorithm.impl.AbstractDifferentialEvolution;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class DifferentialEvolution extends AbstractDifferentialEvolution<DoubleSolution> implements Runnable{
	public static final int BROADCAST = 0;
	public static final int END = 1;
	private int populationSize;
	private int maxEvaluations;
	private SolutionListEvaluator<DoubleSolution> evaluator;
	private Comparator<DoubleSolution> comparator;
	private int evaluations;
	private TXTUtils solutionWriter;
	private boolean needOutput;
	private boolean stopSignal;
	private EventListenerList listenerList;
	/**
	 * Constructor
	 *
	 * @param problem           Problem to solve
	 * @param maxEvaluations    Maximum number of evaluations to perform
	 * @param populationSize
	 * @param crossoverOperator
	 * @param selectionOperator
	 * @param evaluator
	 */
	public DifferentialEvolution(DoubleProblem problem, int maxEvaluations, int populationSize, DifferentialEvolutionCrossover crossoverOperator, DifferentialEvolutionSelection selectionOperator, SolutionListEvaluator<DoubleSolution> evaluator) {
		setProblem(problem);
		this.maxEvaluations = maxEvaluations;
		this.populationSize = populationSize;
		this.crossoverOperator = crossoverOperator;
		this.selectionOperator = selectionOperator;
		this.evaluator = evaluator;
		comparator = new ObjectiveComparator<DoubleSolution>(0);
		listenerList = new EventListenerList();
	}
	public void setComparator(Comparator<DoubleSolution> comparator){
		this.comparator = comparator;
	}
	public int getEvaluations() {
		return evaluations;
	}

	public void setEvaluations(int evaluations) {
		this.evaluations = evaluations;
	}
	public void setSolutionWriter(TXTUtils writer){
		needOutput = true;
		solutionWriter = writer;
	}
	@Override protected void initProgress() {
		evaluations = populationSize;
	}

	@Override protected void updateProgress() {
		evaluations += populationSize;
	}

	@Override protected boolean isStoppingConditionReached() {
		return (evaluations >= maxEvaluations || stopSignal);
	}

	@Override protected List<DoubleSolution> createInitialPopulation() {
		List<DoubleSolution> population = new ArrayList<>(populationSize);
		for (int i = 0; i < populationSize; i++) {
			DoubleSolution newIndividual = getProblem().createSolution();
			population.add(newIndividual);
		}
		return population;
	}

	@Override protected List<DoubleSolution> evaluatePopulation(List<DoubleSolution> population) {
		return evaluator.evaluate(population, getProblem());
	}

	@Override protected List<DoubleSolution> selection(List<DoubleSolution> population) {
		return population;
	}

	@Override protected List<DoubleSolution> reproduction(List<DoubleSolution> matingPopulation) {
		List<DoubleSolution> offspringPopulation = new ArrayList<>();

		for (int i = 0; i < populationSize; i++) {
			selectionOperator.setIndex(i);
			List<DoubleSolution> parents = selectionOperator.execute(matingPopulation);

			crossoverOperator.setCurrentSolution(matingPopulation.get(i));
			List<DoubleSolution> children = crossoverOperator.execute(parents);

			offspringPopulation.add(children.get(0));
		}

		return offspringPopulation;
	}

	@Override protected List<DoubleSolution> replacement(List<DoubleSolution> population,
														 List<DoubleSolution> offspringPopulation) {
		List<DoubleSolution> pop = new ArrayList<>();

		for (int i = 0; i < populationSize; i++) {
			if (comparator.compare(population.get(i), offspringPopulation.get(i)) < 0) {
				pop.add(population.get(i));
			} else {
				pop.add(offspringPopulation.get(i));
			}
		}

		Collections.sort(pop, comparator) ;
		if(needOutput){
			for (int i = 0; i < populationSize; i++) {
				SimSolution solution = ((SimSolution)pop.get(i));
				String str = evaluations/populationSize + "," + Arrays.toString(solution.getInputVariables())
						.replace(" ","")
						.replace("[","")
						.replace("]","") + "," +
						Arrays.toString(solution.getObjectiveValues()).replace(" ","")
								.replace("[","")
								.replace("]","")+ "\r\n";
				solutionWriter.writeNFlush(str);
				informListeners(new ActionEvent(this,BROADCAST,str));
			}

		}
		return pop;
	}

	/**
	 * Returns the best individual
	 */
	@Override public DoubleSolution getResult() {
		Collections.sort(getPopulation(), comparator) ;

		return getPopulation().get(0);
	}

	@Override public String getName() {
		return "DEAlgorithm" ;
	}

	@Override public String getDescription() {
		return "Differential Evolution Algorithm" ;
	}

	public void shutdown() {
		stopSignal = true;
	}

	public String getStopInfo() {
		SimSolution bestSolution = (SimSolution) getResult();
		String ans = "";
		ans += "BestFitness: " + Arrays.toString(bestSolution.getObjectiveValues()) + "\n";
		ans += "BestSolution: " + Arrays.toString(bestSolution.getInputVariables()) + "\n";
		ans += "SimSeed: " + bestSolution.getAttribute("SimSeed") + "\n";
		ans += "AlgSeed: " + JMetalRandom.getInstance().getSeed();
		return ans;
	}

	@Override
	public void run() {
		super.run();
		if (problem instanceof SimProblem)
			((SimProblem)problem).closeProblem();
		informListeners(new ActionEvent(this,END,""));
	}

	public void addAlgListener(ActionListener listener) {
		listenerList.add(ActionListener.class, listener);
	}

	public void removeAlgListener(ActionListener listener) {
		listenerList.remove(ActionListener.class, listener);
	}

	public void informListeners(ActionEvent e) {
		Object actionListeners[] = listenerList.getListeners(ActionListener.class);
		for (Object listener : actionListeners) {
			((ActionListener) listener).actionPerformed(e);
		}
	}
}
