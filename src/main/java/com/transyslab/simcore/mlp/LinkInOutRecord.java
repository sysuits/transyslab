package com.transyslab.simcore.mlp;

import java.time.LocalDateTime;

public class LinkInOutRecord {
    public long fnode;
    public long tnode;
    public String hphm;
    public String hpzl ;
    public LocalDateTime fromTime;
    public LocalDateTime toTime;
    public String dnDetect;
    public String dnKK;
    public String dnCDBH;
    public String dnCDFX;
    public String upDetect;
    public String upKK;
    public String upCDBH;
    public String upCDFX;
    public String upPark;
    public String dnPark;
    public long upFromLaneId;

    public LinkInOutRecord(long fnode, long tnode, String _hphm, String _hpzl, LocalDateTime _upTime, LocalDateTime _dnTime, String _dnDetect , String _dnKK, String _dnCDBH , String _dnCDFX,
                           String _upDetect, String _upKK , String _upCDBH, String _upCDFX , String _upPark, String _dnPark, long _upFromLaneId) {
        this.fnode = fnode;
        this.tnode  = tnode;
        this.hphm = _hphm;
        this.hpzl = _hpzl;
        this.fromTime = _upTime;
        this.toTime = _dnTime;
        this.dnDetect = _dnDetect;
        this.dnCDFX = _dnCDFX;
        this.dnKK = _dnKK;
        this.dnCDBH = _dnCDBH;
        this.upDetect = _upDetect;
        this.upCDFX = _upCDFX;
        this.upKK = _upKK;
        this.upCDBH = _upCDBH;
        this.upPark = _upPark;
        this.dnPark = _dnPark;
        this.upFromLaneId = _upFromLaneId;
    }
}
