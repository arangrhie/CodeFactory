/**
 * 
 */
package javax.arang.bed.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Vector;

import javax.arang.IO.basic.FileReader;
import javax.arang.genome.Chromosome;
import javax.arang.genome.ChromosomeComparator;


/**
 * @author Arang Rhie
 *
 */
public class Bed {
	
	public static final int CHROM = 0;
	public static final int START = 1;
	public static final int END = 2;
	public static final int NOTE = 3;
	
	public static final short REGION_START = 0;
	public static final short REGION_END = 1;
	
	private boolean isSorted = false;
	private ArrayList<String> chrStrArray = new ArrayList<String>();
	private PriorityQueue<Chromosome> chrList = new PriorityQueue<Chromosome>(1, new ChromosomeComparator());
	private HashMap<String, Vector<Long>> starts = new HashMap<String, Vector<Long>>();
	private HashMap<String, Vector<Long>> ends = new HashMap<String, Vector<Long>>();
	private HashMap<String, Vector<String>> notes = new HashMap<String, Vector<String>>();
	
	public Bed(FileReader fr) {
		parseBed(fr);
	}
	
	/***
	 * Parse bed formatted file.
	 * Line starting with \'#\' are ignored.
	 * start: 0-based.
	 * end: exclusive.
	 * start 0 end 100 => spanning from 0 - 99.
	 * @param fr
	 */
	public void parseBed(FileReader fr) {
		String line;
		String[] tokens;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split("\t");
			if (tokens.length < 3)	continue;
			if (!chrStrArray.contains(tokens[CHROM])) {
				chrStrArray.add(tokens[CHROM]);
				chrList.add(new Chromosome(tokens[CHROM]));
			}
			if (tokens.length > NOTE) {
				StringBuffer note = new StringBuffer(tokens[NOTE]);
				if (NOTE + 1 <= tokens.length) {
					for (int i = NOTE + 1; i < tokens.length; i++) {
						note.append("\t" + tokens[i]);
					}
				}
				addRegion(tokens[CHROM], tokens[START], tokens[END], note.toString());	
			} else {
				addRegion(tokens[CHROM], tokens[START], tokens[END], "");
			}
		}
		sortChrList();
		//System.out.println("[DEBUG] :: chrList.size() = " + chrList.size());
		//System.out.println("[DEBUG] :: ParseBed : numChrList = " + chrList.size());
	}
	
	public void sort() {
		if (!isSorted) {
			sortStartEnds();
			isSorted = true;
		}
	}
	
	private void sortStartEnds() {
		Vector<Long> sortedStarts = new Vector<Long>();
		Vector<Long> sortedEnds = new Vector<Long>();
		Vector<String> sortedNotes = new Vector<String>();
		for (String chr : chrStrArray) {
			Vector<Long> startRegion = starts.get(chr);
			Vector<Long> endRegion = ends.get(chr);
			Vector<String> noteRegion = notes.get(chr);
			Long[] startRegionArr = new Long[0];
			startRegionArr = startRegion.toArray(startRegionArr);
			Arrays.sort(startRegionArr);
			sortedStarts = new Vector<Long>();
			sortedEnds = new Vector<Long>();
			sortedNotes = new Vector<String>();
			for (int i = 0; i < startRegionArr.length; i++) {
				//System.out.println("[DEBUG] :: " + chr + " startRegionArr[i]=" + startRegionArr[i]);
				sortedStarts.add(startRegionArr[i]);
				int indexOfSortedVal = startRegion.indexOf(startRegionArr[i]);
				sortedEnds.add(endRegion.get(indexOfSortedVal));
				sortedNotes.add(noteRegion.get(indexOfSortedVal));
				endRegion.remove(indexOfSortedVal);
				noteRegion.remove(indexOfSortedVal);
				startRegion.remove(indexOfSortedVal);
			}
			starts.put(chr, sortedStarts);
			ends.put(chr, sortedEnds);
			notes.put(chr, sortedNotes);
		}
	}

	/***
	 * Add regoin to starts and ends map.
	 * @param chr
	 * @param start
	 * @param end
	 */
	public void addRegion(String chr, String start, String end) {
		isSorted = false;
		if (start.contains(",")) {
			start = start.replace(",", "");
		}
		if (end.contains(",")) {
			end = end.replace(",", "");
		}
		if (starts.containsKey(chr)) {
			Vector<Long> startRegion = starts.get(chr);
			startRegion.add(Long.parseLong(start));
			Vector<Long> endRegion = ends.get(chr);
			endRegion.add(Long.parseLong(end));
		} else {
			Vector<Long> startRegion = new Vector<Long>();
			startRegion.add(Long.parseLong(start));
			starts.put(chr, startRegion);
			Vector<Long> endRegion = new Vector<Long>();
			endRegion.add(Long.parseLong(end));
			ends.put(chr, endRegion);
		}
	}
	
	public void addRegion(String chr, String start, String end, String note) {
		addRegion(chr, start, end);
		if (notes.containsKey(chr)) {
			notes.get(chr).add(note);
		} else {
			Vector<String> noteRegion = new Vector<String>();
			noteRegion.add(note);
			notes.put(chr, noteRegion);
		}
	}
	
	/***
	 * Get number of regions contained in specified chr.
	 * @param chr
	 * @return
	 */
	public int getNumRegions(String chr) {
		return starts.get(chr).size();
	}
	
	public Vector<Long> getStarts(String chr) {
		return starts.get(chr);
	}
	
	public Vector<Long> getEnds(String chr) {
		return ends.get(chr);
	}
	
	/***
	 * 
	 * @param chr
	 * @param index
	 * @return region[REGION_START, REGION_END]
	 */
	public long[] getRegion(String chr, int index) {
		long[] region = new long[2];
		region[REGION_START] = starts.get(chr).get(index);
		region[REGION_END] = ends.get(chr).get(index);
		return region;
	}
	
	public String getNote(String chr, int index) {
		return notes.get(chr).get(index);
	}
	
	/***
	 * 
	 * @param chr
	 * @param index
	 * @return Start of the index's region in chr
	 */
	public Long getStart(String chr, int index) {
		return starts.get(chr).get(index);
	}
	
	/***
	 * 
	 * @param chr
	 * @param index
	 * @return End of the index's region in chr
	 */
	public Long getEnd(String chr, int index) {
		return ends.get(chr).get(index);
	}

	
	public static String getNotes(String[] bedLine) {
		StringBuffer notes = new StringBuffer();
		if (bedLine.length > NOTE) {
			notes.append(bedLine[NOTE]);
			if (bedLine.length > NOTE + 1) {
				for (int i = NOTE + 1; i < bedLine.length; i++) {
					notes.append("\t" + bedLine[i]);
				}
			}
		}
		return notes.toString();
	}
	
	public static int getChromIntVal(String[] bedLine) {
		return Chromosome.getChromIntVal(bedLine[CHROM]);
	}
	
	public static long getStart(String[] bedLine) {
		return Long.parseLong(bedLine[START]);
	}
	
	public static long getEnd(String[] bedLine) {
		return Long.parseLong(bedLine[END]);
	}

	/**
	 * @return
	 */
	public int getChromosomes() {
		return starts.keySet().size();
	}
	
	
	private void sortChrList() {
		Chromosome[] chrArray = null;
		chrArray = new Chromosome[0];
		System.out.println("[DEBUG] :: chrList.size() = " + chrList.size());
		chrArray = chrList.toArray(chrArray);
		Arrays.sort(chrArray);
		System.out.println("[DEBUG] :: chrArray.length = " + chrArray.length);
		chrList.clear();
		chrStrArray.clear();
		for (int i = 0; i < chrArray.length; i++) {
			chrList.add(chrArray[i]);
			chrStrArray.add(chrArray[i].getChromStringVal());
		}
	}
	
	public String getChr(int index) {
		return chrStrArray.get(index);
	}
	
	public Chromosome getChromosome(int index) {
		Chromosome[] chrArray = new Chromosome[0];
		return chrList.toArray(chrArray)[index];
	}
	
	public PriorityQueue<Chromosome> getChrList() {
		return chrList;
	}
	
	public ArrayList<String> getChrStringList() {
		if (chrStrArray.size() != chrList.size()) {
			PriorityQueue<Chromosome> newChrList = new PriorityQueue<Chromosome>(); 
			while (!chrList.isEmpty()) {
				Chromosome chrom = chrList.remove();
				newChrList.add(chrom);
				chrStrArray.add(chrom.getChromStringVal());
			}
			chrList = newChrList;
		}
		return chrStrArray;
	}
	
	public boolean hasChromosome(String chr) {
		return chrList.contains(chr);
	}

}
