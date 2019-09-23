package com.transyslab.app;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.io.TXTUtils;
import com.transyslab.roadnetwork.*;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPLane;
import com.transyslab.simcore.mlp.MLPLink;
import org.apache.commons.configuration2.Configuration;

import javax.swing.plaf.TextUI;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SSATransform {
    public static void main(String[] args) {
        String masterFileName = args[0];
        String roorDir = new File(masterFileName).getParent() + "/";
        Configuration config = ConfigUtils.createConfig(masterFileName);
//        String[] nodeList = config.getString("transformNodes").split(",");
        String outPath = roorDir + "/" + config.getString("outputPath");

        //initiate road network type
        RoadNetwork rn;

        if (config.getString("modelType").equals("MLP")){
            MLPEngine mlpEngine = new MLPEngine(masterFileName);
            mlpEngine.loadFiles();
            rn = mlpEngine.getNetwork();
        }
        else
            return;

        /*List<Node> intersections = new ArrayList<>();
        Arrays.stream(nodeList).forEach(n->{
            intersections.add(rn.findNode(Long.parseLong(n)));
        });*/

        TXTUtils writer = new TXTUtils(outPath + "/" + "ssa_helper.csv");
        writer.writeNFlush("NODEID,JKD,FLINKID,CDZ,TLINKID\r\n");

        TXTUtils laneWriter = new TXTUtils(outPath + "/" + "ssa_lane_helper.csv");
        laneWriter.writeNFlush("LANEID,POS,SEGMENTID,LINKID,NODEID,JKD,CDZ,TLANEID,TSEGMENTID,TLINKID\r\n");

        rn.getNodes().forEach(node -> {
            for (int i = 0; i < node.nUpLinks(); i++) {

                Link fLink = node.getUpLink(i);
                String linkDir = ((MLPLink)fLink).getLinkDir();
                String jkd;
                switch (linkDir){
                    case "E" : jkd = "东"; break;
                    case "S" : jkd = "南"; break;
                    case "W" : jkd = "西"; break;
                    case "N" : jkd = "北"; break;
                    default : continue;
                }

                String jkd_ = jkd;
                HashMap<String,Long> turningMap = new HashMap<>();
                fLink.getEndSegment().getLanes().forEach(lane -> {
                    ((MLPLane)lane).dnStrmConns().forEach(lc->{
                        String cdz = lc.getTurningDir();
                        String cdz_zh = cdz.equals("L")?"左转":
                                cdz.equals("S")?"直行":
                                        cdz.equals("R")?"右转":"";
                        laneWriter.write(lane.getId() + "," +
                                ((MLPLane) lane).getLnPosNum() + "," +
                                lane.getSegment().getId() + "," +
                                lane.getLink().getId() + "," +
                                node.getId() + "," +
                                jkd_ + "," +
                                cdz_zh  + "," +
                                lc.dnLane.getId() + "," +
                                lc.dnLane.getSegment().getId() + "," +
                                lc.dnLinkID() + "\r\n");
                        Long tLinkID = turningMap.get(cdz);
                        if (tLinkID==null)
                            turningMap.put(cdz,lc.dnLinkID());
                        else if (tLinkID!=lc.dnLinkID())
                            System.err.println("duplicated turning indication on node "
                                    + node.getId() + " fLink " + fLink.getId() + " cdz " + cdz
                                    + " tLink" + tLinkID + " " + lc.dnLinkID());
                    });
                });
                if (turningMap.get("L")!=null)
                    writer.write(node.getId() + "," + jkd + "," + fLink.getId() + ","
                            + "左转" + "," + turningMap.get("L") + "\r\n");
                if (turningMap.get("S")!=null)
                    writer.write(node.getId() + "," + jkd + "," + fLink.getId() + ","
                            + "直行" + "," + turningMap.get("S") + "\r\n");
                if (turningMap.get("R")!=null)
                    writer.write(node.getId() + "," + jkd + "," + fLink.getId() + ","
                            + "右转" + "," + turningMap.get("R") + "\r\n");
            }
            writer.flushBuffer();
            laneWriter.flushBuffer();
        });
        writer.closeWriter();
        laneWriter.closeWriter();
    }
}
