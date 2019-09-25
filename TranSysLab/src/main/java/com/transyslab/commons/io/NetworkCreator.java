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

import com.transyslab.commons.tools.CoordTransformUtils;
import com.transyslab.roadnetwork.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


public class NetworkCreator {
    public static void readDataFromDB(RoadNetwork roadNetwork, String nodeIdList, boolean hasDoubleCR) throws SQLException {
        DataSource ds = JdbcUtils.getDataSource();
        org.apache.commons.dbutils.QueryRunner qr = new QueryRunner(ds);
        String sql;
        List<Object[]> result = null;
        // ��ȡ�ڵ�����, roadidΪ�յļ�¼Ϊ����ڽڵ�
        sql = "select nodeid, geom, type from topo_node ";
        // ���ڵ㼯ɸѡ
        if (nodeIdList != null && !nodeIdList.equals(""))
            sql += "where nodeid in (" + nodeIdList + ")";
        else
            sql += "where roadid isnull";
        // �ڵ�����
        result = qr.query(sql, new ArrayListHandler());
        // ������node -> node
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
            linkid = readDoubleCR(qr, sql);
        }
        // ����������
        sql = "select id, name, fnode, tnode from topo_centerroad ";
        if (nodeIdList != null && !nodeIdList.equals(""))
            sql += "where fnode in (" + nodeIdList + ") and tnode in (" + nodeIdList + ")";
        List<Object[]> crData = qr.query(sql, new ArrayListHandler());
        // ������·������
        sql = "select id,geom,roadid,flowdir from topo_link ";
        List<Object[]> linkData = qr.query(sql, new ArrayListHandler());

        // ��������
        sql = "select laneid, laneindex, width, direction, geom,segmentid from topo_lane ";
        List<Object[]> laneData = qr.query(sql, new ArrayListHandler());
        // ��������������
        sql = "select connectorid, fromlaneid, tolaneid, geom from topo_laneconnector ";
        List<Object[]> connectorData = qr.query(sql, new ArrayListHandler());
        // �������������ݣ�topo_centerroad -> link
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

            LinkedHashMap<Long,List<Object[]>> sgmnt2LaneData = new LinkedHashMap();
            // �������ֻ�����һ��
            List<Object[]> sgmtPosFiltered;
            sgmtPosFiltered = linkData.stream().filter(sgmt->obj2Long(sgmt[2]) == crid && obj2Long(sgmt[3]) == 1).collect(Collectors.toList());
            // ɸѡ��������
            for(Object[] sgmt:sgmtPosFiltered){
                List<Object[]> laneFiltered= laneData.stream().filter(lane->obj2Long(lane[5])==obj2Long(sgmt[0])).collect(Collectors.toList());
                sgmnt2LaneData.put(obj2Long(sgmt[0]),laneFiltered);
            }
            Link newLink = roadNetwork.createLink(id, 1, linkName, upNodeId, dnNodeId);
            // �뵱ǰ������ͬ�����·�����ݣ�topo_link -> SgmtInOutRecord
            List<Segment> sgmt2check = readSegments(roadNetwork,sgmtPosFiltered,sgmnt2LaneData);

            // ��������˳��洢Segment
            if (sgmt2check != null && sgmt2check.size() > 0) {
                List<Segment> sortedSgmts = sortSegments(sgmt2check);
                if (sgmt2check.size() != sortedSgmts.size()) {
                    roadNetwork.rmLastLink();
                    for (Segment smt : sgmt2check) {
                        roadNetwork.rmLanes(smt.getLanes());
                    }
                    roadNetwork.rmSegments(sgmt2check);
                    System.out.println("Error: " + String.valueOf(upNodeId) + "_" + String.valueOf(dnNodeId) + "��·�������ι�ϵ�ƶϳ���");
                } else
                    newLink.setSegments(sortedSgmts);
            } else {// segment������Ϊ��
                roadNetwork.rmLastLink();
            }
            // TODO ���޷���·�ε��ж�
            List<Object[]> sgmtNegFiltered;
            sgmtNegFiltered = linkData.stream().filter(sgmt -> obj2Long(sgmt[2]) == crid && obj2Long(sgmt[3]) == -1).collect(Collectors.toList());
            for (Object[] sgmt : sgmtNegFiltered) {
                List<Object[]> laneFiltered = laneData.stream().filter(lane -> obj2Long(lane[5]) == obj2Long(sgmt[0])).collect(Collectors.toList());
                sgmnt2LaneData.put(obj2Long(sgmt[0]), laneFiltered);
            }

            id = -1 * crid;// ���߷���
            if (hasDoubleCR) {
                long[] ids = linkid.stream().filter(ls -> ls[1] == dnNodeId && ls[2] == upNodeId).findFirst().orElse(null);
                if (ids != null)
                    id = ids[0];
            }
            Link newLinkRvs = roadNetwork.createLink(id, 1, linkName, dnNodeId, upNodeId);
            // �뷴��������ͬ�����·�����ݣ�topo_link -> SgmtInOutRecord
            sgmt2check = readSegments(roadNetwork, sgmtNegFiltered, sgmnt2LaneData);

            if (sgmt2check != null && sgmt2check.size() > 0) {
                List<Segment> sortedSgmts = sortSegments(sgmt2check);
                if (sgmt2check.size() != sortedSgmts.size()) {
                    roadNetwork.rmLastLink();
                    for (Segment smt : sgmt2check) {
                        roadNetwork.rmLanes(smt.getLanes());
                    }
                    roadNetwork.rmSegments(sgmt2check);
                    System.out.println("Error: " + String.valueOf(dnNodeId) + "_" + String.valueOf(upNodeId) + "��·�������ι�ϵ�ƶϳ���");
                } else
                    newLinkRvs.setSegments(sortedSgmts);
            } else {// ��segment��segment�ĳ�����Ϊ0
                roadNetwork.rmLastLink();
            }

        }
        // Ŀ����������г�����ż���ɸѡ����صĳ���������
        List<Long> laneIds = roadNetwork.getLanes().stream().mapToLong(e -> e.getId()).boxed().collect(Collectors.toList());
        // �������������� LaneConnector -> Connector

        readConnectors(roadNetwork,connectorData,laneIds);
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
            // ��ʱ���� ȥ�����򳵵������� wym
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
            List<GeoPoint> ctrlPoint = pgMultiLines2Points(geom,"Segment"+String.valueOf(sgmtRow[0])+" ƽ������");

            Segment newSgmt = roadNetwork.createSegment(obj2Long(sgmtRow[0]), 60, 60, 0, ctrlPoint);

            long sgmtId = newSgmt.getId();
            // ��ȡ���ڵ�ǰSegment��Lane
            List<Lane> lanesInSgmt = readLanes(roadNetwork,sgmtId2Lanes.get(sgmtId));
            addLanes.addAll(lanesInSgmt);
            sgmt2check.add(newSgmt);
            if (lanesInSgmt.size() > 0) {
                // ���������������������
                Collections.sort(lanesInSgmt);
                newSgmt.setLanes(lanesInSgmt);

            } else {// segmentû�г���, ���˵���link
                //roadNetwork.rmLastSegment();
                // ��ճ���
                roadNetwork.rmLanes(addLanes);
                roadNetwork.rmSegments(sgmt2check);
                return null;
            }
        }
        return sgmt2check;
    }

    public static List<Lane> readLanes(RoadNetwork roadNetwork,List<Object[]> laneFiltered){
        List<Lane> lanesInSgmt = new ArrayList<>();
        // ����Lane����
        for (Object[] laneRow : laneFiltered) {
            long laneid = obj2Long(laneRow[0]);
            int orderNum = (int) obj2Long(laneRow[1]);
            double width;
            if (laneRow[2] == null)
                width = 3.75;
            else
                width = obj2Double(laneRow[2]);
            String direction = (String) laneRow[3];
            // lane�ļ�������,ƽ������
            PGgeometry geomLane = (PGgeometry) laneRow[4];
            List<GeoPoint> ctrlPoints = pgMultiLines2Points(geomLane,"Lane"+String.valueOf(laneid)+" ƽ������");

            Lane newLane = roadNetwork.createLane(laneid, 3, orderNum, width, direction, ctrlPoints);

            lanesInSgmt.add(newLane);
        }
        return lanesInSgmt;
    }

    public static List<Connector> readConnectors(RoadNetwork roadNetwork,List<Object[]> connectorData,List<Long> laneIds){
        List<Connector> connectors = new ArrayList<>();
        // �������������� LaneConnector -> Connector
        for (Object[] connRow : connectorData) {
            long connId = obj2Long(connRow[0]);
            long fLaneId = obj2Long(connRow[1]);
            long tLaneId = obj2Long(connRow[2]);
            if (!laneIds.contains(fLaneId)||!laneIds.contains(tLaneId))
                continue;
            // ��ʱ���� ȥ�����򳵵������� wym
            if (roadNetwork.findLane(fLaneId).getSegment().getId() ==
                    roadNetwork.findLane(tLaneId).getSegment().getId())
                continue;
            // connector�ļ�������
            PGgeometry geom = (PGgeometry) connRow[3];
            if(geom == null || geom.getGeometry() == null)
                continue;
            List<GeoPoint> ctrlPoints = pgMultiLines2Points(geom,"Connector" + String.valueOf(connId)+" ƽ������");
            Connector connt = roadNetwork.createConnector(connId, fLaneId, tLaneId, ctrlPoints);
            connectors.add(connt);
        }
        return connectors;
    }

    public static List<Segment> sortSegments(List<Segment> sgmt2Check) {
        // �������segment��������
        // ͳ��sgmt�Ĺ��ö��������ҳ���ʼsegment
        Segment startSgmt = null;
        for (Segment sgmt : sgmt2Check) {
            GeoPoint spnt = sgmt.getCtrlPoints().get(0);
            boolean isShared = false;
            for (Segment compareSgmt : sgmt2Check) {
                if (spnt.equal(compareSgmt.getCtrlPoints().get(compareSgmt.getCtrlPoints().size() - 1))) {
                    isShared = true;
                }
            }
            if (!isShared) {// ĳ����ʾ��segment����ʼsegment��ĳ������ֻ����һ�Σ���ʾ��segment����ʼsegment
                startSgmt = sgmt;
                break;
            }
        }
        Segment nextSgmt = null;
        List<Segment> sortedSgmts = null;
        if (startSgmt == null) {
            System.out.println("Error:Could not find the first SgmtInOutRecord of link" +
                    String.valueOf(sgmt2Check.get(0).getLink().getId()));
            return null;
        } else {
            nextSgmt = startSgmt;
            sortedSgmts = new ArrayList<>();
        }

        while (nextSgmt != null) {
            sortedSgmts.add(nextSgmt);
            // ��ȡSegment�յ�
            final GeoPoint endPnt = nextSgmt.getCtrlPoints().get(nextSgmt.getCtrlPoints().size() - 1);
            // ����segment������������յ��غ�
            nextSgmt = sgmt2Check.stream().filter(e -> e.getCtrlPoints().get(0).equal(endPnt)).findFirst().orElse(null);
        }
        return sortedSgmts;
    }
    public static List<long[]> readDoubleCR(QueryRunner qr, String sql) throws SQLException{
        List<Object[]> result;
        // ����������
        result = qr.query(sql, new ArrayListHandler());
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

        LineString[] linesLane2 = ((MultiLineString) geometry.getGeometry()).getLines();
        List<GeoPoint> ctrlPoints = new ArrayList<>();
        for (LineString line : linesLane2) {
            for (Point p : line.getPoints()) {
                GeoPoint gPoint = CoordTransformUtils.latlon2plane(new GeoPoint(p.getX(), p.getY(), p.getZ()));
                long numOfSamePoints= ctrlPoints.stream().filter(pnt->pnt.equal(gPoint)).count();
                if(numOfSamePoints>=1)
                    System.out.println("Warning: " + networkObjInfo + "����" + numOfSamePoints + "���ظ�����");
                ctrlPoints.add(gPoint);
            }
        }
        return ctrlPoints;

    }

    private static long obj2Long(Object obj){
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

    private static double obj2Double(Object obj){
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
