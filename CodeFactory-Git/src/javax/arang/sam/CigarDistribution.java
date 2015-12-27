package javax.arang.sam;

import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CigarDistribution extends Rwrapper {

	
	
	ArrayList<HashMap<Integer, Integer>> mLenCountList = new ArrayList<HashMap<Integer, Integer>>();
	ArrayList<HashMap<Integer, Integer>> iLenCountList = new ArrayList<HashMap<Integer, Integer>>();
	ArrayList<HashMap<Integer, Integer>> dLenCountList = new ArrayList<HashMap<Integer, Integer>>();
	
	int mCountMax;
	int iCountMax;
	int dCountMax;
	
	static int MaxPos = 100;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		ArrayList<String[]> cigarArr;
		HashMap<Integer, Integer> lenCountPointer;
		
		for (int i = 0; i < MaxPos; i++) {
			HashMap<Integer, Integer> mLenCounts = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> iLenCounts = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> dLenCounts = new HashMap<Integer, Integer>();
			mLenCountList.add(mLenCounts);
			iLenCountList.add(iLenCounts);
			dLenCountList.add(dLenCounts);
		}
		
		
		String cigar;
		int cigarLen;
		String op = "";
		int count = 0;
		int position = 1;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split("\t");
			cigar = tokens[Sam.CIGAR];
			cigarArr = Sam.parseArr(cigar);
			//System.out.println(cigarSize);
			position = 0;
			cigarLen = cigarArr.size();
			if (cigarLen > MaxPos) {
				cigarLen = MaxPos;
			}
			for (int i = 0 ; i < cigarLen ; i++) {
				op = cigarArr.get(i)[Sam.OP];
				if (op.equals("H") || (op.equals("S") || (position == 1 && op.equals("I")))) {
					continue;	// We don't need H clipped bases
				}
				count = Integer.parseInt(cigarArr.get(i)[Sam.COUNT]);
				lenCountPointer = getLenCountMap(op, count, position);
				if (lenCountPointer != null) {
					if (lenCountPointer.containsKey(count)) {
						lenCountPointer.put(count, lenCountPointer.get(count) + 1);
					} else {
						lenCountPointer.put(count, 1);
					}
				}
				position++;
			}
		}
		
		writeOutputs("M", mCountMax);
		writeOutputs("I", iCountMax);
		writeOutputs("D", dCountMax);
		
	}
	
	private void writeOutputs(String type, int countMax) {
		FileMaker fm = new FileMaker(prefix + "." + type + ".txt");
		fm.writeLine(type + " Length Distribution");
		fm.write("Len\t1st_pos");
		for (int i = 2; i <= MaxPos; i++) {
			fm.write("\t" + i);
		}
		fm.writeLine();
		
		for (int len = 1; len <= countMax; len++) {
			fm.write(len + "");
			for (int i = 0; i < MaxPos; i++) {
				fm.write("\t" + (getLenCountMap(type, i).get(len) != null ? getLenCountMap(type, i).get(len) : 0));
			}
			fm.writeLine();
		}
	}
	
	private HashMap<Integer, Integer> getLenCountMap(String op, int position) {
		if (op.equals("M")) {
			return mLenCountList.get(position);
		} else if(op.equals("D")) {
			return dLenCountList.get(position);
		} else if (op.equals("I")) {
			return iLenCountList.get(position);
		}
		return null;
	}

	private HashMap<Integer, Integer> getLenCountMap(String op, int count, int position) {
		if (op.equals("M")) {
			if (mCountMax < count) {
				mCountMax = count;
			}
			return mLenCountList.get(position);
		} else if(op.equals("D")) {
			if (dCountMax < count) {
				dCountMax = count;
			}
			return dLenCountList.get(position);
		} else if (op.equals("I")) {
			if (iCountMax < count) {
				iCountMax = count;
			}
			return iLenCountList.get(position);
		}
		return null;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samCigarDistribution.jar <in.sam> <out_prefix>");
		System.out.println("\tReads CIGAR field, and make a distribution data");
		System.out.println("\t<Type>\t<Length>\t<Occurrence>");
		System.out.println("Arang Rhie, 2015-06-06. arrhie@gmail.com");
	}
	
	static String prefix;

	public static void main(String[] args) {
		if (args.length == 2) {
			prefix = args[1];
			new CigarDistribution().go(args[0]);
		} else {
			new CigarDistribution().printHelp();
		}
	}

}
