/**
 * 
 */
package javax.arang.annovar;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.util.ANNOVAR;

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
		Vector<HashMap<String, String>> groups = new Vector<HashMap<String, String>>();
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
					group.put(tokens[i], group.get(tokens[i]) + "\t" + tokens[0]);
				} else {
					group.put(tokens[i], tokens[0]);
				}
			}
		}
		
		
		line = fr1.readLine();
		tokens = line.split("\t");
		
		// write header
		fm.write(tokens[ANNOVAR.CHR]);
		for (int i = ANNOVAR.POS_FROM; i <= ANNOVAR.NOTE; i++) {
			fm.write("\t" + tokens[i]);
		}
		
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
		
		HashMap<String, Integer> sampleIds = new HashMap<String, Integer>();
		for (int i = ANNOVAR.NOTE + 1; i < tokens.length; i++) {
			sampleIds.put(tokens[i], i);
			// System.out.println(tokens[i] + " " + i);
		}
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split("\t");
			
			// write position
			fm.write(tokens[ANNOVAR.CHR]);
			for (int i = ANNOVAR.POS_FROM; i <= ANNOVAR.NOTE; i++) {
				fm.write("\t" + tokens[i]);
			}
			
			// get AF by groups
			for (int i = 0; i < groupNames.length; i++) {
				//System.out.println("< " + groupNames[i] + " >");
				HashMap<String, String> group = groups.get(i);
				Set<String> keys = group.keySet();
				for (String key : keys) {
					// each population
					String[] sampleNamesInGroup = group.get(key).split("\t");
					int ac = 0;
					int na = 0;
					for (int sampleIdx = 0; sampleIdx < sampleNamesInGroup.length; sampleIdx++) {
						if (tokens[sampleIds.get(sampleNamesInGroup[sampleIdx])].equals("NA")) {
							na++;
						} else {
							ac += Integer.parseInt(tokens[sampleIds.get(sampleNamesInGroup[sampleIdx])]);
							//System.out.print(Integer.parseInt(tokens[sampleIds.get(sampleNamesInGroup[sampleIdx])]) + " ");
						}
					}
					//System.out.println();
					int an = (sampleNamesInGroup.length - na)*2;
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
						fm.write("\t" + "NA");
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
		System.out.println("Usage: java -jar annovarAlleleFrequencyByGroup.jar <in.snp> <in_id_group.txt> [COUNT=FALSE]");
		System.out.println("Make an annovar formatted file with AF by groups");
		System.out.println("\tCOUNT: either true or false; true if AF should denoting counts instead of %");
		System.out.println("\t\tif COUNT=TRUE, alleles are noted in \'ref snp\' order, seperated with a space.");
		System.out.println("\t<in_id_group.txt>: sample_id\tgroup_1\tgroup_2\t...");
		System.out.println("\t\t* First line must contain a header line. (will be skipped though)");
		System.out.println("\tOutput: <in.af> or <in.cnt> if COUNT=true.");
		System.out.println("Arang Rhie, 2014-01-22. arrhie@gmail.com");
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
