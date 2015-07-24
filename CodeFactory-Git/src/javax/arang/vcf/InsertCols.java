/**
 * 
 */
package javax.arang.vcf;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.genome.util.Util;


/**
 * @author Arang Rhie
 *
 */
public class InsertCols extends I2Owrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		String line1;
		String[] tokens1 = null;
		String line2;
		String[] tokens2 = null;
		String IDs = "";
		String annotations = "";
		line1 = fr1.readLine();
		line2 = fr2.readLine();
		while (fr1.hasMoreLines() && fr2.hasMoreLines()) {
			while (line1.startsWith("##")) {
				// read fr1
				line1 = fr1.readLine();
				continue;
			}
			if (line1.startsWith("#CHR")) {
				tokens1 = line1.split("\t");
				IDs = getIDs(tokens1);
				line1 = fr1.readLine();
				tokens1 = line1.split("\t");
			}
			
			while (line2.startsWith("##")) {
				line2 = fr2.readLine();
				continue;
			}
			if (line2.startsWith("#CHR")) {
				tokens2 = line2.split("\t");
				annotations = getANNOs(tokens2);
				fm.writeLine(annotations + "\t" + IDs);
				line2 = fr2.readLine();
				tokens2 = line2.split("\t");
			}
			
			// read fr1
			if (Util.getChromIntVal(tokens1[VCF.CHROM]) < Util.getChromIntVal(tokens2[VCF.CHROM])) {
				line1 = fr1.readLine();
				tokens1 = line1.split("\t");
			}
			
			// read fr2
			else if (Util.getChromIntVal(tokens1[VCF.CHROM]) > Util.getChromIntVal(tokens2[VCF.CHROM])) {
				line2 = fr2.readLine();
				tokens2 = line2.split("\t");
			} else {

				// compare line1 and line2
				compare : while (Util.getChromIntVal(tokens1[VCF.CHROM]) == Util.getChromIntVal(tokens2[VCF.CHROM])) {
					if (Long.parseLong(tokens1[VCF.POS]) < Long.parseLong(tokens2[VCF.POS])) {
						System.out.println("Not in " + fr2.getFileName() + ":\t" + line1);

						// read fr1
						if (fr1.hasMoreLines()) {
							line1 = fr1.readLine();
							tokens1 = line1.split("\t");
						} else break compare;
						continue compare;
					} else if (Long.parseLong(tokens1[VCF.POS]) == Long.parseLong(tokens2[VCF.POS])) {
						// insert into fm
						IDs = getIDs(tokens1);
						annotations = getANNOs(tokens2);
						fm.writeLine(annotations + "\t" + IDs);

						// read fr1
						if (fr1.hasMoreLines()) {
							line1 = fr1.readLine();
							tokens1 = line1.split("\t");
						} else break compare;

						// read fr2
						if (fr2.hasMoreLines()) {
							line2 = fr2.readLine();
							tokens2 = line2.split("\t");
						} else break compare;
					} else if (Long.parseLong(tokens1[VCF.POS]) > Long.parseLong(tokens2[VCF.POS])) {
						// read fr2
						if (fr2.hasMoreLines()) {
							line2 = fr2.readLine();
							tokens2 = line2.split("\t");
						} else break compare;
						continue compare;
					}
				}
			}
		}
		
		while (fr1.hasMoreLines()) {
			System.out.println("Not in " + fr2.getFileName() + ":\t" + line1);
			line1 = fr1.readLine();
		}
	}
	
	private String getIDs(String[] tokens) {
		StringBuffer newHeader = new StringBuffer(tokens[VCF.SAMPLE]);
		for (int i = VCF.SAMPLE + 1; i < tokens.length; i++) {
			newHeader.append("\t" + tokens[i]);
		}
		return newHeader.toString();
	}

	private String getANNOs(String[] tokens) {
		StringBuffer newHeader = new StringBuffer(tokens[VCF.CHROM]);
		for (int i = VCF.POS; i <= colNum; i++) {
			newHeader.append("\t" + tokens[i]);
		}
		return newHeader.toString();
	}
	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar vcfInsertCols.jar <in1.vcf> <in2.vcf> <out.vcf> <col_num>");
		System.out.println("Insert columns until <col_num> annotations from <in2.vcf> into GATK formatted <in1.vcf>.");
		System.out.println("QUAL, FILTER, INFO field will be replaced.");
		System.out.println("\t<col_num> starts with 0.");
		System.out.println("\t2013-07-03");
	}

	static int colNum = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 4) {
			colNum = Integer.parseInt(args[3]);
			new InsertCols().go(args[0], args[1], args[2]);
		} else {
			new InsertCols().printHelp();
		}
	}

}
