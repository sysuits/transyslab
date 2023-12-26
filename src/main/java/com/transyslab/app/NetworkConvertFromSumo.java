package com.transyslab.app;

import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.simcore.RdNetrworkGenerator;
import com.transyslab.simcore.mlp.MLPEngine;
import org.apache.commons.configuration2.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NetworkConvertFromSumo extends NetworkConvert{
    @Override
    protected void initGenerator() {
        this.generator = new SumoNetworkGenerator();
    }
}

class SumoNetworkGenerator extends RdNetrworkGenerator{



    @Override
    public void writeXml(String masterFileName, String outputFileName) {

        String roorDir = new File(masterFileName).getParent() + "/";
        Configuration config = ConfigUtils.createConfig(masterFileName);
        String outPath = roorDir + config.getString("outputPath");

        try {
            String sumoNetFileName = roorDir + config.getString("sumoNet");
            Document sumoDom = docBuilder.parse(new File(sumoNetFileName));

            Element tslEle = dom.createElement("TranSysLab");

            tslEle.appendChild(generateNodes(rn.getNodes()));
            tslEle.appendChild(generateLinks(rn.getLinks()));
            tslEle.appendChild(generateConnectors(rn.getConnectors()));
            dom.appendChild(tslEle);

            if (outputFileName==null)
                outputFileName = outPath + "/" + "RN_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xml";

            writeOut(dom,outputFileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
