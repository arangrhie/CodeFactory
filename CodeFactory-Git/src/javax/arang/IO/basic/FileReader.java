/**
 * 
 */
package javax.arang.IO.basic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Reads specified file
 * @author Arang
 *
 */
public class FileReader {

	BufferedReader br;
	private static final int LINE_OFFSET = 70;
	String path;
	
	/***
	 * Reads specified file.
	 * File path directory can be written with "/" or "\\". 
	 * @param path
	 */
	public FileReader(String path) {
		try {
			this.path = path;
			if (path.endsWith(".gz")) {
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path))));
			} else {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * The full path of this FileReader file pointer.
	 * @return The given path including directory, file name
	 */
	public String getFullPath() {
		return path;
	}
	
	public String getDirectory() {
		return IOUtil.retrieveDirectory(path);
	}
	
	public String getFileName() {
		return IOUtil.retrieveFileName(path);
	}
	
	StringBuffer str = new StringBuffer("");
	
	/***
	 * Reads file specified with FileReader object.
	 * @return the line cascaded from the last line,
	 * or null if the line has reached to end of the file.
	 * 
	 */
	public String readLine(){
		str = new StringBuffer();
		try {
			str.append(br.readLine());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str.toString();
	}
	
	public String getLastLine() {
		return str.toString();
	}

	/***
	 * reads numLines from the n-th line of the file 
	 * @param from the line where to start, 1 is the first line
	 * @param numLines	the number of lines to read
	 * @return the StringBuffer containing characters
	 * 
	 */
	public StringBuffer readLine(int from, int numLines) {
		StringBuffer str = new StringBuffer();
		try {
			// skip the lines until we reach 'from'
			while ((--from) != 0) {
				br.readLine();
			}
			// read the number of lines specified in 'numLines'
			while((numLines--) != 0) {
				str.append(br.readLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	/***
	 * Reads sequence with length of length from the given fromIndex
	 * @param fromIndex		the index where start to read
	 * @param length		the length of sequence
	 * @return	the sequence
	 */
	public StringBuffer readSequence(int fromIndex, int length) {
		StringBuffer str = new StringBuffer();
		int lines = fromIndex / LINE_OFFSET;
		int residue = fromIndex % LINE_OFFSET;

		try {
			// skip the lines until we reach 'fromIndex'
			while ((--lines) > 0) {
				br.readLine();
			}

			String line = br.readLine();
			// Skeep if the line contains < ...
//			if (line.contains("<")) {
//				System.out.println(line);
//				line = br.readLine();
//			}

			// read the chars with length of 'length'
			if (length < (line.length() - line.substring(0, residue).length())) {
				str.append(line.substring(residue, residue + length));
			} 
			else {
				str.append(line.substring(residue));
				length -= str.length();

				lines = length / LINE_OFFSET;
				residue = length % LINE_OFFSET;

				while((--lines) >= 0) {
					str.append(br.readLine());
				}
				line = br.readLine();
				str.append(line.substring(0, residue));
			}
		} catch (Exception e) {
			// do nothing
		}
		return str;
	}
	
	public boolean hasMoreLines() {
		try {
			if (br.ready())	return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void closeReader() {
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * Seek forward for n lines
	 * @param offset
	 * @throws IOException 
	 */
	public String seekForward(int offset, int n) throws IOException {
		String line = "";
		try {
			br.mark(offset + 2);
			for (int i = 0; i < n; i++) {
				String nextLine = br.readLine();
				if (nextLine == null) {
					System.err.println("Reached end of reference.");
					break;
				}
				line = line + nextLine.trim();
			}
			br.reset();
		} catch (IOException e) {
			throw e;
		}
		return line;
	}
	
	/***
	 * Reset the buffered reader, and read from the beginning
	 */
	public void reset() {
		try {
			br.close();
			br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
