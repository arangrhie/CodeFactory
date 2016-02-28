package javax.arang.phasing.indel;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class ReduceDeterminedToRegion extends I2Owrapper {

	@Override
	public void hooker(FileReader frSpan, FileReader frRegion, FileMaker fm) {
		String line;
		String[] tokens;
		
		Bed region = new Bed(frRegion);
		
		HashMap<String, Integer> keyToT = new HashMap<String, Integer>();
		HashMap<String, Integer> keyToF = new HashMap<String, Integer>();
		
		if (region.getChrStringList().size() > 1) {
			System.out.println(">1 chromosomes (contigs) detected. Run this code per chromosome (contig).");
			System.exit(-1);
		}
		String contig = region.getChr(0);
		ArrayList<Integer> starts = region.getStarts(contig);
		ArrayList<String> notes = region.getNotes(contig);
		for (int i = 0; i < starts.size(); i++) {
			keyToT.put(notes.get(i).split(RegExp.TAB)[2], 0);
			keyToF.put(notes.get(i).split(RegExp.TAB)[2], 0);
		}
		
		String svKey;
		while (frSpan.hasMoreLines()) {
			line = frSpan.readLine();
			tokens = line.split(RegExp.TAB);
			svKey = tokens[SpannedSVRead.SV_KEY];
			if (!keyToT.containsKey(svKey)) {
				System.out.println("[ERROR] :: " + frRegion.getFileName() + " and " + frSpan.getFileName() + " does not match.");
				System.exit(-1);
			}
			if (tokens[SpannedSVRead.DETERMINED].equals(SpannedSVRead.T)) {
				keyToT.put(svKey, keyToT.get(svKey) + 1);
			} else if (tokens[SpannedSVRead.DETERMINED].equals(SpannedSVRead.F)) {
				keyToF.put(svKey, keyToF.get(svKey) + 1);
			}
		}
		
		int countT = 0;
		int countF = 0;
		for (int i = 0; i < starts.size(); i++) {
			countT = keyToT.get(notes.get(i).split(RegExp.TAB)[2]);
			countF = keyToF.get(notes.get(i).split(RegExp.TAB)[2]);
			if (countT > countF) {
				fm.writeLine(region.getLine(contig, i) + "\t" + countT + "\t" + countF + "\t" + haplotype);
			} else {
				fm.writeLine(region.getLine(contig, i) + "\t" + countT + "\t" + countF + "\tNA");
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar phasingIndelReduceDeterminedToRegion.jar <in.A.span.determined.sort> <in.region.span.bed> <out.region.span.determined.bed> <haplotype>");
		System.out.println("\t<in.A.span.determined>: At the end, T/F/NA. Sort before running this code.");
		System.out.println("\t<in.region.span.bed>: generated with samPacBioExtractRegionSpanningReads.jar");
		System.out.println("\t<out.region.span.determined.bed>: At the end, write the counts of num. Ts and Fs and <haplotype> if num. T > F.");
		System.out.println("\t\t*Run this code per contig(chr)s.");
		System.out.println("Arang Rhie, 2016-01-02. arrhie@gmail.com");
	}

	private static String haplotype;
	public static void main(String[] args) {
		if (args.length == 4) {
			haplotype = args[3]; 
			new ReduceDeterminedToRegion().go(args[0], args[1], args[2]);
		} else {
			new ReduceDeterminedToRegion().printHelp();
		}
	}

}
