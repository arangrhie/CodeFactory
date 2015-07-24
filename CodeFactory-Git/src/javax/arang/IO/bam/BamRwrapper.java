/**
 * 
 */
package javax.arang.IO.bam;

import javax.arang.IO.bambasic.BamReader;
import javax.arang.IO.basic.Wrapper;

/**
 * @author Arang Rhie
 *
 */
public abstract class BamRwrapper extends Wrapper {

	public void go(String inFile) {
		startTiming();
		
		BamReader fr = new BamReader(inFile);
		System.out.println("Processing file " + fr.getFileName());
		
		hooker(fr);
		
		fr.closeReader();
		
		printTiming();
	}
	
	public abstract void hooker(BamReader fr);
	
	public abstract void printHelp();
}
