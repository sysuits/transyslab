package com.transyslab.commons.tools;

public class SyncCounter {
    private long num;
    public SyncCounter(){
        num = 0;
    }
    public synchronized void update(){
        num += 1;
    }
    public synchronized long currentNum(){
        return num;
    }
}
