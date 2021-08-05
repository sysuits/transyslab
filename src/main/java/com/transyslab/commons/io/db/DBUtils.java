package com.transyslab.commons.io.db;

import com.google.inject.Inject;
import com.transyslab.commons.tools.install.Installer;
import org.apache.commons.configuration2.Configuration;

import java.util.List;

public class DBUtils {
    private static DBUtils utils;
    private GeneralDB dbHelper;

    @Inject
    public DBUtils(GeneralDB helper){
        this.dbHelper = helper;
    }

    public static DBUtils getInstance(){
        if (utils==null){
            utils = Installer.getInstance(DBUtils.class);
        }
        return utils;
    }

    public static void setConfig(Configuration config){
        getInstance().dbHelper.setConf(config);
    }
    public static List<Object[]> queryDB(String sql, String dbkey) {
        return getInstance().dbHelper.query(sql,dbkey);
    }
    public static void close(){
        getInstance().dbHelper.close();
    }
}
