package javax.arang.genome.fasta;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ExtractBlastResult extends I2Owrapper {

	@Override
	public void hooker(FileReader faFr, FileReader blastFr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String seqId;
		int cutIdx;
		boolean extractLeft = true;
		String[] blastLine;
		
		HashMap<String, String> blastList = new HashMap<String, String>();
		
		while (blastFr.hasMoreLines()) {
			line = blastFr.readLine();
			tokens = line.split("\\s+");
			if (tokens[12].equals("ExtractLeft")) {
				blastList.put(tokens[0], tokens[6] + "\ttrue");
			} else {
				blastList.put(tokens[0], tokens[7] + "\tfalse");
			}
		}
		
		while (faFr.hasMoreLines()) {
			line = faFr.readLine();
			if (line.startsWith(">")) {
				fm.writeLine(line);
				seqId = line.substring(1);
				line = faFr.readLine();
				if (blastList.containsKey(seqId)) {
					blastLine = blastList.get(seqId).split("\t");
					cutIdx = Integer.parseInt(blastLine[0]);
					extractLeft = Boolean.parseBoolean(blastLine[1]);
					if (extractLeft) {
						fm.writeLine(line.substring(0, cutIdx - 1));
					} else {
						fm.writeLine(line.substring(cutIdx));
					}
				} else {
					fm.writeLine(line);
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaExtractBlastResult.ljar <newref.fa> <blast.filt.out> <newref_blast_filt.fa>");
		System.out.println("\t<newref.fa>: the output generated from samGlobalRealignment.jar and awk '$8!=0&&$1!=\"CHR\" {print \">\"$1\":\"$2\"..\"$3\":\"$4\":\"$5\":\"$8\":\"$9\"\n\"$11}'");
		System.out.println("\t<blast.filt.out>: the output generated from blast result and blastFilterGapFlanked.jar");
		System.out.println("\t<newref_blast_filt.fa>: will be generated using the ExtractLeft or ExtractRight sign");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new ExtractBlastResult().go(args[0], args[1], args[2]);
		} else {
			new ExtractBlastResult().printHelp();
		}
	}

}
