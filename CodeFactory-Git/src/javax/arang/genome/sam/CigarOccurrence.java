package javax.arang.genome.sam;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class CigarOccurrence extends IOwrapper {

	HashMap<Integer, Integer> mPositionCountMap = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> iPositionCountMap = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> dPositionCountMap = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> sPositionCountMap = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> endPositonCountMap = new HashMap<Integer, Integer>();

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		fm.writeLine("\tM\tI\tD\tS\tEnd");
		HashMap<Integer, Integer> lenCountPointer;
		
		String cigar;
		ArrayList<String[]> cigarArr;
		int cigarSize;
		
		String op;
		//int count;

		int position;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@")) {
				continue;
			}
			tokens = line.split(RegExp.TAB);
			cigar = tokens[Sam.CIGAR];
			
			cigarArr = Sam.parseArr(cigar);
			cigarSize = cigarArr.size();
			//System.out.println(cigarSize);
			position = 1;
			for (int i = 0 ; i < cigarSize; i++) {
				op = cigarArr.get(i)[Sam.OP];
				if (op.equals("H") || (op.equals("S") && i == 0)) {
					continue;	// We don't need H clipped bases
				}
				//count = Integer.parseInt(cigarArr.get(i)[Sam.COUNT]);
				lenCountPointer = getLenCountMap(op);
				if (lenCountPointer != null) {
					if (lenCountPointer.containsKey(position)) {
						lenCountPointer.put(position, lenCountPointer.get(position) + 1);
					} else {
						lenCountPointer.put(position, 1);
					}
				}
				
				position++;
			}
			if (endPositonCountMap.containsKey(position)) {
				endPositonCountMap.put(position, endPositonCountMap.get(position) + 1);
			} else {
				endPositonCountMap.put(position, 1);
			}
		}
		
		int max = endPositonCountMap.size();
		
		for (int i = 1; i <= max + 1; i++) {
			fm.writeLine(i
					+ "\t" + (mPositionCountMap.get(i) != null ? mPositionCountMap.get(i) : 0)
					+ "\t" + (iPositionCountMap.get(i) != null ? iPositionCountMap.get(i) : 0)
					+ "\t" + (dPositionCountMap.get(i) != null ? dPositionCountMap.get(i) : 0)
					+ "\t" + (sPositionCountMap.get(i) != null ? sPositionCountMap.get(i) : 0)
					+ "\t" + (endPositonCountMap.get(i) != null ? endPositonCountMap.get(i) : 0));
		}
	}

	private HashMap<Integer, Integer> getLenCountMap(String op) {
		if (op.equals("M")) {
			return mPositionCountMap;
		} else if(op.equals("D")) {
			return dPositionCountMap;
		} else if (op.equals("I")) {
			return iPositionCountMap;
		} else if (op.equals("S")) {
			return sPositionCountMap;
		}
		return null;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samCigarOccurrence.jar <in.sam> <out.txt>");
		System.out.println("\tCounts at each position the occurrence of S M I D within an aligned read.");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new CigarOccurrence().go(args[0], args[1]);
		} else {
			new CigarOccurrence().printHelp();			
		}
		
	}

}
