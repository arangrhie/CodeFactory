package javax.arang.bed;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class Phase extends IOwrapper {

	private static final int REGION_START = 0;
	private static final int REGION_END = 1;
	private static final int MAPPED_LEN = 3;
	private static boolean hasHeader = true;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		HashMap<String, Vector<Integer[]>> chrHaplotypeARegionMap = new HashMap<String, Vector<Integer[]>>();
		HashMap<String, Vector<Integer[]>> chrHaplotypeBRegionMap = new HashMap<String, Vector<Integer[]>>();
		
		String line;
		String[] tokens;
		String chr;
		int start;
		int end;
		int mappedLen;
		Integer[] region;
		Vector<Integer[]> regionMap;
		
		if (hasHeader) {
			line = fr.readLine();
			fm.writeLine(line + "\tHaplotype");
		}
	
		
		// Read bed file
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			chr = tokens[Bed.CHROM];
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			mappedLen = Integer.parseInt(tokens[MAPPED_LEN]);
			region = new Integer[2];
			region[REGION_START] = start;
			region[REGION_END] = end;
			if (!chrHaplotypeARegionMap.containsKey(chr)) {
				regionMap = new Vector<Integer[]>();
				regionMap.add(region);
				chrHaplotypeARegionMap.put(chr, regionMap);
				fm.writeLine(line + "\t" + "A");
			} else {
				regionMap = chrHaplotypeARegionMap.get(chr);
				// isOverlapping
				if (isOverlappingRegion(region, regionMap, mappedLen)) {
					if (!chrHaplotypeBRegionMap.containsKey(chr)) {
						regionMap = new Vector<Integer[]>();
						regionMap.add(region);
						chrHaplotypeBRegionMap.put(chr, regionMap);
					} else {
						regionMap = chrHaplotypeBRegionMap.get(chr);
						regionMap.add(region);
					}
					fm.writeLine(line + "\t" + "B");
				}
				// Not overlapping
				else {
					regionMap.add(region);
					fm.writeLine(line + "\t" + "A");
				}
			}
		}
	}

	private boolean isOverlappingRegion(Integer[] region, Vector<Integer[]> regionMap, int mappedLen) {
		int padding = 0;
		if (mappedLen < 20000) {
			padding = 10000;
		} else if (mappedLen < 50000) {
			padding = mappedLen / 2;
		} else {
			padding = 50000;
		}
		
		Integer[] mappedRegion;
		for (int i = 0; i < regionMap.size(); i++) {
			mappedRegion = regionMap.get(i);
			if (mappedRegion[REGION_START] <= region[REGION_START] && region[REGION_END] <= mappedRegion[REGION_END]) {
				return true;
			}
			else if (region[REGION_START] <= mappedRegion[REGION_START]
					&& mappedRegion[REGION_START] <= region[REGION_END]
					&& region[REGION_END] - mappedRegion[REGION_START] > padding) {
				return true;
			}
			else if (region[REGION_START] <= mappedRegion[REGION_END]
					&& mappedRegion[REGION_END] <= region[REGION_END]
					&& mappedRegion[REGION_END] - region[REGION_START] > padding) {
				return true;
			}
			
		}
		
		return false;
	}

	@Override
	public void printHelp() {
		System.out.println("java -jar -Xmx2g bedPhase.jar <in.bed> <out.bed> [hasHeader=TRUE]");
		System.out.println("\tHaplotype A or B will be added at the end of line.");
		System.out.println("\tWhen one contig is inclusive to another, it will be treated as a partner haplotype.");
		System.out.println("\t<in.bed>: Sort by mapped len or alignment score");
		System.out.println("\t<out.bed>: <in.bed> with last column containing phase A or B");
		System.out.println("\t[hasHeader]: FALSE if no header present in <in.bed>");
		System.out.println("Arang Rhie, 2015-06-18. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			hasHeader = Boolean.parseBoolean(args[2]);
			new Phase().go(args[0], args[1]);
		} else if (args.length == 2) {
			new Phase().go(args[0], args[1]);
		} else {
			new Phase().printHelp();
		}
	}

}
