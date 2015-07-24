/**
 * 
 */
package javax.arang.IO;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Arang
 *
 */
public class FileModifier {
	
	RandomAccessFile raf;
	String fileName;
	int n;
	String text;
	
	public FileModifier(String fileName, int n, String text) {
		this.fileName = fileName;
		this.n = n;
		this.text = text;
	}
	
//	private void algorithm() {
//		try {
//			raf = new RandomAccessFile(new File("testfile.txt"), "rw");
//			replace(n, text);
//			raf.close();		
//			
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	/***
	 * replace the n'th line to text
	 * @param n
	 * @param text
	 */
	public void replace(int n, String text) {
		try {
			raf.readLine();
			raf.writeChars(text);
			raf.getFilePointer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new FileModifier("testfile.txt", 0, ">chr1");
	}

}
