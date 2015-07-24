package javax.arang.gene.ensembl;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Gtf2GeneTable extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<String, String> geneMap = new HashMap<String, String>();
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			String names = tokens[tokens.length - 1];
			String[] ids = names.split(";");
			ids[0] = ids[0].substring(ids[0].indexOf("\"") + 1, ids[0].lastIndexOf("\""));
			ids[1] = ids[1].substring(ids[1].indexOf("\"") + 1, ids[1].lastIndexOf("\""));
			ids[3] = ids[3].substring(ids[3].indexOf("\"") + 1, ids[3].lastIndexOf("\""));
			geneMap.put(ids[0], ids[3]);
			geneMap.put(ids[1], ids[3]);
		}
		
		for (String key : geneMap.keySet()) {
			fm.writeLine(key + "\t" + geneMap.get(key));
		}
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar gtf2GeneTable.jar <in.gtf>");
		System.out.println("\t<output>: ensemblToGeneName.txt");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			new Gtf2GeneTable().go(args[0], "ensemblToGeneName.txt");
		} else {
			new Gtf2GeneTable().printHelp();
		}

	}

}
