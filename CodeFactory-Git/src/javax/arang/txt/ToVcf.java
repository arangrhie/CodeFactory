package javax.arang.txt;

import java.util.Vector;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ToVcf extends Rwrapper {

	int nSNP = 0;
	int nINDEL = 0;
	int nInput = 0;
	
	@Override
	public void hooker(FileReader fr) {
		FileMaker fmSNP = new FileMaker(outFile + "_snp.vcf");
		FileMaker fmINDEL = new FileMaker(outFile + "_indel.vcf");
		
		fmSNP.writeLine("##fileformat=VCFv4.1");
		fmINDEL.writeLine("##fileformat=VCFv4.1");
		
		// parse fr header line
		String line = fr.readLine();
		String[] tokens = line.split("\t");
		
		int posCol = 0;
		int ntChangeCol = 1;
		Vector<Integer> infoCols = new Vector<Integer>();
		Vector<String> infoIDs = new Vector<String>();
		String id;
		
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("Position")) {
				posCol = i;
			} else if (tokens[i].startsWith("Nucleotide Change")) {
				ntChangeCol = i;
			} else {
				infoCols.add(i);
				id = tokens[i].replace(" ", "_").toUpperCase();
				infoIDs.add(id);
				fmSNP.writeLine("##INFO=<ID=" + id + ",Descriptoin=\"" + tokens[i] + "\">");
				fmINDEL.writeLine("##INFO=<ID=" + id + ",Descriptoin=\"" + tokens[i] + "\">");
			}
		}
		fmSNP.writeLine("##source=" + fr.getFileName());
		fmINDEL.writeLine("##source=" + fr.getFileName());
		fmSNP.writeLine("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
		fmINDEL.writeLine("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
		
		String[] bases = new String[2];
		short REF = 0;
		short ALT = 1;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			nInput++;
			tokens = line.split("\t");
			bases = tokens[ntChangeCol].split("-");
			if (bases[ALT].contains("del")) {
				writeVariant(fmINDEL, tokens, infoIDs, infoCols, tokens[posCol],bases[REF] + "\t.");
				nINDEL++;
			} else if (bases[ALT].contains("ins")) {
				writeVariant(fmINDEL, tokens, infoIDs, infoCols, tokens[posCol],bases[REF] + "\t"
						+ bases[ALT].substring(0, bases[ALT].indexOf("ins")));
				nINDEL++;
			} else if (bases[ALT].contains("or")) {
				System.out.println("This line will be split into 2 lines: " + line);
				if (bases[ALT].substring(0,bases[ALT].indexOf("or")).trim().length() == 1) {
					writeVariant(fmSNP, tokens, infoIDs, infoCols, tokens[posCol],bases[REF] + "\t"
							+ bases[ALT].substring(0,bases[ALT].indexOf("or")).trim());
					nSNP++;
				} else {
					writeVariant(fmINDEL, tokens, infoIDs, infoCols, tokens[posCol],bases[REF] + "\t"
							+ bases[ALT].substring(0,bases[ALT].indexOf("or")).trim());	
					nINDEL++;
				}
				if (bases[ALT].substring(bases[ALT].indexOf("or") + 2).trim().length() == 1) {
					writeVariant(fmSNP, tokens, infoIDs, infoCols, tokens[posCol],bases[REF] + "\t"
							+ bases[ALT].substring(bases[ALT].indexOf("or") + 2).trim());
					nSNP++;
				} else {
					writeVariant(fmINDEL, tokens, infoIDs, infoCols, tokens[posCol],bases[REF] + "\t"
							+ bases[ALT].substring(bases[ALT].indexOf("or") + 2).trim());
					nINDEL++;
				}
			} else if (bases[ALT].contains("(")) {
				System.out.println("This line is not included: " + line);
				// do nothing
			} else if (bases[ALT].length()!=1) {
				writeVariant(fmINDEL, tokens, infoIDs, infoCols, tokens[posCol],bases[REF] + "\t" + bases[ALT]);
				nINDEL++;
			} else {
				writeVariant(fmSNP, tokens, infoIDs, infoCols, tokens[posCol], bases[REF] + "\t" + bases[ALT]);
				nSNP++;
			}
				
		}
		
		System.out.println();
		System.out.println("Number of variants in input: " + nInput);
		System.out.println("Number of SNPs: " + nSNP);
		System.out.println("Number of INDELs: " + nINDEL);
	}
	
	private void writeVariant(FileMaker fm, String[] tokens,
			Vector<String> infoIDs, Vector<Integer> infoCols, String pos, String bases) {
		// CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO
		fm.write("chrM\t" + pos + "\t.\t" + bases + "\t.\t.\t");
		for (int i = 0; i < infoCols.size(); i++) {
			fm.write(infoIDs.get(i) + "=" + tokens[infoCols.get(i)] + ";");
		}
		fm.writeLine();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtToVcf.jar <in.txt> <out>");
		System.out.println("This is a test code to convert MitoMap .tab files into VCF format.");
		System.out.println("\t<in.txt>: .tab file downloaded from MitoMap");
		System.out.println("\t<out>: output file prefix. <out_snp.vcf> and <out_indel.vcf> will be generated.");
		System.out.println("Arang Rhie, 2014-09-30. arrhie@gmail.com");
	}

	static String outFile;
	
	public static void main(String[] args) {
		if (args.length < 2) {
			new ToVcf().printHelp();
		} else {
			outFile = args[1];
			new ToVcf().go(args[0]);
		}
	}

}
