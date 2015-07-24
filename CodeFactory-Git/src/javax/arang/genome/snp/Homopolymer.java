package javax.arang.genome.snp;

import java.io.IOException;
import java.util.HashMap;

import javax.arang.IO.basic.FileReader;

public class Homopolymer {

	HashMap<Integer, String> polyA = new HashMap<Integer, String>();
	HashMap<Integer, String> polyT = new HashMap<Integer, String>();
	HashMap<Integer, String> polyG = new HashMap<Integer, String>();
	HashMap<Integer, String> polyC = new HashMap<Integer, String>();
	
	static void getHomoTable(FileReader fr) {
		String line;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			if (line.startsWith(">"))	continue;
			if (line.equals(""))	continue;
			
		}
		
	}
	
	static int homLen = 0;
	static int to = -1;
	static boolean isInHomopolymerRegion(int pos, char ref, char obs) {
		boolean isHomoRegion = false;
		boolean isObs = false;
		homLen = 0;
		
		// check homopolymer stretch
		for (int i = leftBuffer.length() - 1; i>0; i--) {
			if(leftBuffer.charAt(i) == ref) {
				homLen++;
				isHomoRegion = true;
			} else if (leftBuffer.charAt(i) == obs) {
				isObs = true;
				break;
			} else {
				break;
			}
		}

		for (int i = 0; i < rightBuffer.length(); i++) {
			if(rightBuffer.charAt(i) == ref) {
				homLen++;
				isHomoRegion = true;
			} else if (rightBuffer.charAt(i) == obs) {
				isObs = true;
				to = pos + i - 1;
				return isHomoRegion && isObs;
			} else {
				to = pos + i - 1;
				break;
			}
		}
		
//		if (!isHomoRegion) {
//			System.out.println(pos + " " + ref + " " + obs + " " + leftBuffer.substring(leftBuffer.length() - 20) + " " + rightBuffer.substring(0, 20) + " !isHomoRegion");
//		}
//		if (!isObs) {
//			System.out.println(pos + " " + ref + " " + obs + " " + leftBuffer.substring(leftBuffer.length() - 20) + " " + rightBuffer.substring(0, 20) + " !isObs");
//		}
		
		return isHomoRegion && isObs;
	}
	
	static int getHomLen() {
		return homLen;
	}
	
	static int getTo() {
		return to;
	}
	
	static int readChars = 0;
	static String leftBuffer = "";
	static String rightBuffer = "";
	static int prevPos = 0;
	public static void seek(int pos, FileReader fr) {
		pos--;	// 1-base to 0-base coordinate
		if (readChars > pos) {
			int movingSeqLen = pos - prevPos;
			leftBuffer = leftBuffer + rightBuffer.substring(0, movingSeqLen);
			try {
				rightBuffer = rightBuffer.substring(movingSeqLen) + fr.seekForward(80*3, 3);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error occured!!");
			}
			prevPos = pos;
			return;
		}
		
		String line;
		while (fr.hasMoreLines()) {
			line = fr.readLine().trim();
			readChars += line.length();
			if (readChars > pos) {
				leftBuffer = leftBuffer + line.substring(0, line.length() - (readChars - pos));
				try {
					rightBuffer = line.substring(line.length() - (readChars - pos)) + fr.seekForward(line.length()*3, 3);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error occured!!");
				}
				break;
			}
			leftBuffer = leftBuffer + line;
			if (leftBuffer.length() > 40) {
				leftBuffer = leftBuffer.substring(leftBuffer.length() - 40);
			}
		}
		prevPos = pos;
	}

}
