package javax.arang.bed;

import java.util.ArrayList;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class MergeByReadName extends IOwrapper {

	private static String delim = ":";
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String id;
		String prevId = "";
		boolean isFirst = true;
		Bed bedRegions = new Bed();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			id = tokens[Bed.NOTE].substring(0, tokens[Bed.NOTE].indexOf(delim));
			if (!prevId.equals(id)) {
				if (isFirst) {
					isFirst = false;
					System.out.println("[DEBUG] :: Start processing " + id);
				} else {
					writeBed(fm, prevId, bedRegions);
					bedRegions = new Bed();
					System.out.println("[DEBUG] :: Start processing " + id);
				}
			}
			bedRegions.addMergeRegion(tokens[Bed.CHROM], tokens[Bed.START], tokens[Bed.END]);
			prevId = id;
			
		}
		writeBed(fm, prevId, bedRegions);
	}
	
	private void writeBed(FileMaker fm, String id, Bed bedRegions) {
		// write out Bed objects
		//System.out.println("[DEBUG] :: Start writing " + id);
		ArrayList<Integer> starts = null;
		ArrayList<Integer> ends = null;
		ArrayList<String> notes = null;
		ArrayList<Integer> notes2 = null;
		bedRegions.sortChr();
		for (int i = 0; i < bedRegions.getChromosomes(); i++) {
			starts = bedRegions.getStarts(bedRegions.getChr(i));
			ends = bedRegions.getEnds(bedRegions.getChr(i));
			notes = bedRegions.getNotes(bedRegions.getChr(i));
			notes2 = bedRegions.getNotes2(bedRegions.getChr(i));
			for (int j = 0; j < starts.size(); j++) {
				float meanCov = ((float) notes2.get(j) / (ends.get(j) - starts.get(j)));
				fm.writeLine(bedRegions.getChr(i) + "\t" + starts.get(j) + "\t" + ends.get(j) + "\t" + id + "\t"
								+ (ends.get(j) - starts.get(j)) + "\t" + notes.get(j) + "\t" + String.format("%.3f", meanCov));
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedMergeByReadName.jar <in.bed> <out.bed> <delimiter>");
		System.out.println("\t<in.bed>: use samtools to sort by read name, and convert with bedtools bamtobed to a bed format.");
		System.out.println("\t<out.bed>: CONTIG\tSTART\tEND\tREADNAME\tNUM_READS\tLEN\tTOTAL_DEPTH\tMEAN_DEPTH");
		System.out.println("\t<delimiter>: READNAME will be recognized from String.substring(0, firstIndexOf(<delimiter>))");
		System.out.println("\tThis code is made for identifying BAC and 10X barcode covered region.");
		System.out.println("\t\tMean depth is assuming a read is 100bp.");
		System.out.println("Arang Rhie, 2015-09-23. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			delim = args[2];
			new MergeByReadName().go(args[0], args[1]);
		} else {
			new MergeByReadName().printHelp();
		}
	}

}
