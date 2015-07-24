package javax.arang.genome.bed;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class BaseCoverage extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;

		HashMap<String, HashMap<Integer, Integer>> bedCovMap = new HashMap<String, HashMap<Integer, Integer>>();
		HashMap<String, Integer[]> minMaxMap = new HashMap<String, Integer[]>();
		Vector<String> chrList = new Vector<String>();
		
		final int CHR = 0;
		final int POS_START = 1;
		final int POS_END = 2;
//		boolean isTiled = false;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.length() < 5)	continue;
			if (line.startsWith("track")) {
//				if (line.substring(line.indexOf("=") + 1).startsWith("tiled")) {
//					isTiled = true;
//					continue;
//				} else {
					continue;
//				}
			}
//			if (!isTiled)	continue;
			tokens = line.split("\t");
			String chr = tokens[CHR];
			if (!chrList.contains(chr)) {
				chrList.add(chr);
				HashMap<Integer, Integer> posMap = new HashMap<Integer, Integer>();
				bedCovMap.put(chr, posMap);
				Integer[] minMax = new Integer[2];
				minMax[0] = Integer.MAX_VALUE;
				minMax[1] = Integer.MIN_VALUE;
				minMaxMap.put(chr, minMax);
			} else {
				HashMap<Integer, Integer> posMap = bedCovMap.get(chr);
				int from = Integer.parseInt(tokens[POS_START]);
				int to = Integer.parseInt(tokens[POS_END]);
				for (int key = from; key <= to; key++) {
					if (posMap.containsKey(key)) {
						posMap.put(key, posMap.get(key) + 1);
					} else {
						posMap.put(key, 1);
					}
				}
				Integer[] minMax = minMaxMap.get(chr);
				if (from < minMax[0]) {
					minMax[0] = from;
				}
				if (to > minMax[1]) {
					minMax[1] = to;
				}
			}
		}
		
		int[] covPos = new int[2];
		for (String chr : bedCovMap.keySet()) {
			FileMaker fm = new FileMaker(".", chr + ".bed.cov");
			Integer[] minMax = minMaxMap.get(chr);
			HashMap<Integer, Integer> posMap = bedCovMap.get(chr);
			int prevCov = -1;
			for (int i = minMax[0]; i < minMax[1]; i++) {
				if (posMap.containsKey(i)) {
					int cov = posMap.get(i);
//					fm.writeLine(i + "\t" + posMap.get(i));
					if (prevCov != cov) {
						if (prevCov != -1)	fm.writeLine(covPos[0] + "\t" + covPos[1] + "\t" + prevCov);
						prevCov = cov;
						covPos[0] = i;
					} else {
						covPos[1] = i;
					}
				} else {
					if (prevCov != 0) {
						fm.writeLine(covPos[0] + "\t" + covPos[1] + "\t" + prevCov);
						prevCov = 0;
						covPos[0] = i;
					} else {
						covPos[1] = i;
					}
//					fm.writeLine(i + "\t0");
				}
			}
			fm.writeLine(covPos[0] + "\t" + covPos[1] + "\t" + prevCov);
			fm.closeMaker();
		}
		
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedBaseCoverage.jar <in.bed>");
		System.out.println("\toutput: <chr.bed.cov>");
		System.out.println("\t\tpos\tpos\tcoverage");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new BaseCoverage().go(args[0]);
		} else {
			new BaseCoverage().printHelp();
		}

	}

}
