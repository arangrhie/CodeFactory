/**
 * 
 */
package javax.arang.IO.bam;

import javax.arang.IO.bambasic.BaiReader;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.IO.basic.Wrapper;
import javax.arang.bam.util.Bai;

/**
 * @author Arang Rhie
 *
 */
public abstract class BamBaiRwrapper extends Wrapper {
	public void go(String inFile) {
		startTiming();
		
		BaiReader br = new BaiReader(inFile + ".bai");
		System.out.println("Reading bam index file " + br.getFileName());
		Bai bai = br.getBai();

		BamReader fr = new BamReader(inFile);
		System.out.println("Processing file " + fr.getFileName());
		
		hooker(fr, bai);
		
		fr.closeReader();
		
		printTiming();
	}
	
	public abstract void hooker(BamReader fr, Bai bai);
	
}
