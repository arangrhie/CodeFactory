package javax.arang.chain;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class GetBestASBlock extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		double asMax = -1;
		double as;
		boolean isFirstBest = true;
		float mappedP = 0f;
		boolean isToWrite = true;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			if (line.startsWith("chain")) {
				tokens = line.split(RegExp.WHITESPACE);
				if (!isThresholdBased) {
					if (isFirstBest) {
						isFirstBest = false;
						asMax = Double.parseDouble(tokens[Chain.SCORE]);
					} else {
						as = Double.parseDouble(tokens[Chain.SCORE]);
						if (as > asMax) {
							System.out.println(fr.getFileName() + " is not sorted by alignment score; " + asMax + " < " + as);
						}
						break;
					}
				} else {
					mappedP = (Float.parseFloat(tokens[Chain.Q_END]) - Float.parseFloat(tokens[Chain.Q_START])) * 100f / Float.parseFloat(tokens[Chain.Q_SIZE]);
					if (mappedP > mapThreshold) {
						isToWrite = true;
					} else if (!isFirstBest){
						isToWrite = false;
						break;
					} else if (isFirstBest) {
						isToWrite = true;
					}
					isFirstBest = false;
				}
			}
			if (isToWrite) {
				fm.writeLine(line);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar chainGetBestASBlock.jar <in.chain> <out.chain> [map%_threshold]");
		System.out.println("\t<in.chain>: raw chain file, sorted by alignment score");
		System.out.println("\t<out.best.chain>: chain file containing only the first best alignment scored chain block");
		System.out.println("\t[map%_threshold]: MAP_% > [map%_threshold] will be reported. OPTIONAL. If not set, will only return the first best chain.");
		System.out.println("Arang Rhie, 2015-07-24. arrhie@gmail.com");
	}

	private static float mapThreshold = 100.0f;
	private static boolean isThresholdBased = false;
	public static void main(String[] args) {
		if (args.length == 2) {
			new GetBestASBlock().go(args[0], args[1]);
		} else if (args.length == 3) {
			mapThreshold = Float.parseFloat(args[2]);
			isThresholdBased = true;
			System.out.println("mapping % threshold: " + String.format("%,.2f", mapThreshold));
			new GetBestASBlock().go(args[0], args[1]);
		} else {
			new GetBestASBlock().printHelp();
		}
	}

}
