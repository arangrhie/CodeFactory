/**
 * 
 */
package javax.arang.vcf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class AlleleFrequencyByGroup extends I2Owrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		
		// read id_group table and make group vectors
		String line = fr2.readLine();
		String[] tokens = line.split("\t");
		ArrayList<HashMap<String, String>> groups = new ArrayList<HashMap<String, String>>();	// groups or categories in fr2
		String[] groupNames = new String[tokens.length - 1];
		for (int i = 1; i < tokens.length; i++) {
			groups.add(new HashMap<String, String>());
			groupNames[i-1] = tokens[i];
		}
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split("\t");
			for (int i = 1; i < tokens.length; i++) {
				HashMap<String, String> group = groups.get(i-1);
				if (group.containsKey(tokens[i])) {
					group.put(tokens[i], group.get(tokens[i]) + "\t" + tokens[0]);	// Add group - SAMPLE_IDs delemited with TABs
				} else {
					group.put(tokens[i], tokens[0]);
				}
			}
		}
		
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (line.startsWith("#CHROM")) {
				break;
			}
		}
		
		tokens = line.split("\t");
		
		// write header
		fm.write(tokens[VCF.CHROM]);
		for (int i = VCF.POS; i < VCF.SAMPLE; i++) {
			fm.write("\t" + tokens[i]);
		}
		
		// write group names
		for (int i = 0; i < groupNames.length; i++) {
			System.out.println("< " + groupNames[i] + " >");
			HashMap<String, String> group = groups.get(i);
			Set<String> keys = group.keySet();
			for (String key : keys) {
				fm.write("\t" + key);
				System.out.println(key + " :\t" + group.get(key));
			}
		}
		fm.writeLine("");
		
		// make sample id to column idx mapping table - sampleIds
		HashMap<String, Integer> sampleIds = new HashMap<String, Integer>();
		for (int i = VCF.SAMPLE; i < tokens.length; i++) {
			sampleIds.put(tokens[i], i);	// SAMPLE_ID - COL_IDX
			//System.out.println(tokens[i] + " " + i);
		}
		
		String gt;	// genotype: 0|0, 1|0, 0|1, 1|1, .|. or ./. ..
		int ac = 0;	// altered allele count
		int an = 0; // num. samples having non-NA gt
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split("\t");
			
			// write chromosomal position
			fm.write(tokens[VCF.CHROM]);
			for (int i = VCF.POS; i < VCF.SAMPLE; i++) {
				fm.write("\t" + tokens[i]);
			}
			
			// get AF by groups
			for (int i = 0; i < groupNames.length; i++) {
				//System.out.println("< " + groupNames[i] + " >");
				HashMap<String, String> group = groups.get(i);
				Set<String> group_keys = group.keySet();
				// for each population
				for (String group_key : group_keys) {
					String[] sampleNamesInGroup = group.get(group_key).split("\t");
					ac = 0;
					an = 0;
					// for each sample
					for (int sampleIdx = 0; sampleIdx < sampleNamesInGroup.length; sampleIdx++) {
						gt = tokens[sampleIds.get(sampleNamesInGroup[sampleIdx])];
						gt = VCF.parseGT(tokens[VCF.FORMAT], gt);
						if (!gt.equals("NA") && !gt.equals("3")) {
							ac += Integer.parseInt(gt);
							an++;
							//System.out.print(Integer.parseInt(tokens[sampleIds.get(sampleNamesInGroup[sampleIdx])]) + " ");
						}
					}
					//System.out.println();
					an *= 2;	// multiply 2 for getting both alleles
					if (an > 0) {
						if (!isCount) {
							float af = (float) ac / an;
							//System.out.println("AC=" + ac + ";AN=" + an + ";AF=" + String.format("%,.4f", af));
							fm.write("\t" + String.format("%,.4f", af));
						} else {
							fm.write("\t" + (an - ac) + " " + ac);
						}
					} else {
//						System.out.println("AC=" + ac + ";AN=" + an + ";AF=NA");
						if (!isCount) {
							fm.write("\t" + "NA");
						} else {
							fm.write("\t" + "NA NA");
						}
					}
				}
			}
			fm.writeLine("");
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovarAlleleFrequencyByGroup.jar <in.vcf> <in_id_group.txt> [COUNT=FALSE]");
		System.out.println("Make an annovar formatted file with AF by groups");
		System.out.println("\tCOUNT: either true or false; true if AF should denoting counts instead of %");
		System.out.println("\t\tif COUNT=TRUE, alleles are noted in \'ref alt\' order, seperated with a space.");
		System.out.println("\t<in.vcf>: Any .vcf file. Only bi-allelic sites will be counted.");
		System.out.println("\t<in_id_group.txt>: sample_id\tgroup_1\tgroup_2\t...");
		System.out.println("\t\t* First line must contain a header line. (will be skipped)");
		System.out.println("\tOutput: <in.af> or <in.cnt> if COUNT=true.");
		System.out.println("Arang Rhie, 2016-05-28. arrhie@gmail.com");
	}

	static boolean isCount = false; 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new AlleleFrequencyByGroup().go(args[0], args[1], args[0] + ".af");
		} else if (args.length == 3) {
			isCount = Boolean.parseBoolean(args[2]);
			String format = (isCount ? ".cnt" : ".af" );
			new AlleleFrequencyByGroup().go(args[0], args[1], args[0] + format);
		} else {
			new AlleleFrequencyByGroup().printHelp();
		}
	}

}
