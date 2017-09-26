package javax.arang.IO;

import javax.arang.IO.basic.BufferedFileReader;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.Wrapper;


public abstract class I2OBufferedWrapper extends Wrapper {

	public void go(String inFile1, String inFile2, String outFile) {
		startTiming();
		
		BufferedFileReader fr1 = new BufferedFileReader(inFile1);
		BufferedFileReader fr2 = new BufferedFileReader(inFile2);
		FileMaker fm = new FileMaker(outFile);
		
		System.err.println("Processing file : " + fr1.getFileName() + ", " + fr2.getFileName());
		System.err.println("Into : " + fm.getFileName());
		hooker(fr1, fr2, fm);
		
		fr1.closeReader();
		fr2.closeReader();
		fm.closeMaker();

		printTiming();
	}
	
	public abstract void hooker(BufferedFileReader fr1, BufferedFileReader fr2, FileMaker fm);
	
	public abstract void printHelp();

}