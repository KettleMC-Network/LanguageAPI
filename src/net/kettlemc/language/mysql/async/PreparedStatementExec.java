package net.kettlemc.language.mysql.async;

public interface PreparedStatementExec extends PreparedStatementHandler {

	public void onStatementExec(boolean exec);
}