package net.kettlemc.language.mysql;

import java.sql.PreparedStatement;

import net.kettlemc.language.mysql.async.PreparedStatementExec;
import net.kettlemc.language.mysql.async.PreparedStatementHandler;
import net.kettlemc.language.mysql.async.PreparedStatementQuery;

public class MySQLRequest {

	private String sql;
	private PreparedStatementHandler handler;

	public MySQLRequest(String sql, PreparedStatementHandler handler) {
		this.sql = sql;
		this.handler = handler;
	}

	public void doRequest(PreparedStatement statement) {
		try {
			if (handler instanceof PreparedStatementExec)
				((PreparedStatementExec) handler).onStatementExec(statement.execute());
			else
				((PreparedStatementQuery) handler).onResultReceive(statement.executeQuery());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getSql() {
		return sql;
	}

	public PreparedStatementHandler getHandler() {
		return handler;
	}
}