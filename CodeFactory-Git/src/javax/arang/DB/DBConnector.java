package javax.arang.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/***
 * 1. Install MySQL
 * 2. Install ODBC and test mysql on consol
 * 3. Download jdbc (lib folder)
 * 4. Add mysql-connector-java-*-bin.jar to /PATH-TO-JDK/lib, /PATH-TO-TOMCAT/lib
 * 5. Configure build path, and add mysql-connector*.jar under lib
 * 6. Create database fx, use it, and create database users (mysql/users.sql)
 * 7. run
 * @author Arang
 *
 */
public class DBConnector {
	String url = "jdbc:mysql://localhost:3306/";
	String id = "root";
	String pw = "dkfkdsid";
	Connection con = null;
	Statement stmt = null;
	
	/**
	 * DB Connector
	 * @param dbname name of the database to use
	 */
	public DBConnector(String dbname){
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}catch(ClassNotFoundException ex){
			System.err.println("Error while loading JDBC driver");
			ex.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try{
			url = url.concat(dbname);
			con = DriverManager.getConnection(url, id, pw);
			stmt = con.createStatement();
			System.out.println("DB connected on " + url);
		}catch(SQLException ex){
			System.err.println("DB connection failed");
			ex.printStackTrace();
			return;
		}
	}
	
	/**
	 * Send querey
	 * @param query SQL 
	 * @return  True if query has been sent successfuly
	 */
	public boolean sendQuery(String query){
		try{
			stmt.executeUpdate(query);
		}catch(SQLException ex){
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	/***
	 * Get query result set
	 * @param query
	 * @return	ResultSet from the query result
	 */
	public ResultSet getQueryResult(String query){
		ResultSet rs = null;
		try{
			rs = stmt.executeQuery(query);
		}catch(SQLException ex){
			System.err.println("Error while loading query");
			ex.printStackTrace();
		}
		return rs;
	}
	
	/***
	 * INSERT INTO table VALUES (values)
	 * @param table
	 * @param values
	 * @return
	 */
	public boolean insertValue(String table, String values){
		String sql = "insert ignore into "+table+" values ("+values+");";
		try{
			stmt.executeUpdate(sql);
		}catch(SQLException ex){
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * close DB connection(Statment, Connection)
	 * @return close  
	 */
	public boolean closeConnection(){
		try{
			stmt.close();
			con.close();
			System.out.println("MySQL: Bye!");
		}catch(SQLException ex){
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	/***
	 * Sample; mysql/users.sql
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		DBConnector conn = new DBConnector("fx");
		ResultSet rs = conn.getQueryResult("Select * from users where id=\'hadoop\';");
		try {
			while(rs.next()) {
				System.out.println(rs.getString("pwd"));
				// hadoop has password '1q2w3e4r', so this yields 1w2w3e4r.
			}
			conn.closeConnection();
		} catch (SQLException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}