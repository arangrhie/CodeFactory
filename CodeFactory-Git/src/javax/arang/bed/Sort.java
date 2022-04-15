package javax.arang.bed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import javax.arang.genome.Chromosome;

public class Sort extends Rwrapper {

	public void hooker(FileReader fr) {
		
		// chr, <start, end>
		HashMap<Chromosome, HashMap<Integer, Integer>> bedMap   = new HashMap<Chromosome, HashMap<Integer, Integer>>();
		HashMap<Chromosome, HashMap<Integer, String>>  notesMap = new HashMap<Chromosome, HashMap<Integer, String>>();
		// chr, chromosome
		HashMap<String, Chromosome> chrList = new HashMap<String, Chromosome>();
		// chr_start, end
		HashMap<String, ArrayList<Integer>> duplicateStartsMap = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, ArrayList<String>>  duplicateNotesMap  = new HashMap<String, ArrayList<String>>();
		
		String   line;
		String[] tokens;
		
		String  chr;
		Integer start;
		Integer end;
		
		HashMap<Integer, Integer> chrRegion;
		HashMap<Integer, String> noteRegion;
		
		String note;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			
			// Skip comments
			if (line.startsWith("#")){
				writeOutputLine(line);
				continue;
			}
			tokens = line.split(RegExp.TAB);
			
			chr = tokens[Bed.CHROM];
			if (chr.equalsIgnoreCase("chr")) {
				writeOutputLine(line);
				continue;
			}
			
			try {
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			} catch (NumberFormatException e) {
				System.err.println("[ WARNING ] :: Not a valid bed line: " + line);
				continue;
			}
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
				ArrayList<Integer> dupStartEnds  = new ArrayList<Integer>();
				ArrayList<String>  dupStartNotes = new ArrayList<String>();
				if (duplicateStartsMap.containsKey(chr + "_" + start)) {
					dupStartEnds  = duplicateStartsMap.get(chr + "_" + start);
					dupStartNotes = duplicateNotesMap.get(chr + "_" + start);
				} else {
					dupStartEnds  = new ArrayList<Integer>();
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
		Integer[] ends = new Integer[0];
		for (int i = 0; i < chrs.length; i++) {
			chrRegion = bedMap.get(chrs[i]);
			System.err.println("[DEBUG] :: " + chrs[i].getChromStringVal() + " : " + chrRegion.size() + " unique starts");
			Integer[] starts = new Integer[0];
			starts = chrRegion.keySet().toArray(starts);
			Arrays.sort(starts);
			for (int j = 0; j < starts.length; j++) {
				if (chrRegion.get(starts[j]) == -1) {
					ArrayList<Integer> dupStartEnds = duplicateStartsMap.get(chrs[i].getChromStringVal() + "_" + starts[j]);
					ArrayList<String> dupStartNotes = duplicateNotesMap.get(chrs[i].getChromStringVal() + "_" + starts[j]);
					//System.out.println("[DEBUG] :: " + chrs[i].getChromStringVal() + ":" + starts[j] + " : " + dupStartEnds.size() + " dup ends");
					if (dupStartEnds.size() > 1) {
						ArrayList<Integer> endTmpList = new ArrayList<Integer>();
						endTmpList.addAll(dupStartEnds);
						Collections.sort(endTmpList);
						ends = new Integer[endTmpList.size()];
						for (int k = 0; k < ends.length; k++) {
							ends[k] = dupStartEnds.indexOf(endTmpList.get(k));
						}
						for (int k = 0; k < dupStartEnds.size(); k++) {
							end = dupStartEnds.get(ends[k]);
							note = dupStartNotes.get(ends[k]);
							writeOutput(chrs[i].getChromStringVal() + "\t" + starts[j] + "\t" + end);
							if (!note.equals("")) {
								writeOutput(note);
							}
							writeOutputLine("");
						}
					} else {
						end = dupStartEnds.get(0);
						note = dupStartNotes.get(0);
						writeOutput(chrs[i].getChromStringVal() + "\t" + starts[j] + "\t" + end);
						if (!note.equals("")) {
							writeOutput(note);
						}
						writeOutputLine("");
					}
				} else {
					writeOutput(chrs[i].getChromStringVal() + "\t" + starts[j] + "\t" + chrRegion.get(starts[j]));
					note = notesMap.get(chrs[i]).get(starts[j]);
					if (!note.equals("")) {
						writeOutput(note);
					}
					writeOutputLine("");
				}
			}
		}
		
 	}
	
	private void writeOutputLine(String line) {
		if (outToFile) {
			fm.writeLine(line);
		} else {
			System.out.println(line);
		}
	}
	
	private void writeOutput(String line) {
		if (outToFile) {
			fm.write(line);
		} else {
			System.out.print(line);
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar bedSort.jar <in.bed> [out.bed]");
		System.err.println("\t[out.bed]: Sorted bed file according to chr start position. OPTIONAL. STDOUT by default.");
		System.err.println("\t\tLine stating with chr or # will be treated as header line.");
		System.err.println("\t* Support STDOUT if no output file is provided");
		System.err.println("Arang Rhie, 2021-12-22. arrhie@gmail.com");

	}
	
	public static void goWithOutput(String input, String output) {
		fm = new FileMaker(output);
		outToFile = true;
		new Sort().go(input);
	}
	
	private static FileMaker fm = null;
	private static boolean outToFile = false;

	public static void main(String[] args) {
		if (args.length == 2) {
			fm = new FileMaker(args[1]);
			outToFile = true;
			new Sort().go(args[0]);
		} else if (args.length == 1) {
			new Sort().go(args[0]);
		} else {
			new Sort().printHelp();
		}
	}

}
