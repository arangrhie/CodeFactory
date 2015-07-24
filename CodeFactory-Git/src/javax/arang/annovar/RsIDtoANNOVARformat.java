/**
 * 
 */
package javax.arang.annovar;

import java.util.PriorityQueue;
import java.util.Queue;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class RsIDtoANNOVARformat extends I2Owrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader rsIDfr, FileReader dbSNPfr, FileMaker fm) {
		String line;
		String[] tokens;
		Queue<String> rsIDs = new PriorityQueue<String>();
		while (rsIDfr.hasMoreLines()) {
			line = rsIDfr.readLine();
			line = line.trim();
			if (rsIDs.contains(line)) {
				System.out.println(line + " already exists.");
			} else {
				rsIDs.add(line);
			}
		}
		
		final int CHR = 1;
		final int START = 2;
		final int END = 3;
		final int RS_ID = 4;
		final int ALLELES = 9;	// ref/alt
		
		String[] alleles;
		String ref;
		String alt;
	
		while (dbSNPfr.hasMoreLines()) {
			line = dbSNPfr.readLine();
			line = line.trim();
			tokens = line.split("\t");
			if (rsIDs.contains(tokens[RS_ID])) {
				alleles = tokens[ALLELES].split("/");
				ref = alleles[0];
				alt = alleles[1];
				fm.writeLine(tokens[CHR] + "\t" + (Long.parseLong(tokens[START])+1) + "\t" + tokens[END]
						+ "\t" + ref + "\t" + alt + "\t" + tokens[RS_ID]);
				if (alleles.length > 2) {
					for (int i = 2; i < alleles.length; i++) {
						fm.writeLine(tokens[CHR] + "\t" + (Long.parseLong(tokens[START])+1) + "\t" + tokens[END]
							+ "\t" + ref + "\t" + alleles[i] + "\t" + tokens[RS_ID]);
					}
				}
				rsIDs.poll();
			}
			if (rsIDs.size() == 0) {
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		
		System.out.println("Usage: java -jar annovarRsIDtoANNOVARformat.jar <rsID_list> <ANNOVAR_dbSNP_rsIDsorted.txt> <out>");
		System.out.println("\tConverts rsID list to ANNOVAR input format.");
		System.out.println("\t*NOTE* Multialleles will be present in 2 lines");
		System.out.println("\tdbSNP.txt: 585\tchr1\t10247\t10248\trs148908337\t0\t+\tA\tA\tA/T\tgenomic single\t...");
		System.out.println("Arang Rhie, 2013-11-08. arrhie@gmail.com");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new RsIDtoANNOVARformat().go(args[0], args[1], args[2]);
		} else {
			new RsIDtoANNOVARformat().printHelp();
		}
	}

}
