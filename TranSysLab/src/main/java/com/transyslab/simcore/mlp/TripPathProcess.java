package com.transyslab.simcore.mlp;

import com.transyslab.commons.io.JdbcUtils;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.RoadNetwork;

import java.io.BufferedReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TripPathProcess {

    public static ArrayList<TripPathRecord> QueryTripPath(LocalDateTime fromTime, LocalDateTime toTime){
        ArrayList<TripPathRecord> result = new ArrayList<>();
        DateTimeFormatter dtf = SimulationClock.DATETIME_FORMAT;
        String fTime = dtf.format(fromTime);
        String tTime = dtf.format(toTime);
        String queryString =
                " select hphm,hpzl,ftime,sj_series,route from " +
                        "ana_rt_yw_trip_path"  +
                        " where ftime between '" + fTime + "'" +
                        " and '" + tTime + "'" +
                        " order by ftime";
        try {
            List<Object[]> rows = JdbcUtils.query(queryString);
            rows.forEach(r->
                    result.add(
                            new TripPathRecord(
                                    (String)r[0],
                                    (String)r[1],
                                    ((java.sql.Timestamp)r[2]).toLocalDateTime(),
                                    (String)r[3],
                                    (String)r[4])));
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public static TripPathRecord parseCSVRow(String csvRow){
        String[] r = csvRow.split(",");
        return new TripPathRecord(
                r[0],
                r[1],
                (Timestamp.valueOf(r[2])).toLocalDateTime(),
                r[3],
                r[4]);
    }
    // 筛选路径在区域内的车辆出行记录，途经区域节点>=2
    public static ArrayList<TripPathRecord> filterRegion(ArrayList<TripPathRecord> trpRecords, String region){
        ArrayList<TripPathRecord> result = new ArrayList<>();
        for(TripPathRecord tpr:trpRecords){
            int commonCounter = 0;
            String[] viaNodes = tpr.getViaNodes();
            String preNode = "";
            int startIndex=0, endIndex=0;
            HashMap<Integer,Integer> info = new HashMap<>();
            for(int i=0;i<viaNodes.length;i++){
                if(region.contains(viaNodes[i])){
                    if(preNode.isEmpty()){ // 第一个重合节点
                        commonCounter ++ ;
                        preNode = viaNodes[i];
                        startIndex = i;
                    }
                    else{
                        if(preNode.equals(viaNodes[i-1])) { //连续开始，上一节点也在区域内
                            commonCounter ++;
                            preNode = viaNodes[i];
                            endIndex = i;
                        }
                        if(i == viaNodes.length-1){ // 最后一个连续点，终断
                            //记录
                            if(endIndex > startIndex){
                                info.put(startIndex,commonCounter);
                            }
                        }
                    }
                }
                else{
                    if(!preNode.isEmpty()){
                        // 连续结束
                        endIndex = i-1;
                        //记录
                        if(endIndex > startIndex){
                            info.put(startIndex,commonCounter);
                        }
                        //清零
                        commonCounter = 0;
                        // 重置
                        preNode = "";
                        startIndex=0;
                        endIndex=0;
                    }
                }
            }
            startIndex = -1;
            int max = 0;
            for(Integer key:info.keySet()){
                int num = info.get(key);
                if(num>max){
                    startIndex = key;
                    max = num;
                }
            }
            if(startIndex>0){
                String[] filterNodes =  Arrays.copyOfRange(viaNodes, startIndex,startIndex + max);
                tpr.setViaNodes(filterNodes);
                result.add(tpr);
            }
        }
        return result;
    }
    public static ArrayList<TripPathRecord> estimateViaTime(ArrayList<TripPathRecord> nodeFilterTrips){
//        ArrayList<TripPathRecord> results = new ArrayList<>();
//        for(TripPathRecord tpr:nodeFilterTrips){
//          旅行时间分配
//        }
        return  nodeFilterTrips;
    }
    public static void append2InFlow(ArrayList<TripPathRecord> tripPathRecords, RoadNetwork network){
        int rvid = 1;
        for(TripPathRecord tpr:tripPathRecords){
            // vialink
            String[] viaNodes = tpr.getViaNodes();
            List<Link> path = new ArrayList<>();
            Link fLink = null, tLink = null;
            Lane inLane = null;
            for(int i=0;i<viaNodes.length-1;i++){
                long fnodeId = Long.parseLong(viaNodes[i]);
                long tnodeId = Long.parseLong(viaNodes[i+1]);
                Link link = network.findLink(fnodeId,tnodeId);
                long linkId = link.getId();
                path.add(link);
                if(i==0){ //
                    fLink = link;
                    List<Lane> lanes = fLink.getStartSegment().getLanes();
                    List<Double> ratios = new ArrayList<>();
                    for(Lane lane:lanes){
                        ratios.add(1.0/lanes.size());
                    }
                    inLane = (Lane)ratioSampling(lanes,ratios,1).get(0);
                }
                if(i+1==viaNodes.length-1)
                    tLink = link;
            }
            // 进入车道,驶出link
            if(inLane!=null && tLink!=null){
                SimulationClock sc = network.getSimClock();
                long time2Enter = tpr.getUpTime().toEpochSecond(ZoneOffset.of("+8"));
                ((MLPLink) fLink).appendIndentityInflow(inLane.getId(),tLink.getId(),time2Enter,22.2,
                        inLane.getLength(),rvid,path,tpr.getHphm(),tpr.getHpzl());

            }
            rvid ++ ;
        }

    }

    // 按比例（概率）进行采样生成随机结果
    public static List ratioSampling(List sampleObjects,List<Double> probability, int sampleSize){
        if(sampleObjects.size()!=probability.size()) {
            System.out.println("Error: 采样参数有误");
            return null;
        }
        List result = new ArrayList();
        for(int i=0;i<sampleSize;i++){
            // [0-1) 随机数
            double rnd = Math.random();
            for(int j=0;j<probability.size();j++){
                rnd -= probability.get(j);
                if(rnd<0){
                    result.add(sampleObjects.get(j));
                    break;
                }
            }
        }
        return result;
    }
    // 
    public static void main(String[] args) {
        String s1= "1680-1671-1672-1465-1434-1240-1241-1426-1445-1446-1530-1704-1705-1444-1482-1483-1485-1281-1088-1089-1282-1278-1084-1085-1279-1498-1499-1252-1253-1769-1024-1025-1040-1041-1258-1259-1036-1037-1491-1496-1489-1490-1557-1608-1711-1712-1188-1189-1176-1177-1190-1191-1664-1510-1363-1182-1183-1355-1559-1172-1173-1364-1481-1477-1478-1484-6608-1439-1437-1532-1442-1327-1126-1127-1326-1455-1778-1331-1332-1325-1128-1129-1777-1333-1130-1131-1328-1432-1433-1502-1501-1772-1645-1535-7088";
        //String nodeList = "7894,5264,6321,2456,5661";
        String nodeList = "1189,1176,1469,1329,1646,1567,1768,1507,1535,4943,1677,1651,1607," +
                "1608,1362,1490,1493,1499,1252,1509,1495,1492,1276,1577,1258,1259,1508,1494, " +
                "1767,1037,1491,1496,1248,1630,1557,1711,1712,1631,8368,1024,1025,1177,1190," +
                "1361,1363,1182,1576,4573,1427,1497,1448,1702,1510,1466,1447,1450,1250,1276," +
                "1264,1489,1498,1503,1555,1556,1251,1500,1279,1277,9550,1578,1328,1579,1593," +
                "1432,1433,1449,1256,1048,1626,1619,1620,1627,1036,1042,1645,1664,1694,1093," +
                "1715,1716,1188,1330,1786,1249,1732,1014,1015,1085,1191,1097,1090,1091,1277," +
                "1092,1043,1769,1253,1703,1772,1501,1502,1096,1721,1049,1040,1041,1263,1362";
        String[] viaNodes= s1.split("-");
        int commonCounter = 0;
        String preNode = "";
        int startIndex=0, endIndex=0;
        HashMap<Integer,Integer> info = new HashMap<>();
        for(int i=0;i<viaNodes.length;i++){
            if(nodeList.contains(viaNodes[i])){
                if(preNode.isEmpty()){ // 第一个重合节点
                    commonCounter ++ ;
                    preNode = viaNodes[i];
                    startIndex = i;
                }
                else{
                    if(preNode.equals(viaNodes[i-1])) { //连续开始，上一节点也在区域内
                        commonCounter ++;
                        preNode = viaNodes[i];
                        endIndex = i;
                    }
                    if(i == viaNodes.length-1){ // 最后一个连续点，终断
                        //记录
                        if(endIndex > startIndex){
                            info.put(startIndex,commonCounter);
                        }
                    }
                }
            }
            else{
                if(!preNode.isEmpty()){
                    // 连续结束
                    endIndex = i-1;
                    //记录
                    if(endIndex > startIndex){
                        info.put(startIndex,commonCounter);
                    }
                    //清零
                    commonCounter = 0;
                    // 重置
                    preNode = "";
                    startIndex=0;
                    endIndex=0;
                }
            }
        }
        startIndex = -1;
        int max = 0;
        for(Integer key:info.keySet()){
            int num = info.get(key);
            if(num>max){
                startIndex = key;
                max = num;
            }
        }
        if(startIndex>0){
            String[] filterNodes =  Arrays.copyOfRange(viaNodes, startIndex,startIndex + max);
            System.out.println();
        }
    }
}
