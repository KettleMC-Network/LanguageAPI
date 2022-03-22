package net.kettlemc.language.mysql.async;

import java.sql.ResultSet;

public interface PreparedStatementQuery extends PreparedStatementHandler {

	public void onResultReceive(ResultSet result);

}