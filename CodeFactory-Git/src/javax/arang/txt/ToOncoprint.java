package javax.arang.txt;

import java.util.ArrayList;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ToOncoprint extends IOwrapper {

	private static final String AMP = "AMP";
	private static final String GAIN = "GAIN";
	private static final String HETLOSS = "HETLOSS";
	private static final String HOMDEL = "HOMDEL";
	
	private static final float LV_AMP = 0.60f;
	private static final float LV_GAIN = 0.30f;
	private static final float LV_HETLOSS = -0.30f;
	private static final float LV_HOMDEL = -0.60f;
	
	private static final int GENE = 0;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		ArrayList<String> samples = new ArrayList<String>();
		
		line = fr.readLine();
		tokens = line.split("\t");
		for (String sample : tokens) {
			if (!sample.equals("")) {
				samples.add(sample);
			}
		}
		System.out.println("Number of samples: " + samples.size());
		
		fm.writeLine("Sample\tGene\tAlteration");
		String gene;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			gene = tokens[GENE];
			for (int i = 1; i < tokens.length; i++) {
				if (tokens[i].equals(""))	continue;
				fm.writeLine(samples.get(i - 1) + "\t" + gene + "\t" + getCnvLevel(tokens[i]));
			}
		}
	}

	private String getCnvLevel(String string) {
		float level = Float.parseFloat(string);
		if (level == 0)	return "";
		if (level < LV_HOMDEL)	return HOMDEL;
		if (level < LV_HETLOSS)	return HETLOSS;
		if (level > LV_AMP)		return AMP;
		if (level > LV_GAIN)	return GAIN;
		else return "";
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar txtToOncoprint.jar <in>");
		System.out.println("\tMake an input file for oncoprint at cbioportal");
		System.out.println("\t<in>: tab delimited, first line with samples, rows with cnv level starting with gene (or loci)");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ToOncoprint().go(args[0], args[0] + ".onco");
		} else {
			new ToOncoprint().printHelp();
		}
	}

}
