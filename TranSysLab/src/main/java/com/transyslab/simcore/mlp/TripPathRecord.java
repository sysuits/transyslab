package com.transyslab.simcore.mlp;

import java.time.LocalDateTime;

public class TripPathRecord {
    private String hphm;
    private String hpzl;
    private LocalDateTime upTime;
    private String[] viaNodes;
    private String sj_series;

    public TripPathRecord(String hphm, String hpzl, LocalDateTime upTime, String route, String sj_series) {
        this.hphm = hphm;
        this.hpzl = hpzl;
        this.upTime = upTime;
        this.viaNodes = route.split("-");
        this.sj_series = sj_series;
    }

    public String[] getViaNodes() {
        return this.viaNodes;
    }

    public void setViaNodes(String[] viaNodes) {
        this.viaNodes = viaNodes;
    }

    public String getTimeSeries() {
        return this.sj_series;
    }

    public LocalDateTime getUpTime(){
        return this.upTime;
    }

    public String getHphm() {
        return hphm;
    }

    public String getHpzl() {
        return hpzl;
    }

}