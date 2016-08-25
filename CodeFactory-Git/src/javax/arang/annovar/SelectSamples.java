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
public class SelectSamples extends I2Owrapper {

	@Override
	public void hooker(FileReader frSnp, FileReader frSampleList, FileMaker fm) {
		// Create sample list as a vector
		Vector<String> sampleList = new Vector<String>();
		Vector<String> phenotypeList = new Vector<String>();
		
		String line;
		String[] tokens;
		while (frSampleList.hasMoreLines()) {
			line = frSampleList.readLine();
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			sampleList.add(tokens[0]);
			phenotypeList.add(tokens[1]);
		}
		System.out.println(sampleList.size() + " samples collected");

		
		
		// write phenotypes
		for (int i = ANNOVAR.CHR; i < ANNOVAR.NOTE; i++) {
			fm.write("\t");
		}
//		for (int i = 0; i < phenotypeList.size(); i++) {
//			fm.write("\t" + phenotypeList.get(i));
//		}
//		fm.writeLine("");

		// read header
		line = frSnp.readLine();
		tokens = line.split("\t");

		// create a filter mask for samples
		boolean[] mask = new boolean[tokens.length];

		StringBuffer sampleIds = new StringBuffer();

		// write CHR	FROM	TO	REF	ALT	ID
		sampleIds.append(tokens[ANNOVAR.CHR]);
		mask[0] = true;

		
		for (int i = ANNOVAR.POS_FROM; i <= ANNOVAR.NOTE; i++) {
			sampleIds.append("\t" + tokens[i]);
			mask[i] = true;
		}
		

		for (int i = ANNOVAR.NOTE + 1; i < tokens.length; i++) {
			if (sampleList.contains(tokens[i])) {
				//fm.write("\t" + tokens[i]);
				fm.write("\t" + phenotypeList.get(sampleList.indexOf(tokens[i])));
				sampleIds.append("\t" + tokens[i]);
				mask[i] = true;
			} else {
				mask[i] = false;
			}
			// Test code
			//System.out.println(tokens[i] + "\t" + mask[i]);
		}
		fm.writeLine("");
		fm.writeLine(sampleIds.toString());
		
		// write all the masked genotypes
		while (frSnp.hasMoreLines()) {
			line = frSnp.readLine();
			tokens = line.split("\t");
			if (line.isEmpty())	continue;
			fm.write(tokens[0]);	// write CHR
			for (int i = 1; i < tokens.length; i++) {
				if (mask[i])	fm.write("\t" + tokens[i]);
			}
			fm.writeLine("");
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpSelectSamples.jar <in.snp> <sample_list.txt> <out.snp>");
		System.out.println("Select samples listed only in sample_list.txt");
		System.out.println("\t<in.snp>: chr\tstart\tstop\tref\talt\tid\tsample_id_1\t...");
		System.out.println("\t<sample_list.txt>: sample_id\tphenotype (or any other quantitative value;\n" +
				"\t\tthis value will be used in PLINK as phenotype or case/control indicator)");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new SelectSamples().go(args[0], args[1], args[2]);
		} else {
			new SelectSamples().printHelp();
		}
	}

}
