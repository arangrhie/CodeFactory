package javax.arang.phasing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class UpdatePhasedReads extends I2Owrapper {

	@Override
	public void hooker(FileReader frRead, FileReader frSnp, FileMaker fm) {
		HashMap<Integer, PhasedSNP> posToPhasedSNPmap = PhasedSNP.readSNPsStoreSNPs(frSnp, true);
		Integer[] posList = posToPhasedSNPmap.keySet().toArray(new Integer[0]);
		Arrays.sort(posList);
		int numSNPs = posList.length;
		System.out.println("Filtered SNPs: " + numSNPs);
		
		String line;
		String[] tokens;
		int countA = 0;
		int countB = 0;
		int countO = 0;
		int pos;
		String note;
		String haplotype;
		char base;
		char newBase;
		PhasedSNP snp;
		int haplotypeIdx;
		StringBuffer newHaplotype;
		ArrayList<Integer> posListInRead = new ArrayList<Integer>();
		while (frRead.hasMoreLines()) {
			line = frRead.readLine();
			tokens = line.split(RegExp.TAB);
			if (tokens.length <= PhasedRead.HAPLOTYPE)	continue;
			haplotype = tokens[PhasedRead.HAPLOTYPE];
			numSNPs = haplotype.length();
			newHaplotype = new StringBuffer();
			haplotypeIdx = 0;
			posListInRead.clear();
			countA = 0;
			countB = 0;
			countO = 0;

			for (int posIdx = PhasedRead.SNP_POS_LIST; posIdx < tokens.length; posIdx++) {
				pos = Integer.parseInt(tokens[posIdx]);
				if (!posToPhasedSNPmap.containsKey(pos)) {
					haplotypeIdx++;
					continue;
				}
				snp = posToPhasedSNPmap.get(pos);
				//System.out.println("[DEBUG] :: " + posIdx + " (" + tokens[posIdx] + ")");
				note = snp.getPS();
				base = haplotype.charAt(haplotypeIdx);
				if (note.equals("A|A") || note.equals("B|B")) {
					posListInRead.add(pos);
					newHaplotype.append("H");
				} else if (note.startsWith("Ato")) {
					posListInRead.add(pos);
					newBase = note.charAt(note.length() - 1);
					if (Character.isLowerCase(base) && Character.toUpperCase(base) == newBase) {
						newHaplotype.append("A");
						countA++;
					} else if (base == 'A') {
						newHaplotype.append(Character.toLowerCase(snp.getHaplotypeA().charAt(0)));
						countO++;
					} else {
						newHaplotype.append(base);
						countO++;
					}
				} else if (note.startsWith("Bto")) {
					posListInRead.add(pos);
					newBase = note.charAt(note.length() - 1);
					if (Character.isLowerCase(base) && Character.toUpperCase(base) == newBase) {
						newHaplotype.append("B");
						countB++;
					} else if (base == 'B') {
						newHaplotype.append(Character.toLowerCase(snp.getHaplotypeB().charAt(0)));
						countO++;
					} else {
						newHaplotype.append(base);
						countO++;
					}
				} else if (!note.equals("ToRemove")) {	// Do nothing for "ToRemove"
					posListInRead.add(pos);
					newHaplotype.append(base);
					if (base == 'A') {
						countA++;
					} else if (base == 'B') {
						countB++;
					} else {
						countO++;
					}
				}
				haplotypeIdx++;
			}
			haplotype = newHaplotype.toString();
			numSNPs = posListInRead.size();
			if (numSNPs == 0)	continue;
			fm.write(tokens[PhasedRead.READ_ID] + "\t" + countA + "\t" + countB + "\t" + countO + "\t"
					+ tokens[PhasedRead.START] + "\t" + tokens[PhasedRead.END] + "\t" + tokens[PhasedRead.LEN] + "\t"
					+ haplotype);
			for (int i = 0; i < numSNPs; i++) {
				fm.write("\t" + posListInRead.get(i));
			}
			fm.writeLine();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingUpdatePhasedReads.jar <in.read> <in.filt.snp> <out.read>");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new UpdatePhasedReads().go(args[0], args[1], args[2]);
		} else {
			new UpdatePhasedReads().printHelp();
		}
	}

}
