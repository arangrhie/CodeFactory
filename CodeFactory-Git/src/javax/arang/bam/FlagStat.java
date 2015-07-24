package javax.arang.bam;

import javax.arang.IO.bam.BamRwrapper;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.IO.basic.FileMaker;
import javax.arang.bam.util.BamRecord;
import javax.arang.genome.sam.SAMUtil;

public class FlagStat extends BamRwrapper {

	private static FileMaker fm;
	
	@Override
	public void hooker(BamReader fr) {
		
		BamRecord record;
		int flag;
		
		int totalNumReads = 0;
		int totalPrimary = 0;
		int totalPrimaryDuplicates = 0;
		int totalPrimaryProperlyaligned = 0;
		while (fr.hasMoreAlignmentRecord()) {
			record = fr.getNextAlignmentRecord();
			flag = record.getFlag();
			totalNumReads++;
			if (!SAMUtil.isPrimaryAlignment(flag)) {
				continue;
			}
			totalPrimary++;
			
			if (SAMUtil.isDuplicate(flag)) {
				totalPrimaryDuplicates++;
			} else if(!SAMUtil.isUnmapped(flag)) {
				totalPrimaryProperlyaligned++;
			}
			
		}
		
		fm.writeLine(fr.getFileName() + "\t" + totalNumReads
				+ "\t" + totalPrimary
				+ "\t" + totalPrimaryDuplicates
				+ "\t" + (totalPrimary - totalPrimaryDuplicates)
				+ "\t" + totalPrimaryProperlyaligned);
	}
	
	@Override
	public void printHelp() {
		System.out.println("Get a more comprehensive view of FLAGs assigned to the bam file.");
		System.out.println("Usage: java -jar bamFlagStat.jar <*sort.dp.bam>");
		System.out.println("\t<out>: <primary_dup.stat>");
		System.out.println("\tOutput is printed as:");
		System.out.println("\t\tSample\tTotal nu. reads\tPrimary aligned\tDuplicated\tNon-dup.\tNon-dup., mapped");
		System.out.println("Arang Rhie, 2015-03-19. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			fm = new FileMaker(".", "primary_dup.stat");
			fm.writeLine("Sample\tTotal nu. reads\tPrimary aligned\tDuplicated\tNon-dup.\tNon-dup., mapped");
			for (int i = 0; i < args.length; i++) {
				new FlagStat().go(args[i]);
			}
			fm.closeMaker();
		} else {
			new FlagStat().printHelp();
		}
	}

}
