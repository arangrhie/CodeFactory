package javax.arang.TenXG;

import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class MergeRawLinks extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		HashMap<String, Integer> linkToReadCounts = new HashMap<String, Integer>();
		HashMap<String, Integer> linkToBarcodeCounts = new HashMap<String, Integer>();
		String link;
		int readCount;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			link = tokens[Link.COL_C1_BE] + "\t" + tokens[Link.COL_C2_BE];
			readCount = Integer.parseInt(tokens[Link.COL_C1_READS]) + Integer.parseInt(tokens[Link.COL_C2_READS]);
			if (!linkToReadCounts.containsKey(link)) {
				// new link
				linkToReadCounts.put(link, readCount);
				linkToBarcodeCounts.put(link, 1);
			} else {
				// known link
				linkToReadCounts.put(link, linkToReadCounts.get(link) + readCount);
				linkToBarcodeCounts.put(link, linkToBarcodeCounts.get(link) + 1);
			}
		}
		
		for (String linkKey : linkToReadCounts.keySet()) {
			System.out.println(linkKey + "\t" + linkToBarcodeCounts.get(linkKey) + "\t" + String.format("%.2f", ((float) linkToReadCounts.get(linkKey)) / linkToBarcodeCounts.get(linkKey)));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar tenXGmergeRawLinks.jar <bc.BE.mrg.raw>");
		System.out.println("\t<bc.BE.mrg.raw>: Generated with tenXGMergedToScaffoldingLinks.jar");
		System.out.println("\t<stdout>: C1:BE\tC2:BE\tNo.Barcodes\tNo.Reads(Normalized by no. barcodes)");
		System.out.println("Arang Rhie, 2017-04-11. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new MergeRawLinks().go(args[0]);
		} else {
			new MergeRawLinks().printHelp();
		}
		
	}

}
