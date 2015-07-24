/**
 * 
 */
package javax.arang.IO;

import javax.arang.IO.bambasic.BinaryReader;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.Wrapper;

/**
 * @author Arang Rhie
 *
 */
public abstract class BinaryIFileOwrapper extends Wrapper {

	public void go(String inBinary, String outFile) {
		startTiming();
		
		BinaryReader br = new BinaryReader(inBinary);
		System.out.println("Processing file " + br.getFileName());
		FileMaker fm = new FileMaker(outFile);
		System.out.println("Into " + fm.getFileName());
		
		hooker(br, fm);
		
		br.close();
		fm.closeMaker();
		
		printTiming();
	}
	
	public abstract void hooker(BinaryReader br, FileMaker fm);
	
	/* (non-Javadoc)
	 * @see javax.arang.IO.Wrapper#printHelp()
	 */
	@Override
	public abstract void printHelp();

}
