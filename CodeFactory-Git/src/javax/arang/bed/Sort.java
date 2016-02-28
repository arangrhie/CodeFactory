package javax.arang.bed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import javax.arang.genome.Chromosome;

public class Sort extends IOwrapper {

	public void hooker(FileReader fr, FileMaker fm) {
		// chr, <start, end>
		HashMap<Chromosome, HashMap<Integer, Integer>> bedMap = new HashMap<Chromosome, HashMap<Integer, Integer>>();
		HashMap<Chromosome, HashMap<Integer, String>> notesMap = new HashMap<Chromosome, HashMap<Integer, String>>();
		// chr, chromosome
		HashMap<String, Chromosome> chrList = new HashMap<String, Chromosome>();
		// chr_start, end
		HashMap<String, ArrayList<Integer>> duplicateStartsMap = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, ArrayList<String>> duplicateNotesMap = new HashMap<String, ArrayList<String>>();
		
		String line;
		String[] tokens;
		
		String chr;
		Integer start;
		Integer end;
		
		HashMap<Integer, Integer> chrRegion;
		HashMap<Integer, String> noteRegion;
		
		String note;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#")){
				fm.writeLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			
			chr = tokens[Bed.CHROM];
			if (chr.equalsIgnoreCase("chr")) {
				fm.writeLine(line);
				continue;
			}
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			note = "";
			for (int i = Bed.NOTE; i < tokens.length; i++) {
				note = note + "\t" + tokens[i];
			}
			
			if (!chrList.containsKey(chr)) {
				Chromosome chromosome = new Chromosome(chr);
				chrRegion = new HashMap<Integer, Integer>();
				bedMap.put(chromosome, chrRegion);
				noteRegion = new HashMap<Integer, String>();
				notesMap.put(chromosome, noteRegion);
				chrList.put(chr, chromosome);
			} else {
				chrRegion = bedMap.get(chrList.get(chr));
				noteRegion = notesMap.get(chrList.get(chr));
			}
			if (chrRegion.containsKey(start)) {
				ArrayList<Integer> dupStartEnds = new ArrayList<Integer>();
				ArrayList<String> dupStartNotes = new ArrayList<String>();
				if (duplicateStartsMap.containsKey(chr + "_" + start)) {
					dupStartEnds = duplicateStartsMap.get(chr + "_" + start);
					dupStartNotes = duplicateNotesMap.get(chr + "_" + start);
				} else {
					dupStartEnds = new ArrayList<Integer>();
					dupStartNotes = new ArrayList<String>();
					// Move previous start / notes to dupStart maps
					dupStartEnds.add(chrRegion.get(start));
					dupStartNotes.add(noteRegion.get(start));
					chrRegion.put(start, -1);
					noteRegion.put(start, "");
				}
				dupStartEnds.add(end);
				dupStartNotes.add(note);
				duplicateStartsMap.put(chr + "_" + start, dupStartEnds);
				duplicateNotesMap.put(chr + "_" + start, dupStartNotes);
			} else {
				chrRegion.put(start, end);
				noteRegion.put(start, note);
			}
		}
		
		Chromosome[] chrs = new Chromosome[0];
		chrs = bedMap.keySet().toArray(chrs);
		Arrays.sort(chrs);
		
		for (int i = 0; i < chrs.length; i++) {
			chrRegion = bedMap.get(chrs[i]);
			System.out.println("[DEBUG] :: " + chrs[i].getChromStringVal() + " : " + chrRegion.size() + " unique starts");
			Integer[] starts = new Integer[0];
			starts = chrRegion.keySet().toArray(starts);
			Arrays.sort(starts);
			for (int j = 0; j < starts.length; j++) {
				if (chrRegion.get(starts[j]) == -1) {
					ArrayList<Integer> dupStartEnds = duplicateStartsMap.get(chrs[i].getChromStringVal() + "_" + starts[j]);
					ArrayList<String> dupStartNotes = duplicateNotesMap.get(chrs[i].getChromStringVal() + "_" + starts[j]);
					//System.out.println("[DEBUG] :: " + chrs[i].getChromStringVal() + ":" + starts[j] + " : " + dupStartEnds.size() + " dup ends");
					for (int k = 0; k < dupStartEnds.size(); k++) {
						end = dupStartEnds.get(k);
						note = dupStartNotes.get(k);
						fm.write(chrs[i].getChromStringVal() + "\t" + starts[j] + "\t" + end);
						if (!note.equals("")) {
							fm.write(note);
						}
						fm.writeLine();
					}
				} else {
					fm.write(chrs[i].getChromStringVal() + "\t" + starts[j] + "\t" + chrRegion.get(starts[j]));
					note = notesMap.get(chrs[i]).get(starts[j]);
					if (!note.equals("")) {
						fm.write(note);
					}
					fm.writeLine();
				}
			}
		}
		
 	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedSort.jar <in.bed> <out.bed>");
		System.out.println("\t<out.bed>: Sorted bed file according to chr start position");
		System.out.println("\t\tLine stating with chr or # will be treated as header line.");
		System.out.println("Arang Rhie, 2016-02-11. arrhie@gmail.com");

	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new Sort().go(args[0], args[1]);
		} else {
			new Sort().printHelp();
		}
	}

}
