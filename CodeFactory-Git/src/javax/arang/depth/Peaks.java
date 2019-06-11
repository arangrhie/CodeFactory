package javax.arang.depth;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Peaks extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		
		int depth;
		double count = 0;
		double minCount = 0;
		double prevCount = 0;
		double d = 0;
		double prevD = 0;
		boolean isDepthZero = true;
		boolean isDepthOne = false;
		String minmax = "min";
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.WHITESPACE);
			depth = Integer.parseInt(tokens[0]);
			count = Double.parseDouble(tokens[1]);
			// Skip the first 0-depth
			if (isDepthZero) {
				isDepthZero = false;
				prevCount = count;
				prevD = 0;
				isDepthOne = true;
				continue;
			}
			
			if (isDepthOne) {
				minCount = count;
				System.out.println(depth + "\t" + count + "\tStart");
				isDepthOne = false;
			} else if (minCount > count) {
				System.out.println((depth-1) + "\t" + prevCount + "\tStop");
				break;
			}
			
			d = count - prevCount;
			
			if (d * prevD < 0) {
				if (d < 0) {
					minmax = "max";
				} else {
					minmax = "min";
				}
				System.out.println((depth - 1) + "\t" + prevCount + "\t" + minmax + "\t" + (depth-1) * prevCount);
			}
			prevCount = count;
			prevD = d;
			
			
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar depthPeaks.jar <gencov>");
		System.out.println("\tSimply prints each min / max peaks");
		System.out.println("\t<gencov>: Depth\tCount");
		System.out.println("Arang Rhie, 2018-07-17. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new Peaks().go(args[0]);
		} else {
			new Peaks().printHelp();
		}
	}

}
