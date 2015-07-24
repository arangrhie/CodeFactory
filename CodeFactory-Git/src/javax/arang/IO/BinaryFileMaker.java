/**
 * 
 */
package javax.arang.IO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.arang.IO.basic.IOUtil;

/**
 * @author Arang Rhie
 *
 */
public class BinaryFileMaker {
	
	private OutputStream os;
	private String dir;
	private String fileName;
	
	public BinaryFileMaker(String directory, String filename) {
		try {
			dir = directory;
			fileName = filename;
			File newfile = new File(dir);
			newfile.mkdirs();
			newfile = new File(dir+"/"+fileName);
			os = new FileOutputStream(newfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public BinaryFileMaker(String filename) {
		this(IOUtil.retrieveDirectory(filename), IOUtil.retrieveFileName(filename));
	}
	
	public String getDir() {
		return dir;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void write(byte[] text) {
		try {
			os.write(text);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeText(String text) {
		try {
			for (int i = 0; i < text.length(); i++) {
				os.write(text.charAt(i));
			}
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeLong(Long text) {
		byte[] bytes = ByteBuffer.allocate(8).putLong(text).array();
		write(bytes);
	}
	
	public void writeInt(int text) {
		byte[] bytes = ByteBuffer.allocate(4).putInt(text).array();
		write(bytes);
	}
	
	public void writeTab() {
		try {
			os.write('\t');
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeNull() {
		try {
			os.write('\0');
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeEnter() {
		try {
			os.write('\n');
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeMaker() {
		try {
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param length
	 */
	public void writeByte(int length) {
		try {
			os.write((byte) length);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
