package javax.arang.phasing.snp;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedBlock;
import javax.arang.phasing.util.PhasedSNP;

public class AssignPS extends I2Owrapper {

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
			pos = Integer.parseInt(tokens[PhasedBlock.START]);
			startToEnd.put(pos, (Integer.parseInt(tokens[PhasedBlock.END])));
			startToPS.put(pos, tokens[PhasedBlock.ID]);
			startList.add(pos);
		}
		
		boolean isFirst = true;
		while (frSNP.hasMoreLines()) {
			line = frSNP.readLine();
			if (isFirst) {
				isFirst = false;
				if (!line.startsWith("#")) {
					fm.writeLine("#CHR\tPOS\tHAPLOTYPE_A\tHAPLOTYPE_B\tPS");
					continue;
				}
			}
			tokens = line.split(RegExp.TAB);
			pos = Integer.parseInt(tokens[PhasedSNP.POS]);
			ps = PhasedBlock.getPS(pos, startList, startToEnd, startToPS);
			fm.write(tokens[PhasedSNP.CHR] + "\t" + tokens[PhasedSNP.POS]
					+ "\t" + tokens[PhasedSNP.HAPLOTYPE_A] + "\t" + tokens[PhasedSNP.HAPLOTYPE_B] + "\t" + ps);
			if (tokens.length > PhasedSNP.PS + 1) {
				for (int i = PhasedSNP.PS + 1; i < tokens.length; i++) {
					fm.write("\t" + tokens[i]);
				}
			}
			fm.writeLine();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingSnpAssignPS.jar <in.snp> <in.phased.bed> <out.snp>");
		System.out.println("\t<in.snp>: Any PhasedSNP format, does not have to contain PS field CHR\tPOS\tHaplotypeA\tHaplotypeB");
		System.out.println("\t<in.phased.bed>: generated with phasingPhasedReadsToPhasedBlocks.jar");
		System.out.println("Arang Rhie, 2015-09-10. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new AssignPS().go(args[0], args[1], args[2]);
		} else {
			new AssignPS().printHelp();
		}
	}

}
