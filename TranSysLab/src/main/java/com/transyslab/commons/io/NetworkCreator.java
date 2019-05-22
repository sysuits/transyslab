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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class NetworkCreator {
    public static void readDataFromDB(RoadNetwork roadNetwork, String nodeIdList, boolean hasDoubleCR) throws SQLException {
        DataSource ds = JdbcUtils.getDataSource();
        org.apache.commons.dbutils.QueryRunner qr = new QueryRunner(ds);
        String sql;
        List<Object[]> result = null;
        // 读取节点数据, roadid为空的记录为交叉口节点
        sql = "select nodeid, st_transform(geom,2362), type from topo_node ";
        // 按节点集筛选
        if (nodeIdList != null)
            sql += "where nodeid in (" + nodeIdList + ")";
        else
            sql += "where roadid isnull";
        // 节点数据
        result = qr.query(sql, new ArrayListHandler());
        // 遍历，node -> node
        for (Object[] row : result) {
            long nodeid = ((BigDecimal) row[0]).longValue();
            Point pos = ((PGgeometry) row[1]).getGeometry().getFirstPoint();
            int type = (Integer) row[2];
            roadNetwork.createNode(nodeid, type, "N" + String.valueOf(nodeid),
                    new GeoPoint(pos.getX(), pos.getY(), pos.getZ()));
        }
        List<long[]> linkid = new ArrayList<>();
        if(hasDoubleCR) {
            sql = "select id, fnode_, tnode_ from doubleline ";
            if (nodeIdList != null)
                sql += "where fnode_ in (" + nodeIdList + ") and tnode_ in (" + nodeIdList + ")";
            linkid = readDoubleCR(qr, sql);
        }
        // 读取单线数据
        sql = "select gid, name, fnode, tnode from topo_centerroad ";
        if (nodeIdList != null)
            sql += "where fnode in (" + nodeIdList + ") and tnode in (" + nodeIdList + ")";
        // 中心线数据
        result = qr.query(sql, new ArrayListHandler());
        // 遍历中心线数据，topo_centerroad -> link
        for (Object[] row : result) {
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

            Link newLink = roadNetwork.createLink(id, 1, linkName, upNodeId, dnNodeId);
            // 与当前中心线同向的子路段数据，topo_link -> SgmtInOutRecord
            sql = "select id,st_transform(geom,2362) from topo_link " +
                    "where roadid = " + String.valueOf(crid) + " and flowdir = 1";
            List<Segment> sgmt2check = readSegments(qr, sql, roadNetwork);
            // 按上下游顺序存储Segment
            if (sgmt2check != null && sgmt2check.size() > 0) {
                List<Segment> sortedSgmts = sortSegments(sgmt2check);
                if (sgmt2check.size() != sortedSgmts.size()) {
                    roadNetwork.rmLastLink();
                    for (Segment smt : sgmt2check) {
                        roadNetwork.rmLanes(smt.getLanes());
                    }
                    roadNetwork.rmSegments(sgmt2check);
                    System.out.println("Error: " + String.valueOf(upNodeId) + "_" + String.valueOf(dnNodeId) + "子路段上下游关系推断出错");
                } else
                    newLink.setSegments(sortedSgmts);
            } else {// segment车道数为空
                roadNetwork.rmLastLink();
            }
            // TODO 有无反向路段的判断
            id = -1*crid;// 单线反向
            if(hasDoubleCR) {
                long[] ids = linkid.stream().filter(ls->ls[1] == dnNodeId && ls[2] == upNodeId).findFirst().orElse(null);
                if(ids!=null)
                    id = ids[0];
            }
            Link newLinkRvs = roadNetwork.createLink(id, 1, linkName, dnNodeId, upNodeId);
            // 与反向中心线同向的子路段数据，topo_link -> SgmtInOutRecord
            sql = "select id,st_transform(geom,2362) from topo_link " +
                    "where roadid = " + String.valueOf(crid) + " and flowdir = -1";
            sgmt2check = readSegments(qr, sql, roadNetwork);

            if (sgmt2check != null && sgmt2check.size() > 0) {
                List<Segment> sortedSgmts = sortSegments(sgmt2check);
                if (sgmt2check.size() != sortedSgmts.size()) {
                    roadNetwork.rmLastLink();
                    for (Segment smt : sgmt2check) {
                        roadNetwork.rmLanes(smt.getLanes());
                    }
                    roadNetwork.rmSegments(sgmt2check);
                    System.out.println("Error: " + String.valueOf(dnNodeId) + "_" + String.valueOf(upNodeId) + "子路段上下游关系推断出错");
                } else
                    newLinkRvs.setSegments(sortedSgmts);
            } else {// 无segment或segment的车道数为0
                roadNetwork.rmLastLink();
            }

        }
        // 目标区域的所有车道编号集，筛选出相关的车道连接器
        String laneIds = Arrays.toString(roadNetwork.getLanes().stream().mapToLong(e -> e.getId()).toArray());
        laneIds = laneIds.substring(1, laneIds.length() - 1);
        // 读取车道连接器数据
        sql = "select connectorid, fromlaneid, tolaneid, st_transform(geom,2362),geom from topo_laneconnector " +
                "where fromlaneid in (" + laneIds + ") and tolaneid in (" + laneIds + ")";
        // 车道连接器数据 LaneConnector -> Connector
        readConnectors(qr, sql, roadNetwork);
        //JdbcUtils.close();
    }

    public static List<Segment> readSegments(QueryRunner qr, String sql, RoadNetwork roadNetwork) throws SQLException {
        List<Object[]> segmentRslt = qr.query(sql, new ArrayListHandler());
        List<Segment> sgmt2check = new ArrayList<>();
        List<Lane> addLanes = new ArrayList<>();

        for (Object[] sgmtRow : segmentRslt) {
            PGgeometry geom = (PGgeometry) sgmtRow[1];
            List<GeoPoint> ctrlPoint = pgMultiLines2Points(geom,"Segment"+String.valueOf(sgmtRow[0])+" 平面坐标");

            Segment newSgmt = roadNetwork.createSegment(Long.parseLong((String)sgmtRow[0]), 60, 60, 0, ctrlPoint);

            String sql2GetLanes = "select laneid, laneindex, width, direction, st_transform(geom,2362), geom from topo_lane " +
                    "where segmentid = " + String.valueOf(newSgmt.getId());
            // 读取属于当前Segment的Lane
            List<Lane> lanesInSgmt = readLanes(qr, sql2GetLanes, roadNetwork);
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

    public static List<Lane> readLanes(QueryRunner qr, String sql, RoadNetwork roadNetwork) throws SQLException {
        // 读取Segment的Lane

        List<Object[]> laneRslt = qr.query(sql, new ArrayListHandler());
        List<Lane> lanesInSgmt = new ArrayList<>();
        // 遍历Lane数据
        for (Object[] laneRow : laneRslt) {
            long laneid = ((Long) laneRow[0]).longValue();
            int orderNum = ((Integer) laneRow[1]).intValue();
            double width;
            if (laneRow[2] == null)
                width = 3.75;
            else
                width = ((Double) laneRow[2]).doubleValue();
            String direction = (String) laneRow[3];
            // lane的几何属性,平面坐标
            PGgeometry geomLane = (PGgeometry) laneRow[4];
            List<GeoPoint> ctrlPoints = pgMultiLines2Points(geomLane,"Lane"+String.valueOf(laneid)+" 平面坐标");

            Lane newLane = roadNetwork.createLane(laneid, 3, orderNum, width, direction, ctrlPoints);

            lanesInSgmt.add(newLane);
        }
        return lanesInSgmt;
    }

    public static List<Connector> readConnectors(QueryRunner qr, String sql, RoadNetwork roadNetwork) throws SQLException {
        List<Connector> connectors = new ArrayList<>();
        List<Object[]> result = qr.query(sql, new ArrayListHandler());
        // 遍历车道连接器 LaneConnector -> Connector
        for (Object[] connRow : result) {
            long connId = ((BigDecimal) connRow[0]).longValue();
            long fLaneId = ((BigDecimal) connRow[1]).longValue();
            long tLaneId = ((BigDecimal) connRow[2]).longValue();
            // 临时改造 去除横向车道连接器 wym
            if (roadNetwork.findLane(fLaneId).getSegment().getId() ==
                    roadNetwork.findLane(tLaneId).getSegment().getId())
                continue;
            // connector的几何属性
            PGgeometry geom = (PGgeometry) connRow[3];
            LineString[] lines = ((MultiLineString) geom.getGeometry()).getLines();
            List<GeoPoint> ctrlPoints = new ArrayList<>();
            for (LineString line : lines) {
                for (Point p : line.getPoints()) {
                    ctrlPoints.add(new GeoPoint(p.getX(), p.getY(), p.getZ()));
                }
            }
            Connector connt = roadNetwork.createConnector(connId, fLaneId, tLaneId, ctrlPoints);
            connectors.add(connt);
        }
        return connectors;
    }

    public static List<Segment> sortSegments(List<Segment> sgmt2Check) {
        // 按流向对segment进行排序
        // 统计sgmt的公用顶点数，找出起始segment
        Segment startSgmt = null;
        for (Segment sgmt : sgmt2Check) {
            GeoPoint spnt = sgmt.getCtrlPoints().get(0);
            boolean isShared = false;
            for (Segment compareSgmt : sgmt2Check) {
                if (spnt.equal(compareSgmt.getCtrlPoints().get(compareSgmt.getCtrlPoints().size() - 1))) {
                    isShared = true;
                }
            }
            if (!isShared) {// 某条表示该segment是起始segment的某个坐标只出现一次，表示该segment是起始segment
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
            // 获取Segment终点
            final GeoPoint endPnt = nextSgmt.getCtrlPoints().get(nextSgmt.getCtrlPoints().size() - 1);
            // 下游segment，起点与上游终点重合
            nextSgmt = sgmt2Check.stream().filter(e -> e.getCtrlPoints().get(0).equal(endPnt)).findFirst().orElse(null);
        }
        return sortedSgmts;
    }
    public static List<long[]> readDoubleCR(QueryRunner qr, String sql) throws SQLException{
        List<Object[]> result;
        // 中心线数据
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
                GeoPoint gPoint = new GeoPoint(p.getX(), p.getY(), p.getZ());
                long numOfSamePoints= ctrlPoints.stream().filter(pnt->pnt.equal(gPoint)).count();
                if(numOfSamePoints>=1)
                    System.out.println("Warning: " + networkObjInfo + "存在" + numOfSamePoints + "个重复顶点");
                else
                    ctrlPoints.add(new GeoPoint(p.getX(), p.getY(), p.getZ()));
            }
        }
        return ctrlPoints;

    }
}
