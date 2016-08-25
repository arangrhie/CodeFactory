/**
 * 
 */
package javax.arang.annovar;

import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.util.ANNOVAR;

/**
 * @author Arang Rhie
 *
 */
public class CompareGenotypes extends I2Owrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		FileReader frList = new FileReader(listFileName);
		
		String line = frList.readLine();
		String[] tokens = line.split("\t");
		
		String in1Name = tokens[0];
		String in2Name = tokens[1];
		
		Vector<String> in1IdList = new Vector<String>();
		Vector<String> in2IdList = new Vector<String>();
		
		while (frList.hasMoreLines()) {
			tokens = frList.readLine().split("\t");
			if (tokens.length < 2)	continue;
			in1IdList.add(tokens[0]);
			in2IdList.add(tokens[1]);
		}
		
		line = fr1.readLine();	// skip line of other info
		line = fr1.readLine();
		tokens = line.split("\t");
		int[] in1IdIndex = new int[in1IdList.size()];
		int[] in2IdIndex = new int[in2IdList.size()];

		for (int i = 0; i < in1IdList.size(); i++) {
			in1IdIndex[in1IdList.indexOf(tokens[i + ANNOVAR.NOTE + 1])] = i + ANNOVAR.NOTE + 1;
		}
		
		line = fr2.readLine();	// skip line of other info
		line = fr2.readLine();
		tokens = line.split("\t");
		for (int i = 0; i < in2IdList.size(); i++) {
			in2IdIndex[in2IdList.indexOf(tokens[i + ANNOVAR.NOTE + 1])] = i + ANNOVAR.NOTE + 1;
		}
		
//		for (int i = 0; i < in1IdIndex.length; i++) {
//			System.out.println(i + "\t" + in1IdList.get(i) + "\t" + in2IdList.get(i));
//		}
		
		int totalNumSnps = 0;
		int totalMatchedSnps = 0;
		String[] tokens1;
		String[] tokens2;
		boolean isFirstMismatch = false;
		boolean hasMismatch = false;
		while (fr1.hasMoreLines() && fr2.hasMoreLines()) {
			tokens1 = fr1.readLine().split("\t");
			tokens2 = fr2.readLine().split("\t");
			isFirstMismatch = true;
			hasMismatch = false;
			for (int i = 0; i < in1IdIndex.length; i++) {
				//System.out.println(in1IdList.get(i) + " " + tokens1[in1IdIndex[i]] + " "
				//				+ in2IdList.get(i) + " " + tokens2[in2IdIndex[i]]);
				if (!tokens1[in1IdIndex[i]].equals(tokens2[in2IdIndex[i]])) {
					if (isFirstMismatch) {
						isFirstMismatch = false;
						hasMismatch = true;
						fm.write(tokens2[ANNOVAR.CHR] + "\t" + tokens2[ANNOVAR.POS_FROM] + "\t" + tokens2[ANNOVAR.POS_TO] + "\t"
									+ tokens1[ANNOVAR.REF] + ":" + tokens2[ANNOVAR.REF] + "\t"
									+ tokens1[ANNOVAR.ALT] + ":" + tokens1[ANNOVAR.ALT]);
					}
					fm.write("\t" + tokens1[in1IdIndex[i]] + ":" + tokens2[in2IdIndex[i]] + ":" + in1IdList.get(i));
				}
			}
			if (!hasMismatch) {
				totalMatchedSnps++;
			} else {
				fm.writeLine();
			}
			totalNumSnps++;
		}
		
		float identicalRate = (float) 100.0f * totalMatchedSnps / totalNumSnps;
		System.out.println(in1Name + " and " + in2Name + " matches: "
				+ totalMatchedSnps + " / " + totalNumSnps + " " + String.format("%,.2f", identicalRate));
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarCompareGenotypes.jar <in1.snp> <in2.snp> <list> <out.snp>");
		System.out.println("\t<list> : sample ids in <in1.snp> and <in2.snp>, with a header line");
		System.out.println("\t\t<list> example: 1000G\tASAP\t[Population]");
		System.out.println("\t<out.snp> : chr\tstart\tstop\tref\talt\tsample_id[in1_genotype : in2_genotype]");
	}

	static String listFileName; 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 4) {
			listFileName = args[2];
			new CompareGenotypes().go(args[0], args[1], args[3]);
		} else {
			new CompareGenotypes().printHelp();
		}
	}

}
