package javax.arang.spring.userservice;

public class User {

	String id;
	String name;
	String password;
	String institution;
	String email;
	Level level;
	int login;
	int reccomend;
	
	public User() { }
	
	public User(String id, String name, String password, String email) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.email = email;
	}
	
	public User(String id, String name, String password, String institution, String email) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.institution = institution;
		this.email = email;
	}
	
	public User(String id, String name, String password, String institution, String email, Level level, int login, int reccomend) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.institution = institution;
		this.email = email;
		this.level = level;
		this.login = login;
		this.reccomend = reccomend;
	}
	
	public void upgradeLevel() {
		Level nextLevel = this.level.nextLevel();
		if (nextLevel == null) {
			throw new IllegalStateException(this.level + "은 업그레이드가 불가능합니다");
		}
		else {
			this.level = nextLevel;
		}
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getInstitution() {
		return institution;
	}
	public void setInstitution(String institution) {
		this.institution = institution;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public void setLevel(Level level) {
		this.level = level;
	}

	public int getLogin() {
		return login;
	}

	public void setLogin(int login) {
		this.login = login;
	}

	public int getReccomend() {
		return reccomend;
	}

	public void setReccomend(int reccomend) {
		this.reccomend = reccomend;
	}
	
	
}
