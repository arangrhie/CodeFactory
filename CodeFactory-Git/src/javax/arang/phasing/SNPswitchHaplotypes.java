package javax.arang.phasing;

import java.util.ArrayList;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SNPswitchHaplotypes extends I2Owrapper {

	@Override
	public void hooker(FileReader frHet, FileReader frAll, FileMaker fm) {
		String line;
		String[] tokens;
		ArrayList<String> posSwitchList = new ArrayList<String>();
		
		while (frHet.hasMoreLines()) {
			line = frHet.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens[tokens.length - 1].equals("Switch")) {
				posSwitchList.add(tokens[PhasedSNP.POS]);
			}
		}
		System.out.println("Num. SNPs that will be switched:");
		System.out.println(posSwitchList.size());
		
		boolean switched = false;
		while (frAll.hasMoreLines()) {
			line = frAll.readLine();
			tokens = line.split(RegExp.TAB);
			if (posSwitchList.contains(tokens[PhasedSNP.POS])) {
				switched = !switched;
			}
			
			if (switched) {
				fm.writeLine(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS] + "\t" + tokens[PhasedSNP.HAPLOTYPE_B] + "\t" + tokens[PhasedSNP.HAPLOTYPE_A] + "\t" + tokens[PhasedSNP.PS]);
			} else {
				fm.writeLine(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS] + "\t" + tokens[PhasedSNP.HAPLOTYPE_A] + "\t" + tokens[PhasedSNP.HAPLOTYPE_B] + "\t" + tokens[PhasedSNP.PS]);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingSNPswitchHaplotypes.jar <chr.het.mark.snp> <chr.phased.snp> <out.phased.switched.snp>");
		System.out.println("\t<chr.het.mark.snp>: awk '$3!=$4' to get HETs and Generated with phasingMarkHaplotypeSwitch.jar");
		System.out.println("\t<chr.snp>: Generated with phasingSubreadBasedPhasedSNP.jar or phasingUpdatePhasedReads.jar,");
		System.out.println("\t\twith all the HET>HOM corrections applied, iterating phasingFilterPhasedSNPs.jar");
		System.out.println("\t\tIncluding ALL SNPs to be used for final phasingSeperateSubreadsByPahsedSNP.jar");
		System.out.println("Arang Rhie, 2015-08-03. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new SNPswitchHaplotypes().go(args[0], args[1], args[2]);
		} else {
			new SNPswitchHaplotypes().printHelp();
		}
	}

}
