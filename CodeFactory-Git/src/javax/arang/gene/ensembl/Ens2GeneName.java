package javax.arang.gene.ensembl;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Ens2GeneName extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		HashMap<String, String> geneNameMap = new HashMap<String, String>();
		String line;
		String[] tokens;
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			geneNameMap.put(tokens[0], tokens[1]);
		}
		
		System.out.println("Loading " + fr1.getFileName() + " completed");
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].startsWith("EN") && geneNameMap.containsKey(tokens[i])) {
					tokens[i] = geneNameMap.get(tokens[i]);
				}
				fm.write(tokens[i] + "\t");
			}
			fm.writeLine("");
		}
		
		System.out.println("Converting finished to " + fm.getFileName());
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java jar geneEns2GeneName.jar <ensemblToGeneName.txt> <ensGene.txt>");
		System.out.println("\tout <ensGeneNamed.txt>: Converts the ENS gene names to readible gene names");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new Ens2GeneName().go(args[0], args[1], args[1].replace(".txt", "Name.txt"));
		} else {
			new Ens2GeneName().printHelp();
		}

	}

}
