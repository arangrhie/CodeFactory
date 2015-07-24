package javax.arang.falcon;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class CountPreadOccurrence extends IOwrapper {

	// .sif contains START (node) - TILING - END (node)
	private static final short START = 0;
	private static final short TILING = 1;
	private static final short END = 2;
	
	private static final short CONTIG = 0;
	private static final short LEN = 2;
	
	private HashMap<String, Integer> preadOccurrenceMap = new HashMap<String, Integer>();	// pread, occurrence
	private HashMap<String, Integer> pread_contigOccurrenceMap = new HashMap<String, Integer>();	// pread-contig, occurrence
	private HashMap<String, ArrayList<String>> preadContigMap = new HashMap<String, ArrayList<String>>();	// pread, <contig>
	private int maxOccurrence = 0;
	private int maxOccurrenceWithinAContig = 0;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		String[] tokens;
		String[] tilingContig;
		double tiling = 0;
		int occurrence;
		String prevEndPread = "";
		String prevContig = "";
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			tilingContig = tokens[TILING].split("_");
			if (!prevContig.equals(tilingContig[CONTIG]) && !prevEndPread.equals(tokens[START])) {
				occurrence = addToPreadOccurrenceMap(tokens[START], preadOccurrenceMap);
				if (occurrence > maxOccurrence) {
					maxOccurrence = occurrence;
				}
				occurrence = addToPreadOccurrenceMap(tokens[START] + "_" + tilingContig[CONTIG], pread_contigOccurrenceMap);
				if (occurrence > maxOccurrenceWithinAContig) {
					if (occurrence == 3) {
						System.out.println("[DEBUG] :: occurrence = 3: " + tokens[START]);
					}
					maxOccurrenceWithinAContig = occurrence;
				}
				addToPreadContigMap(tokens[START], tilingContig[CONTIG], preadContigMap);
			}
			occurrence = addToPreadOccurrenceMap(tokens[END], preadOccurrenceMap);
			if (occurrence > maxOccurrence) {
				maxOccurrence = occurrence;
			}
			occurrence = addToPreadOccurrenceMap(tokens[END] + "_" + tilingContig[CONTIG], pread_contigOccurrenceMap);
			if (occurrence > maxOccurrenceWithinAContig) {
				if (occurrence == 3) {
					System.out.println("[DEBUG] :: occurrence = 3: " + tokens[END]);
				}
				maxOccurrenceWithinAContig = occurrence;
			}
			addToPreadContigMap(tokens[END], tilingContig[CONTIG], preadContigMap);
			tiling += Integer.parseInt(tilingContig[LEN]);
			
			// To prevent edge to edge counting as a loop
			prevContig = tilingContig[CONTIG];
			prevEndPread = tokens[END];
		}
		System.out.println("Total length of tiling path with uniquely used preads in bp: " + tiling);
		System.out.println("Maximum Occurrence: " + maxOccurrence);
		System.out.println("Maximum occurrence within a contig: " + maxOccurrenceWithinAContig);
		
		Integer[] occurrenceCount = new Integer[maxOccurrence + 1];
		Integer[] loopOccurrenceCount = new Integer[maxOccurrence + 1];
		String[] preadsInLoop = new String[maxOccurrence + 1];
		String[] preadsBranching = new String[maxOccurrence + 1];
		initOccurrenceCount(occurrenceCount);
		initOccurrenceCount(loopOccurrenceCount);
		initOccurrenceCount(preadsInLoop);
		initOccurrenceCount(preadsBranching);
		
		ArrayList<String> contigList = null;
		int numPreadOccurrence = 0;
		for (String pread : preadOccurrenceMap.keySet()) {
			numPreadOccurrence = preadOccurrenceMap.get(pread);
			occurrenceCount[numPreadOccurrence]++;
			preadsBranching[numPreadOccurrence] += "," + pread;
			contigList = preadContigMap.get(pread);
			for (String contig : contigList) {
				if (pread_contigOccurrenceMap.get(pread + "_" + contig) == 2) {
					loopOccurrenceCount[numPreadOccurrence]++;
					preadsInLoop[numPreadOccurrence] += "," + pread;
				}
			}
		}
		
		FileMaker branchingPreadsFm = new FileMaker(fm.getDir(), fm.getFileName() + ".branch");
		FileMaker loopingPreadsFm = new FileMaker(fm.getDir(), fm.getFileName() + ".loop");
		int branching = 0;
		int looping = 0;
		// Write output
		fm.writeLine("N\tNum_unique_preads_occurring_N_times\tNum_preads_in_Loop");
		for (int i = 2; i < occurrenceCount.length; i++) {
			fm.writeLine(i + "\t" + occurrenceCount[i] + "\t" + loopOccurrenceCount[i] + "\t" + preadsInLoop[i]);
			tokens = preadsBranching[i].split(",");
			for (int j = 0; j < tokens.length; j++) {
				if (tokens[j].length() > 0) {
					branchingPreadsFm.writeLine(tokens[j]);
					branching++;
				}
			}
			tokens = preadsInLoop[i].split(",");
			for (int j = 0; j < tokens.length; j++) {
				if (tokens[j].length() > 0) {
					loopingPreadsFm.writeLine(tokens[j]);
					looping++;
				}
			}
		}
		
		System.out.println("Total number of branched preads: " + branching);
		System.out.println("Total number of looping preads: " + looping);
		branchingPreadsFm.closeMaker();
		loopingPreadsFm.closeMaker();
	}

	private void initOccurrenceCount(Integer[] occurrenceCount) {
		for (int i = 0; i < occurrenceCount.length; i++) {
			occurrenceCount[i] = 0;
		}
	}
	
	private void initOccurrenceCount(String[] occurrenceCount) {
		for (int i = 0; i < occurrenceCount.length; i++) {
			occurrenceCount[i] = "";
		}
	}
	
	private void addToPreadContigMap(String pread, String contig, HashMap<String, ArrayList<String>> map) {
		ArrayList<String> contigArr  = null;
		if (!map.containsKey(pread)) {
			contigArr = new ArrayList<String>();
			contigArr.add(contig);
			map.put(pread, contigArr);
		} else {
			contigArr = map.get(pread);
			if (!contigArr.contains(contig)) {
				contigArr.add(contig);
			}
		}
	}

	/***
	 * 
	 * @param pread
	 * @param map
	 * @return occurrence
	 */
	private int addToPreadOccurrenceMap(String pread, HashMap<String, Integer> map) {
		int occurrence = 0;
		if (map.containsKey(pread)) {
			occurrence = map.get(pread) + 1;
			map.put(pread, occurrence);
		} else {
			map.put(pread, 1);
		}
		return occurrence;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar falconCountPreadOccurrence.jar <falcon_ctg_tiling_path_coord_len_start_end.sif> <occurrence_num-preads.txt>");
		System.out.println("\t<falcon_ctg_tiling_path_coord_len_start_end.sif>: generated with falconTilingPathToSIF.jar");
		System.out.println("\t<occurrence_num-preads.txt>: <Occurrence N>\t<Num. of unique preads occurring N times>");
		System.out.println("\tPrints the total tiling path of uniquely used preads");
		System.out.println("Arang Rhie, 2015-07-03. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new CountPreadOccurrence().go(args[0], args[1]);
		} else {
			new CountPreadOccurrence().printHelp();
		}
	}

}
