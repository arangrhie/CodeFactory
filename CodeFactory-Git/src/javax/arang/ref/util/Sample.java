package javax.arang.ref.util;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Sample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Sample sample = new Sample();
//		sample.writeNLines("C:/Documents and Settings/�ƶ�/���� ȭ��/AK1_sample/s_8_1_sequence/s_8_1_sequence.txt",
//				"C:/Documents and Settings/�ƶ�/���� ȭ��/AK1_sample/s_8_1_sequence",
//				"s_8_1_sequence_sample_100.txt",
//				100);
//		sample.writePerChr("C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/files_1st_KM2_TxnSNPs.txt",
//				"C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75");
//		for (int n = 1; n <= 22; n++) {
//			sample.compareTwoFiles("C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/FX_chr" + n + ".snp.anv.in",
//					"C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/ys_chr" + n + ".txt", n);
//		}
//		sample.compareTwoFiles("C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/FX_chrX.snp.anv.in",
//				"C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/ys_chrX.txt", 23);
//		sample.compareTwoFiles("C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/FX_chrY.snp.anv.in",
//				"C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/ys_chrY.txt", 24);
//		sample.compareTwoFiles("C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/FX_chrM.snp.anv.in",
//				"C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/ys_chrM.txt", 25);
	
		if (args.length > 0) {
			sample.compareAnvFiles(args[0], args[1], args[2], args[3]);
		} else {
			sample.compareAnvFiles("C:/Documents and Settings/�ƶ�/���� ȭ��/3Platform/150m17", "150_M17.solexa.snp", "150_M17.ion.snp", "20");
		}
	}
	
	public void compareAnvFiles(String dir, String inFile1, String inFile2, String chrNum) {
		FileReader file1 = new FileReader(dir + "/" + inFile1);
		FileReader file2 = new FileReader(dir + "/" + inFile2);
		FileMaker commFm = new FileMaker(dir, "comm_chr" + chrNum + ".txt");
		FileMaker file1Fm = new FileMaker(dir, inFile1 + "_only_chr" + chrNum + ".txt");
		FileMaker file2Fm = new FileMaker(dir, inFile2 + "_only_chr" + chrNum + ".txt");
		int onlyInFile1 = 0;
		int onlyInFile2 = 0;
		int commonInBothFiles = 0;
		int genoPos1 = readNextPos(file1, true);
		int genoPos2 = readNextPos(file2, false);
		//		while (file1.hasMoreLines() && file2.hasMoreLines()) {
		while (genoPos1 > 0 && genoPos2 > 0) {
			if (genoPos1 == genoPos2) {
				commonInBothFiles++;
				commFm.writeLine(line1 + "\t" + line2);
				genoPos1 = readNextPos(file1, true);
				genoPos2 = readNextPos(file2, false);
			}
			else if (genoPos1 < genoPos2) {
				onlyInFile1++;
				file1Fm.writeLine(line1);
				genoPos1 = readNextPos(file1, true);
			}
			else if (genoPos1 > genoPos2) {
				onlyInFile2++;
				file2Fm.writeLine(line2);
				genoPos2 = readNextPos(file2, false);
			}
		}

		while (file1.hasMoreLines()) {
			onlyInFile1++;
			file1Fm.writeLine(line1);
			genoPos1 = readNextPos(file1, true);
		}

		while (file2.hasMoreLines()) {
			onlyInFile2++;
			file2Fm.writeLine(line2);
			genoPos2 = readNextPos(file2, false);
		}

		System.out.println("chr" + chrNum);
		System.out.println(inFile1 + " only: " + onlyInFile1);
		System.out.println(inFile2 + " only: " + onlyInFile2);
		System.out.println("Common: " + commonInBothFiles);
		System.out.println();
		file1.closeReader();
		file2.closeReader();
		file1Fm.closeMaker();
		file2Fm.closeMaker();
	}

	public void writeNLines(String fileIn, String fileOutDir, String fileOut, int n) {
		FileReader fr = new FileReader(fileIn);
		FileMaker fm = new FileMaker(fileOutDir, fileOut);
		for (int i = 0; i < n; i++) {
			fm.writeLine(fr.readLine().toString());
		}
		fr.closeReader();
		fm.closeMaker();
	}

	public void writePerChr(String fileIn, String fileOutDir) {
		String chr = "0";
		FileReader fr = new FileReader(fileIn);
		String line;
		FileMaker fm = null;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			/***
			 * chr22	GMISNP	SNP	14435171	14435171	.	+	.	ID=rs12628452; status=dbSNP; ref=G; allele=A/G; allele_pct=56.25; allele_count=9; type=Heterozygote
			 * chr22	GMISNP	SNP	14435942	14435942	.	+	.	ID=rs72613661; status=dbSNP; ref=C; allele=T; allele_pct=100.00; allele_count=14; type=Homozygote
			 * chr22	GMISNP	SNP	14436126	14436126	.	+	.	ID=GMISNP02381644; status=novel; ref=G; allele=A/G; allele_pct=36.36; allele_count=8; type=Heterozygote
			 */
			if (line.startsWith("#") || line.equals("")) {
				continue;
			}
			//			System.out.println(line);
			StringTokenizer st = new StringTokenizer(line);
			String chrom = st.nextToken();
			if (!chrom.equals(chr)) {
				if (fm != null) {
					fm.closeMaker();
				}
				fm = new FileMaker(fileOutDir, "ys_" + chrom + ".txt");
				chr = chrom;
			}
			fm.writeLine(line);
//			fm.write(chrom + "\t");	// chr
//			st.nextToken();	// GMISNP
//			st.nextToken();	// SNP
//			fm.write(st.nextToken() + "\t");	// start
//			fm.write(st.nextToken() + "\t");	// end
//			st.nextToken();	// .
//			st.nextToken();	// +
//			st.nextToken();	// .
//			//			String anno = st.nextToken();
//			//			System.out.println(anno);
//			StringTokenizer stt = new StringTokenizer(st.nextToken(), "=;");
//			stt.nextToken();	// ID
//			fm.write(stt.nextToken() + "\t");
//			stt = new StringTokenizer(st.nextToken(), "=;");
//			stt.nextToken();	// status
//			fm.write(stt.nextToken() + "\t");
//			stt = new StringTokenizer(st.nextToken(), "=;");
//			stt.nextToken();	// ref
//			fm.write(stt.nextToken() + "\t");
//			stt = new StringTokenizer(st.nextToken(), "=;");
//			stt.nextToken();	// allele
//			fm.write(stt.nextToken() + "\t");
//			stt = new StringTokenizer(st.nextToken(), "=;");
//			stt.nextToken();	// allele_pct
//			fm.write(stt.nextToken() + "\t");
//			stt = new StringTokenizer(st.nextToken(), "=;");
//			stt.nextToken();	// allele_cnt
//			fm.write(stt.nextToken() + "\t");
//			stt = new StringTokenizer(st.nextToken(), "=;");
//			stt.nextToken();	// type
//			fm.writeLine(stt.nextToken());
		}
		if (fm != null) {
			fm.closeMaker();
		}
		System.out.println("Done!! :D");
	}

	String line1;
	String line2;
	
	public void compareTwoFiles(String inFile1, String inFile2, int n) {
		FileReader file1 = new FileReader(inFile1);
		FileReader file2 = new FileReader(inFile2);
		FileMaker commFm = new FileMaker("C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/", "comm_chr" + n + ".txt");
		FileMaker file1Fm = new FileMaker("C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/", "FX_only_chr" + n + ".txt");
		FileMaker file2Fm = new FileMaker("C:/Documents and Settings/�ƶ�/���� ȭ��/FX/km2_75/", "ys_only_chr" + n + ".txt");
		int onlyInFile1 = 0;
		int onlyInFile2 = 0;
		int commonInBothFiles = 0;
		int genoPos1 = readNextPos(file1, true);
		int genoPos2 = readNextPos(file2, false);
		//		while (file1.hasMoreLines() && file2.hasMoreLines()) {
		while (genoPos1 > 0 && genoPos2 > 0) {
			if (genoPos1 == genoPos2) {
				commonInBothFiles++;
				commFm.writeLine(line1 + "\t" + line2);
				genoPos1 = readNextPos(file1, true);
				genoPos2 = readNextPos(file2, false);
			}
			else if (genoPos1 < genoPos2) {
				onlyInFile1++;
				file1Fm.writeLine(line1);
				genoPos1 = readNextPos(file1, true);
			}
			else if (genoPos1 > genoPos2) {
				onlyInFile2++;
				file2Fm.writeLine(line2);
				genoPos2 = readNextPos(file2, false);
			}
		}

		while (file1.hasMoreLines()) {
			onlyInFile1++;
			file1Fm.writeLine(line1);
			genoPos1 = readNextPos(file1, true);
		}

		while (file2.hasMoreLines()) {
			onlyInFile2++;
			file2Fm.writeLine(line2);
			genoPos2 = readNextPos(file2, false);
		}

		System.out.println("chr" + n);
		System.out.println("FX only: " + onlyInFile1);
		System.out.println("ys only: " + onlyInFile2);
		System.out.println("Common: " + commonInBothFiles);
		System.out.println();
		file1.closeReader();
		file2.closeReader();
		file1Fm.closeMaker();
		file2Fm.closeMaker();
	}

	private int readNextPos(FileReader file, boolean isFile1) {
		String line = file.readLine().toString();
		if (isFile1) {
			line1 = line;
		} else {
			line2 = line;
		}
		try {
			StringTokenizer st1 = new StringTokenizer(line);
			st1.nextToken();	// chr
			return Integer.parseInt(st1.nextToken());
		} catch (NoSuchElementException e) {
			return -1;
		}
	}

}
