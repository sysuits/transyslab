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

import javax.sql.DataSource;
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
            long nodeid = ((BigDecimal) row[0]).longValue();
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
            int crid = (int) row[0];
            String linkName = (String) row[1];
            long upNodeId = ((BigDecimal) row[2]).longValue();
            long dnNodeId = ((BigDecimal) row[3]).longValue();
            long id = crid;
            if(hasDoubleCR) {
                long[] ids = linkid.stream().filter(ls->ls[1] == upNodeId && ls[2] == dnNodeId).findFirst().orElse(null);
                if(ids!=null)
                    id = ids[0];
            }

            LinkedHashMap<Long,List<Object[]>> sgmnt2LaneData = new LinkedHashMap();
            // �������ֻ�����һ��
            List<Object[]> sgmtPosFiltered;
            if(linkData.get(0)[2] instanceof Integer)
                sgmtPosFiltered = linkData.stream().filter(sgmt->(int)sgmt[2] == crid && (int)sgmt[3] == 1).collect(Collectors.toList());
            else
                sgmtPosFiltered = linkData.stream().filter(sgmt->(long)sgmt[2] == crid && (int)sgmt[3] == 1).collect(Collectors.toList());
            // ɸѡ��������
            for(Object[] sgmt:sgmtPosFiltered){
                List<Object[]> laneFiltered= laneData.stream().filter(lane->(long)lane[5]==(long)sgmt[0]).collect(Collectors.toList());
                sgmnt2LaneData.put((long)sgmt[0],laneFiltered);
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
            if (linkData.get(0)[2] instanceof Long) {
                sgmtNegFiltered = linkData.stream().filter(sgmt -> (long) sgmt[2] == crid && (int) sgmt[3] == -1).collect(Collectors.toList());
            } else {
                sgmtNegFiltered = linkData.stream().filter(sgmt -> (int) sgmt[2] == crid && (int) sgmt[3] == -1).collect(Collectors.toList());
            }
            for (Object[] sgmt : sgmtNegFiltered) {
                List<Object[]> laneFiltered = laneData.stream().filter(lane -> (long) lane[5] == (long) sgmt[0]).collect(Collectors.toList());
                sgmnt2LaneData.put((long) sgmt[0], laneFiltered);
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
        List<Object[]> cnntFiltered = connectorData.stream().filter(cnnt->laneIds.contains(cnnt[1]) && laneIds.contains(cnnt[2])).collect(Collectors.toList());
        readConnectors(roadNetwork,cnntFiltered);
    }

    public static List<Segment> readSegments(RoadNetwork roadNetwork,List<Object[]> filteredSgmtData,LinkedHashMap<Long,List<Object[]>> sgmtId2Lanes)  {

        List<Segment> sgmt2check = new ArrayList<>();
        List<Lane> addLanes = new ArrayList<>();

        for (Object[] sgmtRow : filteredSgmtData) {
            PGgeometry geom = (PGgeometry) sgmtRow[1];
            List<GeoPoint> ctrlPoint = pgMultiLines2Points(geom,"Segment"+String.valueOf(sgmtRow[0])+" ƽ������");

            Segment newSgmt = roadNetwork.createSegment(Long.parseLong((String)sgmtRow[0]), 60, 60, 0, ctrlPoint);

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
            long laneid = ((Long) laneRow[0]).longValue();
            int orderNum = ((Integer) laneRow[1]).intValue();
            double width;
            if (laneRow[2] == null)
                width = 3.75;
            else
                width = ((Double) laneRow[2]).doubleValue();
            String direction = (String) laneRow[3];
            // lane�ļ�������,ƽ������
            PGgeometry geomLane = (PGgeometry) laneRow[4];
            List<GeoPoint> ctrlPoints = pgMultiLines2Points(geomLane,"Lane"+String.valueOf(laneid)+" ƽ������");

            Lane newLane = roadNetwork.createLane(laneid, 3, orderNum, width, direction, ctrlPoints);

            lanesInSgmt.add(newLane);
        }
        return lanesInSgmt;
    }

    public static List<Connector> readConnectors(RoadNetwork roadNetwork,List<Object[]> cnntFiltered){
        List<Connector> connectors = new ArrayList<>();
        // �������������� LaneConnector -> Connector
        for (Object[] connRow : cnntFiltered) {
            long connId = ((BigDecimal) connRow[0]).longValue();
            long fLaneId = ((BigDecimal) connRow[1]).longValue();
            long tLaneId = ((BigDecimal) connRow[2]).longValue();
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
                else
                    ctrlPoints.add(gPoint);
            }
        }
        return ctrlPoints;

    }
}
