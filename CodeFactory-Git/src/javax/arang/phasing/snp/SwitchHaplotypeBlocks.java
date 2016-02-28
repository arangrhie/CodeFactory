package javax.arang.phasing.snp;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedSNP;

public class SwitchHaplotypeBlocks extends I2Owrapper {

	@Override
	public void hooker(FileReader frList, FileReader frSNPs, FileMaker fm) {
		String line;
		String[] tokens;
		HashMap<String, String> psToMerged = new HashMap<String, String>();
		HashMap<String, Boolean> psToSwitch = new HashMap<String, Boolean>();
		
		while (frList.hasMoreLines()) {
			line = frList.readLine();
			tokens = line.split(RegExp.TAB);
			psToMerged.put(tokens[0], tokens[1]);
			if (tokens.length > 2 && tokens[2].equals("SwitchThisBlock")) {
				psToSwitch.put(tokens[0], true);
			} else {
				psToSwitch.put(tokens[0], false);
			}
		}
		
		String ps;
		while (frSNPs.hasMoreLines()) {
			line = frSNPs.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			ps = tokens[PhasedSNP.PS];
			
			if (!psToSwitch.containsKey(ps)) {
				fm.writeLine(line);
			}
			else if (psToSwitch.get(ps)) {
				fm.writeLine(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS] + "\t"
						+ tokens[PhasedSNP.HAPLOTYPE_B] + "\t"
						+ tokens[PhasedSNP.HAPLOTYPE_A] + "\t" + psToMerged.get(ps));
			} else {
				fm.writeLine(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS] + "\t"
						+ tokens[PhasedSNP.HAPLOTYPE_A] + "\t"
						+ tokens[PhasedSNP.HAPLOTYPE_B] + "\t" + psToMerged.get(ps));
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingSNPswitchHaplotypeBlocks.jar <chr.ps_to_switch.list> <in.phased.snp> <out.phased.switched.snp>");
		System.out.println("\t<chr.ps_to_switch.list>: generated with phasingMergePhasedSNPwi10X.jar");
		System.out.println("\t<in.phased.snp>: Generated with phasingSubreadBasedPhasedSNP.jar or phasingUpdatePhasedReads.jar containing PS block id");
		System.out.println("\t<out.phased.snp>: PS switched when the block is marked as 'SwitchThisBlock'.");
		System.out.println("Arang Rhie, 2015-08-21. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new SwitchHaplotypeBlocks().go(args[0], args[1], args[2]);
		} else {
			new SwitchHaplotypeBlocks().printHelp();
		}
	}

}
