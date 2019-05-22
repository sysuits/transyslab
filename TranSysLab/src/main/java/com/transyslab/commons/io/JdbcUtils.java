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
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import java.sql.PreparedStatement;


public class JdbcUtils {
	private static String dbPropertiesFileName = "src/main/resources/dbcp.properties";
	private static DataSource dataSource;
	// log4j2 通过 log4j-jcl 实现Common logging接口
	// 可修改配置文件，设置输出优先级DEBUG以上
//	private static Log logger = LogFactory.getLog(JdbcUtils.class);;
	public JdbcUtils() {

	}
	public static void initDataSource(){
		/*Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
		    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
		    .configure(params.properties()
		        .setFileName("src/main/resources/demo_neihuan/scenario2/dbcp.properties"));*/
		Configurations configs = new Configurations();

		try
		{
//			Configuration config = builder.getConfiguration();
			Configuration config = configs.properties(new File(dbPropertiesFileName));
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
		catch(ConfigurationException cex)
		{
			cex.printStackTrace();
		}

	}
	public static DataSource getDataSource(){
		if(dataSource == null)
			initDataSource();
		return dataSource;
	}
	public static Connection getConnection() throws SQLException{
		if(dataSource == null)
			initDataSource();
		return dataSource .getConnection();
	}
	public static void release(Connection con, ResultSet rs, PreparedStatement pstm) {
		DbUtils.closeQuietly(con, pstm, rs);
	}
	public static void close() {
		if (dataSource != null) {
			try {
				((BasicDataSource) dataSource).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			dataSource = null;
		}
	}

	public static boolean isIdle() {
		return (dataSource == null);
	}

	public static void setPropertiesFileName(String fileName){
		JdbcUtils.dbPropertiesFileName = fileName;
	}
}
