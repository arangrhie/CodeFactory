package javax.arang.mashmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Region;

public class Chain extends Rwrapper {

	public static void main(String[] args) {
		if (args.length == 1) {
			new Chain().go(args[0]);
		} else {
			new Chain().printHelp();
		}
	}

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		
		String prevQchr = "";
		String qChr;
		int qStart;
		int qEnd;
		boolean qStrand;
		
		String tChr;
		int tStart;
		int tEnd;
		
		ArrayList<Integer> startsQ = new ArrayList<Integer>();
		HashMap<Integer, Integer> startEndQ = new HashMap<Integer, Integer>();
		HashMap<Integer, Boolean> startStrandQ = new HashMap<Integer, Boolean>();
		HashMap<Integer, Region> startQtoRef = new HashMap<Integer, Region>();
		
		ArrayList<Integer> doubleStarts = new ArrayList<Integer>();
		HashMap<Integer, ArrayList<Integer>> doubleStartEndsQ = new HashMap<Integer, ArrayList<Integer>>();
		HashMap<Integer, ArrayList<Boolean>> doubleStartStrandsQ = new HashMap<Integer, ArrayList<Boolean>>();
		ArrayList<Integer> endList;
		ArrayList<Boolean> strandList;
		
		
		if (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			
			qChr = tokens[MashMap.Q_CHR];
			if (!prevQchr.equals("") && !prevQchr.equals(qChr)) {
				// Sort by q start
				Collections.sort(startsQ);
				
				// Merge when nothing is breaking in-between
				
				
				// Start over with a new Query
				
				
			}
			
			qStart = Integer.parseInt(tokens[MashMap.Q_START]);
			qEnd = Integer.parseInt(tokens[MashMap.Q_END]);
			if (tokens[MashMap.Q_STRAND].equals("+")) {
				qStrand = true;
			} else {
				qStrand = false;
			}
			
			// add to covered Q regions
			qStart = Integer.parseInt(tokens[MashMap.Q_START]);
			qEnd = Integer.parseInt(tokens[MashMap.Q_END]);
			startsQ.add(qStart);
			if (!startEndQ.containsKey(qStart)) {
				startEndQ.put(qStart, qEnd);
				startStrandQ.put(qStart, qStrand);
			} else {
				if (!doubleStarts.contains(qStart)) {
					doubleStarts.add(qStart);
					endList = new ArrayList<Integer>();
					strandList = new ArrayList<Boolean>();
				} else {
					endList = doubleStartEndsQ.get(qStart);
					strandList = doubleStartStrandsQ.get(qStart);
				}
				endList.add(qEnd);
				strandList.add(qStrand);
				doubleStartEndsQ.put(qStart, endList);
				doubleStartStrandsQ.put(qStart, strandList);
			}
			
			// add to covered R regions
			tChr = tokens[MashMap.T_CHR];
			tStart = Integer.parseInt(tokens[MashMap.T_START]);
			tEnd = Integer.parseInt(tokens[MashMap.T_END]);
			
			
			prevQchr = qChr;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar mashmapChain.jar <in.map>");
		System.out.println("\t<stdout>: Q_CONTIG\tQ_LEN\tQ_START\tQ_END\tQ_STRAND\tT_CONTIG\tT_LEN\tT_START\tT_END\tIDENTITY(SCORE)");
		System.out.println("\tMerge alignments when nothing is in between two adjacent alignments from the same pair/orientation.");
		System.out.println("Arang Rhie, 2017-11-13. arrhie@gmail.com");
	}
	
	

}
