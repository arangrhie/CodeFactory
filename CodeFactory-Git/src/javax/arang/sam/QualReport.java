package javax.arang.sam;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class QualReport extends IOwrapper {

	static int offset = 33;
	static int threshold = 17;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		
		HashMap<Integer, Integer> readMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> readCountMap = new HashMap<Integer, Integer>();
		
		int max = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split("\t");
			if (tokens[Sam.RNAME].equals("*"))	continue;
			String qual = "";
			try {
				 qual = tokens[Sam.QUAL];
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println(line);
				e.printStackTrace();
			}
			int length = qual.length();
			if (length > max) {
				max = length;
			}
			char[] dst = new char[length];
			qual.getChars(0, length, dst, 0);
			int qualBases = 0;
			for (int i = 0; i < length; i++) {
				if (dst[i] >= (threshold + offset)) {
					qualBases++;
				}
			}
			
			if (readMap.containsKey(length)) {
				readMap.put(length, readMap.get(length) + qualBases);
				readCountMap.put(length, readCountMap.get(length) + 1);
			} else {
				readMap.put(length, qualBases);
				readCountMap.put(length, 1);
			}
			
		}
		
		fm.writeLine("ReadLength\tUsedBases");
		for (int i = 1; i <= max; i++) {
			if (readMap.containsKey(i)) {
				float avgBases = (float) readMap.get(i) / readCountMap.get(i);
				fm.writeLine(i + "\t" + String.format("%,.2f", avgBases));
//			} else {
//				fm.writeLine(i + "\t-");
			}
		}

	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samQualReport.jar <in.sam> <qualOffset>");
		System.out.println("\t<in.sam>: input sam file");
		System.out.println("\t<qualOffset>: [Optional] default 33, add if you want to change");
		System.out.println("\toutput is the quality report <in.sam.qual.report>");
	}


	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 1) {
			offset = Integer.parseInt(args[1]);
		}
		if (args.length > 0) {
			new QualReport().go(args[0], args[0] + ".qual.report");
		} else {
			new QualReport().printHelp();
		}

	}

}
