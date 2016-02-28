package javax.arang.phasing;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSV2ndCycle;

public class Phased2ndCycleSVtoDelCandidate extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		boolean isDel = false;
		boolean hasSubDel = false;
		String delContig = "";
		String delStart = "";
		String delEnd = "";
		String delHaplotype = "";
		
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			if (!isDel) {
				// if (isDeletion)
				if (tokens[PhasedSV2ndCycle.ALLELES].charAt(tokens[PhasedSV2ndCycle.ALLELES].length() - 1) == 'D') {
					isDel = true;
					delContig = tokens[PhasedSV2ndCycle.CONTIG];
					delStart = tokens[PhasedSV2ndCycle.START];
					delEnd = tokens[PhasedSV2ndCycle.END];
					delHaplotype = tokens[PhasedSV2ndCycle.HAPLPOTYPE];
					if (PhasedSV2ndCycle.isSubstitution(tokens[PhasedSV2ndCycle.TYPE])) {
						hasSubDel = true;
					}
				} else {
					// was not D: do nothing
				}
			} else {
				// was D
				if (tokens[PhasedSV2ndCycle.ALLELES].charAt(tokens[PhasedSV2ndCycle.ALLELES].length() - 1) == 'D') {
					// && is D
					if (!tokens[PhasedSV2ndCycle.START].equals(delEnd)) {
						if (hasSubDel) {
							fm.writeLine(delContig + "\t" + delStart + "\t" + delEnd + "\tDELETION\t" + delHaplotype + "\t" + (Integer.parseInt(delEnd) - Integer.parseInt(delStart)));
						}
						delContig = tokens[PhasedSV2ndCycle.CONTIG];
						delStart = tokens[PhasedSV2ndCycle.START];
						delEnd = tokens[PhasedSV2ndCycle.END];
						delHaplotype = tokens[PhasedSV2ndCycle.HAPLPOTYPE];
					} else {
						// tokens[PhasedSV2ndCycle.START].equals(delEnd)
						delEnd = tokens[PhasedSV2ndCycle.END];
					}
					if (PhasedSV2ndCycle.isSubstitution(tokens[PhasedSV2ndCycle.TYPE])) {
						hasSubDel = true;
					} else {
						hasSubDel = false;
					}
				} else {
					if (hasSubDel) {
						fm.writeLine(delContig + "\t" + delStart + "\t" + delEnd + "\tDELETION\t" + delHaplotype + "\t" + (Integer.parseInt(delEnd) - Integer.parseInt(delStart)));
					}
					isDel = false;
					hasSubDel = false;
					delContig = "";
					delStart = "";
					delEnd = "";
					delHaplotype = "";
				}
			}
		}
		
		if (isDel && hasSubDel) {
			fm.writeLine(delContig + "\t" + delStart + "\t" + delEnd + "\tDELETION\t" + delHaplotype + "\t" + (Integer.parseInt(delEnd) - Integer.parseInt(delStart)));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar phasingPhased2ndCycleToDelCandidate.jar <in.sv> <out.del>");
		System.out.println("\tGet the deletion candidates, including those categorized as SUBSTITUTION + MULTI_ALLELE.");
		System.out.println("\tMULTI_ALLELE D is only considered when SUBSTITUTION is flanked.");
		System.out.println("\t<in.sv>: PasedA.sv + PhasedH.noA.sv, position sorted PhasedSV2ndCycle format.");
		System.out.println("\t<out.del>: Deletion merged bed, with category of DELETION.");
		System.out.println("Arang Rhie, 2015-12-16. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new Phased2ndCycleSVtoDelCandidate().go(args[0], args[1]);
		} else {
			new Phased2ndCycleSVtoDelCandidate().printHelp();
		}
	}

}
