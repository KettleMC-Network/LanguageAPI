package net.kettlemc.language.mysql.async;

import java.sql.PreparedStatement;

public interface PreparedStatementHandler {

	public void onStatementPrepared(PreparedStatement statement);

}