package javax.arang.IO;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class LineTester extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		while (fr.hasMoreLines()) {
			fr.readLine();
			String line = fr.readLine();
			System.out.println(line + " length: " + line.length());
			break;
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inFile = args[0];
		String outFile = args[1];
		new LineTester().go(inFile, outFile);
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		
	}

}
