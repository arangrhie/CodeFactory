package javax.arang.genome.fasta;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ReplaceNBases extends I2Owrapper {

	@Override
	public void hooker(FileReader refFr, FileReader newFr, FileMaker fm) {
		String line;
		String[] tokens;
		HashMap<Integer, String> newBases = new HashMap<Integer, String>();

		int newIdx;
		String seq;
		// Extract the new bases to replace
		while (newFr.hasMoreLines()) {
			line = newFr.readLine();
			if (line.startsWith(">")) {
				tokens = line.split("[:.]+");
				newIdx = Integer.parseInt(tokens[1]);
				seq = newFr.readLine();
				newBases.put(newIdx, seq);
				System.out.println("[DEBUG] :: " + newIdx + " : " + seq);
			}
		}
		
		System.out.println("Number of sequences (contigs) that will be replaced to: " + newBases.size());
		
		int refIdx = 0;
		boolean isFilledGap = false;
		while (refFr.hasMoreLines()) {
			line = refFr.readLine();
			if (line.startsWith(">")) {
				fm.writeLine(line);
			} else {
				for (int i = 0; i < line.length(); i++) {
					refIdx++;
					if (newBases.containsKey(refIdx)) {
						System.out.println("[DEBUG] :: " + refIdx + " " + line.charAt(i));
						fm.write(newBases.get(refIdx));
						isFilledGap = true;
					} else {
						if (isFilledGap && (line.charAt(i) == 'n' || line.charAt(i) == 'N')) {
							// do nothing, skip it
						} else {
							fm.write(line.charAt(i));
							isFilledGap = false;
						}
					}
				}
				fm.writeLine();
			}
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaReplaceNBases.jar <ref.fa> <new.fa> <out.fa>");
		System.out.println("\tReplace the N bases in ref.fa with sequences from new.fa");
		System.out.println("\tSequence ID in <new.fa> should be named as <chr>:<N_gap_start + 1>..<N_gap_end +1>:...");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new ReplaceNBases().go(args[0], args[1], args[2]);
		} else {
			new ReplaceNBases().printHelp();
		}
	}

}
