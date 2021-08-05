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


package com.transyslab.roadnetwork;

/**
 * Label
 *
 */
public class Label {
	protected int id;
	protected String name;
	protected static int sorted;
	protected int length;

	public Label() {
		length = 0;
	}
	public int getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public int getLength() {
		return length;
	}
	public static int sorted() {
		return sorted;
	}
	public int init(int id, String name) {
		if (id == 0) {
			// cerr << "Error:: Label code <0> is not allowed. ";
			return -1;
		}
		this.id = id;
		this.name = name;
		length = getName() != null ? getName().length() : 0;


		return 0;
	}

}
