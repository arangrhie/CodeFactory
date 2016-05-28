package javax.arang.variant;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class AlleleMerge extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar variantAlleleMerge.jar <col_idx_to_merge> <in*.cnt> <out.cnt>");
		System.out.println("\t<col_idx_to_merge>: 1-based. This column will be added.");
		System.out.println("\t<in*.cnt>: Generated with vcfToAlleleCount.jar");
		System.out.println("\t<out.cnt>: Allele count with columns. Header will be each of the corresponding in.cnt file name.");
		System.out.println("\t\tMulti-allelic sites will be removed.");
		System.out.println("\t\tCHR\tPOS\tREF\tALT\t<col_1>\t...\t<col_N>");
		System.out.println("Arang Rhie, 2016-05-24. arrhie@gmail.com");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		ArrayList<String> posList = new ArrayList<String>();	// CHR\tPOS
		HashMap<String, String> posToAlt = new HashMap<String, String>();	// pos | REF\tALT
		HashMap<String, String[]> posToCols = new HashMap<String, String[]>();	// pos | String[col value of each sample]
		ArrayList<String> multiAllelicKey = new ArrayList<String>(); // pos list to remove
		int numSamples = frs.size();
		String pos;
		String alleles;
		String line;
		String[] tokens;
		String header = "#CHR\tPOS\tREF\tALT";
		int sampleIdx = -1;
		for (FileReader fr : frs) {
			header += "\t" + fr.getFileName();
			sampleIdx++;	// starts from 0
			fr.readLine();	// skip header line
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				tokens = line.split(RegExp.TAB);
				pos = tokens[AlleleCount.CHR] + "\t" + tokens[AlleleCount.POS];
				alleles = tokens[AlleleCount.REF] + "\t" + tokens[AlleleCount.ALT];
				// remove if pos is multi-allelic site
				if (posToAlt.containsKey(pos)) {
					if (!posToAlt.get(pos).equals(alleles)) {
						// if is multi-allele
						System.err.println(pos + " has multi altered alleles: " + posToAlt.get(pos) + " and " + alleles + " . This position is discarded.");
						multiAllelicKey.add(pos);
						posToAlt.remove(pos);
						posList.remove(pos);
					} else {
						// allele matches the previously reported one
						posToCols.get(pos)[sampleIdx] = tokens[colIdxToMrg];
					}
				} else {
					if (!multiAllelicKey.contains(pos)) {
						// new pos and alleles
						posList.add(pos);
						posToAlt.put(pos, alleles);
						String[] values = getInitializedArr(numSamples);
						values[sampleIdx] = tokens[colIdxToMrg];
						posToCols.put(pos, values);
					} // else do nothing
				}
			}
		}
		
		fm.writeLine(header);
		for (int i = 0; i < posList.size(); i++) {
			pos = posList.get(i);
			fm.write(pos + "\t" + posToAlt.get(pos));
			for (int j = 0; j < numSamples; j++) {
				fm.write("\t" + posToCols.get(pos)[j]);
			}
			fm.writeLine();
		}
	}
	
	private String[] getInitializedArr(int numSamples) {
		String[] arr = new String[numSamples];
		for (int i = 0; i < numSamples; i++) {
			arr[i] = "NA";
		}
		return arr;
	}

	public static int colIdxToMrg = AlleleCount.ALT_CNT;
	public static void main(String[] args) {
		if (args.length > 3) {
			String[] inFiles = new String[args.length - 2];
			for (int i = 1; i < args.length - 1; i++) {
				inFiles[i - 1] = args[i];
			}
			
			colIdxToMrg = Integer.parseInt(args[0]) - 1;
			new AlleleMerge().go(inFiles, args[args.length - 1]);
		} else {
			new AlleleMerge().printHelp();
		}
	}

}
