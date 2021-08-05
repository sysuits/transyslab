package com.transyslab.commons.io.db;

import org.apache.commons.configuration2.Configuration;

import java.util.List;

public interface GeneralDB {
    List<Object[]> query(String sql, String name);
    Configuration getConf();
    void setConf(Configuration conf);
    default void close(){}
}
