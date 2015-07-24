package javax.arang.mapreduce.test;

import javax.arang.IO.basic.FileReader;

/***
 * An immitative line read input format abstract mapper class.
 * Implement map function, and call the go() method.
 * @author �ƶ�
 *
 */
public class Map {
	
	FileReader reader = null;
	StringBuffer line = new StringBuffer();
	
	protected Map(String path) {
		reader = new FileReader(path);
	}
	
	private Map() {	}
	
	public void go() {
		while (reader.hasMoreLines()) {
			map(reader.readLine().toString());
		}
		cleanup();
	}
	
	public void map(String line) {
		
	}

	public void cleanup() {
		
	}
}
