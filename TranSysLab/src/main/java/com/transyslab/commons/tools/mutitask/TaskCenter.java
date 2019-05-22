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

package com.transyslab.commons.tools.mutitask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskCenter {
	private BlockingQueue<Task> undoneTasksQueue;
	private boolean killThreadSignal;

	public TaskCenter(int taskQueueSize) {
		undoneTasksQueue = new ArrayBlockingQueue<>(taskQueueSize);
		killThreadSignal = false;
	}

	public TaskCenter() {
		this(100);
	}

	protected synchronized boolean dismissAllowed() {
		return killThreadSignal;
	}

	protected synchronized void dismiss() {
		killThreadSignal = true;
		notify();
	}

	protected void addTask(Task task) {
		try {
			undoneTasksQueue.put(task);
//			System.out.println("DEBUG: Task added at " + LocalDateTime.now());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected Task fetchUnspecificTask() {
		try {
			return undoneTasksQueue.poll(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected synchronized Task fetchSpecificTask() {
		Task theTask = undoneTasksQueue.stream()
				.filter(t -> t.workerName.equals(Thread.currentThread().getName()))
				.findFirst().orElse(null);
		if (theTask != null) {
			undoneTasksQueue.remove(theTask);
		}
		return theTask;
	}
}
