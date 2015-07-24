package javax.arang.ref.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/***
 * Merge the region and get the total coverage from a bait fasta file per chr
 * input	chrM:123..456
 * 			chrM:456..789
 * output	chrM	123	789
 * @author Arang Rhie
 *
 */
public class ExtractRefRegion extends IOwrapper {

	final int START = 0;
	final int END = 1;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		Vector<HashMap<Integer, Integer>> positions = new Vector<HashMap<Integer, Integer>>();
		Vector<ArrayList<Integer>> starts = new Vector<ArrayList<Integer>>();

		for (int i = 0; i < 25; i++) {
			positions.add(new HashMap<Integer, Integer>());
			starts.add(new ArrayList<Integer>());
		}
		
		String line;
		StringTokenizer st;
		
		
		
		// parse
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();	// chrM:123..456
			st = new StringTokenizer(line, ">:.");
			String chr = st.nextToken();
			int chrNum = parseChr(chr);
			
			HashMap<Integer, Integer> pos = positions.get(chrNum - 1);
			int left = Integer.parseInt(st.nextToken());
			int right = Integer.parseInt(st.nextToken());
			
			pos.put(left, right);
			starts.get(chrNum - 1).add(left);

			fr.readLine();	// sequence
		}
		
		// for each chromosome
		for (int i = 0; i < starts.size(); i++) {
			int totalRegion = 0;
			
			// sort
			ArrayList<Integer> chrStarts = starts.get(i);
			Collections.sort(chrStarts);

			// merge
			int start = Integer.MAX_VALUE;
			int end = -1;
			HashMap<Integer, Integer> position = positions.get(i);
			for (int j = 0; j < chrStarts.size(); j++) {
				Integer[] pos = new Integer[2];
				pos[START] = chrStarts.get(j);
				pos[END] = position.get(pos[START]);
//				System.out.println("chr" + (i+1) + "\t" + pos[START] + "\t" + pos[END]);
				
				if (start > pos[START] && end < pos[END]) {
					// must be called once per chr!
					System.out.println("Chr" + (i+1));
					start = pos[START];
					if (end < pos[END]) {
						end = pos[END];
					}
				} else if (start <= pos[START] && pos[START] < end && end < pos[END]) {
					end = pos[END];
				} else if (end < pos[START] && start < pos[START]){
					totalRegion += (end - start);
					fm.writeLine("Chr" + (i+1) + "\t" + start + "\t" + end + "\t" + (end - start));
					start = pos[START];
					end = pos[END];
				}
			}
			if (end > 0) {
				fm.writeLine("Chr" + (i+1) + "\t" + start + "\t" + end + "\t" + (end - start));
			}
			if (totalRegion != 0) {
				fm.writeLine("Chr" + (i+1) + "\t" + totalRegion);
			}
		}
		
	}
	
	public int parseChr(String chr) {
		if (chr.endsWith("M")) {
			return 25;
		} else if (chr.endsWith("Y")) {
			return 24;
		} else if (chr.endsWith("X")) {
			return 23;
		} else {
			return Integer.parseInt(chr.substring(chr.indexOf("chr") + 3));
		}
	}

	/**
	 * get the coverage from a bait fasta file
	 * @param args
	 */
	public static void main(String[] args) {
		String inFile = "C://Documents and Settings/아랑/바탕 화면/mito/ref/mitocon_hg19_bait_gsnap_sort.fasta";
		String outFile = "hg19_chrM_bait_coverage.txt";
		if (args.length > 0) {
			inFile = args[0];
			outFile = args[1];
		}
		new ExtractRefRegion().go(inFile, outFile);
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		
	}

}
