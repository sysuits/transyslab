package com.transyslab.app;

import com.transyslab.simcore.RdNetrworkGenerator;

public class NetworkConvert {
    public static void main(String[] args) {
        RdNetrworkGenerator generator = new RdNetrworkGenerator();
        if (args.length<1){
            System.err.println("miss master file.");
            return;
        }
        String master = args[0];
        String oFileName = null;
        for (int i = 1; i < args.length; i++) {
            switch (args[i]){
                case "-h" :
                    System.out.println("generator masterfileName [option]");
                    System.out.println("[options:]");
                    System.out.println("-h : show this message.");
                    System.out.println("-o fileName : specify output file name");
                    break;
                case "-o" :
                    oFileName = args[i+1];
                    i++;
                    break;
                default :
                    System.out.println("unknown command.");
                    return;
            }
        }
        generator.writeXml(master,oFileName);
    }
}
