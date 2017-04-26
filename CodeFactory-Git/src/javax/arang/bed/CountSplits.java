package javax.arang.bed;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;

public class CountSplits extends IOwrapper {

	private static int mqFilter = 5;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		String prevReadId = "";
		String readid;
		int occurrence = 1;
		HashMap<Integer, Integer> occurrenceToReadCount = new HashMap<Integer, Integer>();
		int maxOccurrence = 1;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			if (Integer.parseInt(tokens[Bed.MQ]) < mqFilter) {
				continue;
			}
			
			readid = tokens[Bed.NOTE];
			if (readid.equals(prevReadId)) {
				occurrence++;
			} else {
				if (!prevReadId.equals("")) {
					// Store max occurrence for later use
					if (maxOccurrence < occurrence) {
						maxOccurrence = occurrence;
					}
					
					// Add to table
					if (occurrenceToReadCount.containsKey(occurrence)) {
						occurrenceToReadCount.put(occurrence, occurrenceToReadCount.get(occurrence) + 1);
					} else {
						occurrenceToReadCount.put(occurrence, 1);
					}
					occurrence = 1;
				}
			}
			prevReadId = readid;
		}	// End of reading bed file
		
		System.out.println("Start writing outputs: " + occurrenceToReadCount.size() + " lines to be expected");
		System.out.println("Max occurrence : " + maxOccurrence);
		
		// Start writing the output counts
		for (int i = 1; i <= maxOccurrence; i++) {
			if (occurrenceToReadCount.containsKey(i)) {
				fm.writeLine(i + "\t" + occurrenceToReadCount.get(i));
			} else {
				fm.writeLine(i + "\t0");
			}
		}
	}
	

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedCountSplits.jar <in.r1.bed> <out.hist> [MQfilter=5]");
		System.out.println("Count the number of split alignments over [MQfilter]");
		System.out.println("\t<in.bed>: Generated with bedtools coverage -split option");
		System.out.println("\t\tContig\tStart\tEnd\tReadID\tMQ\tStrand");
		System.out.println("\t<out.hist>: Num.Mappings (Split alignments)\tCount");
		System.out.println("\t\tNum.Mappings: 1 = One alignment, 1 > : more than 1 alignmentss");
		System.out.println("\t\tCount: Num. of unique read-ids with <Num.Mappings>");
		System.out.println("\t[MQfilter]: MQ filter. Reads >= [MQfilter] are counted. DEFAULT=5");
		System.out.println("Arang Rhie, 2017-04-20. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new CountSplits().go(args[0], args[1]);
		} else if (args.length == 3) {
			mqFilter = Integer.parseInt(args[2]);
			new CountSplits().go(args[0], args[1]);
		} else {
			new CountSplits().printHelp();
		}
	}

}
