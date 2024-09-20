package javax.arang.paf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.RegExp;

public class ToCovClippedWig extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		// Write output files
		FileMaker fmCov = new FileMaker(out + ".cov.wig",       false, true);
		FileMaker fmNrm = new FileMaker(out + ".clip_norm.wig", false, true);
		FileMaker fmAbs = new FileMaker(out + ".clip_abs.wig",  false, true);
		
		String line;
		String[] tokens;
		
		String tName = "";
		String prevTName = "";
		
		int qStart, qEnd, qLen, tStart, tEnd, tLen;
		boolean isPositive = true;
		
		// initialize tables
		HashMap<String, HashMap<Integer, Integer>> chr_pos_count = new HashMap<String, HashMap<Integer, Integer>>();
		HashMap<String, HashMap<Integer, Double>>  chr_pos_total = new HashMap<String, HashMap<Integer, Double>>();
		HashMap<String, HashMap<Integer, Integer>>  chr_pos_total_abs = new HashMap<String, HashMap<Integer, Integer>>(); // don't normalize by block coverage; for short reads
		HashMap<Integer, Integer> pos_count = null;
		HashMap<Integer, Double>  pos_total = null;
		HashMap<Integer, Integer> pos_total_abs = null;
		ArrayList<String> chrs = new ArrayList<String>();
		int key;
		Double cov;
		
		while (fr.hasMoreLines()) {
			line 	= fr.readLine();
			tokens	= line.split(RegExp.TAB);
			
			qStart	= Integer.parseInt(tokens[PAF.Q_START]);
			qEnd	= Integer.parseInt(tokens[PAF.Q_END]);
			qLen	= Integer.parseInt(tokens[PAF.Q_LEN]);

			tName	= tokens[PAF.T_NAME];
			tStart	= Integer.parseInt(tokens[PAF.T_START]);
			tEnd	= Integer.parseInt(tokens[PAF.T_END]);
			tLen	= Integer.parseInt(tokens[PAF.T_LEN]);
			
			isPositive = PAF.isPositive(tokens[PAF.STRAND]);
			
			if (!prevTName.equals(tName)) {
				System.err.println("Collecting info for " + tName + " ...");
				pos_count = chr_pos_count.get(tName);
				pos_total = chr_pos_total.get(tName);
				if (pos_count == null) {
					chrs.add(tName);
					HashMap<Integer, Integer> pc = new HashMap<Integer, Integer>();
					HashMap<Integer, Double>  pt = new HashMap<Integer, Double>();
					HashMap<Integer, Integer> pta = new HashMap<Integer, Integer>();

					for (int i = 0; i <= tLen/span; i++) {
						pc.put(i, 0);
						pt.put(i, 0d);
						pta.put(i, 0);
					}
					
					chr_pos_count.put(tName, pc);
					pos_count = pc;
					
					chr_pos_total.put(tName, pt);
					pos_total = pt;

					chr_pos_total_abs.put(tName, pta);
					pos_total_abs = pta;
				} else {
					// For unsorted paf file(s)
					pos_count = chr_pos_count.get(tName);
					pos_total = chr_pos_total.get(tName);
					pos_total_abs = chr_pos_total_abs.get(tName);
				}
			}

			// PAF is weird; tStart and tEnd depends on the orientation.
			key = isPositive ? tStart / span : tEnd / span;
			// start clipped?
			if (qStart > min_clipped) {
				// System.err.println("[ DEBUG ] Inserting " + key + " " + line);
				pos_count.put(key, pos_count.get(key) + 1);
			}

			key = isPositive ? tEnd / span : tStart / span; 
			// end clipped?
			if ((qLen - qEnd) > min_clipped) {
				// System.err.println("[ DEBUG ] Inserting " + key + " " + line);
				pos_count.put(key, pos_count.get(key) + 1);
			}

			for (key = tStart / span; key <= tEnd/span; key++) {
				int wStart = key    * span;   // start position of the window at key
				int wEnd   = wStart + span;   // end   position of the window at key
				if (wEnd > tLen) wEnd = tLen; // not to exceed the target sequence length for the last window
				
				if (tStart > wStart || tEnd < wEnd) {
					cov = (Math.min(tEnd, wEnd) - Math.max(tStart, wStart)) / (double) span;
				} else {
					cov = 1.0d;
				}
				pos_total.put(key, pos_total.get(key) + cov);
				pos_total_abs.put(key, pos_total_abs.get(key) + 1);
			}
			
			prevTName = tName;
		}
		System.err.println("Finished collecting per position info for " + chr_pos_count.size() + " sequences");
		Collections.sort(chrs);
		
		
		fmCov.writeLine("track type=\"wiggle_0\" name=\"" + name + " Cov\"");
		fmNrm.writeLine("track type=\"wiggle_0\" name=\"" + name + " Clipped (%)\"");
		fmAbs.writeLine("track type=\"wiggle_0\" name=\"" + name + " Clipped\"");
		
		for (int i = 0; i < chrs.size(); i++) {
			tName = chrs.get(i);

			// fixedStep chrom=chr1 start=1 step=1024 span=1024
			fmCov.writeLine("fixedStep chrom=" + tName + " start=1 step=" + span + " span=" + span);
			fmNrm.writeLine("fixedStep chrom=" + tName + " start=1 step=" + span + " span=" + span);
			fmAbs.writeLine("fixedStep chrom=" + tName + " start=1 step=" + span + " span=" + span);
			
			pos_count = chr_pos_count.get(tName);
			pos_total = chr_pos_total.get(tName);
			pos_total_abs = chr_pos_total_abs.get(tName);
			
			for (int j = 0; j < pos_count.size(); j++) {
				fmCov.writeLine(String.format("%.2f", pos_total.get(j)));
				fmAbs.writeLine(pos_count.get(j) + "");
				if (pos_total.get(j) == 0) {
					fmNrm.writeLine("-1");
				} else {
					fmNrm.writeLine(String.format("%.2f", ((double) 100 * pos_count.get(j)) / pos_total_abs.get(j)));
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar pafToCovClippedWig.jar <in.paf> <name> <span> <out-prefix> [min-clipped]");
		System.err.println("  in.paf      input paf file. accepts unsorted.");
		System.err.println("  name        name prefix of the tracks. String");
		System.err.println("  span        span of the interval. INT");
		System.err.println("  out-prefix  output prefix.");
		System.err.println("  min-clipped minimum num. of clipped bases. DEFAULT=100. OPTIONAL.");
		System.err.println();
		System.err.println("Three output files will be generated:");
		System.err.println("  out.cov.wig       coverage wiggle file with name=\"<name> Cov\"");
		System.err.println("  out.clip_norm.wig clipped read counts normalized by total reads with name=\"<name> Clipped (%)\"");
		System.err.println("  out.clip_abs.wig  clipped read counts with name=\"<name> Clipped\"");
		System.err.println("Arang Rhie, 2024-01-29. arrhie@gmail.com");
	}
	
	private static int span = 10000;
	private static String name = "";
	private static String out  = "";
	private static double min_clipped = 100;

	public static void main(String[] args) {
		if (args.length >= 4) {
			name = args[1];
			span = Integer.parseInt(args[2]);
			out  = args[3];
			if (args.length == 5) {
				min_clipped = Double.parseDouble(args[4]);
			}
			new ToCovClippedWig().go(args[0]);
		} else {
			new ToCovClippedWig().printHelp();
		}
	}
}
