package javax.arang.paf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToCovWig extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String tName = "";
		String prevTName = "";
		
		int tStart, tEnd, tLen;
		
		// initialize tables
		HashMap<String, HashMap<Integer, Double>> chr_pos_total = new HashMap<String, HashMap<Integer, Double>>();
		HashMap<Integer, Double> pos_total = null;
		ArrayList<String> chrs = new ArrayList<String>();
		int key;
		Double cov;
		
		while (fr.hasMoreLines()) {
			line 	= fr.readLine();
			tokens	= line.split(RegExp.TAB);
			
			tName	= tokens[PAF.T_NAME];
			tStart	= Integer.parseInt(tokens[PAF.T_START]);
			tEnd	= Integer.parseInt(tokens[PAF.T_END]);
			tLen	= Integer.parseInt(tokens[PAF.T_LEN]);
			
			// tLen/span = total num. windows
			
			if (!prevTName.equals(tName)) {
				pos_total = chr_pos_total.get(tName);
				if (pos_total == null) {
					System.err.println("Collecting info for " + tName + " ...");
					chrs.add(tName);
					
					HashMap<Integer, Double> pt = new HashMap<Integer, Double>();
					for (int i = 0; i <= tLen/span; i++) pt.put(i, 0.0d);
					chr_pos_total.put(tName, pt);
					pos_total = pt;
				}
			}

			for (key = tStart / span; key <= tEnd/span; key++) {
				// is it partially falling in the window?
				int wStart = key    * span;   // start position of the window at key
				int wEnd   = wStart + span;   // end   position of the window at key
				if (wEnd > tLen) wEnd = tLen; // not to exceed the target sequence length for the last window
				
				if (tStart > wStart || tEnd < wEnd) {
					cov = (Math.min(tEnd, wEnd) - Math.max(tStart, wStart)) / (double) span;
				} else {
					cov = 1.0d;
				}
				
				pos_total.put(key, pos_total.get(key) + cov);
			}
			
			prevTName = tName;
		}
		System.err.println("Finished collecting per position info for " + chr_pos_total.size() + " sequences");
		Collections.sort(chrs);
		
		// track type="wiggle_0" name="HiFi"
		System.out.println("track type=\"wiggle_0\" name=\"" + name + "\"");
		for (int i = 0; i < chrs.size(); i++) {
			tName = chrs.get(i);

			// fixedStep chrom=chr1 start=1 step=1024 span=1024
			System.out.println("fixedStep chrom=" + tName + " start=1 step=" + span + " span=" + span);
			pos_total = chr_pos_total.get(tName);
			
			for (int j = 0; j < pos_total.size(); j++) System.out.println(String.format("%.2f", pos_total.get(j)));
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar pafToCovWig.jar <in.paf> <name> <span>");
		System.err.println("\t<name>  : name of this track. String");
		System.err.println("\t<span>  : span of the interval. INT");
		System.err.println("\tstdout: .wig format.");
		System.err.println("Arang Rhie, 2023-09-22. arrhie@gmail.com");
	}
	
	private static int span = 10000;
	private static String name = "";
	
	public static void main(String[] args) {
		if (args.length == 3) {
			name = args[1];
			span = Integer.parseInt(args[2]);
			new ToCovWig().go(args[0]);
		} else {
			new ToCovWig().printHelp();
		}
	}
}
