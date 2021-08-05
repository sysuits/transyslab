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

import com.transyslab.commons.io.db.DBUtils;
import com.transyslab.commons.tools.CoordTransformUtils;
import com.transyslab.roadnetwork.*;
import com.transyslab.simcore.mlp.MLPNetwork;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


public class NetworkCreator {
    public static void readDataFromDB(RoadNetwork roadNetwork, String nodeIdList, boolean hasDoubleCR){
        String sql;
        List<Object[]> result = null;
        // 读取节点数据, roadid为空的记录为交叉口节点
        sql = "select nodeid, geom, type from topo_node ";
        // 按节点集筛选
        if (nodeIdList != null && !nodeIdList.equals(""))
            sql += "where nodeid in (" + nodeIdList + ")";
        else
            sql += "where roadid isnull";
        // 节点数据
        result = DBUtils.queryDB(sql,"networkMppName");
        // 遍历，node -> node
        for (Object[] row : result) {
            long nodeid = obj2Long(row[0]);
            Point pos = ((PGgeometry) row[1]).getGeometry().getFirstPoint();
            int type = (Integer) row[2];
            GeoPoint tp = CoordTransformUtils.latlon2plane(new GeoPoint(pos.getX(),pos.getY(),pos.getZ()));
            roadNetwork.createNode(nodeid, type, "N" + String.valueOf(nodeid), tp);
        }
        List<long[]> linkid = new ArrayList<>();
        if(hasDoubleCR) {
            sql = "select id, fnode_, tnode_ from doubleline ";
            if (nodeIdList != null && !nodeIdList.equals(""))
                sql += "where fnode_ in (" + nodeIdList + ") and tnode_ in (" + nodeIdList + ")";
            linkid = readDoubleCR(sql);
        }
        // 中心线数据
        sql = "select id, name, fnode, tnode, geom from topo_centerroad ";
        if (nodeIdList != null && !nodeIdList.equals(""))
            sql += "where fnode in (" + nodeIdList + ") and tnode in (" + nodeIdList + ")";
        List<Object[]> crData = DBUtils.queryDB(sql,"networkMppName");
        // 提取所有中心线的id
        String crIds = "" ;
        for(Object[] crRow:crData){
            crIds  = crIds + String.valueOf(crRow[0]) + ",";
        }
        crIds  =  crIds.substring(0,crIds.length()-1);
        // 有向子路段数据
        sql = "select id,geom,roadid,flowdir,fbpointid,tbpointid from topo_link ";
        if(!crIds.isEmpty())
            sql += "where roadid in (" + crIds + ")";
        else
            sql += "where roadid notnull ";
        List<Object[]> linkData = DBUtils.queryDB(sql,"networkMppName");

        // 车道数据
        sql = "select laneid, laneindex, width, direction, geom, segmentid, rules from topo_lane ";
        List<Object[]> laneData = DBUtils.queryDB(sql,"networkMppName");
        // 车道连接器数据
        sql = "select connectorid, fromlaneid, tolaneid, geom from topo_laneconnector ";
        List<Object[]> connectorData = DBUtils.queryDB(sql,"networkMppName");
        // 遍历中心线数据，topo_centerroad -> link
        for (Object[] row : crData) {
            long crid = obj2Long(row[0]);
            String linkName = (String) row[1];
            long upNodeId = obj2Long(row[2]);
            long dnNodeId = obj2Long(row[3]);
            long id = crid;
            if(hasDoubleCR) {
                long[] ids = linkid.stream().filter(ls->ls[1] == upNodeId && ls[2] == dnNodeId).findFirst().orElse(null);
                if(ids!=null)
                    id = ids[0];
            }
            List<Object[]> sgmtPosFiltered;
            // 正向，数字化方向一致
            sgmtPosFiltered = linkData.stream().filter(sgmt->obj2Long(sgmt[2]) == crid && obj2Long(sgmt[3]) == 1).collect(Collectors.toList());
            segInfer(sgmtPosFiltered, 1, id, linkName, upNodeId, dnNodeId, pgMultiLines2Points((PGgeometry)row[4],"link " + linkid), laneData, roadNetwork);
            // 反向
            sgmtPosFiltered = linkData.stream().filter(sgmt->obj2Long(sgmt[2]) == crid && obj2Long(sgmt[3]) == -1).collect(Collectors.toList());
            segInfer(sgmtPosFiltered, -1, id, linkName, upNodeId, dnNodeId, pgMultiLines2Points((PGgeometry)row[4],"link " + linkid), laneData, roadNetwork);
        }
        // 目标区域的所有车道编号集，筛选出相关的车道连接器
        List<Long> laneIds = roadNetwork.getLanes().stream().mapToLong(e -> e.getId()).boxed().collect(Collectors.toList());
        // 车道连接器数据 LaneConnector -> Connector

        if (connectorData==null)
            System.out.println("DEBUG");
        readConnectors(roadNetwork,connectorData,laneIds);
    }

    public static void segInfer(List<Object[]> sgmtPosFiltered, int dir,
                                long id, String linkName, long upNodeId, long dnNodeId, List<GeoPoint> linkCtrlPoints,
                                List<Object[]> laneData,
                                RoadNetwork roadNetwork){
        LinkedHashMap<Long,List<Object[]>> sgmnt2LaneData = new LinkedHashMap();
        // 按上下游顺序存储Segment 数据
        List<Object[]> sortedSgmtsData = sortSgmtData(sgmtPosFiltered);
        //检查segment上下游关系
        if(sortedSgmtsData == null || sortedSgmtsData.size()!=sgmtPosFiltered.size()) {
            String msg = "info: Incomplete centerRoad no. " + id + " has been ignored.";
            if (roadNetwork instanceof MLPNetwork)
                ((MLPNetwork)roadNetwork).broadcast(msg);
        }
        else {
            // 筛选车道数据
            for(Object[] sgmt:sgmtPosFiltered){
                List<Object[]> laneFiltered= laneData.stream().filter(lane->obj2Long(lane[5])==obj2Long(sgmt[0])).collect(Collectors.toList());
                sgmnt2LaneData.put(obj2Long(sgmt[0]),laneFiltered);
            }
            Link newLink = dir==1 ?
                    roadNetwork.createLink(id, 1, linkName, upNodeId, dnNodeId) :
                    roadNetwork.createLink(-1*id, 1, linkName, dnNodeId, upNodeId);
            newLink.setCtrlPoints(linkCtrlPoints);
            // 与当前中心线同向的子路段数据，topo_link -> SgmtInOutRecord
            List<Segment> sgmts = readSegments(roadNetwork,sortedSgmtsData,sgmnt2LaneData);
            if (sgmts==null){
                roadNetwork.rmLastLink();
            }
            else {
                newLink.setSegments(sgmts);
            }
        }
    }

    public static void readDataFromXML(String fileName, RoadNetwork roadNetwork, String nodeIdList){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document dom;
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            dom = builder.parse(new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("failed parsing dom");
            return;
        }
        //todo: node filter
        NodeList nodeList = dom.getElementsByTagName("N");
        //parse nodes
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element nodeEle = (Element) nodeList.item(i);
            roadNetwork.createNode(
                    Long.valueOf(nodeEle.getAttribute("id")),
                    Integer.valueOf(nodeEle.getAttribute("type")),
                    nodeEle.getAttribute("name"),
                    GeoPoint.parse(nodeEle.getAttribute("geoString")));
        }
        //parse links
        NodeList linkList = dom.getElementsByTagName("L");
        for (int i = 0; i < linkList.getLength(); i++) {
            Element linkEle = (Element) linkList.item(i);
            Link newLink = roadNetwork.createLink(
                    Long.valueOf(linkEle.getAttribute("id")),
                    Integer.valueOf(linkEle.getAttribute("type")),
                    linkEle.getAttribute("name"),
                    Long.valueOf(linkEle.getAttribute("upNode")),
                    Long.valueOf(linkEle.getAttribute("dnNode")));
            newLink.setCtrlPoints(GeoPoints.parse(linkEle.getAttribute("ctrlPoints")));
            //parse segments in a link
            List<Segment> segments = new ArrayList<>();
            Node segmentNode = linkEle.getFirstChild();
            while (segmentNode!=null){
                if (segmentNode.getNodeName().equals("S")){
                    Element segEle = (Element) segmentNode;
                    Segment newSeg = roadNetwork.createSegment(
                            Long.parseLong(segEle.getAttribute("id")),
                            Integer.parseInt(segEle.getAttribute("speedLimit")),
                            Double.parseDouble(segEle.getAttribute("freeSpeed")),
                            Double.parseDouble(segEle.getAttribute("gradient")),
                            GeoPoints.parse(
                                    segEle.getAttribute("ctrlPoints")
                            )
                    );
                    //parse lanes in a segment
                    List<Lane> lanes = new ArrayList<>();
                    Node laneNode = segmentNode.getFirstChild();
                    while (laneNode!=null){
                        if (laneNode.getNodeName().equals("LA")){
                            Element lnEle = (Element) laneNode;
                            lanes.add(
                                    roadNetwork.createLane(
                                            Long.parseLong(lnEle.getAttribute("laneId")),
                                            Integer.parseInt(lnEle.getAttribute("rules")),
                                            Integer.parseInt(lnEle.getAttribute("orderNum")),
                                            Double.parseDouble(lnEle.getAttribute("width")),
                                            lnEle.getAttribute("direction"),
                                            GeoPoints.parse(
                                                    lnEle.getAttribute("ctrlPoints")
                                            ))
                            );
                        }
                        laneNode = laneNode.getNextSibling();
                    }
                    newSeg.setLanes(lanes);
                    segments.add(newSeg);
                }
                segmentNode = segmentNode.getNextSibling();
            }
            newLink.setSegments(segments);
        }
        //parse lane connectors
        NodeList connList = dom.getElementsByTagName("LC");
        for (int i = 0; i < connList.getLength(); i++) {
            Element connEle = (Element) connList.item(i);
            Long fLaneId = Long.parseLong(connEle.getAttribute("fLaneId"));
            Long tLaneId = Long.parseLong(connEle.getAttribute("tLaneId"));
            // 临时改造 去除横向车道连接器 wym
            if (roadNetwork.findLane(fLaneId).getSegment().getId() ==
                    roadNetwork.findLane(tLaneId).getSegment().getId())
                continue;
            Long connectorId = Long.parseLong(connEle.getAttribute("connectorId"));
            List<GeoPoint> ctlPs = GeoPoints.parse(connEle.getAttribute("ctrlPoints"));
            roadNetwork.createConnector(connectorId,fLaneId,tLaneId,ctlPs);
        }
    }

    public static List<Segment> readSegments(RoadNetwork roadNetwork,List<Object[]> filteredSgmtData,LinkedHashMap<Long,List<Object[]>> sgmtId2Lanes)  {

        List<Segment> sgmt2check = new ArrayList<>();
        List<Lane> addLanes = new ArrayList<>();

        for (Object[] sgmtRow : filteredSgmtData) {
            PGgeometry geom = (PGgeometry) sgmtRow[1];
            List<GeoPoint> ctrlPoint = pgMultiLines2Points(geom,"Segment"+String.valueOf(sgmtRow[0])+" 平面坐标");

            Segment newSgmt = roadNetwork.createSegment(obj2Long(sgmtRow[0]), 60, 60, 0, ctrlPoint);

            long sgmtId = newSgmt.getId();
            // 读取属于当前Segment的Lane
            List<Lane> lanesInSgmt = readLanes(roadNetwork,sgmtId2Lanes.get(sgmtId));
            addLanes.addAll(lanesInSgmt);
            sgmt2check.add(newSgmt);
            if (lanesInSgmt.size() > 0) {
                // 将车道按流向从左到右排列
                Collections.sort(lanesInSgmt);
                newSgmt.setLanes(lanesInSgmt);

            } else {// segment没有车道, 过滤掉该link
                //roadNetwork.rmLastSegment();
                // 清空车道
                roadNetwork.rmLanes(addLanes);
                roadNetwork.rmSegments(sgmt2check);
                return null;
            }
        }
        return sgmt2check;
    }

    public static List<Lane> readLanes(RoadNetwork roadNetwork,List<Object[]> laneFiltered){
        List<Lane> lanesInSgmt = new ArrayList<>();
        // 遍历Lane数据
        for (Object[] laneRow : laneFiltered) {
            long laneid = obj2Long(laneRow[0]);
            int orderNum = (int) obj2Long(laneRow[1]);
            double width;
            if (laneRow[2] == null)
                width = 3.75;
            else
                width = obj2Double(laneRow[2]);
            String direction = (String) laneRow[3];
            // lane的几何属性,平面坐标
            PGgeometry geomLane = (PGgeometry) laneRow[4];
            int rules = (int) laneRow[6];
            List<GeoPoint> ctrlPoints = pgMultiLines2Points(geomLane,"Lane"+String.valueOf(laneid)+" 平面坐标");

            Lane newLane = roadNetwork.createLane(laneid, rules, orderNum, width, direction, ctrlPoints);

            lanesInSgmt.add(newLane);
        }
        return lanesInSgmt;
    }

    public static List<Connector> readConnectors(RoadNetwork roadNetwork,List<Object[]> connectorData,List<Long> laneIds){
        List<Connector> connectors = new ArrayList<>();
        // 遍历车道连接器 LaneConnector -> Connector
        for (Object[] connRow : connectorData) {
            long connId = obj2Long(connRow[0]);
            long fLaneId = obj2Long(connRow[1]);
            long tLaneId = obj2Long(connRow[2]);
            if (!laneIds.contains(fLaneId)||!laneIds.contains(tLaneId))
                continue;
            // 临时改造 去除横向车道连接器 wym
            if (roadNetwork.findLane(fLaneId).getSegment().getId() ==
                    roadNetwork.findLane(tLaneId).getSegment().getId())
                continue;
            // connector的几何属性
            PGgeometry geom = (PGgeometry) connRow[3];
            if(geom == null || geom.getGeometry() == null)
                continue;
            List<GeoPoint> ctrlPoints = pgMultiLines2Points(geom,"Connector" + String.valueOf(connId)+" 平面坐标");
            Connector connt = roadNetwork.createConnector(connId, fLaneId, tLaneId, ctrlPoints);
            connectors.add(connt);
        }
        return connectors;
    }

    public static List<Object[]> sortSgmtData(List<Object[]> sgmt2Check) {
        List<Object[]> sortedRslt = new ArrayList<>();
        int n = sgmt2Check.size();
        if (n<=0)
            return null;
        else if (n==1)
            return sgmt2Check;
        List<Long> fps = new ArrayList<>();
        List<Long> tps = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();
        sgmt2Check.forEach(s->{
            fps.add(obj2Long(s[4]));
            tps.add(obj2Long(s[5]));
        });
        for (int i = 0; i < n; i++) {
            idx.add(tps.indexOf(fps.get(i)));
        }
        int k = idx.indexOf(-1);
        while (k>=0){
            sortedRslt.add(sgmt2Check.get(k));
            k = idx.indexOf(k);
        }
        return sortedRslt;
    }
    public static List<long[]> readDoubleCR(String sql){
        List<Object[]> result;
        // 中心线数据
        result = DBUtils.queryDB(sql,"networkMppName");
        List<long[]> doubleCR = new ArrayList<>();
        for(Object[]row:result){
            long[] rl = new long[row.length];
            for(int i=0;i<row.length;i++){
                rl[i] = (int)row[i];
            }
            doubleCR.add(rl);
        }
        return doubleCR;
    }
    private static List<GeoPoint> pgMultiLines2Points(PGgeometry geometry,String networkObjInfo){

        if (geometry==null||geometry.getGeometry()==null)
            System.out.println("DEBUG");

        LineString[] linesLane2 = ((MultiLineString) geometry.getGeometry()).getLines();
        List<GeoPoint> ctrlPoints = new ArrayList<>();
        for (LineString line : linesLane2) {
            for (Point p : line.getPoints()) {
                GeoPoint gPoint = CoordTransformUtils.latlon2plane(new GeoPoint(p.getX(), p.getY(), p.getZ()));
//                long numOfSamePoints= ctrlPoints.stream().filter(pnt->pnt.equal(gPoint)).count();
//                if(numOfSamePoints>=1)
//                    System.out.println("Warning: " + networkObjInfo + "存在" + numOfSamePoints + "个重复顶点");
                ctrlPoints.add(gPoint);
            }
        }
        return ctrlPoints;

    }

    public static long obj2Long(Object obj){
        if (obj instanceof BigDecimal)
            return ((BigDecimal) obj).longValue();
        if (obj instanceof Long)
            return ((Long) obj).longValue();
        if (obj instanceof String){
            String objStr = ((String) obj).replace(":","");
            if (objStr.contains("_")){
                String[] tmp = objStr.split("_");
                long ans = 0;
                for (int i = 0; i < tmp.length; i++) {
                    ans += Long.parseLong(tmp[i])*Math.pow(100,tmp.length-1-i);
                }
                return ans;
            }
            return Long.parseLong((String)obj);
        }
        if (obj instanceof Integer)
            return (int) obj;
        System.err.println("unsolved data type of object " + obj.toString());
        return Long.MAX_VALUE;
    }

    public static double obj2Double(Object obj){
        if (obj instanceof BigDecimal)
            return ((BigDecimal) obj).doubleValue();
        if (obj instanceof Long)
            return ((Long) obj).doubleValue();
        if (obj instanceof String)
            return Double.parseDouble((String)obj);
        if (obj instanceof Double)
            return (double) obj;
        if (obj instanceof Integer)
            return (int) obj;
        System.err.println("unsolved data type of object " + obj.toString());
        return Long.MAX_VALUE;
    }
}
