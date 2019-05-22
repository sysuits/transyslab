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

import java.util.ArrayList;
import java.util.List;



public class Node implements NetworkObject {
	protected long id;
	protected String name;
	// Node数组中的索引
	protected int index;
	// node的类型
	protected int type;
	// 存储终点数组的的索引（当type是NODE_TYPE_DES才有效）
	protected int destIndex;
	// inbound link
	protected List<Link> upLinks;
	// outbound link
	protected List<Link> dnLinks;

	protected Intersection interS;

	protected int state;

	protected String objInfo;

	protected boolean isSelected;

	private List<SignalPlan> signalPlans;

	private GeoSurface boundBox;

	private GeoPoint posPoint;

	public Node() {
		this.interS = null;
		this.state = 0;
		this.upLinks = new ArrayList<>();
		this.dnLinks = new ArrayList<>();
		signalPlans = new ArrayList<>();
	}
	public List<SignalPlan> getSignalPlans(){
		return this.signalPlans;
	}
	public int type(int flag) {
		return type & flag;
	}
	public void setType(int t) {
		type = t;
	}
	public int getType() {
		return type;
	}
	public int odType() {
		return type & Constants.NODE_TYPE_OD;
	}
	public void setState(int s) {
		state |= s;
	}
	public void unsetState(int s) {
		state &= ~s;
	}
	public void setInterS(Intersection intersection) {
		interS = intersection;
	}
	public long getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public String getObjInfo(){return this.objInfo;}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public Intersection getInterS() {
		return interS;
	}
	public int nUpLinks() {
		return upLinks.size();
	}
	public Link getUpLink(int i) {
		return upLinks.get(i);
	}
	public int nDnLinks() {
		return dnLinks.size();
	}

	public Link getDnLink(int i) {
		return dnLinks.get(i);
	}

	public void addUpLink(Link link) {
		upLinks.add(link);
	}
	public void addDnLink(Link link) {
		dnLinks.add(link);
	}
	public void rmUpLink(Link link){
		upLinks.remove(link);
	}
	public void rmDnLink(Link link){
		dnLinks.remove(link);
	}
	public GeoPoint getPosPoint(){
		return this.posPoint;
	}
	public GeoSurface getBoundBox(){
		return this.boundBox;
	}
	public void init(long id, int type, int index,String name, GeoPoint posPoint){
		this.id = id;
		this.type = type;
		this.name = name;
		this.index = index;
		this.objInfo = name;
		this.posPoint =  posPoint;
	}
	// Return local index of an inbound link or -1 if 'link' is not a
	// upstream link of this node
	public int whichUpLink(Link link) {
		int i;
		for (i = nUpLinks() - 1; i >= 0; i--) {
			if (upLinks.get(i) == link)
				break;
		}
		return i;
	}
	// Return local index of an outbound link or -1 if 'link' is not a
	// downstream link of this node
	public int whichDnLink(Link link) {
		int i;
		for (i = nDnLinks() - 1; i >= 0; i--) {
			if (dnLinks.get(i) == link)
				break;
		}
		return i;
	}
	// Sort outbound links based on their directions. Direction is
	// represebted by the angle counter-clockwise, with 3 O'clock
	// being 0.
	public void sortUpLinks() {
		int i, j;
		Link lastarc;
		Link toparc;
		for (i = nUpLinks() - 1; i > 0; i--) {
			lastarc = upLinks.get(i);
			for (j = i - 1; j >= 0; j--) {
				toparc = upLinks.get(j);
				if (toparc.inboundAngle() > lastarc.inboundAngle()) {
					upLinks.set(i, toparc);
					upLinks.set(j, lastarc);
					lastarc = toparc;
				}
			}
		}
	}
	// Sort outbound links based on their directions. Direction is
	// represebted by the angle counter-clockwise, with 3 O'clock
	// being 0.
	public void sortDnLinks() {
		int i, j;
		Link lastarc;
		Link toparc;
		for (i = nDnLinks() - 1; i > 0; i--) {
			lastarc = dnLinks.get(i);
			for (j = i - 1; j >= 0; j--) {
				toparc = dnLinks.get(j);
				if (toparc.outboundAngle() > lastarc.outboundAngle()) {
					dnLinks.set(i, toparc);
					dnLinks.set(j, lastarc);
					lastarc = toparc;
				}
			}
		}
	}

	public int getIndex() {
		return index;
	}
	public int getDestIndex() {
		return destIndex;
	}
	public void setDestIndex(int destIndex){
		this.destIndex = destIndex;
	}
	public void addSignalPlan(int planId) {
		signalPlans.add(new SignalPlan(planId));
	}
	public void addSignalPlan(SignalPlan plan) {
		signalPlans.add(plan);
	}
	public SignalPlan findPlan(int planID) {
		return signalPlans.stream().filter(s->s.getId()==planID).findFirst().orElse(null);
	}
	public SignalPlan findPlan(double now) {
		return signalPlans.stream().filter(s->s.beingApplied(now)).findFirst().orElse(null);
	}
	public List<int[]> greenLightLIDPairs(double now) {
		SignalStage stage = findPlan(now).findStage(now);
		if (stage != null)
			return stage.getLinkIDPairs();
		else
			return null;
	}
	public void calcStaticInfo(WorldSpace world_space){
		posPoint = world_space.worldSpacePoint(posPoint);
		boundBox = new GeoSurface();
		double expand = 1;
		// 以位置点为中心向外拓展出一个边长为2的正方形，以便拾取
		boundBox.addKerbPoint(new GeoPoint(posPoint.getLocationX()-expand, posPoint.getLocationY()-expand,0.0));
		boundBox.addKerbPoint(new GeoPoint(posPoint.getLocationX()+expand, posPoint.getLocationY()-expand,0.0));
		boundBox.addKerbPoint(new GeoPoint(posPoint.getLocationX()+expand, posPoint.getLocationY()+expand,0.0));
		boundBox.addKerbPoint(new GeoPoint(posPoint.getLocationX()-expand, posPoint.getLocationY()+expand,0.0));
	}
	


    // For vehicles that already has a getPath, they use this function
    // to check whether they should keep their current paths or
    // enroute
	/*
    public void routeSwitchingModel(Vehicle pv, MesoODCell od){
    	// the link from which the vehicle pv came

    	   Link slink = pv.getLink();

    	   if (slink!=null && slink.getDnNode() != this) {

    	      // Error in calling this function

    	      pv.donePathIndex();
    	      return;
    	   }

    	   // destination node of this vehicle

    	   Node dnode = pv.desNode();

    	   // Information used to routing the vehicle

    	   Route info = pv.routingInfo();

    	   int flag;
    	   int i, j, n;
    	   float cost;

    	   //Vector<Pointer<RN_PathPointer> ALLOCATOR> choices;
    	   //Vector<float ALLOCATOR> costs;
    	   ArrayList<Float>costs = new ArrayList<>();
    	   ArrayList<Path>choices = new ArrayList<>();
    	   // Prepare the choice sets and find the shortest route
    	   
    	   float smallest = Float.POSITIVE_INFINITY;
    	   if (slink!=null) {					// enroute
    		  for (i = 0; i < slink.nPaths(); i ++) {
    			 Path pp = slink.pathPointer(i);
    			 if (pp.getDesNode() == dnode) { // goes to my desination
    				// add to my choice set
    				cost = pp.cost(info);
    				costs.add(cost);
    				choices.add(pp);
    				if (cost < smallest) {
    				   smallest = cost;
    				}
    			 }
    		  }
    		  flag = 1;
    	   } else {						// at the origin
    		  for (i = 0; i < nDnLinks(); i ++) { // check each out going link
    			 slink = getDnLink(i);
    			 for (j = 0; j < slink.nPaths(); j ++) { // each getPath
    				 Path pp = slink.pathPointer(j);

    				// Dan: bus run paths should not be considered

    			//	if (!theBusAssignmentTable || 
    	        //                        (theBusAssignmentTable && !theBusRunTable.findPath(pp.getPath().code())))
    				 
    			          if (pp.getDesNode() == dnode && // goes to my desination
    			              (od!=null || pp.IsUsedBy(od))) {// this getPath should be also in my MesoODCell getPath set
    				       // add to my choice set
    				       cost = pp.cost(info);
    				       costs.add(cost);
    				       choices.add(pp);
    				       if (cost < smallest) {
    				            smallest = cost;
    				       }
    			          }
    				
    			 }
    		  }
    		  flag = 0;
    	   }

    	   n = choices.size();

    	   if (n > 1) {

    		  // Find the utility of choosing each route

    		  double util[] = new double[n];
    		  int itype = pv.infoType();
    		  float beta = Parameter.routingBeta(itype);
    		  float alpha = Parameter.getInstance().commonalityFactor();
    		  double sum = 0.0;		// sum of utilities

    		  for (i = 0; i < n; i ++) {
    			  Path pp = choices.get(i);
    			 // diversion penalty
    			//wym 更改判断逻辑 考虑到在行驶中改变path 原逻辑： && pp != pv.getPath()
    			 if (flag==1 && pv.getPath.links.containsAll(pp.links)) {
    				cost = Parameter.getInstance().pathDiversionPenalty();
    			 } else {
    				cost = 0;
    			 }
    			 cost += costs.get(i);
    			 util[i] = Math.exp(beta * cost / smallest + alpha * pp.cf());
    			 sum += util[i];
    		  }
    		 
    		  // Select getPath based on the probabilities calculated using a
    		  // logit model

    		  if (sum > 0.0) {

    			 // a uniform (0,1] random number

    			 double rnd = RoadNetwork.getInstance().sysRand.nextDouble();

    			 double cdf;
    			 for (i = n - 1, cdf = util[i] / sum;
    				  i > 0 && rnd > cdf;
    				  i --) {
    				cdf += util[i-1] / sum;
    			 }
    		  } else {
    			 i = RoadNetwork.getInstance().sysRand.nextInt(choices.size());
    		  }

    		  Path pp = choices.get(i);
    		  pv.setPath(pp, flag);


    	   } else if (n==1) {				// n == 1

    		  Path pp = choices.get(0);
    		  pv.setPath(pp, flag);

    	   } else {

    	      // No available getPath at this node. Switch to route generation
    	      // model

    	      pv.setPath(null);
    	      routeGenerationModel(pv);
    	   }
    }*/

}
