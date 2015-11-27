/**
 * 
 */
package javax.arang.IO.basic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Arang
 *
 */
public class FileModifier {
	
	RandomAccessFile raf;
	String fileName;
	
	public FileModifier(String fileName) {
		this.fileName = fileName;
		try {
			raf = new RandomAccessFile(new File(fileName), "rw");
		} catch (FileNotFoundException e) {
			System.err.println(fileName + " Not Found...");
			System.exit(-1);
		}
	}
	
	public void closeModifier() {
		try {
			raf.close();
		} catch (IOException e) {
			
		}
	}
	
	
	
	/***
	 * replace the n'th line to text
	 * @param n
	 * @param text
	 */
	public void replace(int n, String text) {
		try {
			for (int i = 0; i < n-1; i++) {
				raf.readLine();
			}
			for (int i = 0; i < text.length(); i++) {
				raf.write(text.charAt(i));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return this.fileName;
	}

}
