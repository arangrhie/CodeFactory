/**
 * 
 */
package javax.arang.linkage;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class PedPhenoReplace extends I2Owrapper {

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#hooker(javax.arang.IO.FileReader, javax.arang.IO.FileReader, javax.arang.IO.FileMaker)
	 */
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		// Create a HashTable for sample_id - phenotype
		HashMap<String, String> idPhenoTable = new HashMap<String, String>();
		String[] tokens;
		while (fr2.hasMoreLines()) {
			tokens = fr2.readLine().split("\t");
			if (tokens.length < 2) {
				break;
			}
			idPhenoTable.put(tokens[0], tokens[1]);
			System.out.println(tokens[0] + " " + tokens[1]);
		}
		System.out.println("Total of " + idPhenoTable.size() + " sample ids collected from " + fr2.getFileName());
		
		// read ped file
		String line;
		StringBuffer newLine = new StringBuffer();
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split("\t");
			String pop = idPhenoTable.get(tokens[PED.SAMPLE_ID]);
			newLine.append(tokens[PED.FAMILY_ID] + "\t"
					+ tokens[PED.SAMPLE_ID] + "\t"
					+ tokens[PED.FATHER_ID] + "\t"
					+ tokens[PED.MOTHER_ID] + "\t"
					+ tokens[PED.SEX] + "\t"
					+ pop);
			for (int i = PED.GENOTYPE; i < tokens.length; i++) {
				newLine.append("\t" + tokens[i]);
			}
			fm.writeLine(newLine.toString());
			newLine = new StringBuffer();
		}
	}

	/* (non-Javadoc)
	 * @see javax.arang.IO.I2Owrapper#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar linkagePedPhenoReplace.jar <in.ped> <sample_pheno.txt> <out.ped>");
		System.out.println("\tReplace <phenotype> column corresponding to <sample_pheno.txt> by <sample_id>");
		System.out.println("\t<sample_pheno.txt>: <sample_id>\t<phenotype_to_replace>");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new PedPhenoReplace().go(args[0], args[1], args[2]);
		} else {
			new PedPhenoReplace().printHelp();
		}
	}

}
