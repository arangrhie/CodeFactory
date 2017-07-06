package javax.arang.bed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class LinksToHeatmap extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String prevLinkId = "";
		HashMap<String, Integer> linkToCount = new HashMap<String, Integer>();
		ArrayList<String> contigs = new ArrayList<String>();
		String link;
		ArrayList<String> allContigs = new ArrayList<String>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			if (prevLinkId.equals(tokens[Bed.NOTE])) {
				// Collect contigs
				if (!contigs.contains(tokens[Bed.CHROM])) {
					contigs.add(tokens[Bed.CHROM]);
				}
			} else {
				// Export previous links
				if (!prevLinkId.equals("") && contigs.size() > 1) {
					Collections.sort(contigs);
					for (int i = 0; i < contigs.size(); i++) {
						for (int j = i + 1; j < contigs.size(); j++) {
							link = contigs.get(i) + ":" + contigs.get(j);
							if (!linkToCount.containsKey(link)) {
								linkToCount.put(link, 1);
							} else {
								linkToCount.put(link, linkToCount.get(link) + 1);
							}
						}
					}
				}
				
				// Initialize variables
				contigs.clear();
				contigs.add(tokens[Bed.CHROM]);
			}
			if (!allContigs.contains(tokens[Bed.CHROM])) {
				allContigs.add(tokens[Bed.CHROM]);
			}
			prevLinkId = tokens[Bed.NOTE];
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedLinksToHeatmap.jar <in.bed> [linkIdColIdx=4]");
		System.out.println("Generate a heatmap for links linking contigs");
		System.out.println("\t<in.bed>: bed format file generated with bedtools bamtobed");
		System.out.println("\t[linkIdColIdx]: Column index containing the link id (same links have the same id). 1-based.");
		System.out.println("\t\tCoult be readids when <in.bed> was generated with bedtools bamtobed -split");
		System.out.println("\t<stdout>: triangle-like heatmap. Copy-paste on excel to draw a heatmap");
	}

	private static int linkIdColIdx = 3;
	public static void main(String[] args) {
		if (args.length == 1) {
			new LinksToHeatmap().go(args[0]);
		} else if (args.length == 2) {
			linkIdColIdx = Integer.parseInt(args[1]) - 1;
			new LinksToHeatmap().go(args[0]);
		} else {
			new LinksToHeatmap().printHelp();
		}
	}

}
