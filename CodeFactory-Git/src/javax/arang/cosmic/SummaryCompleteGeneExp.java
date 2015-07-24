package javax.arang.cosmic;

import java.text.ParseException;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.IOUtil;

public class SummaryCompleteGeneExp extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String tokens[];
		HashMap<String, Integer> overList = new HashMap<String, Integer>();
		HashMap<String, Integer> normList = new HashMap<String, Integer>();
		HashMap<String, Integer> underList = new HashMap<String, Integer>();
		
		fm.writeLine("GENE_NAME\tOver_N\tNormal_N\tUnder_N");
		
		int GENE_NAME = 2;
		int REGULATION = 3;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (tokens[REGULATION].equals("over")) {
				if (overList.containsKey(tokens[GENE_NAME])) {
					overList.put(tokens[GENE_NAME], overList.get(tokens[GENE_NAME]) + 1);
				} else {
					overList.put(tokens[GENE_NAME], 1);
				}
			} else if (tokens[REGULATION].equals("under")) {
				if (underList.containsKey(tokens[GENE_NAME])) {
					underList.put(tokens[GENE_NAME], underList.get(tokens[GENE_NAME]) + 1);
				} else {
					underList.put(tokens[GENE_NAME], 1);
				}
			} else if (tokens[REGULATION].equals("normal")) {
				if (normList.containsKey(tokens[GENE_NAME])) {
					normList.put(tokens[GENE_NAME], normList.get(tokens[GENE_NAME]) + 1);
				} else {
					normList.put(tokens[GENE_NAME], 1);
				}
			} else {
				System.out.println(line);
			}
		}
		
		System.out.println("Over expressed: "+ overList.size());
		System.out.println("Normal: " + normList.size());
		System.out.println("Under expressed: " + underList.size());
		
		for (String geneName : overList.keySet()) {
			fm.write(geneName + "\t" + overList.get(geneName));
			if (normList.containsKey(geneName)) {
				fm.write("\t" + normList.get(geneName));
				normList.remove(geneName);
			} else {
				fm.write("\t0");
			}
			if (underList.containsKey(geneName)) {
				fm.write("\t" + underList.get(geneName));
				underList.remove(geneName);
			} else {
				fm.write("\t0");
			}
			fm.writeLine();
		}
		
		for (String geneName : normList.keySet()) {
			fm.write(geneName + "\t0\t" + normList.get(geneName));
			if (underList.containsKey(geneName)) {
				fm.write("\t" + underList.get(geneName));
				underList.remove(geneName);
			} else {
				fm.write("\t0");
			}
			fm.writeLine();
		}
		for (String geneName : underList.keySet()) {
			fm.writeLine(geneName + "\t0\t0\t" + underList.get(geneName));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("usage: java -jar cosmicSummaryCompleteGeneExp.jar <CosmicCompleteGeneExpression.tsv>");
		System.out.println("\t<out>: <date>_CosmicCompleteGeneExpression.summary");
		System.out.println("\t<out> Format: <GENE_NAME>\t<over: Num_Samples>\t<normal: Num_Samples>\t<under: Num_Samples>");
		System.out.println("Arang Rhie, 2014-11-24. arrhie@gmail.com");
		
	}

	public static void main(String[] args) throws ParseException {
		if (args.length == 1) {
			new SummaryCompleteGeneExp().go(args[0], IOUtil.getDate() + "_" + args[0].replace(".tsv", ".summary"));
		} else {
			new SummaryCompleteGeneExp().printHelp();
		}
	}

}
