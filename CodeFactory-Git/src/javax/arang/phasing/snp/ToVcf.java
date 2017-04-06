package javax.arang.phasing.snp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class ToVcf extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String contig;
		String pos;
		String hapA;
		String hapB;
		String ps;
		
		fm.writeLine("##fileformat=VCFv4.1");
		fm.writeLine("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tNA12878");
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			contig = tokens[PhasedSNP.CHR];
			pos = tokens[PhasedSNP.POS];
			hapA = tokens[PhasedSNP.HAPLOTYPE_A];
			hapB = tokens[PhasedSNP.HAPLOTYPE_B];
			if (hapA.equalsIgnoreCase("D") || hapB.equalsIgnoreCase("D"))	continue;
			if (tokens.length-1 >=  PhasedSNP.PS) {
				ps = tokens[PhasedSNP.PS];
				writeToVcf(fm, contig, pos, hapA, hapB, ps);
			} else {
				writeToVcf(fm, contig, pos, hapA, hapB);
			}
		}
	}
	
	private void writeToVcf(FileMaker fm, String contig, String pos, String hapA, String hapB, String ps) {
		if (!hapA.equals(hapB)) {
			fm.writeLine(contig + "\t" + pos + "\t.\t" + hapA + "\t" + hapB + "\t20\tPASS\tAN=2\tGT:PS\t0|1:" + ps);
		}
	}
	
	private void writeToVcf(FileMaker fm, String contig, String pos, String hapA, String hapB) {
		if (!hapA.equals(hapB)) {
			fm.writeLine(contig + "\t" + pos + "\t.\t" + hapA + "\t" + hapB + "\t20\tPASS\tAN=2\tGT\t0|1");
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingSnpToVcf.jar <in.snp> <out.vcf>");
		System.out.println("\tMake an artificialy phased .vcf file");
		System.out.println("\t<in.snp>: phased .snp file");
		System.out.println("\t\tD will be not shown in .vcf file");
		System.out.println("\t<out.vcf>: CONTIG\tPOS\t.\tHaplotypeA\tHaplotypeB\tScore\tPASS\tAN=2\tGT\t0|1 or 1|0:PS");
		System.out.println("Arang Rhie, 2015-12-05. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToVcf().go(args[0], args[1]);
		} else {
			new ToVcf().printHelp();
		}
	}

}
