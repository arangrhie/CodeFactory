package javax.arang.bam;

import javax.arang.IO.bam.BamRwrapper;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.bam.util.BamRecord;
import javax.arang.bam.util.RefInfo;
import javax.arang.sam.SAMUtil;
import javax.arang.sam.Sam;

public class ToBedWiBX extends BamRwrapper {

	@Override
	public void hooker(BamReader fr) {
		//fr.readBamBlock();
		BamRecord alignment;
		RefInfo ref = fr.getRefInfo();
		String tags;
		String bc = "";
		while (fr.hasMoreAlignmentRecord()) {
			alignment = fr.getNextAlignmentRecord();
			if (SAMUtil.isAligned(alignment.getFlag())) {
				tags = alignment.getTags();
				for (int i = 0; i < tags.length() - 10; i++) {
					if (tags.charAt(i) == 'B' && tags.charAt(i + 1) == 'X' && tags.charAt(i + 2) == 'Z') {
						bc = tags.substring(i + 3, i + 3 + 8);
						break;
					}
				}
				System.out.println(alignment.getRefName(ref) +
						"\t" + (alignment.getPos() - 1) +
						"\t" + (alignment.getPos() - 1 + Sam.getMatchedBasesLen(alignment.getCigarString()) + Sam.getDeletedSplicedBasesLen(alignment.getCigarString())) +
						"\t" + alignment.getReadName() +
						"\t" + alignment.getFlag() +
						"\t" + alignment.getMQ() +
						"\t" + alignment.getCigarString() +
						"\t" + bc);
			}
		}
		System.err.println("Done");
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bamToBedWiBX.jar <in.bam>");
		System.out.println("\tExtract the alignment region in bam, with sequence read-id, cigar string, flag, mq, and BX");
		System.out.println("\t<sysout>: contig\tstart\tend\tread-id\tcigar\tflag\tmq\t[tag(s)]");
		System.out.println("Arang Rhie, 2017-04-06. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToBedWiBX().go(args[0]);
		} else {
			new ToBedWiBX().printHelp();
		}
	}

}
