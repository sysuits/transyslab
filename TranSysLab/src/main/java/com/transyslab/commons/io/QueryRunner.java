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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class QueryRunner extends org.apache.commons.dbutils.QueryRunner {

	//wym 将Apache的para[][]改成List<Obj[]>的形式

	public QueryRunner(DataSource ds) {
		super(ds);
	}

	public int[] batch(String sql, List<Object[]> params) throws SQLException {
		Connection conn = this.prepareConnection();
		return this.batch(conn, true, sql, params);
	}


	public int[] batch(Connection conn, boolean closeConn, String sql, List<Object[]> params) throws SQLException {
		int count = 0;
		final int batchSize = 1000;
		if(conn == null) {
			throw new SQLException("Null connection");
		} else if(sql == null) {
			if(closeConn) {
				this.close(conn);
			}

			throw new SQLException("Null SQL statement");
		} else if(params == null) {
			if(closeConn) {
				this.close(conn);
			}

			throw new SQLException("Null parameters. If parameters aren't need, pass an empty array.");
		} else {
			PreparedStatement stmt = null;
			int[] rows = null;

			try {
				stmt = this.prepareStatement(conn, sql);

				long t_start = System.currentTimeMillis();

				for (Object[] p : params) {
					this.fillStatement(stmt, p);
					stmt.addBatch();
					if(++count % batchSize == 0) {
						stmt.executeBatch();
					}
				}

				System.out.println("batch processing time: " + (System.currentTimeMillis()-t_start));

				rows = stmt.executeBatch();
			} catch (SQLException var11) {
				this.rethrow(var11, sql, (Object[])params.toArray());
			} finally {
				this.close(stmt);
				if(closeConn) {
					this.close(conn);
				}

			}

			return rows;
		}
	}

	/*public void closeConn() throws SQLException {
		Connection conn = this.prepareConnection();
		this.close(conn);
	}*/
}
