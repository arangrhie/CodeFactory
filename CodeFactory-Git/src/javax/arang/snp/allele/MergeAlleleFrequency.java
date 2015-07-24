/**
 * 
 */
package javax.arang.snp.allele;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class MergeAlleleFrequency extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Useage: java -jar mergeAlleleFreq.jar <sample1>.afq <sample2>.afq ... <sampleN>.afq");
		System.out.println("\t<output>: N_allele_freq.mfq");
		System.out.println("\t\tchr\tpos\tpos\tref\tsnp\tsample1\tsample2\t...\tsampleN\t#WW\t#RW\t#RR\tW\tR\tWW(%)\tRW(%)\tRR(%)");
	}

	int GENOTYPE = 6;
	
	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		fm.write("Chr\tStart\tStop\tRef\tSnp");
		for (int i = 0; i < frs.size(); i++) {
			fm.write("\t" + frs.get(i).getFileName().replace(".afq", ""));
		}
		fm.writeLine("\t#WW\t#RW\t#RR\tW\tR\tWW(%)\tRW(%)\tRR(%)");
		
		String line;
		String[] tokens;
		while (frs.get(0).hasMoreLines()) {
			line = frs.get(0).readLine();
			tokens = line.split("\t");
			for (int i = 0; i < 5; i++) {
				fm.write(tokens[i] + "\t");
			}
			
			int num0 = 0;
			int num1 = 0;
			int num2 = 0;
			if (tokens[GENOTYPE].equals("0")) {
				num0++;
			} else if (tokens[GENOTYPE].equals("1")) {
				num1++;
			} else if (tokens[GENOTYPE].equals("2")) {
				num2++;
			}
			fm.write(tokens[GENOTYPE] + "\t");
			
			
			for (int i = 1; i < frs.size(); i++) {
				line = frs.get(i).readLine();
				tokens= line.split("\t");
				if (tokens[GENOTYPE].equals("0")) {
					num0++;
				} else if (tokens[GENOTYPE].equals("1")) {
					num1++;
				} else if (tokens[GENOTYPE].equals("2")) {
					num2++;
				}
				fm.write(tokens[GENOTYPE] + "\t");
			}
			
			int r = num1 + num2*2;
			int w = num0*2 + num1;
			
			if (r+w == 0) {
				fm.writeLine(num0 + "\t" + num1 + "\t" + num2 + "\tNA\tNA\tNA\tNA\tNA");
				continue;
			}
			
			float R = (float) r/(r+w);
			float W = (float) w/(r+w);
			fm.writeLine(num0 + "\t" + num1 + "\t" + num2
					+ "\t" + W + "\t" + R
					+ "\t" + String.format("%,.10f",W*W*100)
					+ "\t" + String.format("%,.10f",2*R*W*100)
					+ "\t" + String.format("%,.10f",R*R*100));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			new MergeAlleleFrequency().printHelp();
		} else {
			new MergeAlleleFrequency().go(args, args.length + "_allele_freq.mfq");
		}
	}

}
