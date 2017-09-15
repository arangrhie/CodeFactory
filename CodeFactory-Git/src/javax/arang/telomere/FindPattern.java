package javax.arang.telomere;

import javax.arang.genome.fasta.FindSequence;

public class FindPattern extends FindSequence {

	private static String telMotiv = "TTAGGG";
	
	public static void main(String[] args) {
		if (args.length == 1) {
			setSequenceToSearch(telMotiv);
			new FindPattern().go(args[0]);
		} else if (args.length == 2) {
			telMotiv = args[1];
			setSequenceToSearch(telMotiv);
			new FindPattern().go(args[0]);
		} else {
			new FindPattern().printHelp();
		}
	}

	@Override
	protected void matchingStrategy(String line, int i, String contig, int pos) {
		if (line.charAt(i) == sequenceToSearchFirstBase && line.substring(i).startsWith(sequenceToSearch)
				|| line.charAt(i) == sequenceToSearchRCFirstBase && line.substring(i).startsWith(sequenceToSearchRC)) {
			System.out.println(contig + "\t" + pos + "\t" + (pos + sequenceToSearchLen));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar telomereFindPattern.jar <in.fasta> [telMotiv=TTAGGG]");
		System.out.println("\tFind positions of telomere sequence motives within a given fasta file.");
		System.out.println("\t<in.fasta>: Any fasta formatted file");
		System.out.println("\t<stdout>: CONTIG\tSTART(0)\tEND(1)\tTelomereID\tEditDist\tStrand");
		System.out.println("\t[telMotive]: In most mammalian genomes, TTAGGG hexamer. Try other motive sequences.");
		System.out.println("Arang Rhie, 2017-08-17. arrhie@gmail.com");
	}

}
