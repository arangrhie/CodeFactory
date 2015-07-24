/**
 * 
 */
package javax.arang.spring;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Arang
 *
 */
public class LocalDBConnection implements ConnectionMaker{

	String url = "jdbc:mysql://localhost:3306/fx";
	String id = "bioinfo";
	String pw = "gene2011!";
	Connection con = null;
	
	@Override
	public Connection makeConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		con = DriverManager.getConnection(url, id, pw);
		System.out.println("DB connected on " + url);
		return con;
	}

}
