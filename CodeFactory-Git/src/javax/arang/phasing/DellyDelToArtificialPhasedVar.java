package javax.arang.phasing;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.fasta.Seeker;
import javax.arang.vcf.VCF;

public class DellyDelToArtificialPhasedVar extends I2Owrapper {

	@Override
	public void hooker(FileReader fr, FileReader frFasta, FileMaker fm) {
		String line;
		String[] tokens;
		String filter;
		String prevChr = "";
		String chr;
		int pos;
		int end;
		String ref;
		String gt;
		Seeker seeker = new Seeker(frFasta);
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				continue;
			}
			tokens = line.split(RegExp.TAB);
			filter = tokens[VCF.FILTER];
			if (!filter.equals("PASS")) {
				continue;
			}
			chr = tokens[VCF.CHROM];
			if (!chr.equals(prevChr)) {
				if (!seeker.goToContig(chr)) {
					System.err.println("[ERROR] Failed to load " + chr + " from given fasta.");
					System.exit(-9);
				}
			}
			// collect required fields
			ref = tokens[VCF.REF];
			if (ref.equals("N"))	continue;
			pos = Integer.parseInt(tokens[VCF.POS]);
			end = Integer.parseInt(VCF.parseINFO(tokens[VCF.INFO], "END"));
			
			if (end - pos > 10000) continue;
			gt = VCF.parseGT(tokens[VCF.FORMAT], tokens[VCF.SAMPLE]);

			// sampling on every <sampling> bases
			for (; pos < end; pos += 50) {
				if (gt.equals("1")) {
					fm.writeLine(chr + "\t" + pos + "\t" + seeker.baseAt(pos) + "\tD");
				} else if (gt.equals("2")) {
					fm.writeLine(chr + "\t" + pos + "\t" + "D" + "\tD");
				} // 0/0 will not be considered
			}
			prevChr = chr;
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingDellyDelToArtificialPhasedVar.jar <in.delly.sv.vcf> <in.fa> <out.delly.sv.artificial.var> [sampling_every_N_bp=50]");
		System.out.println("\t<in.delly.sv.vcf>: delly output. only deletions will be transfered for creating phasing marker variants.");
		System.out.println("\t<in.fa>: reference fasta");
		System.out.println("\t<out.delly.sv.artificial.var>: CHR\tPOS\tD\tD or CHR\tPOS\tHapA(deleted base on the other haplotype)\tD");
		System.out.println("\t\tGenotype (0/1 and 1/1) will be transfered to base D or D D.");
		System.out.println("\t\tLowQual deletions will not be used.");
		System.out.println("\t[sampling]: sampling and make a D base at every N bps. DEFAULT=50");
		System.out.println("\t* Deletions > 10,000 bp will be discarded.");
		System.out.println("Arang Rhie, 2016-09-29. arrhie@gmail.com");
	}

	private static int sampling;
	public static void main(String[] args) {
		if (args.length == 3) {
			new DellyDelToArtificialPhasedVar().go(args[0], args[1], args[2]);
		} else if (args.length == 4) {
			sampling = Integer.parseInt(args[3]);
			new DellyDelToArtificialPhasedVar().go(args[0], args[1], args[2]);
		} else {
			new DellyDelToArtificialPhasedVar().printHelp();
		}
	}

}
