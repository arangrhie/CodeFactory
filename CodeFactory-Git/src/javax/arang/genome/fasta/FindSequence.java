package javax.arang.genome.fasta;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class FindSequence extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String contig = "";
		int pos = 0;
		int i;
		String prevLineEnd = "";
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(">")) {
				// header line. new contig.
				contig = line.substring(1);
				if (contig.contains(RegExp.WHITESPACE)) {
					contig = contig.split(RegExp.WHITESPACE)[0];
				}
				pos = 0;
				prevLineEnd = "";
				continue;
			}
			line = prevLineEnd + line.trim().toLowerCase();
			for (i = 0; i < line.length() - sequenceToSearchLen + 1; i++) {
				matchingStrategy(line, i, contig, pos);
				pos++;
			}
			if (line.length() > sequenceToSearchLen) {
				prevLineEnd = line.substring(line.length() - sequenceToSearchLen + 1);	// part that has not been read at the end of line
			}
		}
	}
	
	protected void matchingStrategy(String line, int i, String contig, int pos) {
		if (line.substring(i, i + sequenceToSearchLen).matches(sequenceToSearch)
				|| line.substring(i, i + sequenceToSearchLen).matches(sequenceToSearchRC)) {
			System.out.println(contig + "\t" + pos + "\t" + (pos + sequenceToSearchLen));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaFindSequence.jar <sequence> <in.fasta>");
		System.out.println("Simple exact match finder.");
		System.out.println("\tSearch for <sequence> in <in.fasta> and report positions in bed format.");
		System.out.println("\t<sequence>: Sequence to find. Finds for reverse complement sequence as well.");
		System.out.println("\t\tAccepts wild card: N. ex. GANTC will search for GAATC, GATTC, GAGTC, GACTC, and GATC.");
		System.out.println("\t<in.fasta>: Fasta file to search in.");
		System.out.println("\t<stdout>: BED format of positions containing the <sequence>.");
		System.out.println("Arang Rhie, 2018-03-15. arrhie@gmail.com");
		
	}
	
	/***
	 * Set the sequence to search.
	 * The reverse complement is calculated on the fly.
	 * @param sequence
	 */
	public static void setSequenceToSearch(String sequence) {
		sequenceToSearch = sequence.toLowerCase();
		sequenceToSearchRC = FASTA.getReverseComplement(sequenceToSearch).toString();
		sequenceToSearchLen = sequenceToSearch.length();
		sequenceToSearch = sequenceToSearch.replace("n", "([atgc])?") + "\\S+";
		sequenceToSearchRC = sequenceToSearchRC.replace("n", "([atgc])?") + "\\S+";
		sequenceToSearchFirstBase = sequenceToSearch.charAt(0);
		sequenceToSearchRCFirstBase = sequenceToSearchRC.charAt(0);
		System.err.println("Sequence to find: " + sequenceToSearch);
		System.err.println("Reverse complement: " + sequenceToSearchRC);
		
	}

	protected static String sequenceToSearch;	// sequence to search.
	protected static String sequenceToSearchRC;	// sequence to search, reverse complement.
	protected static int sequenceToSearchLen;
	protected static char sequenceToSearchFirstBase;
	protected static char sequenceToSearchRCFirstBase;
	
	public static void main(String[] args) {
		if (args.length == 2) {
			setSequenceToSearch(args[0]);
			new FindSequence().go(args[1]);
		} else {
			new FindSequence().printHelp();
		}
		
//		System.out.println();
//		System.out.println();
//		
//		String seqToSearch = "GA([ATGC])?TC\\S+";
//		String givenSeq = "GATCAA";
//		System.out.println("givenSeq.matches(seqToSearch) = " + givenSeq.matches(seqToSearch));
	}
	
	

}
