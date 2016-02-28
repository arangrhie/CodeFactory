package javax.arang.phasing;

import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.Haps;
import javax.arang.phasing.util.PhasedSNP;

public class HapsToVariant extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap;
		Integer[] snpPosList;
		
		// Read snp data from SHAPEIT
		snpPosToPhasedSNPmap = readHapsStoreSNPs(fr);
		snpPosList = snpPosToPhasedSNPmap.keySet().toArray(new Integer[0]);
		Arrays.sort(snpPosList);
		
		PhasedSNP snp;
		for (int i = 0; i < snpPosList.length; i++) {
			snp = snpPosToPhasedSNPmap.get(snpPosList[i]);
			fm.writeLine(snp.getChr() + "\t" + snp.getPos() + "\t" + snp.getHaplotypeA() + "\t" + snp.getHaplotypeB() + "\t" + snp.getPS());
		}
		
	}
	
	private HashMap<Integer, PhasedSNP> readHapsStoreSNPs(FileReader frHaps) {
		HashMap<Integer, PhasedSNP> snpPosToPhasedSNPmap = new HashMap<Integer, PhasedSNP>();
		String line;
		String[] tokens;
		int pos;
		PhasedSNP snp;
		String a;
		String b;
		while (frHaps.hasMoreLines()) {
			line = frHaps.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			//if (tokens[Haps.HAPLOTYPE_A].equals(tokens[Haps.HAPLOTYPE_B]))	continue;	// exclude homozygotes
			pos = Integer.parseInt(tokens[Haps.POS]);
			if (tokens[Haps.HAPLOTYPE_A].equals("0")) {
				a = tokens[Haps.ALLELE_A];
			} else {
				a = tokens[Haps.ALLELE_B];
			}
			if (tokens[Haps.HAPLOTYPE_B].equals("0")) {
				b = tokens[Haps.ALLELE_A];
			} else {
				b = tokens[Haps.ALLELE_B];
			}
			snp = new PhasedSNP(tokens[Haps.CHR], pos, a, b, tokens[Haps.POS]);
			snpPosToPhasedSNPmap.put(pos, snp);
		}
		return snpPosToPhasedSNPmap;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingHapsToVariants.jar <in.haps> <out.snp>");
		System.out.println("\t<in.haps>: phased.snp from SHAPEIT2");
		System.out.println("\t<out.snp>: CHR POS HaplotypeA_allele HaplotypeB_allele");
		System.out.println("Arang Rhie, 2015-11-18. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new HapsToVariant().go(args[0], args[1]);
		} else {
			new HapsToVariant().printHelp();
		}
	}

}
