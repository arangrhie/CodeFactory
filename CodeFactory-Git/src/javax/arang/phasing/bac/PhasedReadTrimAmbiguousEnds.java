package javax.arang.phasing.bac;

import java.util.ArrayList;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.phasing.util.PhasedRead;

public class PhasedReadTrimAmbiguousEnds extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		ArrayList<String> posToRemove = new ArrayList<String>();
		String haplotype;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			if (tokens.length <= PhasedRead.HAPLOTYPE || tokens[PhasedRead.HAPLOTYPE].length() < 2) {
				continue;
			}
			haplotype = tokens[PhasedRead.HAPLOTYPE];
			if (haplotype.charAt(0) != haplotype.charAt(1) && !posToRemove.contains(tokens[PhasedRead.SNP_POS_LIST])) {
				posToRemove.add(tokens[PhasedRead.SNP_POS_LIST]);
			}
			
			if (haplotype.length() > 2 && haplotype.charAt(haplotype.length() - 2) != haplotype.charAt(haplotype.length() - 1)
					&& !posToRemove.contains(tokens[tokens.length - 1])) {
				posToRemove.add(tokens[tokens.length - 1]);
			}
		}
		fr.reset();
		
		int hapIdx = 0;
		String pos;
		StringBuffer newHap = new StringBuffer();
		ArrayList<String> posList = new ArrayList<String>();
		int countA = 0;
		int countB = 0;
		int countO = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			if (tokens.length <= PhasedRead.HAPLOTYPE || tokens[PhasedRead.HAPLOTYPE].length() < 2) {
				fm.writeLine(line);
				continue;
			}
			haplotype = tokens[PhasedRead.HAPLOTYPE];
			hapIdx = 0;
			posList.clear();
			newHap = new StringBuffer();
			countA = 0;
			countB = 0;
			countO = 0;
			for (int i = PhasedRead.SNP_POS_LIST; i < tokens.length; i++) {
				pos = tokens[i];
				if (!posToRemove.contains(pos)) {
					newHap.append(haplotype.charAt(hapIdx));
					posList.add(pos);
					if (haplotype.charAt(hapIdx) == 'A') {
						countA++;
					} else if (haplotype.charAt(hapIdx) == 'B') {
						countB++;
					} else {
						countO++;
					}
				}
				hapIdx++;
			}
			fm.write(tokens[PhasedRead.READ_ID] + "\t" + countA + "\t" + countB + "\t" + countO + "\t" + tokens[PhasedRead.START] + "\t" + tokens[PhasedRead.END]
					+ "\t" + tokens[PhasedRead.LEN] + "\t" + newHap.toString());
			for (int i = 0; i < posList.size(); i++) {
				fm.write("\t" + posList.get(i));
			}
			fm.writeLine();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingBacPhasedReadTrimAmbiguousEnds.jar <in.read> <out.read>");
		System.out.println("\t<in.read>: generated with phasingBaseCallPhasing.jar");
		System.out.println("\t<out.read>: beginning and ending base will be removed when making an ambiguous switch");
		System.out.println("Arang Rhie, 2015-10-08. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new PhasedReadTrimAmbiguousEnds().go(args[0], args[1]);
		} else {
			new PhasedReadTrimAmbiguousEnds().printHelp();
		}
		
	}

}
