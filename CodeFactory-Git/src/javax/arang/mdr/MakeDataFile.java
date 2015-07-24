/**
 * 
 */
package javax.arang.mdr;

import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class MakeDataFile extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
//		String[] genotype;
		
		Vector<String> ids = new Vector<String>();
		Vector<String> genotypes = new Vector<String>();
		
		fr.readLine();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			// skip snps with missing data
			if (line.contains("?"))	continue;
			ids.add(tokens[0]);
			//genotype = tokens[1].split("\t");
			for (int i = 1; i < tokens.length; i++) {
				if (genotypes.size() == i - 1) {
					genotypes.add(tokens[i]);
				} else {
					String geno = genotypes.remove(i - 1);
					genotypes.add(i - 1, geno + "\t" + tokens[i]);
				}
			}
		}
		
		System.out.println("# of SNPs without missing data: " + ids.size());
		for (int i = 0; i < ids.size(); i++) {
			fm.write(ids.get(i) + "\t");
		}
		fm.writeLine("class");
		
		if (numCases == 0) {
			numCases = genotypes.size()/2;
		}
		System.out.println("# of cases: " + numCases);
		
		int type = 1;	// case
		
		for (int i = 0; i < numCases; i++) {
			fm.writeLine(genotypes.get(i) + "\t" + type);
		}
		
		type = 0;	// control
		for (int i = numCases; i < genotypes.size(); i++) {
			fm.writeLine(genotypes.get(i) + "\t" + type);
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar mdrMakeDataFile.jar <input> [num_cases]");
		System.out.println("\t<input>: ID\tGenotype");
		System.out.println("\t\tGenotype is space delemeted, first [num_cases] are \"Affected(Case)\", left are \"Unaffected(Control)\"");
		System.out.println("\t[num_cases]: DEFAULT = Half of the genotypes");
		System.out.println("\t<output>: MDR input data file format");
		System.out.println("\t\tID1\tID2\t..\tIDn\tclass");
		System.out.println("\t**Missing data with \"?\" are not included**");
	}

	static int numCases = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new MakeDataFile().go(args[0], args[0].replace(".", "_mdr."));
		} else if (args.length == 2) {
			numCases = Integer.parseInt(args[1]);
			new MakeDataFile().go(args[0], args[0].replace(".", "_" + numCases + "_mdr."));
		} else {
			new MakeDataFile().printHelp();
		}
	}

}
