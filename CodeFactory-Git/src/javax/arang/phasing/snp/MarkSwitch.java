package javax.arang.phasing.snp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class MarkSwitch extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			
			if (tokens[numPadds + PhasedSNP.HAPLOTYPE_A].equals(".")) {
				// 1 only
				fm.writeLine(line + "\t1_Only");
			} else if (tokens[PhasedSNP.HAPLOTYPE_A].equals(".")){
				// 2 only
				fm.writeLine(line + "\t2_Only");
			} else {
				// 1, 2 presented
				
				// Need to be switched?
				if (tokens[PhasedSNP.HAPLOTYPE_A].equals(tokens[PhasedSNP.HAPLOTYPE_B])
						&& tokens[numPadds + PhasedSNP.HAPLOTYPE_A].equals(tokens[numPadds + PhasedSNP.HAPLOTYPE_B])){
					fm.writeLine(line + "\tHom");
				} else if (tokens[PhasedSNP.HAPLOTYPE_A].equals(tokens[numPadds + PhasedSNP.HAPLOTYPE_B])
						&& tokens[PhasedSNP.HAPLOTYPE_B].equals(tokens[numPadds + PhasedSNP.HAPLOTYPE_A])) {
					fm.writeLine(line + "\tSwitch");
				} else if (tokens[PhasedSNP.HAPLOTYPE_A].equals(tokens[numPadds + PhasedSNP.HAPLOTYPE_A])
						&& tokens[PhasedSNP.HAPLOTYPE_B].equals(tokens[numPadds + PhasedSNP.HAPLOTYPE_B])) {
					fm.writeLine(line + "\tSame");
				} else {
					fm.writeLine(line + "\tDiff");
				}
				
			}
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingPhasedSnpMarkSwitch.jar <in.shared.snp> <out.mark.snp> [NUM_PADDS]");
		System.out.println("\t<in.shared.snp>: 2 file merged with phasingPhasedSnpIntersect.jar");
		System.out.println("\t<out.mark.snp>: mark if heterozygous SNPs are not in the same haplotype");
		System.out.println("\t\tMarking will be categorized into : Hom/Switch/Same/Diff");
		System.out.println("\t[NUM_PADDS]: DEFAULT = 5. If 2nd snp file is only represented, the number of padded columns for 1st snp.");
		System.out.println("Arang Rhie, 2015-11-23. arrhie@gmail.com");
	}
	
	private static int numPadds = 5;

	public static void main(String[] args) {
		if (args.length == 2) {
			new MarkSwitch().go(args[0], args[1]);
		} else if (args.length == 3) {
			numPadds = Integer.parseInt(args[2]);
			new MarkSwitch().go(args[0], args[1]);
		} else {
			new MarkSwitch().printHelp();
		}
		
	}

}
