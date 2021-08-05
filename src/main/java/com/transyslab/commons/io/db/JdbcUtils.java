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


package com.transyslab.commons.io.db;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import java.sql.PreparedStatement;
import java.util.List;


public class JdbcUtils implements GeneralDB{
	Configuration config;
	private DataSource dataSource;

	public JdbcUtils() {

	}

	public void initDataSource(){

		String driver = config.getString("driverClassName");
		String url = config.getString("dburl");
		String user = config.getString("username");
		String pwd = config.getString("password");
		int initialSize = config.getInt("initialSize");
		int maxActive = config.getInt("maxActive");
		int minIdle = config.getInt("minIdle");
		int maxIdle = config.getInt("maxIdle");
		int maxWait = config.getInt("maxWait");
		BasicDataSource bds = new BasicDataSource();
		bds.setDriverClassName(driver);
		bds.setUrl(url);
		bds.setUsername(user);
		bds.setPassword(pwd);
		bds.setInitialSize(initialSize);
		bds.setMaxTotal(maxActive);
		bds.setMinIdle(minIdle);
		bds.setMaxIdle(maxIdle);
		bds.setMaxWaitMillis(maxWait);
		dataSource = bds;

	}

	public static void release(Connection con, ResultSet rs, PreparedStatement pstm) {
		DbUtils.closeQuietly(con, pstm, rs);
	}
	public void close() {
		if (dataSource != null) {
			try {
				((BasicDataSource) dataSource).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			dataSource = null;
		}
	}

	public QueryRunner getQueryRunner(){
		return new QueryRunner(dataSource);
	}

	@Override
	public List<Object[]> query(String sql, String name) {
		try {
			if (name.substring(0,2).equals("DB")){
				String[] dbStr = config.getString(name).split(",");
				BasicDataSource bds = new BasicDataSource();
				bds.setUrl(dbStr[0]);
				bds.setUsername(dbStr[1]);
				bds.setPassword(dbStr[2]);
				//substitude
				dataSource = bds;
				List<Object[]> res = getQueryRunner().query(sql, new ArrayListHandler());
				//change back
				initDataSource();
				return res;
			}
			return getQueryRunner().query(sql, new ArrayListHandler());
		} catch (SQLException e) {
			System.err.println("DB error");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Configuration getConf() {
		return config;
	}

	@Override
	public void setConf(Configuration conf) {
		this.config = conf;
		initDataSource();
	}
}
