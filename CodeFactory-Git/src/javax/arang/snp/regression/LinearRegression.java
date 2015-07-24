package javax.arang.snp.regression;

import java.util.Vector;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.ANNOVAR;

import flanagan.analysis.Regression;

public class LinearRegression extends I2Owrapper {
	
	static double p = 0.05;

	@Override
	public void hooker(FileReader phenotypeReader, FileReader fr2, FileMaker fm) {
		Vector<Double> phenotypes = new Vector<Double>();
		while (phenotypeReader.hasMoreLines()) {
			String line = phenotypeReader.readLine();
			if (line.equals(""))	continue;
			phenotypes.add(Double.parseDouble(line));
		}
		
		Vector<Double> phenos = new Vector<Double>();
		
		fm.writeLine(fr2.readLine());
		int totalVars = 0;
		Vector<Double> genotypes = new Vector<Double>();
		READ_LINE : while (fr2.hasMoreLines()) {
			String line = fr2.readLine();
			String[] tokens = line.split("\t");
			String toWrite = tokens[ANNOVAR.CHR] + "\t"
				+ tokens[ANNOVAR.POS_FROM] + "\t"
				+ tokens[ANNOVAR.POS_TO] + "\t"
				+ tokens[ANNOVAR.REF] + "\t" + tokens[ANNOVAR.ALT];
			genotypes.clear();
			phenos.clear();
			for (int i = ANNOVAR.NOTE; i < ANNOVAR.NOTE + phenotypes.size(); i++) {
				if (!tokens[i].startsWith("NA")) {
					try {
						genotypes.add(Double.parseDouble(tokens[i]));
						phenos.add(phenotypes.get(i - ANNOVAR.NOTE));
					} catch (NumberFormatException e) {
						System.out.println("Exception occured in \n" + line);
						System.out.println(i + "\t" + tokens[i]);
						System.out.println(ANNOVAR.NOTE + "\t" + tokens[ANNOVAR.NOTE]);
						System.out.println((ANNOVAR.NOTE + phenotypes.size() - 1) + "\t" + tokens[ANNOVAR.NOTE + phenotypes.size() - 1]);
						throw e;
					}
				}
				toWrite = toWrite + "\t" + tokens[i];
			}
			
			/***
			 * Write the remaining annotated notes
			 */
			for (int i = ANNOVAR.NOTE + phenotypes.size(); i < tokens.length; i++) {
				toWrite = toWrite + "\t" + tokens[i];
			}
			
 			double[] xData = new double[genotypes.size()];
 			double[] yData = new double[phenos.size()];
			for (int i = 0; i < xData.length; i++) {
				xData[i] = genotypes.get(i);
				yData[i] = phenos.get(i);
//				System.out.println(tokens[ANNOVAR.POS_FROM] + "\t" + xData[i] + "\t" + yData[i]);
			}
			
			Regression reg = new Regression(xData, yData);
			try {
				reg.linear();
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage() + " at " + line);
				continue READ_LINE;
			}
			double[] estimates = reg.getBestEstimates();
			double[] pvalues = reg.getPvalues();
			for (int i = 0; i < estimates.length; i++) {
				if (pvalues[i] > p)	{
					continue READ_LINE; 
				}
				toWrite = toWrite + "\t" + estimates[i] + "\t" + pvalues[i]; 
			}
			totalVars++;
			fm.writeLine(toWrite + "\t" + reg.getCoefficientOfDetermination());
			genotypes.clear();
		}
		
		System.out.println("Total variables w/ p < " + p + "\t" + totalVars);
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar snpLinearRegression.jar <pheno.list> <in.snp> [p-value]");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new LinearRegression().go(args[0], args[1], args[1] + ".linear." + p);
		} else if (args.length == 3) {
			p = Double.parseDouble(args[2]);
			new LinearRegression().go(args[0], args[1], args[1] + ".linear." + args[2]);
		} else {
			new LinearRegression().printHelp();
		}
	}

}
