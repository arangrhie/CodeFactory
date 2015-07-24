package javax.arang.genome.snp;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CutPart extends IOwrapper {

	static int from = 0;
	static int to = 1;
	static int POS = 1;
	
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			int pos = 0; 
			try {
				if (tokens[POS].contains(",")) {
					tokens[POS] = tokens[POS].substring(0, tokens[POS].indexOf(","));
				}
				pos = Integer.parseInt(tokens[POS]);
			} catch (NumberFormatException e) {
				System.out.println("Assuming " + (POS + 1) + " is not the position col. shifting +1");
				POS++;
			}
			if ( from <= pos && pos <= to ) {
				fm.writeLine(line);
			}
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpCutPart.jar <in.snp> <outfile.snp> <from> <to>");
		System.out.println("\t<in.snp>: chr\tpos\tA\tC\tG\tT\tsnp_count\tother_count\tsnp_%\thom/het");
		System.out.println("\t<outfile.snp>: snp list ranging in [<from> ~ <to>]");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 4) {
			from = Integer.parseInt(args[2]);
			to = Integer.parseInt(args[3]);
			new CutPart().go(args[0], args[1]);
		} else {
			new CutPart().printHelp();
		}

	}

}
