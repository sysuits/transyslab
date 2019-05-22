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

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.transyslab.roadnetwork.*;
import com.transyslab.simcore.mesots.*;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class XmlParser {
	public static void parseNetwork(RoadNetwork network, String filePath){
		File inputXml = new File(filePath);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element node = document.getRootElement();
			parseNetworkObjects(node,network);
		}
		catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
	}
	private static void parseNetworkObjects(Element node,RoadNetwork network){
		String tmpName = "";
		double beginX = -1,beginY = -1,endX = -1,endY = -1,tmpFreeSpeed = -1,kerbx = -1,kerby =-1;
		int tmpId = -1,tmpType = -1,tmpUpId = -1,tmpDnId = -1,gradient = -1,tmpSpeedLimit = -1;
		int tmpSegId = -1;
		List<Attribute> attrs = node.attributes();
		// 解析Node
		if (node.getName().equals("N")) {
			for (Attribute attr : attrs) {
				if (attr.getName().equals("id"))
					tmpId = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("type"))
					tmpType = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("name"))
					tmpName = attr.getValue();
				if(attr.getName().equals("NodeX"))
					beginX = Double.parseDouble(attr.getValue());
				if(attr.getName().equals("NodeY"))
					beginY = Double.parseDouble(attr.getValue());

			}
			network.createNode(tmpId,tmpType,tmpName,new GeoPoint(beginX,beginY,0));
		}
		// 解析Link
		if (node.getName().equals("L")) {
			for (Attribute attr : attrs) {
				if (attr.getName().equals("id"))
					tmpId = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("type"))
					tmpType = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("UpNode"))
					tmpUpId = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("DnNode"))
					tmpDnId = Integer.parseInt(attr.getValue());
			}
			network.createLink(tmpId,tmpType,null,tmpUpId,tmpDnId);
		}
		//解析Segment
		if (node.getName().equals("S")) {
			for (Attribute attr : attrs) {
				if (attr.getName().equals("id"))
					tmpId = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("speedLimit"))
					tmpSpeedLimit = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("freeSpeed"))
					tmpFreeSpeed = Double.parseDouble(attr.getValue());
				if (attr.getName().equals("gradient"))
					gradient = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("beginX"))
					beginX = Double.parseDouble(attr.getValue());
				if (attr.getName().equals("beginY"))
					beginY = Double.parseDouble(attr.getValue());
				if (attr.getName().equals("endX"))
					endX = Double.parseDouble(attr.getValue());
				if (attr.getName().equals("endY"))
					endY = Double.parseDouble(attr.getValue());

			}
			//network.createSegment(tmpId,tmpSpeedLimit,tmpFreeSpeed,gradient,beginX,beginY,0, endX,endY);

		}
		//解析Lane
		if (node.getName().equals("LA")) {

			for (Attribute attr : attrs) {
				if (attr.getName().equals("LaneID"))
					tmpId = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("rule"))
					tmpType = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("beginX"))
					beginX = Double.parseDouble(attr.getValue());
				if (attr.getName().equals("beginY"))
					beginY = Double.parseDouble(attr.getValue());
				if (attr.getName().equals("endX"))
					endX = Double.parseDouble(attr.getValue());
				if (attr.getName().equals("endY"))
					endY = Double.parseDouble(attr.getValue());

			}
			//network.createLane(tmpId,tmpType,beginX,beginY,endX,endY);
		}
		//解析LaneConnector
		if (node.getName().equals("LC")) {
			int flag = -1;
			for (Attribute attr : attrs) {
				if (attr.getName().equals("ID")) {
					tmpId = Integer.parseInt(attr.getValue());
				}
				if (attr.getName().equals("UpLane"))
					tmpUpId = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("DnLane"))
					tmpDnId = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("Successive"))
					flag = Integer.parseInt(attr.getValue());

			}
			List<GeoPoint> polyline = new ArrayList<>();
			List<Element> nodelist = node.elements();
			for(Element n : nodelist){
				List<Attribute> pattr = n.attributes();
				for(Attribute attr : pattr){
					if(attr.getName()=="X")
						kerbx = Double.parseDouble(attr.getValue());
					if(attr.getName()=="Y")
						kerby = Double.parseDouble(attr.getValue());
				}
				polyline.add(new GeoPoint(kerbx,kerby));
			}
			// TODO 从外部组织几何连接
			// 拓扑连接
			network.addLaneConnector(tmpId,tmpUpId,tmpDnId,flag,polyline);
		}
		//解析Boundary
		if (node.getName().equals("Boundary")) {
			for (Attribute attr : attrs) {

				if (attr.getName().equals("BoundaryID"))
					tmpId = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("BeginX"))
					beginX = Double.parseDouble(attr.getValue());
				if (attr.getName().equals("BeginY"))
					beginY = Double.parseDouble(attr.getValue());
				if (attr.getName().equals("EndX"))
					endX = Double.parseDouble(attr.getValue());
				if (attr.getName().equals("EndY"))
					endY = Double.parseDouble(attr.getValue());

			}
			//创建Boundary对象，用于绘制车道分隔线，与仿真模型无关
			network.createBoundary(tmpId, beginX, beginY, endX, endY);
		}
		//解析Surface
		if(node.getName() == "Surface"){
			for (Attribute attr : attrs) {
				if (attr.getName() == "SurfaceID")
					tmpId = Integer.parseInt(attr.getValue());
				if (attr.getName() == "ArcID")
					tmpSegId = Integer.parseInt(attr.getValue());

			}
			//创建Surface对象，用于绘制路面，与仿真模型无关
			List<GeoPoint> kerbPoint = new ArrayList<>();
			List<Element> kerblist = node.elements();
			for(Element p : kerblist){
				List<Attribute> pattr = p.attributes();
				for(Attribute attr : pattr){
					if(attr.getName()=="KerbX")
						kerbx = Double.parseDouble(attr.getValue());
					if(attr.getName()=="KerbY")
						kerby = Double.parseDouble(attr.getValue());
				}
				kerbPoint.add(new GeoPoint(kerbx,kerby));
			}
			network.createSurface(tmpId,tmpSegId,kerbPoint);
		}
		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		// 遍历
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element e = it.next();
			// 对子节点进行遍历
			parseNetworkObjects(e,network);
		}
	}
	public static void parseSensors(RoadNetwork network, String filePath){
		File inputXml = new File(filePath);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element node = document.getRootElement();
			parseSensor(node, network);
		}
		catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
	}
	private static void parseSensor(Element node,RoadNetwork network) {
		int tmpId = -1,tmpType = -1,tmpSegId = -1;
		String tmpName = "";
		double interval = -1,zone = -1,pos = -1;
		List<Attribute> list = node.attributes();
		// 遍历属性节点
		if (node.getName().equals("station")) {
			for (Attribute attr : list) {
				if (attr.getName().equals("type"))
					tmpType = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("name")) {
					tmpName = attr.getValue();
				}
				if (attr.getName().equals("interval"))
					interval = Float.parseFloat(attr.getValue());
				if (attr.getName().equals("zone"))
					zone = Float.parseFloat(attr.getValue());
				if (attr.getName().equals("segid"))
					tmpSegId = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("id"))
					tmpId = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("pos"))
					pos = Float.parseFloat(attr.getValue());

			}
			network.createSensor(tmpId,tmpType,tmpName,tmpSegId,pos,zone,interval);
		}

		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		// 遍历
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element e = it.next();
			// 对子节点进行遍历
			parseSensor(e,network);
		}
	}

	public static void parseODXml(String filename, int tarid, RoadNetwork network) {
		File inputXml = new File(filename);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element node = document.getRootElement();
			listODNodes(node, tarid, network);
		}
		catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
	}
	public static void listODNodes(Element node, int tarid, RoadNetwork network) {
		// System.out.println("当前节点的名称：：" + node.getName());
		Element tare = null;
		int ODtime = -100;
		int s = -100;
		int u = -100;
		int oid = -100;
		int did = -100;
		int flow = -100;
		int c1 = -100;
		int c2 = -100;
		// rootchild为Time
		List<Element> rootchild = node.elements();
		// 找到对应id的Time元素
		for (Element tempe : rootchild) {
			List<Attribute> list = tempe.attributes();
			for (Attribute attr1 : list) {
				if (attr1.getName().equals("timeid") && tarid == Integer.parseInt(attr1.getValue()))
					tare = tempe;
			}
		}
		if (tare != null) {
			// System.out.println("当前节点的名称：：" + tare.getName());
			// 遍历Time节点的所有属性
			List<Attribute> arroftare = tare.attributes();
			for (Attribute attr2 : arroftare) {
				// System.out.println(attr2.getText() + "-----" +
				// attr2.getName()
				// + "---" + attr2.getValue());
				if (attr2.getName().equals( "sttime")) {
					String strDate = attr2.getValue();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
					// LocalDateTime
					LocalTime time = LocalTime.parse(strDate,formatter);
					int h = time.getHour();
					int m = time.getMinute();
					int sec = time.getSecond();
					// 往后读5分钟，故+300秒
					ODtime = h * 3600 + m * 60 + sec + 3600;
					// 最后一个时间间隔
					if (ODtime == 68700)
						ODtime = Constants.INT_INF;
					// System.out.println(ODtime+"-----");
				}
				if (attr2.getName().equals("s")){
					s = Integer.parseInt(attr2.getValue());
					// System.out.println(s+"-----");
				}
				if (attr2.getName().equals("u")) {
					u = Integer.parseInt(attr2.getValue());
					// System.out.println(u+"-----");
				}
			}
			MesoODTable odTable = ((MesoNetwork)network).getODTable();
			odTable.init(ODtime, s, u);
			// Time的子节点Item
			List<Element> childoftare = tare.elements();
			for (Element temptar : childoftare) {

				arroftare = temptar.attributes();
				for (Attribute attr2 : arroftare) {
					// System.out.println(attr2.getText() + "-----" +
					// attr2.getName()
					// + "---" + attr2.getValue());
					if (attr2.getName().equals("o")) {
						oid = Integer.parseInt(attr2.getValue());
					}
					if (attr2.getName().equals("d")) {
						did = Integer.parseInt(attr2.getValue());
					}
					if (attr2.getName().equals("flow")) {
						flow = Integer.parseInt(attr2.getValue());
					}
					if (attr2.getName().equals("c1")) {
						c1 = Integer.parseInt(attr2.getValue());
					}
					if (attr2.getName().equals("c2")) {
						c2 = Integer.parseInt(attr2.getValue());
					}

				}
				MesoODCell tempcell = ((MesoNetwork)network).createODCell(oid, did, flow, c1, c2);

				// 更新odtable时会重新生成odcell，则每个odcell都需要建立新的path
				// TODO 不同模式来决定路径从图还是文件生成
				if (network.nPaths()!=0) {
					for (int pindex = 0; pindex < network.nPaths(); pindex++) {
						Path pathInTable = network.getPath(pindex);
						if (pathInTable.getOriNode().getId() == oid
								&& pathInTable.getDesNode().getId() == did)
							tempcell.addPath(pathInTable);
					}
				}
			}
		}
	}
	// 解析路径表
	public static void parsePathTable(RoadNetwork network,String filename) {
		File inputXml = new File(filename);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element node = document.getRootElement();
			listPathTableNodes(node,network);
		}
		catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
	}
	public static void listPathTableNodes(Element node,RoadNetwork network) {
		// System.out.println("当前节点的名称：：" + node.getName());
		int pid = -1,lkid = -1,oid = -1,did = -1;
		List<Attribute> list = node.attributes();
		// 遍历属性节点
		if (node.getName().equals("P")) {
			for (Attribute attr : list) {
				if (attr.getName().equals("id"))
					pid = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("o"))
					oid = Integer.parseInt(attr.getValue());
				if (attr.getName().equals("d"))
					did = Integer.parseInt(attr.getValue());

			}
			Path newPath = network.createPathFromFile(pid,oid,did);
			List<Element> childofp = node.elements();
			for (Element lofp : childofp) {
				// P元素下的子元素L
				List<Attribute> listofl = lofp.attributes();
				for (Attribute attrofl : listofl) {

					if (attrofl.getName().equals("id")) {
						lkid = Integer.parseInt(attrofl.getValue());
						newPath.getLinks().add(network.findLink(lkid));
					}
					// 完成path初始化
				}

			}
		}

		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		// 遍历
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element e = it.next();
			// 对子节点进行遍历
			listPathTableNodes(e,network);
		}
	}

	
	//解析路网快照
	//注意：应先解析VehicleTable，得到od和path
	public static void parseSnapshotXml(String filename, List<MesoVehicle> vhclist){
		File inputXml=new File(filename); 
		SAXReader saxReader = new SAXReader(); 
		try { 
		Document document = saxReader.read(inputXml); 
		Element node = document.getRootElement();
		listSnapshotNodes(node,vhclist);
		} catch (DocumentException e) { 
		System.out.println(e.getMessage()); 
		} 
	}
	public static void listSnapshotNodes(Element node, List<MesoVehicle> vhclist){
		int vhcid=-1,type=-1;
		float distance=0,length=0,departtime=0;
		List<Element> rootchild = node.elements();
		for(Element vehicleobj : rootchild){
			List<Attribute> vehiclearr = vehicleobj.attributes();
			for(Attribute vehicle: vehiclearr){
				if(vehicle.getName().equals("vhcID"))
					vhcid = Integer.parseInt(vehicle.getValue());
				if(vehicle.getName().equals("length"))
					length = Float.parseFloat(vehicle.getValue());
				if(vehicle.getName().equals("getDistance"))
					distance = Float.parseFloat(vehicle.getValue());
				if(vehicle.getName().equals("type"))
					type = Integer.parseInt(vehicle.getValue());
				if(vehicle.getName().equals("departtime"))
					departtime = Float.parseFloat(vehicle.getValue());
			}
			// TODO 待设计
			MesoVehicle tmp = new MesoVehicle();// = MesoVehiclePool.getInstance().recycle();
			tmp.init(vhcid, type, length,distance,departtime);
			// TODO tmp.initialize()
			vhclist.add(tmp);
		}
	}

}
