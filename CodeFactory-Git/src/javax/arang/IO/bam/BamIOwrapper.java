package javax.arang.IO.bam;

import javax.arang.IO.bambasic.BamMaker;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.IO.basic.Wrapper;

public abstract class BamIOwrapper extends Wrapper {

	public void go(String inFile, String outFile) {
		startTiming();
		
		BamReader fr = new BamReader(inFile);
		System.out.println("Processing file " + fr.getFileName());
		BamMaker fm = new BamMaker(outFile);
		System.out.println("Into " + fm.getFileName());
		
		hooker(fr, fm);
		
		fr.closeReader();
		fm.closeMaker();

		printTiming();
	}
	
	public abstract void hooker(BamReader fr, BamMaker fm);
	
	public abstract void printHelp();
}
