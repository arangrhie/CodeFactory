package javax.arang.spring;

import java.sql.SQLException;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class JdbcContext {
	private DataSource dataSource;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void workWithStatementStrategy(StatementStrategy stmt) throws SQLException {
		Connection c = null;
		PreparedStatement ps = null;
		
		try {
			c = this.dataSource.getConnection();
			ps = stmt.makePreparedStatement(c);
			ps.executeUpdate();
			
		} catch (SQLException e) {
			throw e;
		} finally {
			if (ps != null) { try {	ps.close();	} catch (SQLException e2) {	} }
			if (c != null) { try {	c.close();	} catch (SQLException e2) {	} }
		}
	}
}
