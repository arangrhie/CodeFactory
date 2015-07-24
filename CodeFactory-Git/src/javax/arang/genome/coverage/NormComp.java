package javax.arang.genome.coverage;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class NormComp extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		String line;
		String[] tokens;
		HashMap<Integer, Integer> controlMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Float> sampleMap = new HashMap<Integer, Float>();
		int minPos = Integer.MAX_VALUE;
		int maxPos = -1;
		
		// control
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (line.length() < 4)	continue;
			if (line.startsWith("#"))	continue;
			tokens = line.split("\t");
			int pos = Integer.parseInt(tokens[0]);
			int depth = Integer.parseInt(tokens[1]) + C;
			if (pos < minPos) {
				minPos = pos;
			} else if (pos > maxPos) {
				maxPos = pos;
			}
			controlMap.put(pos, depth + C);
		}
		
		// sample
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split("\t");
			int pos = Integer.parseInt(tokens[0]);
			int depth = Integer.parseInt(tokens[1]) + C;
			if (pos < minPos) {
				minPos = pos;
			} else if (pos > maxPos) {
				maxPos = pos;
			}
			sampleMap.put(pos, X * depth);
		}
		
		for (int key = minPos; key <= maxPos; key++) {
			float sample = sampleMap.get(key);
			int control = controlMap.get(key);
//			fm.writeLine(key + "\t" + control + "\t" + String.format("%,.2f", sample) + "\t"+ String.format("%,.2f", (sample/control)));
			fm.writeLine(key + "\t"+ String.format("%,.2f", (sample/control)));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar coverageNormComp.jar <control.cov> <sample.cov> [C] <X>");
		System.out.println("\t<cov.comp>: <pos>\t(X*(C+b<sample>))/(C+b<control>)");
	}

	static int C = 5;
	static float X = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 4) {
			C = Integer.parseInt(args[2]);
			X = Float.parseFloat(args[3]);
			new NormComp().go(args[0], args[1], args[1] + ".comp");
		} else if (args.length == 3) {
			X = Float.parseFloat(args[2]);
			new NormComp().go(args[0], args[1], args[1] + ".comp");
		} else {
			new NormComp().printHelp();
		}
	}

}
