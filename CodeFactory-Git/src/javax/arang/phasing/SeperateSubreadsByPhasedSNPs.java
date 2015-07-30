package javax.arang.phasing;

import java.util.HashMap;

import javax.arang.IO.bam.BamBaiIFileOwrapper;
import javax.arang.IO.bambasic.BamReader;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bam.util.Bai;
import javax.arang.bam.util.BamRecord;

public class SeperateSubreadsByPhasedSNPs extends BamBaiIFileOwrapper {

	private static String outPrefix;
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingSeperateSubreadsByPhasedSNPs.jar <in.sam> <in.phased.snp> <out_prefix>");
		System.out.println("\t<in.bam>: hg19_subreads.sam");
		System.out.println("\t<in.phased.snp>: phased SNPs. CHR POS BaseOfHaplotypeA BaseOfHaplotypeB");
		System.out.println("\t<out_prefix>: <out_prefix>.HaplotypeA.list, <out_prefix>.HaplotypeB.list, <out_prefix>.unphased.list\n"
				+ "\t\twill be generated containing the Read ID of fasta.");
		System.out.println("\t\tEach list consists of: <Read_ID> <Total num. phased SNPs in this read> <Num. haplotype A SNPs> <Num. haplotype B SNPs>");
		System.out.println("Arang Rhie, 2015-07-16. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			outPrefix = args[2];
			new SeperateSubreadsByPhasedSNPs().go(args[0], args[1], args[2] + ".unphased.list");
		} else {
			new SeperateSubreadsByPhasedSNPs().printHelp();
		}
	}

	private static short CHR = 0;
	private static short POS = 1;
	private static short HAPLOTYPE_A = 2;
	private static short HAPLOTYPE_B = 3;
	private static short PS = 4;
	private static short DISTANCE = 5;
	
	@Override
	public void hooker(BamReader bamFr, Bai bai, FileReader fileFr, FileMaker fm) {
		
		// Store chr pos haplotypeA haplotypeB info
		HashMap<String, HashMap<Integer, String>> chrPosToHaplotypeAmap = new HashMap<String, HashMap<Integer, String>>();
		HashMap<String, HashMap<Integer, String>> chrPosToHaplotypeBmap = new HashMap<String, HashMap<Integer, String>>();
		HashMap<String, HashMap<Integer, String>> chrPosToPSmap = new HashMap<String, HashMap<Integer, String>>();
		
		String line;
		String[] tokens;
		String chr;
		int pos;
		String haplotypeA;
		String haplotypeB;
		String ps;
		while (fileFr.hasMoreLines()) {
			line = fileFr.readLine();
			tokens = line.split(RegExp.TAB);
			chr = tokens[CHR];
			pos = Integer.parseInt(tokens[POS]);
			haplotypeA = tokens[HAPLOTYPE_A];
			haplotypeB = tokens[HAPLOTYPE_B];
			ps = tokens[PS];
			if (!chrPosToHaplotypeAmap.containsKey(chr)) {
				
			}
		}
		
		BamRecord record;
		while (bamFr.hasMoreAlignmentRecord()) {
			record = bamFr.getNextAlignmentRecord();
			
			record.getPos();
			record.getSeq();
			record.getCigar();
		}
	}

	@Override
	public void hooker(BamReader bamFr, Bai bai, FileMaker fm) {}

}
