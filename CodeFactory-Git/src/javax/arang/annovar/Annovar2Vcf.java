/**
 * 
 */
package javax.arang.annovar;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.util.ANNOVAR;

/**
 * @author Arang Rhie
 *
 */
public class Annovar2Vcf extends IOwrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		if (hasPhenotype) {
			System.out.println("Line containing Phenotype detected. This line is discarded.");
			fr.readLine();	
		}
		
		int sampleStart = ANNOVAR.NOTE + 1;
		line = fr.readLine();
		tokens = line.split("\t");
		fm.write("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT");
		for (int i = sampleStart; i < tokens.length; i++) {
			fm.write("\t" + tokens[i]);
		}
		fm.writeLine("");
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			fm.write(tokens[ANNOVAR.CHR] + "\t" + tokens[ANNOVAR.POS_FROM] + "\t" + tokens[ANNOVAR.NOTE] + "\t"
					+ tokens[ANNOVAR.REF] + "\t" + tokens[ANNOVAR.ALT] + "\t.\tPASS\t.\tGT:NN");
			for (int i = sampleStart; i < tokens.length; i++) {
				fm.write("\t" + getGT(tokens[i]) + ":");
			}
			fm.writeLine("");
		}
	}
	
	private String getGT(String gt) {
		if (gt.equals("0")) {
			return 0 + "/" + 0;
		} else if (gt.equals("1")) {
			return 0 + "/" + 1;
		} else if (gt.equals("2")) {
			return 1 + "/" + 1;
		} else {
			return "./.";
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.IOwrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpAnnovar2Vcf.jar <in.snp> [has_phenotype=TRUE]");
		System.out.println("Make a vary simple VCF file.");
		System.out.println("\t<in.snp>: ANNOVAR formatted file");
		System.out.println("\t<has_phenotype=TRUE>: 1st line containing phenotypes instead of CHR\tSTART\tSTOP...?");
	}

	static boolean hasPhenotype = true;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new Annovar2Vcf().go(args[0], args[0].replace(".snp", ".vcf"));
		} else if (args.length == 2) {
			hasPhenotype = Boolean.parseBoolean(args[1]);
			new Annovar2Vcf().go(args[0], args[0].replace(".snp", ".vcf"));
		}
	}

}
