/**
 * 
 */
package javax.arang.spring.userservice;

import javax.sql.DataSource;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Arang
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="../../../../applicationContext.xml")
public class UserDaoTest {

//	@Autowired
//	private ApplicationContext context;

	@Autowired	UserDao dao;
	@Autowired	DataSource dataSource;
	
	User user1;
	User user2;
	User user3;
	User user4;
	
	@Before
	public void setUp() {
//		ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
//		UserDao dao = new DaoFactory().userDao();
//		System.out.println(this.context);
//		System.out.println(this);
//		this.dao = context.getBean("userDao", UserDao.class);
		this.user1 = new User("hadoop", "1q2w3e4r", "Hadoop", "GMI, Ewha", "arrhie@gmail.com", Level.BASIC, 1, 0);
		this.user2 = new User("bioinfo", "gene2011!", "Bioinfolab", "Ewha", "bioinfo@gmail.com", Level.SILVER, 55, 10);
		this.user3 = new User("test", "test", "Test Case", "GMI", "test@gmail.com", Level.GOLD, 100, 40);
	}
	
	@Test
	public void addAndGet() throws Exception {
		dao.deleteAll();
		Assert.assertThat(dao.getCount(), CoreMatchers.is(0));
		
		dao.add(user1);
		Assert.assertThat(dao.getCount(), CoreMatchers.is(1));
		
		User user2 = dao.get("hadoop");
		
		Assert.assertThat(user2.getName(), CoreMatchers.is(user1.getName()));
		Assert.assertThat(user2.getPassword(), CoreMatchers.is(user1.getPassword()));
		
		User userget1 = dao.get(user1.getId());
		checkSameUser(user1, userget1);
		
		User userget2 = dao.get(user2.getId());
		checkSameUser(user2, userget2);
		
	}
	
	@Test
	public void count() throws Exception {
		dao.deleteAll();
		Assert.assertThat(dao.getCount(), CoreMatchers.is(0));
		
		dao.add(user1);
		Assert.assertThat(dao.getCount(), CoreMatchers.is(1));

		dao.add(user2);
		Assert.assertThat(dao.getCount(), CoreMatchers.is(2));
		
		dao.add(user3);
		Assert.assertThat(dao.getCount(), CoreMatchers.is(3));
		
	}
	
	@Test(expected=EmptyResultDataAccessException.class)
	public void getUserFailure() throws Exception {
		dao.deleteAll();
		Assert.assertThat(dao.getCount(), CoreMatchers.is(0));
		dao.get("unknown_id");
	}
	
	@Test(expected=DataAccessException.class)
	public void duplicateKey() {
		dao.deleteAll();
		
		dao.add(user1);
		dao.add(user1);
	}
	
	@Test
	public void update() {
		dao.deleteAll();
		
		dao.add(user1);	// 수정할 사용자
		dao.add(user2);	// 수정하지 않을 사용자
		
		user1.setName("Hadoop doop dop");
		user1.setPassword("1234");
		user1.setLogin(1000);
		user1.setReccomend(999);
		
		dao.update(user1);
		
		User user1update = dao.get(user1.getId());
		checkSameUser(user1, user1update);
		
		User user2same = dao.get(user2.getId());
		checkSameUser(user2, user2same);
	}
	
	public void checkSameUser(User user1, User user2) {
		Assert.assertThat(user1.getId(), CoreMatchers.is(user2.getId()));
		Assert.assertThat(user1.getPassword(), CoreMatchers.is(user2.getPassword()));
		Assert.assertThat(user1.getName(), CoreMatchers.is(user2.getName()));
		Assert.assertThat(user1.getInstitution(), CoreMatchers.is(user2.getInstitution()));
		Assert.assertThat(user1.getEmail(), CoreMatchers.is(user2.getEmail()));
		Assert.assertThat(user1.getLevel(), CoreMatchers.is(user2.getLevel()));
		Assert.assertThat(user1.getLogin(), CoreMatchers.is(user2.getLogin()));
		Assert.assertThat(user1.getReccomend(), CoreMatchers.is(user2.getReccomend()));
	}
}
