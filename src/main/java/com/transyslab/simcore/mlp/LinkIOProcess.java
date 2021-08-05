package com.transyslab.simcore.mlp;

import com.alibaba.fastjson.JSON;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.transyslab.commons.io.*;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.install.Installer;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.Segment;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class LinkIOProcess {

    private static LinkIOProcess processor;
    private Spatiotemporal selector;

    @Inject
    public LinkIOProcess(@Named("InoutRec") Spatiotemporal selector){
        this.selector = selector;
    }

    public static LinkIOProcess getProcessor(){
        if (processor==null)
            processor = Installer.getInstance(LinkIOProcess.class);
        return processor;
    }

    public static List<LinkInOutRecord> getLinkIORecords(LocalDateTime fromTime, LocalDateTime toTime, String simRegion, String sourceName){
        List<Object> raw = getProcessor().selector.select(fromTime,toTime,simRegion,sourceName);
        List<LinkInOutRecord> result = new ArrayList<>();
        raw.forEach(r->result.add((LinkInOutRecord)r));
        return result;
    }

    public static Lane assignLane(Lane lastLane, Segment onSeg){
        double[] weigths = new double[onSeg.nLanes()];
        Arrays.stream(weigths).forEach(w->w=1.0);

        if (onSeg instanceof MLPSegment){
            MLPLink theLink = (MLPLink) onSeg.getLink();
            for (int i = 0; i < weigths.length; i++) {
                MLPLane fLane = ((MLPSegment) onSeg).getLane(i);
                weigths[i] = 1.0 / theLink.getLCRouteWeight(fLane,(MLPLane) lastLane);
            }
        }

        double sum = Arrays.stream(weigths).sum();
        Arrays.stream(weigths).forEach(w->w=w/sum);

        Random rnd = onSeg.getNetwork().getSysRand();
        double dice = rnd.nextDouble();
        for (int i = 0; i < weigths.length; i++) {
            if (dice < weigths[i])
                return onSeg.getLane(i);
            dice -= weigths[i];
        }

        System.out.println("lane selection error");
        return null;
//        return onSeg.getLane(rnd.nextInt(onSeg.nLanes()));
    }

    public static void append2InFlow(List<LinkInOutRecord> trimRecords, RoadNetwork roadNetwork,LocalDateTime fromTime){
        Map<String,List<LinkInOutRecord>> vhcPath = trimRecords.stream().collect(Collectors.groupingBy(r -> r.hphm + "_" + r.hpzl));
        vhcPath.values().forEach(
                inout ->{inout.sort(new Comparator<LinkInOutRecord>() {
                    @Override
                    public int compare(LinkInOutRecord o1, LinkInOutRecord o2) {
                        return o1.fromTime.compareTo(o2.fromTime);
                    }});}
        );
        // 生成发车数据（快照）
        for(String identity:vhcPath.keySet()){
            List<LinkInOutRecord> recs = vhcPath.get(identity);
            Link emitLink = null;
            Lane emitLane = null;
            double time2Enter = 0;
            double pos =0.0;
            long travelTime = 0;
            double avgSpeed = 22.22;
            int rvid = 1;
            String[] infos = identity.split("_");
            List<List<LinkInOutRecord>> paths =new ArrayList<>();
            paths.add(new ArrayList<>());
            paths.get(0).add(recs.get(0));
            for(int i =1;i<recs.size();i++){
                if(recs.get(i-1).tnode != recs.get(i).fnode && !recs.get(i-1).toTime.equals(recs.get(i).fromTime)) {
                    paths.add(new ArrayList<>());
                }
                paths.get(paths.size()-1).add(recs.get(i));
            }
            for(List<LinkInOutRecord> p:paths){
                List<Link> path = new ArrayList<>();
                // 找到可计算的路径开始路段，作为发车点
                for(int i =0;i<p.size();i++){
                    LinkInOutRecord rec = p.get(i);
                    Link itrLink = roadNetwork.findLink(rec.fnode,rec.tnode);
                    if(itrLink!=null && emitLink == null){
                        emitLink =itrLink;
                        travelTime = Duration.between(rec.fromTime,rec.toTime).toMillis();
                        avgSpeed = emitLink.length()/travelTime * 1000; // m/s
                        // 在网车
                        if(rec.fromTime.compareTo(fromTime)<0){
                            long tillFTime = Duration.between(rec.fromTime,fromTime).toMillis();
                            double distance = avgSpeed * tillFTime / 1000;
                            Segment onSegment = emitLink.getStartSegment();
                            double len = onSegment.getLength();
                            while(len<distance){
                                onSegment = onSegment.getDnSegment();
                                len += onSegment.getLength();
                            }
                            if(i < p.size()-1 && p.get(i+1).upFromLaneId!=0){
                                emitLane = assignLane(roadNetwork.findLane(p.get(i+1).upFromLaneId),onSegment);
                                if(emitLane == null)
                                    continue;
                            }
                            else{
                                Random rnd = roadNetwork.getSysRand();
                                int il = rnd.nextInt(onSegment.getLanes().size());
                                emitLane = onSegment.getLane(il);
                            }
                            // 线性参考位置
                            pos = emitLane.getLength()*(len-distance)/onSegment.getLength();
                            time2Enter = roadNetwork.getSimClock().secondsUntil(fromTime);
                        }
                        // 新出行
                        else{
                            Segment onSegment = emitLink.getStartSegment();
                            // 随机车道
                            Random rnd = roadNetwork.getSysRand();
                            int il = rnd.nextInt(onSegment.getLanes().size());
                            emitLane = onSegment.getLane(il);
                            pos = emitLane.getLength();
                            time2Enter = roadNetwork.getSimClock().secondsUntil(rec.fromTime);
                        }
                    }
                    path.add(itrLink);
                }
                if(emitLink!=null && emitLane!=null){
                    ((MLPLink) emitLink).appendIndentityInflow(emitLane.getId(),path.get(path.size()-1).getId(),time2Enter,avgSpeed,
                            pos,rvid,path,infos[0],infos[1]);
                }
            }
        }
    }

    public static void main(String[] args) {
//        List<LinkInOutRecord> test = getLinkIORecords(LocalDateTime.of(2019,11,3,18,10),LocalDateTime.of(2019,11,3,18,30),null,"S_XC_RT_SEGM_INOUT_SHANGHAI");
//        System.out.println();
        Configuration config = ConfigUtils.createConfig(args[0]);
        LocalDateTime fromTime = LocalDateTime.parse(config.getString("timeStart"), SimulationClock.DATETIME_FORMAT);
        LocalDateTime toTime = LocalDateTime.parse(config.getString("timeEnd"), SimulationClock.DATETIME_FORMAT);
        String simRegion = config.getString("nodes2Construct");
        String sourceName = "emitSource";
        List<LinkInOutRecord> records = LinkIOProcess.getLinkIORecords(LocalDateTime.of(2019,11,5,17,0), LocalDateTime.of(2019,11,5,18,0), simRegion, sourceName);
        String data = JSON.toJSONString(records);
        String roorDir = new File(args[0]).getParent() + "/";
        String outPath = roorDir + "/" + config.getString("outputPath");
        TXTUtils writer = new TXTUtils(outPath + "/" + "IO.JSON");
        writer.writeNFlush(data);
        writer.closeWriter();
        System.out.println("FNISHED");
    }
}
