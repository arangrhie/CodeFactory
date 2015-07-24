package javax.arang.bam;

import javax.arang.IO.bam.BamIOwrapper;
import javax.arang.IO.bambasic.BamMaker;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.bam.util.BamRecord;
import javax.arang.bam.util.RefInfo;

public class Cut10BamRecords extends BamIOwrapper {

	@Override
	public void hooker(BamReader fr, BamMaker fm) {
		if (fr.hasMoreAlignmentRecord()) {
			RefInfo ref = fr.getRefInfo();
			fm.write(ref.getRefInfo());
		}
		
		for (int i = 0; i < 10; i++) {
			if (fr.hasMoreAlignmentRecord()) {
				BamRecord record = fr.getNextAlignmentRecord();
				fm.write(record.getRecordBytes());
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bamCut2BamBlocks.jar <in.bam> <out.bam>");
		System.out.println("For testing purpose, cut 2 blocks from <in.bam> and copy to <out.bam>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new Cut10BamRecords().go(args[0], args[1]);
		} else {
			new Cut10BamRecords().printHelp();
		}
	}

}
