package javax.arang.phasing;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNPBase;

public class MarkHaplotypeSwitch extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String a;
		String b;
		int AtoA;
		int BtoB;
		int AtoB;
		int BtoA;
		int countSwitched = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")) {
				fm.writeLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			a = tokens[PhasedSNPBase.HAPLOTYPE_A];
			b = tokens[PhasedSNPBase.HAPLOTYPE_B];
			
			// Homo
			if (a.equals(b)) {
				fm.writeLine(line);
				continue;
			}
			
			// Hetero
			AtoA = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.AA + 2]);	// + 2 for the | signs
			BtoB = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.BB + 2]);
			AtoB = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.AB + 2]);
			BtoA = Integer.parseInt(tokens[PhasedSNPBase.OFFSET + PhasedSNPBase.BA + 2]);
			
			if (AtoA + BtoB < BtoA + AtoB) {
				fm.writeLine(line + "\tSwitch");
				countSwitched++;
			} else {
				fm.writeLine(line);
			}
		}
		System.out.println("Switch marked SNPs: " + countSwitched);
	}
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingMarkHaplotypeSwitch.jar <in.snp> <out.snp>");
		System.out.println("\t<in.snp>: Generated with iterative phasingPhasedReadsToSnpBlock.jar, phasingFilterPhasedSNP.jar");
		System.out.println("\t<out.snp>: Adding \"Switch\" at the end of line where haplotype switch is to be made");
		System.out.println("Arang Rhie, 2015-08-05. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new MarkHaplotypeSwitch().go(args[0], args[1]);
		} else {
			new MarkHaplotypeSwitch().printHelp();
		}
	}

}
