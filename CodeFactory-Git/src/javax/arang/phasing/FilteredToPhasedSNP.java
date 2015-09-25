package javax.arang.phasing;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import javax.arang.phasing.util.PhasedBlock;
import javax.arang.phasing.util.PhasedSNPBase;

public class FilteredToPhasedSNP extends I2Owrapper {

	@Override
	public void hooker(FileReader frSNP, FileReader frBed, FileMaker fm) {
		String line;
		String[] tokens;
		int pos;
		
		String ps;
		
		ArrayList<Integer> startList = new ArrayList<Integer>();	// START list for binary search
		HashMap<Integer, Integer> startToEnd = new HashMap<Integer, Integer>();	// START, END
		HashMap<Integer, String> startToPS = new HashMap<Integer, String>();	// START, PS
		
		while (frBed.hasMoreLines()) {
			line = frBed.readLine();
			tokens = line.split(RegExp.TAB);
			pos = Integer.parseInt(tokens[Bed.START]);
			startToEnd.put(pos, (Integer.parseInt(tokens[Bed.END])));
			startToPS.put(pos, tokens[Bed.NOTE]);
			startList.add(pos);
		}
		
		fm.writeLine("#CHR\tPOS\tHAPLOTYPE_A\tHAPLOTYPE_B\tPS");
		while (frSNP.hasMoreLines()) {
			line = frSNP.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			pos = Integer.parseInt(tokens[PhasedSNPBase.POS]);
			ps = PhasedBlock.getPS(pos, startList, startToEnd, startToPS);
			if (tokens[PhasedSNPBase.PS].startsWith("ToRemove")) {
				continue;
			}
			fm.write(tokens[PhasedSNPBase.CHR] + "\t" + tokens[PhasedSNPBase.POS]);
			if (tokens[PhasedSNPBase.PS].equals("A|A")) {
				fm.write("\t" + tokens[PhasedSNPBase.HAPLOTYPE_A]);
				fm.write("\t" + tokens[PhasedSNPBase.HAPLOTYPE_A]);
			} else if (tokens[PhasedSNPBase.PS].equals("B|B")) {
				fm.write("\t" + tokens[PhasedSNPBase.HAPLOTYPE_B]);
				fm.write("\t" + tokens[PhasedSNPBase.HAPLOTYPE_B]);
			} else if (tokens[PhasedSNPBase.PS].contains("to")) {
				char haplotypeToChange = tokens[PhasedSNPBase.PS].charAt(0);
				char changed = tokens[PhasedSNPBase.PS].charAt(tokens[PhasedSNPBase.PS].length() - 1);
				if (haplotypeToChange == 'A') {
					fm.write("\t" + changed + "\t" + tokens[PhasedSNPBase.HAPLOTYPE_B]);
				} else if (haplotypeToChange == 'B') {
					fm.write("\t" + tokens[PhasedSNPBase.HAPLOTYPE_A] + "\t" + changed);
				} else {
					System.out.println("[DEBUG] :: " + line);
				}
			} else {
				fm.write("\t" + tokens[PhasedSNPBase.HAPLOTYPE_A] + "\t" + tokens[PhasedSNPBase.HAPLOTYPE_B]);
			}
			fm.writeLine("\t" + ps);
		}
	}



	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingFilteredToPhasedSNP.jar <in.base.filt.snp> <in.phased.bed> <out.phased.snp>");
		System.out.println("\t<in.filt.snp>: generated with phasingPhasedReadsToSnpBaseCount.jar");
		System.out.println("\t<in.phased.bed>: generated with phasingPhasedReadsToPhasedBlocks.jar");
		System.out.println("\t<out.phased.snp>: phased snp file format. CHR POS HAPLOTYPE_A HAPLOTYPE_B PS");
		System.out.println("Arang Rhie, 2015-09-11. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new FilteredToPhasedSNP().go(args[0], args[1], args[2]);
		} else {
			new FilteredToPhasedSNP().printHelp();
		}
	}

}
