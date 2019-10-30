package com.transyslab.simcore;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.roadnetwork.*;
import com.transyslab.simcore.mlp.MLPEngine;
import org.apache.commons.configuration2.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RdNetrworkGenerator {
    Document dom;
    RoadNetwork rn;

    public RdNetrworkGenerator(){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            dom = builder.newDocument();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("failed build xml");
            dom = null;
        }
    }

    public Element wrap(Element body, String wrapStr){
        Element wrapEle = dom.createElement(wrapStr);
        wrapEle.appendChild(body);
        return wrapEle;
    }

    public Element generateNodes(List<Node> nodes){
        Element nodesEle = dom.createElement("Nodes");
        nodesEle.setAttribute("nodeNum",String.valueOf(nodes.size()));
        nodes.stream()
                .forEach(node -> {
                    nodesEle.appendChild(generateNode(node));
                });
        return nodesEle;
    }

    public Element generateNode(Node node){
        Element nodeEle = dom.createElement("N");
        nodeEle.setAttribute("id",String.valueOf(node.getId()));
        nodeEle.setAttribute("type",String.valueOf(node.getType()));
        nodeEle.setAttribute("name",String.valueOf(node.getName()));
        nodeEle.setAttribute("geoString",String.valueOf(rn.getWorldSpace().recover(node.getPosPoint()).toString()));
        return nodeEle;
    }

    public Element generateLinks(List<Link> links){
        Element linksEle = dom.createElement("Links");
        linksEle.setAttribute("linkNum", String.valueOf(links.size()));
        links.stream()
                .forEach(link -> {
                    linksEle.appendChild(generateLink(link));
                });
        return linksEle;
    }

    public Element generateLink(Link link){
        Element linkEle = dom.createElement("L");
        linkEle.setAttribute("id",String.valueOf(link.getId()));
        linkEle.setAttribute("name",String.valueOf(link.getName()));
        linkEle.setAttribute("type",String.valueOf(link.type()));
        linkEle.setAttribute("upNode",String.valueOf(link.getUpNode().getId()));
        linkEle.setAttribute("dnNode",String.valueOf(link.getDnNode().getId()));
        for (int i = 0; i < link.nSegments(); i++) {
            linkEle.appendChild(generateSegment(link.getSegment(i)));
        }
        return linkEle;
    }

    public Element generateSegment(Segment segment){
        Element segmentEle = dom.createElement("S");
        segmentEle.setAttribute("id",String.valueOf(segment.getId()));
        segmentEle.setAttribute("speedLimit",String.valueOf(segment.speedLimit()));
        segmentEle.setAttribute("freeSpeed",String.valueOf(segment.getFreeSpeed()));
        segmentEle.setAttribute("gradient",String.valueOf(segment.getGrade()));
        segmentEle.setAttribute("ctrlPoints", GeoPoints.toString(rn.getWorldSpace().recover(segment.getCtrlPoints())));
        for (int i = 0; i < segment.nLanes(); i++) {
            segmentEle.appendChild(generateLane(segment.getLane(i)));
        }
        return segmentEle;
    }

    public Element generateLane(Lane lane){
        Element laneEle = dom.createElement("LA");
        laneEle.setAttribute("laneId",String.valueOf(lane.getId()));
        laneEle.setAttribute("rules",String.valueOf(lane.rules()));
        laneEle.setAttribute("orderNum",String.valueOf(lane.getOrderNum()));
        laneEle.setAttribute("width",String.valueOf(lane.getWidth()));
        laneEle.setAttribute("direction",lane.getDirection());
        laneEle.setAttribute("ctrlPoints", GeoPoints.toString(rn.getWorldSpace().recover(lane.getCtrlPoints())));
        return laneEle;
    }

    public Element generateConnectors(List<Connector> conns){
        Element connsEle = dom.createElement("LaneConnectors");
        conns.stream()
                .forEach(c->{
                    connsEle.appendChild(generateConnector(c));
                });
        return connsEle;
    }

    public Element generateConnector(Connector conn){
        Element connEle = dom.createElement("LC");
        connEle.setAttribute("connectorId",String.valueOf(conn.getId()));
        connEle.setAttribute("fLaneId",String.valueOf(conn.upLaneID()));
        connEle.setAttribute("tLaneId",String.valueOf(conn.dnLaneID()));
        connEle.setAttribute("ctrlPoints", GeoPoints.toString(rn.getWorldSpace().recover(conn.getShapePoints())));
        return connEle;
    }

    public static void writeOut(Document dom, String outputFileName){
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
//            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // send DOM to file
            tr.transform(new DOMSource(dom),
                    new StreamResult(new FileOutputStream(outputFileName)));

        } catch (TransformerException te) {
            System.out.println(te.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public void writeXml (String masterFileName, String outputFileName){
        String roorDir = new File(masterFileName).getParent() + "/";
        Configuration config = ConfigUtils.createConfig(masterFileName);
        String nodeList = config.getString("nodes2Construct");
        String outPath = roorDir + config.getString("outputPath");
        boolean lineStyle = config.getBoolean("doubleLine");

        //initiate road network type

        if (config.getString("modelType").equals("MLP")){
//            rn = new MLPNetwork();
            MLPEngine mlpEngine = new MLPEngine(masterFileName);
            mlpEngine.loadFiles();
            this.rn = mlpEngine.getNetwork();
        }
        else
            return;

        /*//read db
        JdbcUtils.setPropertiesFileName(masterFileName);
        try {
            NetworkCreator.readDataFromDB(rn,nodeList,lineStyle);
        } catch (SQLException e) {
            System.err.println("failed reading network from db.");
            e.printStackTrace();
        }
        finally {
            if (rn==null)
                return;
        }*/


        Element tslEle = dom.createElement("TranSysLab");

        tslEle.appendChild(generateNodes(rn.getNodes()));
        tslEle.appendChild(generateLinks(rn.getLinks()));
        tslEle.appendChild(generateConnectors(rn.getConnectors()));
        dom.appendChild(tslEle);

        if (outputFileName==null)
            outputFileName = outPath + "/" + "RN_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xml";

        writeOut(dom,outputFileName);
    }

}
