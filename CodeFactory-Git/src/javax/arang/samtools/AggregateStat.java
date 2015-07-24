/**
 * 
 */
package javax.arang.samtools;

import java.util.ArrayList;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

/**
 * @author Arang Rhie
 *
 */
public class AggregateStat extends INOwrapper {

	String header = "Sample ID\tTotal # of Reads\tDuplicates\tMapped\t" +
			"Paired in Sequencing\tRead 1\tRead 2\tProperly Paired\tMate Mapped\t" +
			"Singletons\tMate Mapped to Different Chr\t" +
			"Mate Mapped to Different Chr (mapQ>5)\t" +
			"Mapped (%)\tProperly Paired (%)\tSingletons (%)\tDuplicates (%)";
	
	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samtoolsAggregateStat.jar *.stat");
		System.out.println("\t<input>: .stat files generated with samtools flagstat");
		System.out.println("\t<output>: [align_summary.stat] summary of the .stat files");
		System.out.println(header);
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		fm.writeLine(header);
		String line;
		String[] tokens;
		for (FileReader fr : frs) {
			String sampleName = fr.getFileName().substring(0, fr.getFileName().indexOf("."));
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int totalNumReads = Integer.parseInt(tokens[0].trim());
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int duplicates = Integer.parseInt(tokens[0]);
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int mapped = Integer.parseInt(tokens[0]);
			float mappedP = Float.parseFloat(tokens[1].substring(tokens[1].indexOf("(")+1, tokens[1].indexOf("%")));
			float duplicatesP = (float) duplicates*100 / mapped;
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int paired = Integer.parseInt(tokens[0]);
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int read1 = Integer.parseInt(tokens[0]);
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int read2 = Integer.parseInt(tokens[0]);
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int properlyPaired = Integer.parseInt(tokens[0]);
			float properlyParedP = Float.parseFloat(tokens[1].substring(tokens[1].indexOf("(")+1, tokens[1].indexOf("%")));
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int mateMapped = Integer.parseInt(tokens[0]);
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int singletons = Integer.parseInt(tokens[0]);
			float singletonsP = Float.parseFloat(tokens[1].substring(tokens[1].indexOf("(")+1, tokens[1].indexOf("%")));
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int diffChr = Integer.parseInt(tokens[0]);
			line = fr.readLine();
			tokens = line.split(" \\+ ");
			int diffChrMapQ = Integer.parseInt(tokens[0]);
			fm.writeLine(sampleName + "\t"
					+ String.format("%,10d", totalNumReads) + "\t"
					+ String.format("%,10d", duplicates) + "\t"
					+ String.format("%,10d", mapped) + "\t"
					+ String.format("%,10d", paired) + "\t"
					+ String.format("%,10d", read1) + "\t"
					+ String.format("%,10d", read2) + "\t"
					+ String.format("%,10d", properlyPaired) + "\t"
					+ String.format("%,10d", mateMapped) + "\t"
					+ String.format("%,10d", singletons) + "\t"
					+ String.format("%,10d", diffChr) + "\t"
					+ String.format("%,10d", diffChrMapQ) + "\t"
					+ String.format("%10.2f", mappedP) + "%\t"
					+ String.format("%10.2f", properlyParedP) + "%\t"
					+ String.format("%10.2f", singletonsP) + "%\t"
					+ String.format("%10.2f", duplicatesP) + "%");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			new AggregateStat().printHelp();
		} else {
			new AggregateStat().go(args, "align_summary.stat");
		}
	}

}
