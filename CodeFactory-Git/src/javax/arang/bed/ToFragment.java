package javax.arang.bed;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToFragment extends IOwrapper {

	private static final short CHR = 0;
	private static final short START = 1;
	private static final short END = 2;
	private static final short READ_ID = 3;
	private static final short ORIENTATION = 5;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String prevReadID = "";
		String chr1;
		String chr2;
		int start1;
		int end1;
		int start2;
		int end2;
		String orientation1;
		String orientation2;
		
		line = fr.readLine();
		tokens = line.split(RegExp.TAB);
		chr1 = tokens[CHR];
		start1 = Integer.parseInt(tokens[START]);
		end1 = Integer.parseInt(tokens[END]);
		prevReadID = tokens[READ_ID].substring(0, tokens[READ_ID].indexOf("/"));
		orientation1 = tokens[ORIENTATION];
		
		StringBuffer fragmentsToWrite = new StringBuffer();
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			if (!tokens[READ_ID].substring(0, tokens[READ_ID].indexOf("/")).equals(prevReadID)) {
				fm.write(fragmentsToWrite.toString());
				chr1 = tokens[CHR];
				start1 = Integer.parseInt(tokens[START]);
				end1 = Integer.parseInt(tokens[END]);
				prevReadID = tokens[READ_ID].substring(0, tokens[READ_ID].indexOf("/"));
				orientation1 = tokens[ORIENTATION];
				fragmentsToWrite = new StringBuffer(chr1 + "\t" + start1 + "\t" + end1 + "\t" + (end1 - start1) + "\t" + prevReadID + "\t" + orientation1 + "\n");
			} else {
				// readID /2
				chr2 = tokens[CHR];
				start2 = Integer.parseInt(tokens[START]);
				end2 = Integer.parseInt(tokens[END]);
				orientation2 = tokens[ORIENTATION];
				if (chr1.equals(chr2)) {
					int newStart = Math.min(start1, start2);
					int newEnd = Math.max(end1, end2);
					fragmentsToWrite = new StringBuffer(chr1 + "\t" + newStart + "\t" + newEnd + "\t" + (newEnd - newStart) + "\t" + prevReadID + "\t" + orientation1 + " " + orientation2 + "\n");
				} else {
					fragmentsToWrite.append(chr2 + "\t" + start2 + "\t" + end2 + "\t" + (end2 - start2) + "\t" + prevReadID + "\t" + orientation2 + "\n");
				}
			}
		}
		fm.write(fragmentsToWrite.toString());
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx512m bedToFragment.jar <in.bed> <out.bed>");
		System.out.println("\t<in.bed>: Results from bedtools bamToBed sorted by readID");
		System.out.println("\t<out.bed>: Fragments for paired ends, reads for single ends.");
		System.out.println("\t\tCHR\tSTART\tEND\tLEN\tREADID");
		System.out.println("Arang Rhie, 2015-05-20. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new ToFragment().go(args[0], args[1]);
		} else {
			new ToFragment().printHelp();
		}
	}

}
