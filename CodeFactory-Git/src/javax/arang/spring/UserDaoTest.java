/**
 * 
 */
package javax.arang.spring;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Arang
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="../../../applicationContext.xml")
public class UserDaoTest {

//	@Autowired
//	private ApplicationContext context;

	@Autowired
	UserDao dao;
	
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
		this.user1 = new User("hadoop", "1q2w3e4r", "Hadoop", "GMI, Ewha", "arrhie@gmail.com");
		this.user2 = new User("bioinfo", "gene2011!", "Bioinfolab", "Ewha", "bioinfo@gmail.com");
		this.user3 = new User("test", "test", "Test Case", "GMI", "test@gmail.com");
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
}
