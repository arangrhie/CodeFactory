/**
 * 
 */
package javax.arang.bam;

import java.util.Vector;

import javax.arang.IO.bam.BamBaiFileRwrapper;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.IO.basic.FileReader;
import javax.arang.bam.util.Bai;
import javax.arang.bam.util.BamRecord;
import javax.arang.bam.util.RefInfo;
import javax.arang.bed.util.Bed;

/**
 * @author Arang Rhie
 *
 */
public class FindRegion extends BamBaiFileRwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.BamBaiRwrapper#hooker(javax.arang.IO.BamReader, javax.arang.bam.Bai)
	 */
	@Override
	public void hooker(BamReader bamFr, Bai bai, FileReader fileFr) {
		Bed bed = new Bed(fileFr);
		
		RefInfo refInfo = bamFr.getRefInfo();
		
		// chr1 10010 10030
		int numChrs = bed.getChromosomes();
		System.out.println("numChromosomes: " + numChrs);
		for (int i = 0; i < numChrs; i++) {
			String chr = bed.getChr(i);
			int numRegions = bed.getNumRegions(chr);
			System.out.println("numRegions: " + numRegions);
			for (int j = 0; j < numRegions; j++) {
				long start = bed.getRegion(chr, j)[Bed.REGION_START];
				long end = bed.getRegion(chr, j)[Bed.REGION_END];
				System.out.println(chr + " " + start + " " + end);
				//long binID = Bai.region2Bin(start, end);
				Vector<Long[]> chunks = bai.getChunks(refInfo.getRefID(chr), start, end);
				if (chunks == null) {
					System.out.println("No record found for " + chr + ":" + start + "-" + end);
					continue;
				}
				for (Long[] chunk : chunks) {
					//System.out.println(binID + " : " + chunk[0] + " " + chunk[1]
					//		+ " - " + chunk[2] + " " + chunk[3]);
					bamFr.goTo(chunk[0], chunk[1]);
					while (bamFr.hasMoreAlignmentRecord()) {
						BamRecord record = bamFr.getNextAlignmentRecord();
						if (record.getPos() > end)	break;
						if (record.getPos() + record.getSeqLength() > start) {
							//System.out.println("record " + record.getRefName(refInfo) + " " + record.getPos() + " " + record.getSeq());
							System.out.println(record.getRecordLine(refInfo));
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.Wrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bamFindRegion.jar <in.bam> <region.bed>");
		System.out.println("\tFinds alignment records in <in.bam> that are in <region.bed>");
		System.out.println("\t<in.bam.bai> or <in.bai> should be in the same directory of <in.bam>.");
		System.out.println("Arang Rhie, 2014-12-16. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new FindRegion().go(args[0], args[1]);
		} else {
			new FindRegion().printHelp();
		}
	}

}
