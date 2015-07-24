/**
 * 
 */
package javax.arang.IO.bam;

import java.io.File;

import javax.arang.IO.bambasic.BaiReader;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;
import javax.arang.IO.basic.Wrapper;
import javax.arang.bam.util.Bai;

/**
 * @author Arang Rhie
 *
 */
public abstract class BamBaiFileRwrapper extends Wrapper {
	
	public void go(String inBam, String inFile) {
		startTiming();
		String baiFileName = inBam + ".bai";
		File baiFile = new File(baiFileName);
		if (!baiFile.exists()) {
			baiFileName = inBam.replace(".bam", ".bai");
		}
		System.out.println("Reading bam index file " + IOUtil.retrieveFileName(baiFileName));
		BaiReader br = new BaiReader(baiFileName);
		Bai bai = br.getBai();

		System.out.println("Processing file " + IOUtil.retrieveFileName(inBam));
		BamReader bamFr = new BamReader(inBam);
		
		FileReader fileFr = new FileReader(inFile);
		System.out.println("Processing file " + fileFr.getFileName());
		
		hooker(bamFr, bai, fileFr);
		
		bamFr.closeReader();
		fileFr.closeReader();
		
		printTiming();
	}

	public abstract void hooker(BamReader bamFr, Bai bai, FileReader fileFr);
	
	@Override
	public abstract void printHelp() ;

}
