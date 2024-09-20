package javax.arang.gff;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class Summarize extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		int geneCnt = 0;
		int geneCntPrtn = 0;
		
		int transcriptCnt = 0;
		int transcriptCntPrtn = 0;
		
		String attributes;
		String[] tags;
		
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			
			// Ignore comments
			if (line.startsWith("#")) continue;
			
			tokens = line.split(RegExp.TAB);
			
			
			if (DB_TYPE == REFSEQ) {
				if (!line.startsWith("NC"))	continue;  // Ignore non-primary chromosomes
				if (line.startsWith("NC_012920.1")) continue;	// Ignore MT
				
				attributes = tokens[GFF.NOTE];
				
				if (attributes.startsWith("ID=gene-")) {
					geneCnt++;
					tags = attributes.split(RegExp.SEMICOLON);
					
					for (int i = 0; i < tags.length; i++) {
						if (tags[i].startsWith("gene_biotype") && tags[i].split("=")[1].equalsIgnoreCase("protein_coding")) {
							geneCntPrtn++;
							break;
						}
					}
					
				}
				else if (attributes.startsWith("ID=rna-")) {
					transcriptCnt++;
					if (tokens[GFF.TYPE].equalsIgnoreCase("mRNA")) {
						transcriptCntPrtn++;
					}
				}
			} else if (DB_TYPE == LIFTOFF) {

				// new gene
				if (tokens[GFF.TYPE].equalsIgnoreCase("gene")) {
					geneCnt++;

					attributes = tokens[GFF.NOTE];
					tags = attributes.split(RegExp.SEMICOLON);

					// new gene is protein coding
					for (int i = 0; i < tags.length; i++) {
						if (tags[i].startsWith("gene_biotype") && tags[i].split("=")[1].equalsIgnoreCase("protein_coding")) {
							geneCntPrtn++;
							break;
						}
					}
				}

				if (tokens[GFF.TYPE].equalsIgnoreCase("transcript")) {
					transcriptCnt++;

					attributes = tokens[GFF.NOTE];
					tags = attributes.split(RegExp.SEMICOLON);

					for (int i = 0; i < tags.length; i++) {
						if (tags[i].startsWith("gbkey") && tags[i].split("=")[1].equalsIgnoreCase("mRNA")) {
							transcriptCntPrtn++;
							break;
						}
					}
				}
			} // DB_TYPE == LIFTOFF
		} // end of while
		
		System.out.println("Total num. genes: " + geneCnt);
		System.out.println("  protein coding: " + geneCntPrtn);
		System.out.println();
		System.out.println("Total num. transcripts: " + transcriptCnt);
		System.out.println("        protein coding: " + transcriptCntPrtn);
		
	}

	@Override
	public void printHelp() {
		System.err.println("Usage: java -jar gff3Summarize.jar in.gff3 [DB]");
		System.err.println("Summarize annotations by gene / transcript, protein-coding");
		System.err.println("  in.gff3    input annotation gff3");
		System.err.println("  DB         db is from RefSeq or Liftoff. DEFAULT=RefSeq");
		System.err.println("2023-07-06");
	}
	
	
	private static final short REFSEQ  = 0;
	private static final short LIFTOFF = 1;

	private static short DB_TYPE = REFSEQ;
	
	public static void main(String[] args) {
		String dbType = "RefSeq"; 
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("Liftoff")) {
				DB_TYPE = LIFTOFF;
				dbType = "Liftoff";
			}
		} 
		if (args.length == 1 || args.length == 2) {
			System.err.println();
			System.err.println("in.gff: " + args[0]);
			System.err.println("DB    : " + dbType);
			System.err.println();
			new Summarize().go(args[0]);
		} else {
			new Summarize().printHelp();
		}
	}

}
