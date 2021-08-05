package com.transyslab.app.service;

import com.transyslab.commons.tools.mutitask.Task;

public class ServiceTask extends Task {
    private boolean stop = false;
    public String fTime;
    public String tTime;
    public String nodeList;

    public ServiceTask(String fTime, String tTime, String nodeList){
        this.fTime = fTime;
        this.tTime = tTime;
        this.nodeList = nodeList;
    }

    public void stop(){
        stop = true;
    }

    public boolean needStop(){
        return stop;
    }
}
