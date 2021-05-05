package javax.arang.kmer.meryl;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class DumpToWig extends Rwrapper {

	static public final short SEQ_NAME = 0;
	static public final short SEQ_ID = 1;  
	static public final short SEQ_POS = 2;
	static public final short EXISTS = 3;
	static public final short FWD_MER = 4;
	static public final short FWD_VAL = 5;
	static public final short REV_MER = 6;
	static public final short REV_VAL = 7;
	
	@Override
	public void hooker(FileReader fr) {
		
		String line;
		String[] tokens;
		
		String seqName = "";
		String prevName = "";
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			
			seqName = tokens[SEQ_NAME];
			if (!seqName.equals(prevName)) {
				System.out.println("variableStep chrom=" + seqName);
			}
			System.out.println((Integer.parseInt(tokens[SEQ_POS])+1) + "\t" + (Integer.parseInt(tokens[FWD_VAL]) + Integer.parseInt(tokens[REV_VAL])));
			prevName = seqName;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx1g merylDumpToWig.jar <dump>");
		System.out.println("\t<dump>: meryl-lookup -dump, use - for piping in");
		System.out.println("\t<stdout>: .wig formatted coverage. Use wigToBigWig to make a binary version.");
		System.out.println("Arang Rhie, 2020-06-08. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new DumpToWig().go(args[0]);
		} else {
			new DumpToWig().printHelp();
		}
	}

}
