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


package com.transyslab.simcore.mesots;

import com.transyslab.roadnetwork.Link;


public class MesoLink extends Link {

	protected MesoVehicle queueHead = new MesoVehicle(); // first vehicle in
															// the queue
	protected MesoVehicle queueTail = new MesoVehicle(); // last vehicle in the
															// queue
	protected int queueLength_; // number of vehicle in the queue

	public MesoLink() {
		queueHead = null;
		queueTail = null;
		queueLength_ = 0;
	}

	public MesoVehicle queueHead() {
		return queueHead;
	}
	public MesoVehicle queueTail() {
		return queueTail;
	}
	public int queueLength() {
		return queueLength_;
	}

	/*
	 * Returns the 1st traffic cell in this link
	 */
	public MesoTrafficCell firstCell() {
		return ((MesoSegment) getEndSegment()).firstCell();
	}
	/*
	 * Returns the last traffic cell in this link
	 */
	public MesoTrafficCell lastCell() {
		return ((MesoSegment) getStartSegment()).lastCell();
	}

	/*
	 * Print the number of vehicles queue for enter a link
	 */
	/*
	 * Î´´¦Àí public int MESO_Link::reportQueueLength(ostream &os) { const int
	 * MinQueueStepSize = 20; if (queueLength_ > MinQueueStepSize) { os << " ("
	 * << code_ << ":" << queueLength_ << ")"; return 1; } return 0; }
	 */

	// These two maintains the virtual queue before entering the
	// network

	public void dequeue(MesoVehicle pv) {
		if (pv.leading() != null) {
			pv.leading().trailing(pv.trailing());
		}
		else { /* first vehicle in the queue */
			queueHead = pv.trailing();
		}
		if (pv.trailing() != null) {
			pv.trailing().leading(pv.leading());
		}
		else { /* last vehicle in the queue */
			queueTail = pv.leading();
		}
		queueLength_--;
		// theStatus.nInQueue(-1);
	}

	public void queue(MesoVehicle vehicle) {
		if (queueTail != null) { /* current queue is not empty */
			queueTail.trailing(vehicle);
			vehicle.leading(queueTail);
			queueTail = vehicle;
		}
		else { /* current queue is empty */
			vehicle.leading(null);
			queueHead = queueTail = vehicle;
		}
		vehicle.trailing(null);
		queueLength_++;
		// theStatus.nInQueue(1);
	}

	public void prequeue(MesoVehicle vehicle) {
		if (queueHead != null) { /* current queue is not empty */
			queueHead.leading(vehicle);
			vehicle.trailing(queueHead);
			queueHead = vehicle;
		}
		else { /* current queue is empty */
			vehicle.trailing(null);
			queueHead = queueTail = vehicle;
		}
		vehicle.leading(null);
		queueLength_++;
		// theStatus.nInQueue(1);
	}

	// These are used in moving vehicles
	/*
	 * Move vehicles based on current cell speeds. This function is called by
	 * MESO_Node::advanceVehicles() in random permuted order.
	 */



	public void checkConnectivity() {
	}

	public int isJammed() {
		return ((MesoSegment) getStartSegment()).isJammed();
	}

	public double calcTravelTime(double currentTime) // virtual
	{
		MesoSegment ps = (MesoSegment) getStartSegment();
		MesoTrafficCell pc = new MesoTrafficCell();
		MesoVehicle pv = new MesoVehicle();
		double sum = 0.0;
		int cnt = 0;
		double tt =  travelTime();

		// Vehicles already on the link

		while (ps != null) {
			pc = firstCell();
			while (pc != null) {
				pv = pc.firstVehicle();
				while (pv != null) {
					double pos = pv.distanceFromDownNode();
					sum += pv.timeInLink(currentTime) + pos / length() * tt;
					cnt++;
					pv = pv.trailing();
				}
				pc = pc.trailing();
			}
			ps = ps.getDnStream();
		}

		// Vehicles in pretrip queue

		pv = queueHead;
		while (pv != null) {
			sum += pv.timeInLink(currentTime) + tt * 1.25;
			cnt++;
			pv = pv.trailing();
		}

		if (cnt != 0) {
			return (sum / cnt);
		}
		else {
			return tt;
		}
	}
}
