package javax.arang.IO;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.Wrapper;

public abstract class StreamWrapper extends Wrapper {

	public void go(String inFile, String outFile) {
		startTiming();
		
		FileReader fr = new FileReader(inFile);
		System.err.println("Processing file " + fr.getFileName());
		FileMaker fm = new FileMaker(outFile);
		System.err.println("Into " + fm.getFileName());
		
		hooker(fr);
		
		fr.closeReader();
		fm.closeMaker();

		printTiming();
	}
	
	public abstract void hooker(FileReader fr);
	
	public abstract void printHelp();

}
