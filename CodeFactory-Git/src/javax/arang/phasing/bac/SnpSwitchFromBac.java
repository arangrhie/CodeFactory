package javax.arang.phasing.bac;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class SnpSwitchFromBac extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		boolean hasNot2ndSnpReached = true;
		
		// Hold the lines on the same phased block:
		// When the 2nd side BAC-based phased snp sais to Switch, then switch the entire buffer before writing
		// Else, when the first SNP sais 'Same', write it out as it is.
		// When a new phased block met wihtin 1_only, write it out as it is.
		StringBuffer buffer = new StringBuffer();
		String prevPS = "";
		String ps;
		boolean isToSwitch = false;
		String prevPS2 = "";
		String ps2;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			ps = tokens[PhasedSNP.PS];
			ps2 = tokens[ps2Idx];
			if (!prevPS.equals(ps) && !prevPS2.equals(ps2)) {
				hasNot2ndSnpReached = true;
			}
			
			// Not reaching the 1st snp covered with 2nd side BAC-based phased snp
			if (tokens[ps2Idx + 1].startsWith("1_Only")) {
				if (hasNot2ndSnpReached) {
					// When ps has changed, write out what's in the buffer
					if (!prevPS.equals(ps) && buffer.length() > 0) {
						writeLine(fm, buffer.toString(), false);
						buffer = new StringBuffer();
					}
					buffer.append(line + "\n");
				} else {
					if (prevPS.equals(ps)) {
						writeLine(fm, tokens, isToSwitch);
					} else {
						hasNot2ndSnpReached = true;
						buffer.append(line + "\n");
					}
				}
			//
			} else {
				// has met the 2nd SNP, for the first time
				hasNot2ndSnpReached = false;
				if (tokens[ps2Idx + 1].startsWith("Same")) {
					isToSwitch = false;
				} else {
					isToSwitch = true;
				}
				if (buffer.length() > 0) {
					writeLine(fm, buffer.toString(), isToSwitch);
					buffer = new StringBuffer();
				}
				
				writeLine(fm, tokens, isToSwitch);	
			}
			
			prevPS = ps;
			prevPS2 = ps2;
		}
		
		if (buffer.length() > 0) {
			writeLine(fm, buffer.toString(), isToSwitch);
		}
		
	}

	private void writeLine(FileMaker fm, String lines, boolean isToSwitch) {
		if (lines.contains("\n")) {
			String[] line = lines.split("\n");
			for (int i = 0; i < line.length; i++) {
				writeLine(fm, line[i].split(RegExp.TAB), isToSwitch);
			}
		} else {
			writeLine(fm, lines.split(RegExp.TAB), isToSwitch);
		}
	}
	
	private void writeLine(FileMaker fm, String[] tokens, boolean isToSwitch) {
		if (!isToSwitch) {
			fm.writeLine(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS]
					+ "\t" + tokens[PhasedSNP.HAPLOTYPE_A]
					+ "\t" + tokens[PhasedSNP.HAPLOTYPE_B]
					+ "\t" + tokens[PhasedSNP.PS]);
		} else {
			fm.writeLine(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS]
					+ "\t" + tokens[PhasedSNP.HAPLOTYPE_B]
					+ "\t" + tokens[PhasedSNP.HAPLOTYPE_A]
					+ "\t" + tokens[PhasedSNP.PS]);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: phasingSnpSwitchFromBac.jar <in.intersect.mark.lrswitch> <out.snp> [2nd_ps_idx]");
		System.out.println("\t<in.intersect.mark.switch>: Intersecting original .snp with BAC-phased .snp, holding all variants");
		System.out.println("\t\tPut Hom snps in a different file. Make sure <in> consists of het snps only.");
		System.out.println("\t\tLong range switches are also marked as Long, shorts are Short1/Short2.");
		System.out.println("\t<out.snp>: Left side of intersected SNPs will be reported, switched when BACs are supported.");
		System.out.println("\t[2nd_ps_idx]: BAC-phased snp PS idx. 1-based. DEFAULT=10");
		System.out.println("\t*Run phasingAssignPS.jar after running this code. The PS are assigned, but not merged.");
		System.out.println("Arang Rhie, 2015-11-28. arrhie@gmail.com");
	}
	

	private static int ps2Idx = 9;
	public static void main(String[] args) {
		if (args.length == 3) {
			ps2Idx = Integer.parseInt(args[2]) - 1;
			new SnpSwitchFromBac().go(args[0], args[1]);
		} else if (args.length == 2) {
			new SnpSwitchFromBac().go(args[0], args[1]);
		} else {
			new SnpSwitchFromBac().printHelp();
		}
	}

}
