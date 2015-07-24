package javax.arang.fusion;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ToTable extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fusionToTable.jar <*star.out.chimeric.fusion>");
		System.out.println("\t<out>: chimera_table.fusion");
		System.out.println("\tAggregates .fusion files into one unique file, with sample IDs at the end coded with 1/0. 1: has fusion, 0: not found.");
		System.out.println("Arang Rhie, 2014-12-09. arrhie@gmail.com");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		HashMap<String, String> fusionMap = new HashMap<String, String>();
		String line;
		String[] tokens;
		String key;
		String genotype;
		int numSamples = 0;
		
		fm.write("#DONOR_CHR\tDONOR_POS\tACCEPTOR_CHR\tACCEPTOR_POS");
		for (FileReader fr : frs) {
			fm.write("\t" + fr.getFileName().substring(0, fr.getFileName().indexOf("_")));
			numSamples++;
			while (fr.hasMoreLines()) {
				line = fr.readLine();
				if (line.startsWith("#")) continue;
				tokens = line.split("\t");
				key = tokens[0] + "\t" + tokens[1] + "\t" + tokens[2] + "\t" + tokens[3];
				if (!fusionMap.containsKey(key)) {
					fusionMap.put(key, "");
				}
				genotype = paddTable(fusionMap, key, numSamples);
				genotype += "\t" + tokens[4];
				fusionMap.put(key, genotype);
			}
		}
		fm.writeLine();
		
		System.out.println("Start writing fusion map table");
		for (String fusion : fusionMap.keySet()) {
			fm.writeLine(fusion + paddTable(fusionMap, fusion, numSamples + 1));
		}
		System.out.println(fusionMap.size() + " fusions have been written.");
		
	}
	
	private String paddTable(HashMap<String, String> fusionMap, String key, int numSamples) {
		String genotype;
		String[] genotypes;
		if (!fusionMap.get(key).equals("")) {
			genotype = fusionMap.get(key);
			genotypes = genotype.split("\t");
			if (genotypes.length < numSamples) {
				for (int i = genotypes.length; i < numSamples; i++) {
					genotype += "\t0";
				}
			}
		} else {
			genotype = "";
			for (int i = 0; i < numSamples - 1; i++) {
				genotype += "\t0";
			}
		}
		
		
		return genotype;
	}

	public static void main(String[] args) {
		if (args.length >= 1) {
			new ToTable().go(args, "chimera_table.fusion");
		} else {
			new ToTable().printHelp();
		}
	}

}
