package javax.arang.chain;

import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class FilterBestMQ extends IOwrapper {

	private static final int CONTIG_START = 6;
	private static final int CONTIG_END = 7;
	private static final int START = 0;
	private static final int END = 1;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		String[] tokens;
		
		int start;
		int end;
		
		Vector<Integer[]> regions = new Vector<Integer[]>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			start = Integer.parseInt(tokens[CONTIG_START]);
			end = Integer.parseInt(tokens[CONTIG_END]);
			Integer[] region = new Integer[2];
			region[START] = start;
			region[END] = end;
			
			if (!isOverlapping(regions, region)) {
				regions.add(region);
				fm.writeLine(line);
			}
		}
		
	}

	private boolean isOverlapping(Vector<Integer[]> regions, Integer[] region) {
		if (regions.size() == 0) {
			return false;
		}
		
		Integer[] alignedRegion;
		for (int i = 0; i < regions.size(); i++) {
			alignedRegion = regions.get(i);
			if (alignedRegion[START] <= region[START] && region[START] <= alignedRegion[END]
					|| alignedRegion[START] <= region[END] && region[END] <= alignedRegion[END]
					|| region[START] <= alignedRegion[START] && alignedRegion[END] <= region[END] ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar chainFilterBestMQ.jar <in.sorted.bed> <out.bed>");
		System.out.println("\tGet non-overlapping best MQ chains from <in.sorted.bed>");
		System.out.println("\t<in.sorted.bed>:  CHROM	START	END	STRND	LEN	CONTIG	START	END	STRAND	LEN	MQ	ID");
		System.out.println("\t");
		System.out.println("Arang Rhie, 2015-06-17. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new FilterBestMQ().go(args[0], args[1]);
		} else {
			new FilterBestMQ().printHelp();
		}
	}

}
