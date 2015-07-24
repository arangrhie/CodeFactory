package javax.arang.gene.sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.genome.util.Util;


public class SortGeneByGenomePosition extends Rwrapper {

	Integer[][]	minMax = new Integer[25][2];
	public static final short MIN = 0;
	public static final short MAX = 1;
	
	@Override
	public void hooker(FileReader fr) {
		
		ArrayList<HashMap<Integer, String>> chrPositions = new ArrayList<HashMap<Integer, String>>();
		for (int i = 0; i < 25; i++) {
			chrPositions.add(new HashMap<Integer, String>());
			minMax[i][MIN] = Integer.MAX_VALUE;
			minMax[i][MAX] = Integer.MIN_VALUE;
		}
		
		String line;
		StringTokenizer st;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			st = new StringTokenizer(line);
			String geneId = st.nextToken();	// geneId
			st.nextToken();	// gene length
			st.nextToken();	// num_exons
			String chr = st.nextToken();	// chr
			String strand = st.nextToken();	// strand
			int numExons = Integer.parseInt(st.nextToken());	// num_exons
			String starts = st.nextToken();	// start positions
			String ends = st.nextToken();	// end positions
			
			mergePositions(chrPositions, chr, numExons, starts, ends, strand, geneId);
		}
		
		for (int i = 0; i < 25; i++) {
			HashMap<Integer, String> gene = chrPositions.get(i);
			FileMaker fm = new FileMaker("exons", "chr" + Util.getHumanChromosome((byte)(i+1)) + ".exons");
			int prevEnd = -1;
			int overlaps = 0;
			for (int pos = minMax[i][MIN]; pos <= minMax[i][MAX]; pos++) {
				if (gene.containsKey(pos)) {
					if (prevEnd < getEnd(gene.get(pos))) {
						fm.writeLine(pos + "\t" + gene.get(pos));
						prevEnd = getEnd(gene.get(pos));
					} else {
						overlaps++;
					}
				}
			}
			System.out.print("chr" + Util.getHumanChromosome((byte)(i+1)) + " contains " + gene.size() + " exons ");
			System.out.println("with " + overlaps + " overlaps.");
		}

	}

	private void mergePositions(
			ArrayList<HashMap<Integer, String>> chrPositions, String chr,
			int numExons, String starts, String ends, String strand, String geneId) {
		
		int chrInt = Util.getChromIntVal(chr);
		
		StringTokenizer startSt = new StringTokenizer(starts, ",");
		StringTokenizer endSt = new StringTokenizer(ends, ",");
		for (int i = 0; i < numExons; i++) {
			Integer start = Integer.parseInt(startSt.nextToken());
			Integer end = Integer.parseInt(endSt.nextToken());
			HashMap<Integer, String> gene = chrPositions.get(chrInt-1);
			if (gene.containsKey(start)) {
				int prevEnd = getEnd(gene.get(start));
				if (prevEnd < end) {
					gene.put(start, end + "\t" + geneId + "\t" + strand);
				}
			} else {
				gene.put(start, end + "\t" + geneId + "\t" + strand);
				if (start > minMax[chrInt-1][MAX]) {
					minMax[chrInt-1][MAX] = start;
				}
				if (start < minMax[chrInt-1][MIN]) {
					minMax[chrInt-1][MIN] = start;
				}
			}
		}
		
	}
	
	private Integer getEnd(String contents) {
		StringTokenizer st = new StringTokenizer(contents);
		return Integer.parseInt(st.nextToken());
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar sortGeneByGenomePosition.jar <inFile>");
		System.out.println("\t<inFile>: gene list of 3 db");
		System.out.println("\tex)\tuc001aaa.2      2122    3       chr1    +       3       1115,2475,3083, 2090,2584,4121,");
		System.out.println("Outputs are written under exons.");
		System.out.println("Start, end positions are not overlapping. Exons may overlap.");
		System.out.println("<Part of chr1.exons>");
		System.out.println("1115    2090    uc001aaa.2      +");
		System.out.println("2475    4272    uc009vip.1      +");
		System.out.println("4224    4692    NR_028269       -");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new SortGeneByGenomePosition().go(args[0]);
		} else {
			new SortGeneByGenomePosition().printHelp();
		}

	}

}
