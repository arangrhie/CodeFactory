package javax.arang.txt;

import java.util.ArrayList;
import java.util.Vector;

import javax.arang.IO.INwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ParseBedCountCoveredBasesLog extends INwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtParseBedCountCoveredBasesLog.jar <sample1_*.log> <sample2_*.log> ... <sampleN_*.log>");
		System.out.println("Parse \'Total number of bases covered:\' and write it with sample names.");
		System.out.println("\t<infiles.log>: log files generated from bedCountCoveredBases.jar");
		System.out.println("\t<out>: output is written to a file named \'bed_coved_bases_summary.txt\'");
		System.out.println("Arang Rhie, 2014-03-27. arrhie@gmail.com");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs) {
		String line;
		String[] tokens;
		Vector<String> sampleNames = new Vector<String>();
		Vector<String> basesCovered = new Vector<String>();
			
		for (FileReader fr : frs) {
			sampleNames.add(fr.getFileName().substring(0, fr.getFileName().indexOf("_")));
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.startsWith("Total number of bases covered")) {
					tokens = line.split("\t");
					basesCovered.add(tokens[1]);
					break;
				}
			}
		}
		
		FileMaker fm = new FileMaker("bed_covered_bases_summary.txt");
		fm.write("Sample");
		for(String sample : sampleNames) {
			fm.write("\t" + sample);
		}
		fm.writeLine();
		
		fm.write("Bases");
		for(String bases : basesCovered) {
			fm.write("\t" + bases);
		}
		fm.writeLine();
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			new ParseBedCountCoveredBasesLog().printHelp();
		} else {
			new ParseBedCountCoveredBasesLog().go(args);
		}
	}

}
