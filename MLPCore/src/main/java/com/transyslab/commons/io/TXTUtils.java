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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class TXTUtils {
	protected BufferedWriter writer;
	
	public TXTUtils(String filepath) {
		//establish writer
		File file = new File(filepath);
		try {
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
		} catch (Exception e) {
			System.err.println("failed to create " + filepath);
			e.getMessage();
			e.getStackTrace();
		}
	}
	
	public synchronized void writeNFlush(String str) {
		try {
			writer.write(str);
			writer.flush();
		} catch (Exception e) {
			e.getMessage();
			e.getStackTrace();
		}
	}
	public synchronized void write(String str) {
		try {
			writer.write(str);
		} catch (Exception e) {
			e.getMessage();
			e.getStackTrace();
		}
	}
	public synchronized void flushBuffer() {
		try {
			writer.flush();
		} catch (Exception e) {
			e.getMessage();
			e.getStackTrace();
		}
	}
	public void closeWriter() {
		try {
			writer.close();
		} catch (Exception e) {
			e.getMessage();
			e.getStackTrace();
		}
	}
}
