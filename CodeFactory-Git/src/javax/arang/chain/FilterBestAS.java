package javax.arang.chain;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Region;

public class FilterBestAS extends IOwrapper {

	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		String[] tokens;
		
		int start;
		int end;
		
		HashMap<String, ArrayList<Region>> queryToRegions = new HashMap<String, ArrayList<Region>>();
		ArrayList<Region> regions;
		Region region;
		
		String query;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			query = tokens[ChainBed.QUERY];
			start = Integer.parseInt(tokens[ChainBed.QUERY_START]) - 1;	// bed format
			end = Integer.parseInt(tokens[ChainBed.QUERY_END]);
			region = new Region(start, end, query);
			if (!queryToRegions.containsKey(query)) {
				queryToRegions.put(query, new ArrayList<Region>());
			}
			regions = queryToRegions.get(query);
			
			if (!isOverlapping(regions, region)) {
				regions.add(region);
				fm.writeLine(line);
			}
		}
		
	}

	private boolean isOverlapping(ArrayList<Region> regions, Region region) {
		if (regions.size() == 0) {
			return false;
		}
		
		int start = region.getStart() + 1;	// 1-based
		int end = region.getEnd();
		for (Region regionUsed : regions) {
			if (regionUsed.isInRegion(start) || regionUsed.isInRegion(end)
					|| start <= regionUsed.getStart() && regionUsed.getEnd() <= end) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar chainFilterBestMQ.jar <in.sorted.bed> <out.bed>");
		System.out.println("\tGet non-overlapping best AS chains from <in.sorted.bed>");
		System.out.println("\t<in.sorted.bed>: Sorted by AS");
		System.out.println("\t\tTARGET	TARGET_START	TARGET_END	TARGET_STRAND	TARGET_SIZE	QUERY	QUERY_START	QUERY_END	QUERY_STRAND	QUERY_SIZE	ALIGNMENT_BLOCK	AS");
		System.out.println("\t<out.bed>: non-overlapping best chain bed");
		System.out.println("Arang Rhie, 2016-01-27. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new FilterBestAS().go(args[0], args[1]);
		} else {
			new FilterBestAS().printHelp();
		}
	}

}
