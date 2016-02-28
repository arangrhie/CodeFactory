package javax.arang.scaffold.link;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Merge extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		String prevContig1 = "";
		String prevContig2 = "";
		String contig1;
		String contig2;
		int weight = 0;
		int weightSum = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#"))	continue;
			tokens = line.split(RegExp.TAB);
			contig1 = tokens[Link.NODE_1];
			contig2 = tokens[Link.NODE_2];
			weight = Integer.parseInt(tokens[Link.WEIGHT]);
			
			if (prevContig1.equals("")) {
				weightSum = weight;
				// do nothing
			} else {
				if (prevContig1.equals(contig1) && prevContig2.equals(contig2)) {
					weightSum += weight;
				} else {
					fm.writeLine(prevContig1 + "\t" + prevContig2 + "\t\t" + weightSum);
					weightSum = weight;
				}
			}
			
			prevContig1 = contig1;
			prevContig2 = contig2;
		}
		
		fm.writeLine(prevContig1 + "\t" + prevContig2 + "\t\t" + weightSum);
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar linkMerge.jar <in.sort.link> <out.link>");
		System.out.println("\t<in.sort.link>: generated with bedToEdge.jar, then sort -k1,1 (sort by contig1 name)");
		System.out.println("\t<out.link>: merged by identical links: weight will be summed, when >=2 BACs exists.");
		System.out.println("Arang Rhie, 2015-10-05. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new Merge().go(args[0], args[1]);
		} else {
			new Merge().printHelp();
		}
	}

}
