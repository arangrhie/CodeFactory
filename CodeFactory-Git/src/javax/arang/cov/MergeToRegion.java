package javax.arang.cov;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class MergeToRegion extends Rwrapper {

	private static String chrom;
	
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String faEntry;
		double pos;
		double val;
		double posStart = 0;
		double posEnd = 0;
		double valStart = 0;
		double valEnd = 0;
		String strand = "";
		String prevStrand = "";
		
		String prevFaEntry = "";
		double prevPos = 0;
		double prevVal = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			faEntry = tokens[Cov.FA_ENTRY];
			pos = Double.parseDouble(tokens[Cov.POS]);
			val = Double.parseDouble(tokens[Cov.VALUE]);
			
			if (!prevFaEntry.equals(faEntry)) {
				if (!prevFaEntry.equals("")) {
					// Print previous values
					print(prevFaEntry, posStart, posEnd, valStart, valEnd, prevStrand);
				}
				posStart = pos;
				posEnd = pos;
				valStart = val;
				valEnd = val;
				strand = ".";
			} else {
				if (prevPos + 1 == pos) {
					posEnd = pos;
					if (prevVal + 1 == val) {
						strand = "+";
						valEnd = val;
					} else if (prevVal - 1 == val) {
						strand = "-";
						valStart = val;
					} else {
						// Print previous values
						print(prevFaEntry, posStart, posEnd, valStart, valEnd, prevStrand);
						
						// initialize
						posStart = pos;
						posEnd = pos;
						valStart = val;
						valEnd = val;
						strand = ".";
					}
				} else {
					// Print previous values
					print(prevFaEntry, posStart, posEnd, valStart, valEnd, prevStrand);
					
					// initialize
					posStart = pos;
					posEnd = pos;
					valStart = val;
					valEnd = val;
					strand = ".";
				}
			}
			prevFaEntry = faEntry;
			prevPos = pos;
			prevVal = val;
			prevStrand = strand;
		}
		
		// Don't forget the last line
		print(prevFaEntry, posStart, posEnd, valStart, valEnd, prevStrand);
	}
	
	private void print(String faEntry, double posStart, double posEnd, double valStart, double valEnd, String strand) {
		double overlap = posEnd - posStart + 1;
		System.out.println(faEntry + "\t" +
				String.format("%.0f", posStart-1)  + "\t" + String.format("%.0f", posEnd) + "\t" +
				chrom + "\t" + String.format("%.0f", valStart-1) + "\t" + String.format("%.0f", valEnd)  + "\t" +
				faEntry + "\t" + String.format("%.0f", overlap) + "\t" + strand);
	}


	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar covMergeToRegion.jar <chrom> <in.pos>");
		System.out.println();
		System.out.println("Merge positions");
		System.out.println("\t<chrom> : chrom name for value.");
		System.out.println("\t<in.pos>: assumes the value is a position somewhere else.");
		System.out.println("\t\tFormat: faEntry\tpos[1-based]\tvalue[1-based]");
		System.out.println("\t<sysout>: paired end bed format.");
		System.out.println("\t\tFormat: faEntry\tposStart\tposEnd\tchrom\tvalStart\tvalEnd\tfaEntry(name)\tscore(num.overlaps)\tstrand");
		System.out.println("Arang Rhie, 2019-02-16. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			chrom = args[0];
			new MergeToRegion().go(args[1]);
		} else {
			new MergeToRegion().printHelp();
		}
	}

}
