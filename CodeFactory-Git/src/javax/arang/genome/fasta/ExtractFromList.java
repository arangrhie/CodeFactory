package javax.arang.genome.fasta;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ExtractFromList extends I2Owrapper {

	@Override
	public void hooker(FileReader frFasta, FileReader frSequenceList, FileMaker fm) {
		String line;
		ArrayList<String> sequenceList = new ArrayList<String>();
		HashMap<String, String> newSequenceList = new HashMap<String, String>();	
		
		String sequenceName;
		while (frSequenceList.hasMoreLines()) {
			line = frSequenceList.readLine();
			sequenceName = ">" + line.trim();
			sequenceList.add(sequenceName);
			if (hasNewSequenceNames) {
				line = frNewSequenceReader.readLine();
				newSequenceList.put(sequenceName, ">" + line.trim());
			}
		}
		
		System.out.println("[DEBUG] :: Sequence listed: " + sequenceList.size());
		if (hasNewSequenceNames) {
			System.out.println("[DEBUG] :: New sequence names listed: " + newSequenceList.size());
			readFaExtractSeq(frFasta, sequenceList, fm, newSequenceList);
		} else {
			readFaExtractSeq(frFasta, sequenceList, fm);
		}
	}
	
	public static ArrayList<String> readFaExtractSeq(FileReader frFasta, ArrayList<String> sequenceList,
											FileMaker fm, HashMap<String, String> newSequenceList) {
		String line;
		String[] tokens;
		boolean toInclude = false;
		ArrayList<String> sequenceWritten = new ArrayList<String>();
		String faName;
		while (frFasta.hasMoreLines()) {
			line = frFasta.readLine();
			if (line.startsWith(">")) {
				tokens = line.split(RegExp.WHITESPACE);
				faName = tokens[0].substring(1);
				if (sequenceList.contains(tokens[0]) || sequenceList.contains(faName)) {
					//System.out.println("[DEBUG] :: Adding " + newScaffoldList.get(tokens[0]));
					fm.writeLine(newSequenceList.get(tokens[0]));
					sequenceWritten.add(faName);
					toInclude = true;
				} else {
					toInclude = false;
				}
			} else if (toInclude) {
				fm.writeLine(line);
			}
		}
		return sequenceWritten;
	}
	
	public static ArrayList<String> readFaExtractSeq(FileReader frFasta, ArrayList<String> sequenceList, FileMaker fm) {
		String line;
		String[] tokens;
		boolean toInclude = false;
		String faName;
		ArrayList<String> sequenceWritten = new ArrayList<String>();
		while (frFasta.hasMoreLines()) {
			line = frFasta.readLine();
			if (line.startsWith(">")) {
				tokens = line.split(RegExp.WHITESPACE);
				faName = tokens[0].substring(1);
				if (sequenceList.contains(tokens[0]) || sequenceList.contains(faName)) {
					//System.out.println("[DEBUG] :: Adding " + line);
					fm.writeLine(line);
					toInclude = true;
					sequenceWritten.add(faName);
				} else {
					toInclude = false;
				}
			} else if (toInclude) {
				fm.writeLine(line);
			}
		}
		return sequenceWritten;
	}
	

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaExtractFromList.jar <in.fasta> <in_sequence.list> <out.fasta> [new_sequence.list]");
		System.out.println("\t<in.fasta>: Total list of fasta file. Matches only the first token of lines starting with >.");
		System.out.println("\t<in_sequence.list>: List of scaffold (contig) names, with no \">\"");
		System.out.println("\t<out.fasta>: Fasta file containing only listed scaffolds");
		System.out.println("\t[new_sequence.list]: Optional. Instead of names used in <in_sequence.list>, use these names");
		System.out.println("\t\tThe order must be the same as in <in_sequence.list>");
		System.out.println("\t*Recommended -Xmx option slightly higher than <in_scaffold.list> +  [new_sequence.list] size");
		System.out.println("Arang Rhie, 2018-03-31. arrhie@gmail.com");
	}
	
	static boolean hasNewSequenceNames = false;
	static FileReader frNewSequenceReader = null;
	
	public static void main(String[] args) {
		if (args.length == 3) {
			new ExtractFromList().go(args[0], args[1], args[2]);
		} else if (args.length == 4) {
			hasNewSequenceNames = true;
			frNewSequenceReader = new FileReader(args[3]);
			new ExtractFromList().go(args[0], args[1], args[2]);
		} else {
			new ExtractFromList().printHelp();
		}
	}

}
