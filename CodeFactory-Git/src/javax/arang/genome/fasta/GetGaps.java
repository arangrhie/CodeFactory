package javax.arang.genome.fasta;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class GetGaps extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String chr = "";
		char type = 'n';
		int len = 0;
		long basePos = 0l;
		int refNbaseLen = 0;
		boolean wasN = false;
		int numGaps = 0;
		int totalNumGaps = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			if (line.startsWith(">"))	{
				if (wasN) {
					writeLine(fm, basePos, refNbaseLen, type, chr, numGaps);
					refNbaseLen = 0;
					wasN = false;
				}
				chr = line.replace(">", "");
				basePos = 0;
				numGaps = 0;
				totalNumGaps += numGaps;
				continue;
			}
			len = line.length();
			for (int i = 0; i < len; i++) {
				basePos++;
				//if (line.charAt(i) == 'N') {
				if (line.charAt(i) == 'N' || line.charAt(i) == 'n') {
					if (!wasN) {
						wasN = true;
						type = line.charAt(i);
						fm.write(chr + "\t" + (basePos - 1));
						numGaps++;
						//System.out.println("[DEBUG] :: " + line);
					}
					refNbaseLen++;
				} else {
					if (wasN) {
						writeLine(fm, basePos, refNbaseLen, type, chr, numGaps);
						refNbaseLen = 0;
						wasN = false;
					}
				}
			}
		}
		if (wasN) {
			writeLine(fm, basePos, refNbaseLen, type, chr, numGaps);
		}
		totalNumGaps += numGaps;
		System.out.println("Total number of gaps: " + totalNumGaps);
	}
	
	private void writeLine(FileMaker fm, long basePos, int refNbaseLen, char type, String chr, int numGaps) {
		fm.writeLine("\t" + basePos + "\t" + refNbaseLen + "\t" + type + "\t" + chr + "_gap" + String.format("%03d", numGaps));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaGetGaps.java <in.fasta> <gap.bed>");
		System.out.println("\tRetrieve the gap positions in a bed format");
		System.out.println("Arang Rhie, 2015-12-18. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new GetGaps().go(args[0], args[1]);
		} else {
			new GetGaps().printHelp();
		}
	}

}
