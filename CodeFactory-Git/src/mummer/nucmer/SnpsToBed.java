package mummer.nucmer;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class SnpsToBed extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String contig = "";
		double start = -1;
		double end = -1;
		int len = 0;
		int buff = 0;
		String type = "-";
		boolean isIndel = false;
		
		String prevRpos = "";
		String prevQpos = "";
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			buff = Integer.parseInt(tokens[SNPS.R_BUFF]);
			
			if (buff == 1) {
				if (isIndel && !prevQpos.equals(tokens[SNPS.Q_POS])) {
					printOut(contig, start, end, type, len);
					isIndel = false;
				}
				type = "DEL";
				end = Double.parseDouble(tokens[SNPS.R_POS]);
				if (!isIndel) {
					start = end - 1;
					len = 0;
				}
				isIndel = true;
				len++;
			} else if (buff == 0) {
				if (isIndel && !prevRpos.equals(tokens[SNPS.R_POS])) {
					printOut(contig, start, end, type, len);
					isIndel = false;
				}
				type = "INS";
				if (!isIndel) {
					end = Double.parseDouble(tokens[SNPS.R_POS]);
					start = end - 1;
					len = 0;
				}
				isIndel = true;
				len++;
			} else {
				if (!contig.equals("") && isIndel) {
					printOut(contig, start, end, type, len);
					isIndel = false;
				}
				contig = tokens[SNPS.R_CONTIG];
				end = Double.parseDouble(tokens[SNPS.R_POS]);
				start = end - 1;
				len = 1;
				if (tokens[SNPS.R_BASE].equals(".")) {
					type = "INS";
				} else if (tokens[SNPS.Q_BASE].equals(".")) {
					type = "DEL";
				} else {
					type = "SNP";
				}
				printOut(contig, start, end, type, len);
			}
			
			prevRpos = tokens[SNPS.R_POS];
			prevQpos = tokens[SNPS.Q_POS];
			contig = tokens[SNPS.R_CONTIG];
		}
		
		if (!contig.equals("") && isIndel) {
			printOut(contig, start, end, type, len);
		}
	}
	
	private void printOut(String contig, double start, double end, String type, int len) {
		System.out.println(contig + "\t" + String.format("%.0f", start) + "\t" + String.format("%.0f", end) +
				"\t" + type + "\t" + len);
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar nucmerSnpsToBed.jar <in.snps>");
		System.out.println("\t<in.snps>: .snps file generated with dnadiff");
		System.out.println("\t<sysout>: bed formated file.");
		System.out.println("\t\tR_CONTIG\tSTART(0-base)\tEND(1-base)\tTYPE(INS/DEL/SNP)\tLEN");
		System.out.println("\t\tInsertions and SNPs are written as (pos-1) (pos).");
		System.out.println("Arang Rhie, 2018-01-06. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new SnpsToBed().go(args[0]);
		} else {
			new SnpsToBed().printHelp();
		}
	}

}
