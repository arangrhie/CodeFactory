package javax.arang.IO;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.arang.IO.basic.FileReader;

public class LineBucket {

	FileReader fr;
	String line;
	Vector<String> columns;
	
	public LineBucket(FileReader fr) {
		this.fr = fr;
		columns = new Vector<String>();
	}
	
	/***
	 * Returns the n'th column from the hasNext() line.
	 * @param n 0-based coordinate column number
	 * @return
	 */
	public String getColumn(int n) {
		return columns.get(n);
	}
	
	/***
	 * Check if fr has next line.
	 * If true, bucket initializes next line columns.
	 * Else, returns false.
	 * @return
	 */
	public boolean hasNext() {
		if (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			StringTokenizer st = new StringTokenizer(line);
			columns.clear();
			while (st.hasMoreTokens()) {
				columns.add(st.nextToken());
			}
			return true;
		} else {
			return false;
		}
	}
	
	public String getLine() {
		return line;
	}
}
