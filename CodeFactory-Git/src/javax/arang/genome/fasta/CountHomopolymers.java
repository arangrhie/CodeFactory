package javax.arang.genome.fasta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CountHomopolymers extends IOwrapper {

	StringBuffer homoStretch = new StringBuffer();
	StringBuffer homoStretchAllCase = new StringBuffer();
	char prevBase = ' ';
	char prevBaseToUpper = ' ';
	HashMap<Integer, Integer[]> homopolymerLenCountMap = new HashMap<Integer, Integer[]>();
	ArrayList<Integer> homopolymerLenList = new ArrayList<Integer>();

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = null;
		boolean isReading = false;
		boolean isStretchInsertedIntoMap = false;
		boolean isStretchInsertedIntoAllCaseMap = false;
		int faIdx = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			
			// fa name
			if (line.startsWith(">")) {
				if (isReading) {
					if (!isStretchInsertedIntoMap) {
						putIntoMap(homoStretch.length() + 1, false);
					} 
					if (!isStretchInsertedIntoAllCaseMap) {
						putIntoMap(homoStretchAllCase.length() + 1, true);
					}
				}
				isReading=false;
				prevBase = ' ';
				prevBaseToUpper = ' ';
				System.out.println("[DEBUG] :: " + line);
			} else {
				if (!isReading) {
					isReading = true;
					prevBase = line.charAt(0);
					prevBaseToUpper = Character.toUpperCase(line.charAt(0));
					faIdx = 1;
				} else {
					faIdx = 0;
					isReading=true;
				}
				
				for (; faIdx < line.length(); faIdx++) {
					if (prevBase != line.charAt(faIdx)) {
						// different base observed
						putIntoMap(homoStretch.length() + 1, false);
						
						// Initialize prevBase
						prevBase = line.charAt(faIdx);
						homoStretch = new StringBuffer(prevBase);
						isStretchInsertedIntoMap = true;
					} else {
						// same base observed : append homoStretch
						homoStretch.append(prevBase);
						isStretchInsertedIntoMap = false;
					}
					
					if (prevBaseToUpper != Character.toUpperCase(line.charAt(faIdx))) {
						// different base observed
						putIntoMap(homoStretchAllCase.length() + 1, true);
						
						// Initialize prevBase
						prevBaseToUpper = Character.toUpperCase(line.charAt(faIdx));
						homoStretchAllCase = new StringBuffer(prevBaseToUpper);
						isStretchInsertedIntoAllCaseMap = true;
					} else {
						// same base observed : append homoStretch
						homoStretchAllCase.append(prevBaseToUpper);
						isStretchInsertedIntoAllCaseMap = false;
					}
				}
			}
		}
		
		// Add homopolymerLenCountMap(homoStretchLen, count++) of the last prevBase
		if (!isStretchInsertedIntoMap) {
			putIntoMap(homoStretch.length() + 1, false);
		}
		if (!isStretchInsertedIntoAllCaseMap) {
			putIntoMap(homoStretchAllCase.length() + 1, true);
		}
		
		Integer[] homopolymerLenSortedArr = new Integer[homopolymerLenList.size()];
		Arrays.sort(homopolymerLenList.toArray(homopolymerLenSortedArr));
		// Write result table
		fm.writeLine("HomopolymerLen\tA\tC\tG\tT\tN\ta\tc\tg\tt\tn\tA_or_a\tC_or_c\tG_or_g\tT_or_t\tN_or_n");
		for (int i = 0; i < homopolymerLenSortedArr.length; i++) {
			fm.writeLine(homopolymerLenSortedArr[i] + arrayToString(homopolymerLenCountMap.get(homopolymerLenSortedArr[i])));
		}
	}

	private void putIntoMap(int homoStretchLen, boolean isAllCases) {
		Integer[] lenArr;
		char base = (isAllCases ? prevBaseToUpper : prevBase);
		int baseIdx = -1;
		try {
			baseIdx = getBaseIdx(base);
			// Add homopolymerLenCountMap(homoStretchLen, count++)
			if (homoStretchLen > 2) {
				if ((!homopolymerLenList.contains(homoStretchLen) && (isAllCases && getBaseIdx(prevBaseToUpper) < 12))
					|| (!homopolymerLenList.contains(homoStretchLen) && (!isAllCases && getBaseIdx(prevBase) < 12))) {
					homopolymerLenList.add(homoStretchLen);
					lenArr = new Integer[15];
					initialize(lenArr);
				} else {
					lenArr = homopolymerLenCountMap.get(homoStretchLen);
				}
				if (isAllCases) {
					//System.out.println("[DEBUG] :: " + base + " " + baseIdx);
					lenArr[10 + baseIdx]++;
					
				} else {
					lenArr[baseIdx]++;
				}
				homopolymerLenCountMap.put(homoStretchLen, lenArr);
			}
		} catch (IndexOutOfBoundsException e) {
			System.err.println("[DEBUG] :: IndexOutOfBoundsException - Non-ACGTN base " +  base + " ovserved.");
			e.printStackTrace();
			throw e;
		}
		
	}

	private String arrayToString(Integer[] integers) {
		StringBuffer stringOut = new StringBuffer();
		for (int i = 0; i < integers.length; i++) {
			stringOut.append("\t" + integers[i]);
		}
		return stringOut.toString();
	}

	private int getBaseIdx(char prevBase) {
		switch (prevBase) {
		case 'A' : return 0;
		case 'C' : return 1;
		case 'G' : return 2;
		case 'T' : return 3;
		case 'N' : return 4;
		case 'a' : return 5;
		case 'c' : return 6;
		case 'g' : return 7;
		case 't' : return 8;
		case 'n' : return 9;
		// Dismiss N or n
		}
		return -1;
	}

	private void initialize(Integer[] lenArr) {
		for (int i = 0; i < lenArr.length; i++) {
			lenArr[i] = 0;
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaCountHomopolymers.jar <in.fa> <out.txt>");
		System.out.println("\tCount num. of homopolymer bases per A/C/G/T");
		System.out.println("\t<in.fa>: Input fa file");
		System.out.println("\t<out.txt>: HomopolymerLen\tA\tC\tG\tT\tN\ta\tc\tg\tt\tn\tA_or_a\tC_or_c\tG_or_g\tT_or_t\tN_or_n");
		System.out.println("Arang Rhie, 2015-07-01. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new CountHomopolymers().go(args[0], args[1]);
		} else {
			new CountHomopolymers().printHelp();
		}
	}

}
