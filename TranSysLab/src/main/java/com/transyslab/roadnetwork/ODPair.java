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

import com.transyslab.simcore.mlp.MLPLink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ODPair implements NetworkObject{
	protected long id;
	protected String name;
	protected Node oriNode;
	protected Node desNode;
	protected List<Path> paths;
	protected double[] splits; // probabilities to choose each getPath
	protected static int nSplits; // num of splits parsed so far for current odpair
	protected boolean isSelected;

	public ODPair() {
		paths = new ArrayList<>();
	}
	public ODPair(Node o, Node d) {
		oriNode = o;
		desNode = d;
		paths = new ArrayList<>();
	}
	public int setSplit(float split) {
		int n = nPaths();
		if (nSplits >= n) {
			return -1; // too many splits
		}
		if (splits == null) {
			splits = new double[n];
		}
		else {
			split += splits[nSplits - 1];
		}

		if (split < 0.0)
			return -2;
		if (split > 1.0)
			return -3;

		splits[nSplits] = split;
		nSplits++;

		return 0;
	}
	public int nSplits() {
		return nSplits;
	}
	public double split(int i) {
		return splits[i];
	}
	public double[] splits() {
		return splits;
	}
	public int nPaths(){
		return paths.size();
	}
	public long getId(){
		return this.id;
	}
	public String getObjInfo(){
		return this.name;
	}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public Node getOriNode() {
		return oriNode;
	}
	public Node getDesNode() {
		return desNode;
	}
	public Path getPath(int i){
		return paths.get(i);
	}
	public List<Path> getPaths() {
		return paths;
	}
	public void addPath(Path p){
		if (checkTurning(p))
			paths.add(p);
	}
	public Path findPathAccd2Id(int pathId) {
		for(Path tmpPath:paths){
			if (tmpPath.id == pathId) {
				return tmpPath;
			}
		}
		return null;
	}
	public Path findPathAccd2Index(int index) {
		if(index>=paths.size())
			return null;
		else
			return paths.get(index);
	}
	public Path chooseRoute(Vehicle pv, RoadNetwork network) {
		double r = network.getSysRand().nextDouble();
		int n = nPaths();
		int i;
		for (n = n - 1, i = 0; i < n && r > splits[i]; i++);
		return paths.get(i);
	}
	public List<Path> verifyPath(Vehicle veh) {
		//todo 检查path可行性
		if (veh.getLink()==null)
			return paths;
		//make sure path begins with the link on which the vehicle is running
		return paths.stream()
				.filter(p->p.getLink(0).getId()==veh.getLink().getId())
				.collect(Collectors.toList());
	}
	public Path assignRoute(Vehicle veh, double rand) {
		//TODO 路径选择行为：可以考虑放在Vehicle类中
		List<Path> paths = verifyPath(veh);
		if (paths.size()==0)
			return null;
		return paths.get(0);
//		if (paths.size()==1)
//			return paths.get(0);
//		double[] pathLength = paths.stream()
//				.mapToDouble(p->p.links.stream()
//						.mapToDouble(l->l.length()*((double)p.links.size()*0.1+1.0)).sum())
//				.toArray();
//		double sum = Arrays.stream(pathLength).sum();
//		int n = pathLength.length,i;
//		for (n = n - 1, i = 0; i < n && rand*sum > splits[i]; i++);
//		return paths.get(i);
	}
	public static boolean checkTurning(Path p){
		if (p.nLinks()<=1)
			return true;
		for (int i = 0; i < p.nLinks()-1; i++) {
			if (!((MLPLink)p.getLink(i)).checkTurningTo(p.getLink(i+1).getId()))
				return false;
		}
		return true;
	}
}
