package javax.arang.genome.base;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.INOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.ANNOVAR;

public class MarkNotCovered extends INOwrapper {

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar ~/codes/baseMarkNotCovered.jar <chrN.annotated.snp> <*.chrN.bas> <out>");
		System.out.println("\tMark on each position with \"0\" as NA if base coverage is not sufficient");
		System.out.println("\t<.annotated>: 1st line -> tab tab sample_name");
		System.out.println("\t\t2nd line -> phenotype (copied as it is)");
		System.out.println("\t\t3rd line~ -> annovar type. 0/1/2.");
	}

	@Override
	public void hooker(ArrayList<FileReader> frs, FileMaker fm) {
		FileReader annotated = frs.get(0);
		
		HashMap<String, FileReader> sampleMap = new HashMap<String, FileReader>();
		for (int i = 1; i < frs.size(); i++) {
			FileReader sampleFr = frs.get(i);
			sampleMap.put(sampleFr.getFileName().substring(0, 6), sampleFr);
		}
		
		ArrayList<String> sampleList = new ArrayList<String>();
		String line = annotated.readLine();
		fm.writeLine(line);
		String[] tokens = line.split("\t");
		for (int i = ANNOVAR.NOTE; i < tokens.length; i++) {
			if (tokens[i].length() > 0) {
				sampleList.add(tokens[i]);
			}
		}
		
		int lineNum = 0;
		int dropped = 0;
		
		READ_LINE : while (annotated.hasMoreLines()) {
			line = annotated.readLine();
			if (!line.startsWith("chr")) {
				fm.writeLine(line);		
				continue;
			}
			lineNum++;
			tokens = line.split("\t");
			System.out.println("Processing line " + lineNum + ":\t" + tokens[ANNOVAR.CHR] + "\t" + tokens[ANNOVAR.POS_FROM]);
			String toWrite = tokens[ANNOVAR.CHR] + "\t"
				+ tokens[ANNOVAR.POS_FROM] + "\t" + tokens[ANNOVAR.POS_TO] + "\t"
				+ tokens[ANNOVAR.REF] + "\t" + tokens[ANNOVAR.ALT];
			int countNA = 0;
			for (int i = ANNOVAR.NOTE; i < ANNOVAR.NOTE + sampleMap.size(); i++) {
				if (tokens[i].equals("0")) {
					// go and read base coverage
					int cov = getCoverage(tokens[ANNOVAR.CHR], tokens[ANNOVAR.POS_FROM], sampleMap.get(sampleList.get(i - ANNOVAR.NOTE)));
					if (cov < 5)	{
						tokens[i] = "NA";
						countNA++;
						if (countNA > 2) {
							// remove this line
							dropped++;
							continue READ_LINE;
						}
					}
				}
				toWrite = toWrite + "\t" + tokens[i];
			}
			
			fm.writeLine(toWrite);
		}
		
		System.out.println("Dropped w/ NA\t" + dropped);
		
	}

	private int getCoverage(String chr, String pos, FileReader fr) {
		String line = fr.getLastLine();
		int position = Integer.parseInt(pos);
		if (!line.equals("")) {
			String[] tokens = line.split("\t");
			if (Integer.parseInt(tokens[ANNOVAR.POS_FROM]) == position) {
				return Integer.parseInt(tokens[8]);
			}
		}
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			String[] tokens = line.split("\t");
			if (Integer.parseInt(tokens[ANNOVAR.POS_FROM]) < position) {
				continue;
			} else if (Integer.parseInt(tokens[ANNOVAR.POS_FROM]) == position) {
				return Integer.parseInt(tokens[8]);
			} else {
				break;
			}
		}
		return -1;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			new MarkNotCovered().printHelp();
		} else {
			new MarkNotCovered().go(args);
		}
	}

}
