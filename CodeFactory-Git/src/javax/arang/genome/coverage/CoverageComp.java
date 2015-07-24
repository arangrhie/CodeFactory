package javax.arang.genome.coverage;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CoverageComp extends I2Owrapper {

	static int PORTION = 1000; 
	
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		
		String line;
		String[] tokens;
		HashMap<Integer, Float> controlMap = new HashMap<Integer, Float>();
		HashMap<Integer, Integer> sampleMap = new HashMap<Integer, Integer>();
		int minPos = Integer.MAX_VALUE;
		int maxPos = -1;
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (line.length() < 4)	continue;
			if (line.startsWith("#"))	continue;
			tokens = line.split("\t");
			int pos = Integer.parseInt(tokens[0]);
			if (pos < minPos) {
				minPos = pos;
			} else if (pos > maxPos) {
				maxPos = pos;
			}
			controlMap.put(pos, Float.parseFloat(tokens[1]));
		}
		
		int totalNumBases = 0;
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split("\t");
			int pos = Integer.parseInt(tokens[0]);
			int depth = Integer.parseInt(tokens[1]);
			totalNumBases += depth;
			if (pos < minPos) {
				minPos = pos;
			} else if (pos > maxPos) {
				maxPos = pos;
			}
			sampleMap.put(pos, depth);
		}
		
		for (int key = minPos; key <= maxPos; key++) {
			if (controlMap.containsKey(key) && sampleMap.containsKey(key)) {
				float sampleNorm = (float)(sampleMap.get(key) * PORTION) / totalNumBases;
				fm.writeLine(key + "\t" + String.format("%,.3f", controlMap.get(key))
						+ "\t" + String.format("%,.3f", sampleNorm)
						+ "\t" + String.format("%,.3f", (sampleNorm - controlMap.get(key))));
			} else if (!controlMap.containsKey(key) && !sampleMap.containsKey(key)) {
				System.out.println("no reads on " + key);
			} else if (!controlMap.containsKey(key)) {
				float sampleNorm = (float)(sampleMap.get(key) * PORTION) / totalNumBases;
				fm.writeLine(key + "\t" + String.format("%,.3f", 0f)
						+ "\t" + String.format("%,.3f", sampleNorm)
						+ "\t" + String.format("%,.3f", sampleNorm));
			} else if (!sampleMap.containsKey(key)) {
				fm.writeLine(key + "\t" + String.format("%,.3f", controlMap.get(key))
						+ "\t" + String.format("%,.3f", 0f)
						+ "\t" + String.format("%,.3f", (0f - controlMap.get(key))));
			}
			
		}
		
		
	}
	
	

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar <control.cov.norm> <sample.cov>");
		System.out.println("\t<output_control_sample.cov.comp>: normalized sample sequencing depth and compared depth to control");
		System.out.println("\t\tFormat: <pos>\t<control>\t<sample>\t<sample - control>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			String control = args[0].substring(0, args[0].indexOf("_"));
			String sample = args[1].substring(0, args[1].indexOf("_"));
			new CoverageComp().go(args[0], args[1], control + "_" + sample + ".cov.comp");
		} else {
			new CoverageComp().printHelp();
		}
	}

}
