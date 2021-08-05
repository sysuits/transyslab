package com.transyslab.commons.io;

public interface IOWriter {

    void write(String str);

    void flushBuffer();

    default void writeNFlush(String str){
        write(str);
        flushBuffer();
    }

    void closeWriter();
}
