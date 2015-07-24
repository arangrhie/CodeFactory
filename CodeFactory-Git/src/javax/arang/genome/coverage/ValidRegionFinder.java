package javax.arang.genome.coverage;

import java.util.StringTokenizer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class ValidRegionFinder extends Rwrapper {

	int start;
	int end;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		StringTokenizer st;
		
		int min = end;
		int max = start;
		int lineNum = 0;
		int startLineNum = 0;
		int endLineNum = 0;
		boolean hasStart = false;
		boolean hasEnd = false;
		
		while (fr.hasMoreLines()) {
			lineNum++;
			line = fr.readLine().toString();
			st = new StringTokenizer(line);
			st.nextToken();	// chr
			int pos = Integer.parseInt(st.nextToken());
			if (pos < start) {
				continue;
			}
			if (pos == start) {
				min = start;
				startLineNum = lineNum;
				hasStart = true;
				continue;
			}
			if (pos > start) {
				if (!hasStart) {
					min = pos;
					startLineNum = lineNum;
					hasStart = true;
				}
				if (pos < end) {
					if (!hasEnd) {
						max = pos;
						endLineNum = lineNum;
					}
					continue;
				}
				if (pos == end) {
					max = end;
					endLineNum = lineNum;
					hasEnd = true;
					break;
				}
				if (pos > end) {
					break;
				}
			}
		}
		System.out.println("startLineNum:\t" + startLineNum);
		System.out.println("endLineNum:\t" + endLineNum);
		System.out.println("min:\t" + min);
		System.out.println("max:\t" + max);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ValidRegionFinder finder = new ValidRegionFinder();
		if (args.length < 3) {
			finder.printHelp();
			return;
		}
		System.out.println(args[0] + "\t" + args[1] + "\t" + args[2]);
		finder.start = Integer.parseInt(args[1]);
		finder.end = Integer.parseInt(args[2]);
		finder.go(args[0]);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar validRegionFinder <inPath> <start> <end>");
		System.out.println("Find the start / end position from a sorted base coverage file");
		System.out.println("Input file example: <chr>\t<pos>\trefAllele\tqualAvg\tcounts");
		System.out.println("2	10024	C	71	20");
	}

}
