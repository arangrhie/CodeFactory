package javax.arang.paf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToMinAvgMedMaxWig extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String tName = "";
		String prevTName = "";
		
		int tStart, tEnd, tLen;
		double val;
		
		// initialize tables
		ArrayList<String> chrs = new ArrayList<String>();
		HashMap<String, HashMap<Integer, ArrayList<Double>>> chr_pos_val = new HashMap<String, HashMap<Integer, ArrayList<Double>>>();
		HashMap<Integer, ArrayList<Double>> pos_val = null;
		ArrayList<Double> vals = null;
		

		// read paf and collect vals
		while (fr.hasMoreLines()) {
			line	= fr.readLine();
			tokens	= line.split(RegExp.TAB);
			
			tName	= tokens[PAF.T_NAME];
			
			if (!prevTName.equals(tName)) {
				pos_val = chr_pos_val.get(tName);
				if (pos_val == null) {
					System.err.println("Collecting info for " + tName + " ...");
					chrs.add(tName);
					HashMap<Integer, ArrayList<Double>> pv = new HashMap<Integer, ArrayList<Double>>();
					
					tLen	= Integer.parseInt(tokens[PAF.T_LEN]);
					for (int i = 0; i <= tLen/span; i++) {
						ArrayList<Double> v = new ArrayList<Double>();
						pv.put(i, v);
					}
					chr_pos_val.put(tName, pv);
					pos_val = pv;
				}
			}
			
			tStart	= Integer.parseInt(tokens[PAF.T_START]);
			tEnd	= Integer.parseInt(tokens[PAF.T_END]);
			val	= Double.parseDouble(tokens[colIdx]);
			
			for (int i = tStart/span; i <= tEnd/span; i++) {
				pos_val.get(i).add(val);
			}
			
			prevTName = tName;
			
		}
		
		System.err.println("Finished collecting per position info for " + chr_pos_val.size() + " sequences");
		Collections.sort(chrs);
		
		oMin = new FileMaker(fr.getDirectory() + "/" + outPrefix + ".min.wig");
		oAvg = new FileMaker(fr.getDirectory() + "/" + outPrefix + ".avg.wig");
		oMed = new FileMaker(fr.getDirectory() + "/" + outPrefix + ".med.wig");
		oMax = new FileMaker(fr.getDirectory() + "/" + outPrefix + ".max.wig");
		oBed = new FileMaker(fr.getDirectory() + "/" + outPrefix + ".bed");
		
		oMin.writeLine("track type=\"wiggle_0\" name=\"" + name + " Min\"");
		oAvg.writeLine("track type=\"wiggle_0\" name=\"" + name + " Avg\"");
		oMed.writeLine("track type=\"wiggle_0\" name=\"" + name + " Med\"");
		oMax.writeLine("track type=\"wiggle_0\" name=\"" + name + " Max\"");
		
		double sum, avg;
		
		for (int i = 0; i < chrs.size(); i++) {
			tName = chrs.get(i);
			pos_val	= chr_pos_val.get(tName);
			writeOutWigLine("fixedStep chrom=" + tName + " start=1 step=" + span + " span=" + span);
			
			for (int j = 0; j < pos_val.size(); j++) {
				vals = pos_val.get(j);
				if (vals.size() == 0) {
					writeOutWigLine("-1");
					writeOutBed(tName, j, -1, -1, -1, -1);
				} else if (vals.size() == 1) {
					// 1 depth of cov.
					writeOutWigLine(vals.get(0));
					writeOutBed(tName, j, vals.get(0), vals.get(0), vals.get(0), vals.get(0));
				} else {
					Collections.sort(vals);
					sum = 0;
					for (int k = 0; k < vals.size(); k++) {
						sum += vals.get(k);
					}
					avg = sum / vals.size();
					writeOutWigLine(vals.get(0), avg, vals.get(vals.size() / 2), vals.get(vals.size() - 1));
					writeOutBed(tName, j, vals.get(0), avg, vals.get(vals.size() / 2), vals.get(vals.size() - 1));
				}
			}
		}
		
	}
	
	private void writeOutBed(String tName, int j, double min, double avg, double med, double max) {
		oBed.writeLine(tName + "\t" + (j * span) + "\t" + ((j+1) * span) + "\t" +
				String.format("%.2f", min) + "\t" +
				String.format("%.2f", avg) + "\t" +
				String.format("%.2f", med) + "\t" + 
				String.format("%.2f", max));
	}

	private void writeOutWigLine(Double min, double avg, Double med, Double max) {
		oMin.writeLine(String.format("%.2f", min));
		oAvg.writeLine(String.format("%.2f", avg));
		oMed.writeLine(String.format("%.2f", med));
		oMax.writeLine(String.format("%.2f", max));
	}

	private void writeOutWigLine(Double val) {
		String text = String.format("%.2f", val);
		writeOutWigLine(text);		
	}

	private void writeOutWigLine(String text) {
		oMin.writeLine(text);
		oAvg.writeLine(text);
		oMed.writeLine(text);
		oMax.writeLine(text);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar pafToMinAvgMedMaxWig.jar <in.paf> <name> <span> <col-idx> <out-prefix>");
		System.out.println("\t<name>   : name of this track. String");
		System.out.println("\t<span>   : span of the interval. INT");
		System.out.println("\t<col-idx>: column to collect stats. DEFAULT=2");
		System.out.println("\t<out-prefix>: output prefix");
		System.out.println("\t<out-prefix.type.wig> : 4 .wig formatted files for type = min avg med and max.");
		System.out.println("Arang Rhie, 2021-09-12. arrhie@gmail.com");

	}
	
	private static int span = 10000;
	private static String name = "";
	private static int colIdx = 1;

	private static FileMaker oMin, oAvg, oMed, oMax, oBed;
	private static String outPrefix;
	
	public static void main(String[] args) {
		if (args.length >= 4) {
			name = args[1];
			span = Integer.parseInt(args[2]);
			colIdx = Integer.parseInt(args[3]) - 1;
			outPrefix = args[4];
			new ToMinAvgMedMaxWig().go(args[0]);
		} else {
			new ToMinAvgMedMaxWig().printHelp();
		}

	}

}
