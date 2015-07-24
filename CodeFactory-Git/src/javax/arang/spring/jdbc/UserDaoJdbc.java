package javax.arang.spring.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.arang.spring.User;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDaoJdbc {

	private JdbcTemplate jdbcTemplate;
	private RowMapper<User> userMapper = new RowMapper<User>() {

		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setId(rs.getString("id"));
			user.setName(rs.getString("name"));
			user.setPassword(rs.getString("pwd"));
			user.setInstitution(rs.getString("institution"));
			user.setEmail(rs.getString("email"));
			return user;
		}
		
	};
	
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	

	public User get(final String id) {
		return this.jdbcTemplate.queryForObject(
				"SELECT * FROM users WHERE id = ?",
				new Object[] {id},
				this.userMapper);
	}
	
	public List<User> getAll() {
		return this.jdbcTemplate.query("SELECT * FROM users ORDER BY id", this.userMapper);
	}
	
	public void add(final User user) {
		this.jdbcTemplate.update("INSERT INTO users (id, name, pwd, institution, email)",
				user.getId(), user.getName(), user.getPassword(),
				user.getInstitution(), user.getEmail());
	}
	
	public int getCount() {
		return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM users");
	}
	
	public void deleteAll() {
		this.jdbcTemplate.update("DELETE FROM users");
	}
	
	
	
}
