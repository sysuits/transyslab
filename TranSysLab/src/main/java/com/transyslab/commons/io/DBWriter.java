/*
 * Copyright 2019 The TranSysLab Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.transyslab.commons.io;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBWriter implements IOWriter{
    String newTableName;
    StringBuilder sb;
    Connection conn;

    public DBWriter(String newTableName, String createTableCmd, String dbUrl, String usr, String pwd) {
        this.newTableName = newTableName;
        sb = new StringBuilder();
        try {
            conn = DriverManager.getConnection(dbUrl, usr,pwd);
            if (createTableCmd!=null && !createTableCmd.equals("")){
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(createTableCmd);
            }
        }
        catch (Exception e){
            System.out.println("db failed");
        }
    }

    private InputStream transfer(){
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    @Override
    public void write(String str){
        synchronized (this){
            sb.append(str);
        }
    }

    @Override
    public void flushBuffer(){
        synchronized (this){
            if (sb.length()<=0)
                return;
            try {
                CopyManager copyManager = new CopyManager((BaseConnection)conn);
                long num = copyManager.copyIn("copy " + newTableName + " from STDIN DELIMITER ',' ", transfer());
//                System.out.println(newTableName + " updated " + num + " rows.");
                sb.setLength(0);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("fail to flush to db");
            }
        }
    }

    public void softFlush(){
        if (sb.length()>1e7){
            flushBuffer();
        }
    }

    @Override
    public void closeWriter(){
        try {
            flushBuffer();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}