package com.transyslab.commons.io;

import com.transyslab.commons.io.Spatiotemporal;
import com.transyslab.commons.io.db.DBUtils;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.simcore.mlp.LinkInOutRecord;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcInoutRec implements Spatiotemporal {
    @Override
    public List<Object> select(LocalDateTime fromTime, LocalDateTime toTime, String simRegion, String sourceName) {
        String fTime = SimulationClock.DATETIME_FORMAT.format(fromTime.minusSeconds(30));
        String tTime =SimulationClock.DATETIME_FORMAT.format(toTime);
        List<Object> result = new ArrayList<>();
        String nodeList = null;
        if(!simRegion.contains("'")){
            nodeList = "";
            String[] region = simRegion.split(",");
            for(String node:region){
                nodeList +=  "'"+ node + "'" + ",";
            }
            nodeList = nodeList.substring(0,nodeList.length()-1);
        }
        else{
             nodeList = simRegion;
        }
//        String queryString =
//                " select fnode,tnode,hphm,hpzl,from_time,to_time, up_kk, up_cdbh, up_detect, up_cdzfx, up_park_id, up_fromlane, " +
//                        "dn_kk, dn_cdbh, dn_detect, dn_cdzfx, dn_park_id" +
//                        " from " + sourceName + " t" +
//                        " where (from_time between '" + fTime + "'" +
//                        " and '" + tTime + "'" ;
        String queryString =
                " select fnode,tnode,hphm,hpzl,from_time,to_time, up_fromlane " +
                        " from " + "s_xc_rt_segm_inout" + " t" +
                        " where (from_time between '" + fTime + "'" +
                        " and '" + tTime + "')" ;
        if(simRegion!=null)
            queryString += " and fnode in (" + nodeList + ") and tnode in (" + nodeList + ") " ;
        List<Object[]> rows = DBUtils.queryDB(queryString,sourceName);
        for(Object[] row:rows){
            long fnode = Long.parseLong(String.valueOf(row[0]));
            long tnode = Long.parseLong(String.valueOf(row[1]));
            String hphm = String.valueOf(row[2]);
            String hpzl = String.valueOf(row[3]);
            LocalDateTime sTime = ((java.sql.Timestamp) row[4]).toLocalDateTime();
            LocalDateTime eTime = ((java.sql.Timestamp) row[5]).toLocalDateTime();
            long upFromLaneId = 0;

            if(row[6]!=null && StringUtils.isNotEmpty(String.valueOf(row[6]))) {
                upFromLaneId = NetworkCreator.obj2Long(row[6]);
            }
            LinkInOutRecord record = new LinkInOutRecord(fnode,tnode,hphm,hpzl,sTime,eTime,"","","","","",
                    "","","","","",upFromLaneId);
            result.add(record);

        }
        // 按时间严格筛选,车辆在网时间至少5s
        result = result.stream().filter(r-> Duration.between(fromTime,((LinkInOutRecord)r).toTime).toMillis()/1000 > 5 && ((LinkInOutRecord)r).fromTime.compareTo(toTime)<0).collect(Collectors.toList());
        return result;
    }
}
