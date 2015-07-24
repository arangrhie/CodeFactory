/**
 * 
 */
package javax.arang.spring;

import java.sql.Connection;

/**
 * @author Arang
 *
 */
public interface ConnectionMaker {
	public Connection makeConnection() throws Exception;
}
