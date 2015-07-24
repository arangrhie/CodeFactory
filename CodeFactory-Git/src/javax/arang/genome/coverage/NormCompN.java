package javax.arang.genome.coverage;

import java.util.ArrayList;
import java.util.Vector;

import javax.arang.IO.INwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class NormCompN extends INwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar coverageNormCompN.jar <x_list> <sample1.cov> <sample2.cov> ... <sampleN.cov>");
		System.out.println("\t<output>: normalized sequencing depth level on each sample as <sampleN.cov.norm>");
		System.out.println("\t\tCalculates the mean value of total samples, and compare it against each sample.");
		System.out.println("\t\tr = x * (b(si) + C) / (b(mi) + C)");
		System.out.println("\tCalculate x before beginning.\n" +
				"\t\tx = (Total # of aligned bases of the control (or medium of all samples)" +
							" + C * target region)" +
							" / (Total # of aligned bases of each sample + C * target region)");
		System.out.println("\t<x_list>: list of x in format <sample name>\t<x>\t<C>\t<add_to_normalize_model:TRUE/FALSE>");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs) {
		FileReader xReader = frs.get(0);
		Vector<String> sampleList = new Vector<String>();
		Vector<Float> xList = new Vector<Float>();
		Vector<Integer> cList = new Vector<Integer>();
		Vector<Boolean> isModelList = new Vector<Boolean>();
		
		while (xReader.hasMoreLines()) {
			String line = xReader.readLine();
			if (line.equals(""))	continue;
			String[] tokens = line.split("\t");
			if (tokens.length != 4)	{
				System.out.println("Wrong input file " + xReader.getFileName() + ". read the help.");
				this.printHelp();
				throw new RuntimeException();
			}
			sampleList.add(tokens[0]);
			xList.add(Float.parseFloat(tokens[1]));
			cList.add(Integer.parseInt(tokens[2]));
			if (Boolean.parseBoolean(tokens[3]))	isModelList.add(true);
			else	isModelList.add(false);
		}
		
		ArrayList<FileMaker> fms = new ArrayList<FileMaker>();
		FileMaker avgFm = new FileMaker(".", "avg.cov.norm");
		for (int i = 0; i < sampleList.size(); i++) {
			FileMaker fm = new FileMaker(this.getPath(frs.get(i+1).getFullPath()), sampleList.get(i) + ".cov.norm");
			fms.add(fm);
		}
		
		Vector<Integer> depthOnPos = new Vector<Integer>();
		while (frs.get(1).hasMoreLines()) {
			// on each position, calc mean
			depthOnPos.clear();
			String pos = "0";
			int depthSum = 0;
			for (int i = 1; i < frs.size(); i++) {
				String line = frs.get(i).readLine();
				String[] tokens = line.split("\t");
				pos = tokens[0];
				int depth = Integer.parseInt(tokens[1]) + cList.get(i-1);
				depthOnPos.add(depth);
				if (isModelList.get(i - 1)) {
					depthSum += depth;
				}
			}
			float avgDepth = ((float)depthSum / sampleList.size());
			avgFm.writeLine(pos + "\t" + String.format("%,.2f", avgDepth));

			for (int i = 0; i < sampleList.size(); i++) {
				int depth = depthOnPos.get(i);
				float sampleNormDepth = (float) depth * xList.get(i);
				fms.get(i).writeLine(pos + "\t" + String.format("%,.2f", avgDepth) + "\t" 
						+ String.format("%,.2f", sampleNormDepth) + "\t"
						+ String.format("%,.2f", (sampleNormDepth / avgDepth)));
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 2) {
			new NormCompN().go(args);
		} else {
			new NormCompN().printHelp();
		}

	}

}
