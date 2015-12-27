package javax.arang.sam;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class BuildMapqMatrics extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		HashMap<Integer, Integer> mapqMatrics = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> primaryMapqMatrics = new HashMap<Integer, Integer>();
		
		fm.writeLine("MAPQ\tReadCounts\tPrimaryAlignedReadCounts");
		
		for (int i = 0; i < 256; i++) {
			mapqMatrics.put(i, 0);
			primaryMapqMatrics.put(i, 0);
		}
		
		int mapq = 0;
		int numReads = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			tokens = line.split("\t");
			mapq = Integer.parseInt(tokens[Sam.MAPQ]);
			mapqMatrics.put(mapq, mapqMatrics.get(mapq) + 1);
			if (!SAMUtil.isSecondaryAlignment(Integer.parseInt(tokens[Sam.FLAG]))) {
				primaryMapqMatrics.put(mapq, primaryMapqMatrics.get(mapq) + 1);
			}
			numReads++;
		}
		
		System.out.println("Total reads: " + numReads);
		
		for (int i = 0; i < 256; i++) {
			fm.writeLine(i + "\t" + mapqMatrics.get(i) + "\t" + primaryMapqMatrics.get(i));
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samBuildMapqMatrics.jar <in.sam>");
		System.out.println("\t<out>: Mapq matrix, with 3 cols");
		System.out.println("\t\tMAPQ[0-255]\tReadCounts\tPrimaryAlignedReadCounts");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new BuildMapqMatrics().go(args[0], args[0] + ".mapq");
		} else {
			new BuildMapqMatrics().printHelp();
		}
		
	}

}
