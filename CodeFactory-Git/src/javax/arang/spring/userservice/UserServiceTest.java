package javax.arang.spring.userservice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="../../../../applicationContext.xml")
public class UserServiceTest {
	
	@Autowired	UserService userService;
	@Autowired	UserDao userDao;
	@Autowired 	DataSource dataSource;
	@Autowired	PlatformTransactionManager transactionManager;
	
	User user;
	List<User> users;
	
	@Before
	public void setUp() {
		user = new User();
		users = Arrays.asList(
				new User("hadoop", "1q2w3e4r", "Hadoop", "GMI, Ewha", "arrhie@gmail.com", Level.BASIC, UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER-1, 0),
				new User("bioinfo", "gene2011!", "Bioinfolab", "Ewha", "bioinfo@gmail.com", Level.BASIC, UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER, 0),
				new User("test", "test", "Test Case", "GMI", "test@gmail.com", Level.SILVER, 60, UserServiceImpl.MIN_RECCOMEND_FOR_GOLD-1),
				new User("abcd", "abcd", "Test ABC", "GMI", "test@gmail.com", Level.SILVER, 60, UserServiceImpl.MIN_RECCOMEND_FOR_GOLD),
				new User("green", "p5", "Five", "GMI", "test@gmail.com", Level.GOLD, 100, Integer.MAX_VALUE)
		);
	}
	
	@Test
	public void upgradeLevels() {
		userDao.deleteAll();
		for (User user : users) {
			userDao.add(user);
		}
		
		userService.upgradeLevels();
		
		checkLevelUpgraded(users.get(0), false);
		checkLevelUpgraded(users.get(1), true);
		checkLevelUpgraded(users.get(2), false);
		checkLevelUpgraded(users.get(3), true);
		checkLevelUpgraded(users.get(4), false);
	}
	
	private void checkLevelUpgraded(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		if (upgraded) {
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		}
		else {
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}
	
	@Test
	public void upgradeAllOrNothing () {
		TestUserService testUserService = new TestUserService(users.get(3).getId());
		testUserService.setUserDao(userDao);
		
		UserServiceTx txUserService = new UserServiceTx();
		txUserService.setTransactionManager(transactionManager);
		txUserService.setUserService(testUserService);
		
		userDao.deleteAll();
		for(User user : users) {
			userDao.add(user);
		}
		
		try {
			txUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		checkLevelUpgraded(users.get(1), false);	// 처음 상태로 바뀌었나 확인
	}

	@Test()
	public void upgradeLevel() {
		Level[] levels = Level.values();
		for (Level level : levels) {
			if (level.nextLevel() == null) continue;
			user.setLevel(level);
			user.upgradeLevel();
			assertThat(user.getLevel(), is(level.nextLevel()));
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void cannotUpgradeLevel() {
		Level[] levels = Level.values();
		for (Level level : levels) {
			if (level.nextLevel() != null) continue;
			user.setLevel(level);
			user.upgradeLevel();
		}
	}
	
	@Test
	public void bean() {
		assertThat(this.userService, is(notNullValue()));
	}

}

class TestUserService extends UserServiceImpl {
	private String id;
	
	public TestUserService(String id) {
		this.id = id;
	}
	
	protected void upgradeLevel(User user) {
		if (user.getId().equals(this.id))	throw new TestUserServiceException();
		super.upgradeLevel(user);
	}

}

@SuppressWarnings("serial")
class TestUserServiceException extends RuntimeException {
	
}
