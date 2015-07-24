package javax.arang.genome.bed;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class AverageCoverage extends IOwrapper {

	public static final int GROUP = 1;
	public static final int COVERAGE = 2;
	public static final int DEPTH = 3;
		
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		HashMap<Integer, Float> coverageMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> depthMap = new HashMap<Integer, Float>();
		HashMap<Integer, Integer> countMap = new HashMap<Integer, Integer>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.equals(""))	continue;
			tokens = line.split("[\t ]");
			int group = Integer.parseInt(tokens[GROUP].substring(tokens[GROUP].indexOf("#") + 1));
			if (!coverageMap.containsKey(group)) {
				coverageMap.put(group, Float.parseFloat(tokens[COVERAGE]));
				depthMap.put(group, Float.parseFloat(tokens[DEPTH]));
				countMap.put(group, 1);
			} else {
				coverageMap.put(group, coverageMap.get(group) + Float.parseFloat(tokens[COVERAGE]));
				depthMap.put(group, depthMap.get(group) + Float.parseFloat(tokens[DEPTH]));
				countMap.put(group, countMap.get(group) + 1);
			}
		}
	
		for (int group = 1; group <= coverageMap.size(); group++) {
			fm.writeLine(group + "\t"
					+ String.format("%,.2f", coverageMap.get(group) / countMap.get(group)) + "\t"
					+ String.format("%,.2f", depthMap.get(group) / countMap.get(group)));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedAverageCoverage.jar <sample.summary>");
		System.out.println("\t<in>: output tailed(4 lines) result from bedCoverageDepth.jar");
		System.out.println("\t\tchr1 #1	99.82	427.38\n" +
				"\t\tchr1 #2	99.27	427.36\n" +
				"\t\tchr1 #3	98.78	427.31\n" +
				"\t\tchr1 #4	98.00	427.14\n" +
				"\t\tchr2 #1	99.55	243.04\n" +
				"\t\tchr2 #2	98.74	243.01");
		System.out.println("\t<out>: sample.summary.total : total average base coverage on 1x, 8x, 15x, 30x");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new AverageCoverage().go(args[0], args[0] + ".total");
		} else {
			new AverageCoverage().printHelp();
		}

	}

}
