package javax.arang.annovar;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ToAdegenet extends I2Owrapper {

	@Override
	public void hooker(FileReader frAnn, FileReader frPopId, FileMaker fm) {
		// Write header
		fm.writeLine(printHeader());
		
		StringBuffer posBuffer = new StringBuffer();
		StringBuffer alleleBuffer = new StringBuffer();
		StringBuffer chrBuffer = new StringBuffer();
		HashMap<String, StringBuffer> genotypeBuffers = new HashMap<String, StringBuffer>();
		HashMap<String, String> samplePopMap = new HashMap<String, String>();
		
		String[] tokens;
		while (frPopId.hasMoreLines()) {
			tokens = frPopId.readLine().split("\t");
			if (samplePopMap.containsKey(tokens[1])) {
				samplePopMap.put(tokens[1], samplePopMap.get(tokens[1]) + " " + tokens[0]);
			} else {
				samplePopMap.put(tokens[1], tokens[0]);
			}
		}
		
		System.out.println("Total number of populations: " + samplePopMap.size());
		
		// Start reading ANNOVAR formatted file
		tokens = frAnn.readLine().split("\t");
		
		// sample ids
		Vector<String> sampleIds = new Vector<String>();
		for (int i = ANNOVAR.NOTE + 1; i < tokens.length; i++) {
			sampleIds.add(tokens[i]);
			genotypeBuffers.put(tokens[i], new StringBuffer());
		}
		System.out.println("Total number of individuals: " + sampleIds.size());
		
		// collect contents
		while (frAnn.hasMoreLines()) {
			tokens = frAnn.readLine().split("\t");
			chrBuffer.append(" " + tokens[ANNOVAR.CHR]);
			posBuffer.append(" " + tokens[ANNOVAR.POS_FROM]);
			alleleBuffer.append(" " + tokens[ANNOVAR.REF] + "/" + tokens[ANNOVAR.ALT]);
			for (int i = ANNOVAR.NOTE + 1; i < tokens.length; i++) {
				if (tokens[i].equals("NA"))	tokens[i] = "-";
				genotypeBuffers.get(sampleIds.get(i - ANNOVAR.NOTE - 1)).append(tokens[i]);
			}
		}
		
		// >> position
		fm.writeLine(">> position");
		fm.writeLine(posBuffer.toString().trim());
		posBuffer = new StringBuffer();
				
		// >> allele
		fm.writeLine(">> allele");
		fm.writeLine(alleleBuffer.toString().trim());
		alleleBuffer = new StringBuffer();
		
		// >> ploidy
		fm.writeLine(">> ploidy");
		fm.writeLine("2");
		
		// >> chromosome
		fm.writeLine(">> chromosome");
		fm.writeLine(chrBuffer.toString().trim());
		chrBuffer = new StringBuffer();
		
		// >> population
		StringBuffer popBuffer = new StringBuffer();
		fm.writeLine(">> population");
		for (String pop : samplePopMap.keySet()) {
			tokens = samplePopMap.get(pop).split(" ");
			for (int i = 0; i < tokens.length; i++) {
				popBuffer.append(pop + " ");
			}
			System.out.println(pop + " " + tokens.length);
		}
		fm.writeLine(popBuffer.toString().trim());
		
		// > sample ID and genotype
		for (String pop : samplePopMap.keySet()) {
			tokens = samplePopMap.get(pop).split(" ");
			for (int i = 0; i < tokens.length; i++) {
				fm.writeLine("> " + tokens[i]);
				fm.writeLine(genotypeBuffers.get(tokens[i]).toString().trim());
			}
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarToAdegenet.jar <in.annovar> <pop_id.txt> <out.adg>");
		System.out.println("\tConverts ANNOVAR formatted file into adegenet input file for PCA analysis.");
		System.out.println("\t<in.annovar>: CHR\tSTART\tEND\tREF\tSNP\tID\tSAMPLE_1\t...\tSAMPLE_N");
		System.out.println("\t<pop_id.txt>: <sample_id> <populatoin>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new ToAdegenet().go(args[0], args[1], args[2]);
		} else {
			new ToAdegenet().printHelp();
		}

	}
	
	private String printHeader() {
		return ">>>> begin comments - do not remove this line <<<<\n" +
				"Here is a description of the format.\n" +
				"Any information is stored on two lines, the first for the type of information, the second for the content.\n" +
				"\n\n" +
				"=== Lines starting with \">>\" ===\n" +
				"They store generic information about the loci or the individuals.\n" +
				"They are all optional.\n" +
				"Character strings following the \">>\" can match:\n" +
				"- \"position\": the following line contains integers giving the position of the SNPs on the sequence\n" +
				"- \"allele\": the following line contains a vector of two alleles separated by \"/\"\n" +
				"- \"population\": population, or more generally a grouping factor for the individuals\n" +
				"- \"ploidy\": the ploidy of each individual given as an integer; alternatively, one single integer if\n" +
				"all individuals have the same ploidy\n" +
				"- \"chromosome\": the chromosome where the SNP are located\n" +
				"Elements are separated by a space, and their length must match exactly the number of loci\n" +
				"(position, allele, chromosome) or individuals (population, ploidy). Therefore, no space is allowed\n" +
				"for the names of these items (especially chromosomes or populations).\n" +
				"\n\n" +
				"=== Lines starting with \">\" ===\n" +
				"They store individual genotypes.\n" +
				"The character string following the sign \">\" is the label of the individual. Spaces before and after are ignored.\n" +
				"The following line contains integers without separators, each representing the number of copies of the second allele.\n" +
				"Missing values are to be coded by a single non-integer character (by default, \"-\").\n" +
				"For instance, in the following toy dataset:\n" +
				"- foo (\"1020\") is \"at\" at position 1, \"gg\" at position 8, \"cc\" at position 11, and \"tt\" at position 43.\n" +
				"- bar (\"0012\") is \"aa\", \"gg\", \"ac\" and \"aa\"\n" +
				"- toto (\"10-0\") is \"at\", \"gg\", not typed (NA) and \"tt\"\n" +
				"\n\n" +
				">>>> end comments - do not remove this line <<<<\n\n";
				
	}

}
