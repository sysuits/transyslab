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

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.ConstraintViolationComparator;
import org.uma.jmetal.util.comparator.impl.OverallConstraintViolationComparator;

import java.io.Serializable;
import java.util.Comparator;

public class DominanceComparator<S extends Solution<?>> implements Comparator<S>, Serializable {
	private ConstraintViolationComparator<S> constraintViolationComparator;
	private double boundedValue;
	private int slackObjIndex = -1;

	/** Constructor */
	public DominanceComparator() {
		this(new OverallConstraintViolationComparator<S>(), 0.0) ;
	}

	/** Constructor */
	public DominanceComparator(double epsilon) {
		this(new OverallConstraintViolationComparator<S>(), epsilon) ;
	}

	/** Constructor */
	public DominanceComparator(ConstraintViolationComparator<S> constraintComparator) {
		this(constraintComparator, 0.0) ;
	}

	/** Constructor */
	public DominanceComparator(ConstraintViolationComparator<S> constraintComparator, double epsilon) {
		constraintViolationComparator = constraintComparator ;
	}
	/**判断约束条件**/
	public DominanceComparator(int slackObjIndex,double boundedValue) {
		this(new OverallConstraintViolationComparator<S>(), 0.0) ;
		this.boundedValue = boundedValue;
		this.slackObjIndex = slackObjIndex;
	}
	/**
	 * Compares two solutions.
	 *
	 * @param solution1 Object representing the first <code>Solution</code>.
	 * @param solution2 Object representing the second <code>Solution</code>.
	 * @return -1, or 0, or 1 if solution1 dominates solution2, both are
	 * non-dominated, or solution1  is dominated by solution2, respectively.
	 */
	@Override
	public int compare(S solution1, S solution2) {
		if (solution1 == null) {
			throw new JMetalException("Solution1 is null") ;
		} else if (solution2 == null) {
			throw new JMetalException("Solution2 is null") ;
		} else if (solution1.getNumberOfObjectives() != solution2.getNumberOfObjectives()) {
			throw new JMetalException("Cannot compare because solution1 has " +
					solution1.getNumberOfObjectives()+ " objectives and solution2 has " +
					solution2.getNumberOfObjectives()) ;
		}
		int result ;
		if(constraintViolationComparator!=null)
			result = constraintViolationComparator.compare(solution1, solution2) ;
		else
			result = 0;
		if (result == 0) {
			result = dominanceTest(solution1, solution2) ;
		}

		return result ;
	}

	private int dominanceTest(S solution1, S solution2) {
		int bestIsOne = 0 ;
		int bestIsTwo = 0 ;
		int result ;
		for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
			// 目标函数值小于boundedValue，则不进行比较
			if(slackObjIndex == i && solution1.getObjective(i)<=boundedValue && solution2.getObjective(i)<=boundedValue){
				continue;
			}
			double value1 = solution1.getObjective(i);
			double value2 = solution2.getObjective(i);
			if (value1 != value2) {
				if (value1 < value2) {
					bestIsOne = 1;
				}
				if (value2 < value1) {
					bestIsTwo = 1;
				}
			}


		}
		if (bestIsOne > bestIsTwo) {
			result = -1;
		} else if (bestIsTwo > bestIsOne) {
			result = 1;
		} else {
			result = 0;
		}
		return result ;
	}
}
