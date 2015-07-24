package javax.arang.genome.snp;

import java.util.StringTokenizer;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.ANNOVAR;

public class AddSnpFile extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		int paddLength = 0;
		if (fr1.hasMoreLines()) {
			String line = fr1.readLine();
			while (line.startsWith("#"))	{
				line = fr1.readLine();
			}
			fm.writeLine(line + "\t" + fr2.getFileName().substring(0, fr2.getFileName().indexOf("_")));
			StringTokenizer st = new StringTokenizer(line);
			st.nextToken();	// chr
			st.nextToken();	// pos
			st.nextToken();	// pos
			st.nextToken();	// refAllele
			st.nextToken();	// snpAllele
			while (st.hasMoreTokens()) {
				paddLength++;
				st.nextElement();
			}
		}
		System.out.println("Merging " + fr1.getFileName() + " and " + fr2.getFileName() + " ...");
		
		String line1;
		String line2;
		String[] tokens1;
		String[] tokens2;
		int pos1 = 0;
		int pos2 = 0;
		String others = "";
		
		// make fr1 ready
		line1 = fr1.readLine();
		while (line1.startsWith("#")) {
			line1 = fr1.readLine();
		}
		tokens1 = line1.split("\t");
		for (int i = ANNOVAR.NOTE; i < tokens1.length; i++) {
			others = others + "\t" + tokens1[i];
		}
		
		try {
			pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
		} catch (NumberFormatException e) {
			System.out.println("Number format exception at " + tokens1[ANNOVAR.POS_FROM] + ", writing as note");
			line1 = fr1.readLine();
			pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
		}
		
		// make fr2 ready
		line2 = fr2.readLine();
		while (line2.startsWith("#")) {
			line2 = fr2.readLine();
		}
		tokens2 = line2.split("\t");
		
		try {
			pos2 = Integer.parseInt(tokens2[ANNOVAR.POS_FROM]);
		} catch (NumberFormatException e) {
			System.out.println("Number format exception at " + tokens2[ANNOVAR.POS_FROM] + ", writing as note");
			line2 = fr2.readLine();
			tokens2 = line2.split("\t");
			pos2 = Integer.parseInt(tokens2[ANNOVAR.POS_FROM]);
		}
		
		boolean fr1Finished = false;
		boolean fr2Finished = false;
		READ_FR1 : while (true) {
			while (pos1 < pos2) {
				Merge2SnpFiles.writeRaw(fm, tokens1[ANNOVAR.CHR], pos1,
						tokens1[ANNOVAR.REF], tokens1[ANNOVAR.ALT], tokens1[ANNOVAR.ALT],
						others, Merge2SnpFiles.NORM);
				if (!fr1.hasMoreLines()) {
					fr1Finished = true;
					break READ_FR1;
				}
				line1 = fr1.readLine();
				tokens1 = line1.split("\t");
				pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
				others = "";
				for (int i = ANNOVAR.NOTE; i < tokens1.length; i++) {
					others = others + "\t" + tokens1[i];
				}
			}
			while (pos1 > pos2) {
				Merge2SnpFiles.writeRaw(fm, tokens2[ANNOVAR.CHR], pos2,
						tokens2[ANNOVAR.REF], tokens2[ANNOVAR.ALT], tokens2[ANNOVAR.ALT],
						getPaddedOthers(paddLength), Merge2SnpFiles.getSnpType(tokens2[ANNOVAR.NOTE]));
				if (!fr2.hasMoreLines()) {
					fr2Finished = true;
					break READ_FR1;
				}
				line2 = fr2.readLine();
				tokens2 = line2.split("\t");
				pos2 = Integer.parseInt(tokens2[ANNOVAR.POS_FROM]);
			}
			LOOP2 : while (pos1 == pos2) {
				if (tokens1[ANNOVAR.ALT].equals(tokens2[ANNOVAR.ALT])) {
					Merge2SnpFiles.writeRaw(fm, tokens1[ANNOVAR.CHR], pos1,
							tokens1[ANNOVAR.REF], tokens1[ANNOVAR.ALT], tokens1[ANNOVAR.ALT],
							others, Merge2SnpFiles.getSnpType(tokens2[ANNOVAR.NOTE]));
					if (!fr1.hasMoreLines()) {
						fr1Finished = true;
						if (!fr2.hasMoreLines()) {
							fr2Finished = true;
						}
						break READ_FR1;
					}
					line1 = fr1.readLine();
					tokens1 = line1.split("\t");
					pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
					others = "";
					for (int i = ANNOVAR.NOTE; i < tokens1.length; i++) {
						others = others + "\t" + tokens1[i];
					}
					if (!fr2.hasMoreLines()) {
						fr2Finished = true;
						break READ_FR1;
					}
					line2 = fr2.readLine();
					tokens2 = line2.split("\t");
					pos2 = Integer.parseInt(tokens2[ANNOVAR.POS_FROM]);
				} else {
					Merge2SnpFiles.writeRaw(fm, tokens1[ANNOVAR.CHR], pos1,
							tokens1[ANNOVAR.REF], tokens1[ANNOVAR.ALT], tokens1[ANNOVAR.ALT],
							others, Merge2SnpFiles.NORM);
					if (!fr1.hasMoreLines()) {
						fr1Finished = true;
						break READ_FR1;
					}
					line1 = fr1.readLine();
					tokens1 = line1.split("\t");
					pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
					others = "";
					for (int i = ANNOVAR.NOTE; i < tokens1.length; i++) {
						others = others + "\t" + tokens1[i];
					}
					continue LOOP2;
				}
			} 
		}
		
		if (!fr1Finished) {
			Merge2SnpFiles.writeRaw(fm, tokens1[ANNOVAR.CHR], pos1,
					tokens1[ANNOVAR.REF], tokens1[ANNOVAR.ALT], tokens1[ANNOVAR.ALT],
					others, Merge2SnpFiles.NORM);
			while (fr1.hasMoreLines()) {
				line1 = fr1.readLine();
				tokens1 = line1.split("\t");
				pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
				others = "";
				for (int i = ANNOVAR.NOTE; i < tokens1.length; i++) {
					others = others + "\t" + tokens1[i];
				}
				Merge2SnpFiles.writeRaw(fm, tokens1[ANNOVAR.CHR], pos1,
						tokens1[ANNOVAR.REF], tokens1[ANNOVAR.ALT], tokens1[ANNOVAR.ALT],
						others, Merge2SnpFiles.NORM);
			}
		} else if (!fr2Finished){
			Merge2SnpFiles.writeRaw(fm, tokens2[ANNOVAR.CHR], pos2,
					tokens2[ANNOVAR.REF], tokens2[ANNOVAR.ALT], tokens2[ANNOVAR.ALT],
					getPaddedOthers(paddLength), Merge2SnpFiles.getSnpType(tokens2[ANNOVAR.NOTE]));
			while (fr2.hasMoreLines()) {
				line2 = fr2.readLine();
				tokens2 = line2.split("\t");
				pos2 = Integer.parseInt(tokens2[ANNOVAR.POS_FROM]);
				Merge2SnpFiles.writeRaw(fm, tokens2[ANNOVAR.CHR], pos2,
						tokens2[ANNOVAR.REF], tokens2[ANNOVAR.ALT], tokens2[ANNOVAR.ALT],
						getPaddedOthers(paddLength), Merge2SnpFiles.getSnpType(tokens2[ANNOVAR.NOTE]));
			}
		}
	}
	
	public String getPaddedOthers(int paddLen) {
		String line = "";
		for (int i = 0; i < paddLen; i++) {
			line = line + "\t" + Merge2SnpFiles.NORM;
		}
		return line;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar addSnpFiles.jar <snpTable> <sample_File> <outFile>");
		System.out.println("\tsnpTable: SNP table (1: het, 2: hom)");
		System.out.println("\tsample_File: snp file to add (Het, Hom)");
		System.out.println("\t\t*Input sample name is considered from 0 to first _ substring.*");
		System.out.println("\toutFile: out file (possibly next snp table to merge)");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			new AddSnpFile().printHelp();
		} else {
			new AddSnpFile().go(args[0], args[1], args[2]);
		}
	}

}
