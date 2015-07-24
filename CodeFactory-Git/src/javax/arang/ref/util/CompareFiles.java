package javax.arang.ref.util;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CompareFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CompareFiles cmp = new CompareFiles();

		String dir = "C:\\Documents and Settings\\�ƶ�\\���� ȭ��\\3Platform\\150m17";
		String inFile1 = "150m17_m20i0_unpaired_no_ecoli_bac.fasta.txt";
		String inFile2 = "150m17_m20i0_chr20.blast";
		
		if (args.length > 0) {
			dir = args[0];
			inFile1 = args[1];
			inFile2 = args[2];
		}
		
		cmp.makeTableFile(dir, inFile1.replace(".txt", ""));
		System.out.println("fasta converting done!");
		
		// cmp.compareFiles(dir, inFile1 + ".txt", inFile2);
		
		HashMap<String, String> unpairedList = cmp.getReadList(dir, inFile1);
		System.out.println(inFile1+ "(unpaired) contains : " + unpairedList.size() + " reads");
		int ecoliCounts = cmp.getCommonReads(unpairedList, dir, inFile2 );
		System.out.println(inFile2  + " contains : " + ecoliCounts + " reads in common with " + inFile1);
		
		// Filter out reads that are not aligned to hg chr20
		HashMap<String, String> ecoliAlignedList = cmp.getReadList(dir, inFile2);
		System.out.println("Human, chr20 aligned reads: " + ecoliAlignedList.size());
		int count = cmp.filterFasta(dir, inFile1.replace(".txt", ""), ecoliAlignedList);
		System.out.println("Total " + count + " reads not in Human, chr20 aligned");
	}
	
	
	public int filterFasta(String dir, String file, HashMap<String, String> list) {
		FileReader fr = new FileReader(dir + "/" + file);
		FileMaker fm = new FileMaker(dir, file.replace(".fasta", "_chr20.fasta"));
		boolean isFiltered = false;
		int filterCount = 0;
		while(fr.hasMoreLines()) {
			String line = fr.readLine().toString();
			if (line.startsWith(">")) {
				StringTokenizer st = new StringTokenizer(line);
				String id = st.nextToken();
				id = id.substring(id.indexOf(">") + 1);
				if (!list.containsKey(id)) {
					fm.writeLine(line);
					isFiltered = true;
					filterCount++;
				} else {
					isFiltered = false;
				}
			} else {
				if (isFiltered) {
					fm.writeLine(line);
				}
			}
		}
		
		fr.closeReader();
		fm.closeMaker();
		return filterCount;
	}
	
	public HashMap<String, String> getReadList(String dir, String file) {
		HashMap<String, String> list = new HashMap<String, String>();
		
		FileReader fr = new FileReader(dir + "/" + file);
		
		while(fr.hasMoreLines()) {
			String line = fr.readLine().toString();
			StringTokenizer st = new StringTokenizer(line);
			String id = st.nextToken();
			id = id.trim();
			list.put(id, id);
		}
		
		fr.closeReader();
		return list;
	}
	
	public int getCommonReads(HashMap<String, String> list, String dir, String file) {
		FileReader fr = new FileReader(dir + "/" + file);
		FileMaker fm = new FileMaker(dir, file + "_common.txt");
		HashMap<String, String> ecoliList = new HashMap<String, String>();
		while(fr.hasMoreLines()) {
			String line = fr.readLine().toString();
			StringTokenizer st = new StringTokenizer(line);
			String id = st.nextToken();
			id = id.trim();
			ecoliList.put(id, id);
			if (list.containsKey(id)) {
				fm.writeLine(id);
			}
		}
		System.out.println(file + " contains : " + ecoliList.size() + " reads");
		
		fr.closeReader();
		fm.closeMaker();
		return ecoliList.size();
	}
	
	public void makeTableFile(String dir, String file) {
		FileReader fr = new FileReader(dir + "/" + file);
		FileMaker fm = new FileMaker(dir, file + ".txt");
		StringBuffer newLine = new StringBuffer();
		while (fr.hasMoreLines()) {
			String ln = fr.readLine().toString();
			if (ln.startsWith(">")) {
				newLine.append(ln.substring(ln.indexOf(">") + 1) + "\t");
			} else {
				newLine.append(ln);
				fm.writeLine(newLine.toString());
				newLine.delete(0, newLine.length());
			}
		}
		fr.closeReader();
		fm.closeMaker();
	}

	String line1;
	String line2;
	
	public void compareFiles(String dir, String inFile1, String inFile2) {
		FileReader file1 = new FileReader(dir + "/" + inFile1);
		FileReader file2 = new FileReader(dir + "/" + inFile2);
		FileMaker commFm = new FileMaker(dir, "common" + ".txt");
		FileMaker file1Fm = new FileMaker(dir, inFile1 + "_only" + ".txt");
		FileMaker file2Fm = new FileMaker(dir, inFile2 + "_only" + ".txt");
		int onlyInFile1 = 0;
		int onlyInFile2 = 0;
		int commonInBothFiles = 0;
		float genoPos1 = readNextPos(file1, true);
		float genoPos2 = readNextPos(file2, false);
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

		System.out.println(inFile1 + " left");
		while (file1.hasMoreLines()) {
			onlyInFile1++;
			file1Fm.writeLine(line1);
			genoPos1 = readNextPos(file1, true);
		}

		System.out.println(inFile2 + " left");
		while (file2.hasMoreLines()) {
			onlyInFile2++;
			file2Fm.writeLine(line2);
			genoPos2 = readNextPos(file2, false);
		}

		System.out.println("DONE! :D");
		System.out.println(inFile1 + " only: " + onlyInFile1);
		System.out.println(inFile2 + " only: " + onlyInFile2);
		System.out.println("Common: " + commonInBothFiles);
		System.out.println();
		file1.closeReader();
		file2.closeReader();
		file1Fm.closeMaker();
		file2Fm.closeMaker();
	}

	
	private float readNextPos(FileReader file, boolean isFile1) {
		String line = file.readLine().toString();
		line = line.trim();
		if (isFile1) {
			line1 = line;
		} else {
			line2 = line;
		}

		String id = ""; 
		// System.out.println(line);
		try {
			StringTokenizer st1 = new StringTokenizer(line);
			id = st1.nextToken();	// read id
			String flowNum = id.substring(id.indexOf(":") + 1);
			flowNum = flowNum.replace(":", ".");
			return Float.parseFloat(flowNum);
		} catch (NoSuchElementException e) {
			return -1.0f;
		} catch (NumberFormatException e) {
			System.out.println(id);
			System.out.println(line);
			e.printStackTrace();
			return -1.0f;
		}
	}
}
