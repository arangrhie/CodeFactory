package javax.arang.spring.userservice;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class UserDaoJdbc implements UserDao{

	private JdbcTemplate jdbcTemplate;
	private RowMapper<User> userMapper = new RowMapper<User>() {

		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setId(rs.getString("id"));
			user.setPassword(rs.getString("pwd"));
			user.setName(rs.getString("name"));
			user.setInstitution(rs.getString("institution"));
			user.setEmail(rs.getString("email"));
			user.setLevel(Level.valueOf(rs.getInt("lv")));
			user.setLogin(rs.getInt("login"));
			user.setReccomend(rs.getInt("reccomend"));
			return user;
		}
		
	};
	
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public User get(final String id) {
		return this.jdbcTemplate.queryForObject(
				"SELECT * FROM users WHERE id = ?",
				new Object[] {id},
				this.userMapper);
	}
	
	@Override
	public List<User> getAll() {
		return this.jdbcTemplate.query("SELECT * FROM users ORDER BY id", this.userMapper);
	}
	
	@Override
	public void add(final User user) {
		jdbcTemplate.update("INSERT INTO users (id, pwd, name, institution, email, lv, login, reccomend) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?)",
				user.getId(), user.getPassword(), user.getName(), 
				user.getInstitution(), user.getEmail(),
				user.getLevel().intValue(), user.getLogin(), user.getReccomend());
	}
	
	@Override
	public int getCount() {
		return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM users");
	}
	
	@Override
	public void deleteAll() {
		jdbcTemplate.update("DELETE FROM users");
	}


	@Override
	public void update(User user) {
		jdbcTemplate.update("UPDATE users SET name = ?, pwd = ?, " +
				"lv = ?, login = ?, " +
				"reccomend = ? where id = ?",
				user.getName(), user.getPassword(),
				user.getLevel().intValue(), user.getLogin(), user.getReccomend(),
				user.getId());
	}

}
