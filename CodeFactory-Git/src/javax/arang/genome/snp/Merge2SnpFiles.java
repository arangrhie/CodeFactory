package javax.arang.genome.snp;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.annovar.ANNOVAR;

public class Merge2SnpFiles extends I2Owrapper {

	public static final short HOM = 2;
	public static final short HET = 1;
	public static final short NORM = 0;
	
	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		// TODO Auto-generated method stub

		fm.writeLine("chr\tpos\tpos\tref\tsnp\t"
						+ fr1.getFileName().substring(0, fr2.getFileName().indexOf("_")) + "\t"
						+ fr2.getFileName().substring(0, fr2.getFileName().indexOf("_")));
		
		String line1;
		String line2;
		String[] tokens1;
		String[] tokens2;
		int pos1 = 0;
		int pos2 = 0;

		line1 = fr1.readLine();
		while (line1.startsWith("#")) {
			line1 = fr1.readLine();
		}
		tokens1 = line1.split("\t");
		
		try {
			pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
		} catch (NumberFormatException e) {
			System.out.println("Number format exception at " + tokens1[ANNOVAR.POS_FROM] + ", writing as note");
			line1 = fr1.readLine();
			pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
		}
		
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
				writeRaw(fm, tokens1[ANNOVAR.CHR], pos1,
						tokens1[ANNOVAR.REF], tokens1[ANNOVAR.ALT], tokens1[ANNOVAR.ALT],
						getSnpType(tokens1[ANNOVAR.NOTE]), NORM);
				if (!fr1.hasMoreLines()) {
					fr1Finished = true;
					break READ_FR1;
				}
				line1 = fr1.readLine();
				tokens1 = line1.split("\t");
				pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
			}
			while (pos1 > pos2) {
				writeRaw(fm, tokens2[ANNOVAR.CHR], pos2,
						tokens2[ANNOVAR.REF], tokens2[ANNOVAR.ALT], tokens2[ANNOVAR.ALT],
						NORM, getSnpType(tokens2[ANNOVAR.NOTE]));
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
					writeRaw(fm, tokens1[ANNOVAR.CHR], pos1,
							tokens1[ANNOVAR.REF], tokens1[ANNOVAR.ALT], tokens2[ANNOVAR.ALT],
							getSnpType(tokens1[ANNOVAR.NOTE]), getSnpType(tokens2[ANNOVAR.NOTE]));
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
					
					if (!fr2.hasMoreLines()) {
						fr2Finished = true;
						break READ_FR1;
					}
					line2 = fr2.readLine();
					tokens2 = line2.split("\t");
					pos2 = Integer.parseInt(tokens2[ANNOVAR.POS_FROM]);
				} else {
					// snpAllele1 기준으로 먼저 작성
					writeRaw(fm, tokens1[ANNOVAR.CHR], pos1,
							tokens1[ANNOVAR.REF], tokens1[ANNOVAR.ALT], tokens1[ANNOVAR.ALT],
							getSnpType(tokens1[ANNOVAR.NOTE]), NORM);
					if (!fr1.hasMoreLines()) {
						fr1Finished = true;
						break READ_FR1;
					}
					line1 = fr1.readLine();
					tokens1 = line1.split("\t");
					pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
					continue LOOP2;
				}
			}
		}
		
		if (!fr1Finished) {
			writeRaw(fm, tokens1[ANNOVAR.CHR], pos1,
					tokens1[ANNOVAR.REF], tokens1[ANNOVAR.ALT], tokens1[ANNOVAR.ALT],
					getSnpType(tokens1[ANNOVAR.NOTE]), NORM);
	
			while (fr1.hasMoreLines()) {
				line1 = fr1.readLine();
				tokens1 = line1.split("\t");
				pos1 = Integer.parseInt(tokens1[ANNOVAR.POS_FROM]);
				writeRaw(fm, tokens1[ANNOVAR.CHR], pos1,
						tokens1[ANNOVAR.REF], tokens1[ANNOVAR.ALT], tokens1[ANNOVAR.ALT],
						getSnpType(tokens1[ANNOVAR.NOTE]), NORM);
			}
		} else if (!fr2Finished) {
			writeRaw(fm, tokens2[ANNOVAR.CHR], pos2,
					tokens2[ANNOVAR.REF], tokens2[ANNOVAR.ALT], tokens2[ANNOVAR.ALT],
					NORM, getSnpType(tokens2[ANNOVAR.NOTE]));
			
			while (fr2.hasMoreLines()) {
				line2 = fr2.readLine();
				tokens2 = line2.split("\t");
				pos2 = Integer.parseInt(tokens2[ANNOVAR.POS_FROM]);
				writeRaw(fm, tokens2[ANNOVAR.CHR], pos2,
						tokens2[ANNOVAR.REF], tokens2[ANNOVAR.ALT], tokens2[ANNOVAR.ALT],
						NORM, getSnpType(tokens2[ANNOVAR.NOTE]));
			}
		}
	}
	
	public static void writeRaw(FileMaker fm, String chr, int pos,
			String ref, String snpAllele1, String snpAllele2, int snpType1, int snpType2) {
		fm.writeLine(chr + "\t" + pos + "\t" + pos
				+ "\t" + ref + "\t" + snpAllele1 + "\t" + snpType1 + "\t" + snpType2);
	}
	
	public static void writeRaw(FileMaker fm, String chr, int pos,
			String ref, String snpAllele1, String snpAllele2, String otherCells, int snpType2) {
		fm.writeLine(chr + "\t" + pos + "\t" + pos
				+ "\t" + ref + "\t" + snpAllele1 + otherCells + "\t" + snpType2);
	}
	
	public static int getSnpType(String snpType) {
		if (snpType.equals("Hom")) {
			return HOM;
		} else if (snpType.equals("Het")) {
			return HET;
		} else {
			return NORM;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar merge2snpFiles.jar <sample1_file.snp> <sample2_file.snp> <outFile>");
		System.out.println("\tinFile1: snp list 1 in ANNOVAR format");
		System.out.println("\tinFile2: snp list 2 in ANNOVAR format");
		System.out.println("\t\t*Input sample names are considered from 0 to first _ substring.*");
		System.out.println("\toutFile: merged snp list of 1 and 2, with a 0/1/2 matrix");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new Merge2SnpFiles().go(args[0], args[1], args[2]);
		} else {
			new Merge2SnpFiles().printHelp();
		}
	}

}
