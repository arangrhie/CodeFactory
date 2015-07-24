/**
 * 
 */
package javax.arang.IO.bam;

import java.io.File;

import javax.arang.IO.BinaryFileMaker;
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
public abstract class BamBaiFileIBinaryOwrapper extends Wrapper {

	public void go(String inBam, String inFile, String outFile) {
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
		
		BinaryFileMaker fm = new BinaryFileMaker(outFile);
		System.out.println("Into " + fm.getFileName());
		
		hooker(bamFr, bai, fileFr, fm);
		
		bamFr.closeReader();
		fileFr.closeReader();
		fm.closeMaker();
		
		printTiming();
	}
	
	public void go(String inBam, String outFile) {
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
		
		BinaryFileMaker fm = new BinaryFileMaker(outFile);
		System.out.println("Into " + fm.getFileName());
		
		hooker(bamFr, bai, fm);
		
		bamFr.closeReader();
		fm.closeMaker();
		
		printTiming();
	}
	
	public abstract void hooker(BamReader bamFr, Bai bai, FileReader fileFr, BinaryFileMaker fm);
	public abstract void hooker(BamReader bamFr, Bai bai, BinaryFileMaker fm);
	
	
	@Override
	public abstract void printHelp() ;

}
