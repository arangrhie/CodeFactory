package javax.arang.spring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;

public class UserDao {

	private DataSource dataSource;
	private JdbcContext jdbcContext;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setJdbcContext(JdbcContext jdbcContext) {
		this.jdbcContext = jdbcContext;
	}

	public void add(final User user) throws SQLException {
		this.jdbcContext.workWithStatementStrategy(
				new StatementStrategy() {

					@Override
					public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
						PreparedStatement ps = c.prepareStatement("INSERT INTO users(id, pwd, name, institution, email)" +
						" values(?,?,?,?,?)");
						ps.setString(1, user.getId());
						ps.setString(2, user.getPassword());
						ps.setString(3, user.getName());
						ps.setString(4, user.getInstitution());
						ps.setString(5, user.getEmail());
						return ps;
					}
				});
	}

	/***
	 * Return the user bean with the id. If not exists, return null.
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public User get(final String id) throws SQLException {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		User user = null;
		try {
			c = dataSource.getConnection();
			ps = c.prepareStatement("SELECT * FROM users WHERE id=?");
			ps.setString(1, id);
			rs = ps.executeQuery();
			if (rs.next()) {
				user = new User();
				user.setId(rs.getString("id"));
				user.setPassword(rs.getString("pwd"));
				user.setName(rs.getString("name"));
				user.setInstitution(rs.getString("institution"));
				user.setEmail(rs.getString("email"));
			}
			if (user == null) throw new EmptyResultDataAccessException(1);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e2) {
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e2) {
				}
			}
			if ( c != null) {
				try {
					c.close();
				} catch (Exception e2) {
				}
			}
		}
		return user;
	}

	public void deleteAll() throws SQLException {
		this.jdbcContext.workWithStatementStrategy(
				new StatementStrategy() {

					@Override
					public PreparedStatement makePreparedStatement(Connection c)
							throws SQLException {
						return c.prepareStatement("DELETE FROM users");
					}
				}
		);
	}


	public int getCount() throws Exception {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			c = dataSource.getConnection();
			ps = c.prepareStatement("SELECT count(*) FROM users");
			rs = ps.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e2) {
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e2) {
				}
			}
			if ( c != null) {
				try {
					c.close();
				} catch (Exception e2) {
				}
			}
		}
	}
}
