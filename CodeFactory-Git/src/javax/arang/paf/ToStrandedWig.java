package javax.arang.paf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToStrandedWig extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String tName = "";
		String prevTName = "";
		
		int tStart, tEnd, tLen;
		boolean isPositive;	// true = +, false = -
		
		// initialize tables
		HashMap<String, HashMap<Integer, Integer>> chr_pos_count = new HashMap<String, HashMap<Integer, Integer>>();
		HashMap<String, HashMap<Integer, Integer>> chr_pos_total = new HashMap<String, HashMap<Integer, Integer>>();
		HashMap<Integer, Integer> pos_count = null;
		HashMap<Integer, Integer> pos_total = null;
		ArrayList<String> chrs = new ArrayList<String>();
		int key;
		
		while (fr.hasMoreLines()) {
			line 	= fr.readLine();
			tokens	= line.split(RegExp.TAB);
			
			tName	= tokens[PAF.T_NAME];
			tStart	= Integer.parseInt(tokens[PAF.T_START]);
			tEnd	= Integer.parseInt(tokens[PAF.T_END]);
			
			isPositive	= (tokens[PAF.STRAND].equals("+")) ? true : false;
			
			if (!prevTName.equals(tName)) {
				pos_count = chr_pos_count.get(tName);
				pos_total = chr_pos_total.get(tName);
				if (pos_count == null) {
					System.err.println("Collecting info for " + tName + " ...");
					chrs.add(tName);
					HashMap<Integer, Integer> pc = new HashMap<Integer, Integer>();
					tLen	= Integer.parseInt(tokens[PAF.T_LEN]);
					for (int i = 0; i <= tLen/span; i++) pc.put(i, 0);
					
					chr_pos_count.put(tName, pc);
					pos_count = pc;
					
					if (type == NORM) {
						HashMap<Integer, Integer> pt = new HashMap<Integer, Integer>();
						for (int i = 0; i <= tLen/span; i++) pt.put(i, 0);
						chr_pos_total.put(tName, pt);
						pos_total = pt;
					}
				}
			}
			
			// + or -. Collect + for type=POS (1) and type=NORM (3).
			if (( type %2 == 1 && isPositive) || (type == NEG && !isPositive )) {
				for (key = tStart/span; key < tEnd/span; key++) {
					pos_count.put(key, pos_count.get(key) + 1);
				}
			}
			
			// Count the total for normalizing
			if (type == NORM) {
				for (key = tStart/span; key < tEnd/span; key++) {
					pos_total.put(key, pos_total.get(key) + 1);
				}
			}
			
			prevTName = tName;
		}
		System.err.println("Finished collecting per position info for " + chr_pos_count.size() + " sequences");
		Collections.sort(chrs);
		
		// track type="wiggle_0" name="HiFi"
		System.out.println("track type=\"wiggle_0\" name=\"" + name + "\"");
		for (int i = 0; i < chrs.size(); i++) {
			tName = chrs.get(i);

			// fixedStep chrom=chr1 start=1 step=1024 span=1024
			System.out.println("fixedStep chrom=" + tName + " start=1 step=" + span + " span=" + span);
			
			pos_count = chr_pos_count.get(tName);
			
			if (type == NORM) {
				pos_total = chr_pos_total.get(tName);
			}
			
			for (int j = 0; j < pos_count.size(); j++) {
				if (type == NORM) {
					if (pos_total.get(j) == 0) {
						System.out.println("-1");
					} else {
						System.out.println(String.format("%.2f", ((double) pos_count.get(j)) / pos_total.get(j))); 
					}
				} else {
					System.out.println(pos_count.get(j));
				}
			}
		}		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar pafToStrandedWig.jar <in.paf> <name> <span> <type>");
		System.out.println("\t<name>  : name of this track. String");
		System.out.println("\t<span>  : span of the interval. INT");
		System.out.println("\t<type>  : + | - | norm. norm = normalized + counts by total reads");
		System.out.println("\tstdout: .wig format.");
		System.out.println("Arang Rhie, 2021-09-12. arrhie@gmail.com");
		
	}
	
	private static int span = 10000;
	private static String name = "";
	private static int type = 0;
	
	private static final int NEG=2;
	private static final int NORM=3;

	public static void main(String[] args) {
		if (args.length >= 3) {
			name = args[1];
			span = Integer.parseInt(args[2]);
			if (args.length == 4) {
				if (args[3].equals("+")) {
					type = 1;
				} else if (args[3].equals("-")) {
					type = 2;
				} else if (args[3].equals("norm")) {
					type = 3;
				} else {
					new ToStrandedWig().printHelp();
					System.err.println("ERROR: Unknown type " + args[3]);
					System.exit(-1);
				}
			}
			new ToStrandedWig().go(args[0]);
		} else {
			new ToStrandedWig().printHelp();
		}
	}

}
