/**
 * 
 */
package javax.arang.annovar;

import java.util.HashMap;
import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class GetTargetOverlaps extends I2Owrapper {

	@Override
	public void hooker(FileReader frIn, FileReader frTarget, FileMaker fm) {
		
		HashMap<String, String> targetTable = new HashMap<String, String>();
		Vector<String> notInTarget = new Vector<String>();
		
		String line;
		String[] tokens;
		// read target.snp file and create a hash table
		while (frTarget.hasMoreLines()) {
			line = frTarget.readLine();
			if (line.isEmpty())	break;
			tokens = line.split("\t");
			targetTable.put(tokens[ANNOVAR.POS_FROM], tokens[ANNOVAR.REF] + "\t" + tokens[ANNOVAR.ALT]);
			notInTarget.add(tokens[ANNOVAR.POS_FROM]);
		}
		
		// write header
		try {
			fm.writeLine(frIn.readLine());
		} catch (Exception e) {
			System.out.println(frIn.getFileName() + " should start with header, containing sample names");
			e.printStackTrace();
			System.exit(-1);
		}
		
		FileMaker fmMultiAllel = new FileMaker(fm.getDir(), fm.getFileName().replace(".snp", ".snp.mult_allele"));
		System.out.println("Multiallelic positions are written in " + fmMultiAllel.getFileName());
		
		// read in.snp file
		while (frIn.hasMoreLines()) {
			line = frIn.readLine();
			tokens = line.split("\t");
			// compare position start
			if (targetTable.containsKey(tokens[ANNOVAR.POS_FROM])) {
				if (!targetTable.get(tokens[ANNOVAR.POS_FROM]).equals(tokens[ANNOVAR.REF] + "\t" + tokens[ANNOVAR.ALT])) {
					// write on snp.mult_allele
					fmMultiAllel.writeLine(line);
					System.out.println(tokens[ANNOVAR.POS_FROM] + " differs; 1000G: " + tokens[ANNOVAR.REF] + "\t" + tokens[ANNOVAR.ALT] + "\ttarget: " + targetTable.get(tokens[ANNOVAR.POS_FROM]));
				} else {
					// write on out.snp if intersects
					fm.writeLine(line);
					notInTarget.remove(tokens[ANNOVAR.POS_FROM]);
				}
			}
		}
		
		// snps not in target files
		if (notInTarget.size() > 0) {
			FileMaker fm0Target = new FileMaker(fm.getDir(), fm.getFileName().replace(".snp", ".snp.na_pos"));
			System.out.println(notInTarget.size() + " are not in " + frIn.getFileName());
			System.out.println("These positions are written in " + fm0Target.getFileName());
			for (String pos : notInTarget) {
				fm0Target.writeLine(pos);
			}
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpGetTargetOverlaps.jar <in.chrN.snp> <target.chrN.snp> <fileName.chrN.snp>");
		System.out.println("\t<in.snp>: annovar formatted. chr\tstart\tstop\tref\tsnp\tid\tsamples...");
		System.out.println("\t<target.snp>: chr\tstart\tstop\tref\tsnp\tid\t...");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new GetTargetOverlaps().go(args[0], args[1], args[2]);
		} else {
			new GetTargetOverlaps().printHelp();
		}
	}

}
