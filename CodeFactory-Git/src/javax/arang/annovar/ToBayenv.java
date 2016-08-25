/**
 * 
 */
package javax.arang.annovar;

import java.util.Vector;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.util.ANNOVAR;

/**
 * @author Arang Rhie
 *
 */
public class ToBayenv extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		int[] popIndex = new int[populations.size()];
		
		String[] tokens;
		tokens = fr.readLine().split("\t");
		
		System.out.println("Used populations:");
		int idx = 0;
		for (int i = ANNOVAR.NOTE; i < tokens.length; i++) {
			if (populations.contains(tokens[i])) {
				popIndex[idx++] = i;
			}
		}

		for (int i = 0; i < popIndex.length; i++) {
			System.out.print(tokens[popIndex[i]] + " ");
		}
		
		System.out.println();
		
		FileMaker fmUsedPos = new FileMaker(fm.getDir(), fm.getFileName() + ".pos");
		StringBuffer allele1 = new StringBuffer();
		StringBuffer allele2 = new StringBuffer();
		
		int novelNum = 0;
		while (fr.hasMoreLines()) {
			tokens = fr.readLine().split("\t");
			allele1 = new StringBuffer();
			allele2 = new StringBuffer();
			for (int i = 0; i < popIndex.length; i++) {
				String[] allele = tokens[popIndex[i]].split(" ");
				allele1.append(allele[0] + "\t");
				allele2.append(allele[1] + "\t");
			}
			if (allele1.toString().equals("0\t0\t0\t") || allele2.toString().equals("0\t0\t0\t")) {
				continue;
			}
			fmUsedPos.writeLine(tokens[ANNOVAR.CHR] + "\t" + tokens[ANNOVAR.POS_FROM] + "\t" + tokens[ANNOVAR.POS_TO] + "\t" + tokens[ANNOVAR.NOTE]);
			fm.writeLine(allele1.toString());
			fm.writeLine(allele2.toString());
			tokens[ANNOVAR.NOTE] = tokens[ANNOVAR.NOTE].equals(".")? ("novel" + ++novelNum) : tokens[ANNOVAR.NOTE]; 
			FileMaker fmSNP = new FileMaker(fm.getDir() + "/snp", tokens[ANNOVAR.NOTE]);
			fmSNP.writeLine(allele1.toString());
			fmSNP.writeLine(allele2.toString());
			fmSNP.closeMaker();
		}
		fmUsedPos.closeMaker();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar annovar2Bayenv.jar <in.annovar> <out.bayenv> [pop_id_1 pop_id_2 .. pop_id_N]");
		System.out.println("\tConverts ANNOVAR formatted file into bayenv input file");
		System.out.println("\t1st line should contain header: chr\tstart\tend\tref\talt\trsid\t<pop1>\t..\t<popN>");
	}

	private static Vector<String> populations;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length >= 3) {
			populations = new Vector<String>();
			for (int i = 2; i < args.length; i++) {
				populations.add(args[i]);
			}
			new ToBayenv().go(args[0], args[1]);
		} else {
			new ToBayenv().printHelp();
		}

	}

}
