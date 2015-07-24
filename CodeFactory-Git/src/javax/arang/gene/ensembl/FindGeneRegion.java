package javax.arang.gene.ensembl;

import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class FindGeneRegion extends I2Owrapper {

	@Override
	public void hooker(FileReader targetFr, FileReader ensFr, FileMaker fm) {

		String line;
		String[] tokens;
		
		Vector<String> geneList = new Vector<String>();
		
		while (targetFr.hasMoreLines()) {
			line = targetFr.readLine();
			geneList.add(line);
		}
		
		while (ensFr.hasMoreLines()) {
			line = ensFr.readLine();
			tokens = line.split("\t");
			
			if (geneList.contains(tokens[1])) {
				fm.writeLine(tokens[1] + "\t" + tokens[2] + "\t" + tokens[4] + "\t" + tokens[5]);
			}
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar ensFindGeneRegion.jar <target_list.txt> <ensGeneName.txt>");
		System.out.println("\t<out> ensGeneNameRegion.txt");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new FindGeneRegion().go(args[0], args[1], "ensGeneNameRegion.txt");
		} else {
			new FindGeneRegion().printHelp();
		}
	}

}
