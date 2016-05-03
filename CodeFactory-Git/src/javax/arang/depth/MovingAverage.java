package javax.arang.depth;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class MovingAverage extends IOwrapper{

	private static int window=100;
	private static final int CHR = 0;
	private static final int POSITION = 1;
	private static final int DEPTH = 2;
	
	public static void main(String[] args) {
		if (args.length==2) {
			new MovingAverage().go(args[0], args[1]);
		} else if (args.length == 3) {
			window = Integer.parseInt(args[2]) * 100;
			new MovingAverage().go(args[0], args[1]);
		} else {
			new MovingAverage().printHelp();
		}
	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String prevChr = "";
		String chr;
		int prevPosition = 0;
		int position = 0;
		double depth = 0d;
		int start = 0;
		
		boolean isFirst = true;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			chr = tokens[CHR];
			position = Integer.parseInt(tokens[POSITION]);

			if (isFirst) {
				start = position;
				prevPosition = start + 1;
				isFirst = false;
				depth = Double.parseDouble(tokens[DEPTH]);
				prevChr = chr;
				continue;
			}
			
			if (!chr.equals(prevChr)) {
				fm.writeLine(prevChr + "\t" + start + "\t" + prevPosition + "\t" + (prevPosition - start) + "\t" + depth + "\t" + String.format("%.2f", ((float) depth / (prevPosition - start))));
				start = position;
				prevPosition = start + 1;
				depth = Double.parseDouble(tokens[DEPTH]);
				prevChr = chr;
				continue;
			}
			
			if ((position - start) >= window) {
				//System.out.println(position + "\t" + start + "\t" + (position - start));
				fm.writeLine(chr + "\t" + start + "\t" + prevPosition + "\t" + (prevPosition - start) + "\t" + depth + "\t" + String.format("%.2f", ((float) depth / (prevPosition - start))));
				start = position;
				prevPosition = start + 1;
				depth = Double.parseDouble(tokens[DEPTH]);
				continue;
			}
			
			depth += Double.parseDouble(tokens[DEPTH]);
			prevPosition = position;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar depthMovingAverage.jar <in.depth> <out.depth_by_moving_average> [window=1]");
		System.out.println("\t<in.depth>: depth file generated with samtools depth");
		System.out.println("\t<out.depth_by_moving_average>: in 1 window, count the number of positions and devide the total aggregated depth in this region.");
		System.out.println("\t\tFormat: CHR\tFrom\tTo\t(To-From)\tDepthSum\tAvgDepth");
		System.out.println("\t[window]: window size in 100 bp. DEFAULT=1. Must be INTEGER.");
		System.out.println("Arang Rhie, 2016-04-29. arrhie@gmail.com");
	}

}
