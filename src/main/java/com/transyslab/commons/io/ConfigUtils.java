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

package com.transyslab.commons.io;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.net.URL;

public class ConfigUtils {
	public static Configuration createConfig(String fileName) {
		try {
			return new Configurations().properties(fileName);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Configuration createConfig(File inputFile) {
		try {
			return new Configurations().properties(inputFile);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
