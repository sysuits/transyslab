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
import org.uma.jmetal.util.comparator.ConstraintViolationComparator;

public class ConstrainsComparator<S extends Solution<?>> implements ConstraintViolationComparator<S> {
    @Override
    public int compare(S s1, S s2) {

        double value1 = (double)s1.getAttribute("GEH");
        double value2 = (double)s2.getAttribute("GEH");
        if(value1 == value2){
            return 1;
        }
        if(value1<5){
            if(value2<value1){
                // v2<v1<5
                return 0;
            }
            else{
                if(value2>=5){
                    //v2>5,v1<5
                    return 1;
                }
                else{
                    //v1<v2<5
                    return 0;
                }

            }
        }
        else{
            if(value2<5){
                //v2<5,v1>=5
                return -1;
            }
            else{
                if(value1>value2){
                    // v1>v2>=5
                    return -1;
                }
                else{
                    //v2>v1>=5
                    return 1;
                }
            }
        }
    }
}
