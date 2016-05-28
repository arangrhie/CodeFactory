package javax.arang.depth;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ToBed extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String contig;
		String prevContig = "";
		int position = 0;
		int start = 0;
		int depth;
		int baseSum = 0;
		boolean isTargetDepth = false;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			contig = tokens[Depth.CHR];
			if (!prevContig.equals("") && !prevContig.equals(contig) && isTargetDepth) {
				// new contig
				writeResults(fm, prevContig, start, position, baseSum);
				isTargetDepth = false;
				baseSum = 0;
			}
			
			
			depth = Integer.parseInt(tokens[Depth.DEPTH]);
			if (depth > MIN_DEPTH) {
				if (!isTargetDepth) {
					position = Integer.parseInt(tokens[Depth.POSITION]);
					start = position - 1;
					isTargetDepth = true;
				}
				baseSum += depth;
			} else {
				if (isTargetDepth) {
					writeResults(fm, prevContig, start, position, baseSum);
					isTargetDepth = false;
				}
				baseSum = 0;
			}
			position = Integer.parseInt(tokens[Depth.POSITION]);
			prevContig = contig;
		}
	}
	
	private static void writeResults(FileMaker fm, String contig, int start, int end, int baseSum) {
		int len = (end - start);
		fm.writeLine(contig + "\t" + start + "\t" + end + "\t" + baseSum + "\t" + len + "\t" + String.format("%.2f", (float) baseSum / len));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar depthToBed.jar <in.samtools.depth> <out.bed> [>=5x]");
		System.out.println("\tConvert to bed format with total bases and length");
		System.out.println("\t<in.samtools.depth>: samtools depth output");
		System.out.println("\t<out.bed>: CHR\tSTART(0-based)\tEND(1-based)\tBASES\tLENGTH\tAVG.DEPTH");
		System.out.println("\t[>=5x]: INTEGER. DEFAULT=5. Only consider regions with depth >= 5x.");
		System.out.println("Arang Rhie, 2016-05-23. arrhie@gmail.com");
	}

	static int MIN_DEPTH = 4;
	public static void main(String[] args) {
		if (args.length == 3) {
			MIN_DEPTH = Integer.parseInt(args[2]) - 1;
			new ToBed().go(args[0], args[1]);
		} else if (args.length == 2) {
			new ToBed().go(args[0], args[1]);
		} else {
			new ToBed().printHelp();
		}
	}

}
