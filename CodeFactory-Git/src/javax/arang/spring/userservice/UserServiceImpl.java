package javax.arang.spring.userservice;

import java.util.List;

import org.springframework.mail.MailSender;


public class UserServiceImpl implements UserService{
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECCOMEND_FOR_GOLD = 30;

	UserDao userDao;

	MailSender mailSender;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}



	@Override
	public void add(User user) {
		// 

	}

	@Override
	public void upgradeLevels() {
		List<User> users = userDao.getAll();
		for (User user : users) {
			if (canUpgradeLevel(user)) {
				upgradeLevel(user);
			}
		}
	}

	private boolean canUpgradeLevel(User user) {
		Level currentLevel = user.getLevel();
		switch (currentLevel) {
		case BASIC:		return (user.getLogin() >= MIN_LOGCOUNT_FOR_SILVER);
		case SILVER:	return (user.getReccomend() >= MIN_RECCOMEND_FOR_GOLD);
		case GOLD:		return false;
		default:	throw new IllegalArgumentException("Unknown level: " + 
				currentLevel);
		}
	}

	protected void upgradeLevel(User user) {
		user.upgradeLevel();
		userDao.update(user);
	}


}
