package javax.arang.genome.snp;

import java.util.HashMap;
import java.util.StringTokenizer;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class CommonSnps extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		
		HashMap<Integer, String> list1 = new HashMap<Integer, String>();
		HashMap<Integer, String> list2 = new HashMap<Integer, String>();
		
		int common = 0;
		String commonLines = "";
		String list1Unique = "";
		String list2Unique = "";
		
		String line;
		StringTokenizer st;
		while (fr1.hasMoreLines()) {
			line = fr1.readLine().toString();
			st = new StringTokenizer(line);
			st.nextToken();	// chr
			Integer start = Integer.parseInt(st.nextToken());	// start
			list1.put(start, line);
		}
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine().toString();
			st = new StringTokenizer(line);
			st.nextToken();	// chr
			Integer start = Integer.parseInt(st.nextToken());	// start
			list2.put(start, line);
			if (list1.containsKey(start)) {
				common++;
				commonLines = commonLines + "\n" + list1.get(start) + "\n" + line + "\n";
			} else {
				list2Unique = list2Unique + "\n" + line;
			}
		}
		
		for (Integer list1Start : list1.keySet()) {
			if (!list2.containsKey(list1Start)) {
				list1Unique = list1Unique + "\n" + list1.get(list1Start);
			}
		}
		
		fm.writeLine(fr1.getFullPath() + " : " + list1.size() + " in total");
		fm.writeLine(fr2.getFullPath() + " : " + list2.size() + " in total");
		
		fm.writeLine("Number of Common: " + common);
		fm.writeLine(commonLines);

		fm.writeLine(fr1.getFullPath() + " unique: " + (list1.size() - common));
		fm.writeLine(list1Unique);
		
		fm.writeLine(fr2.getFullPath() + " unique: " + (list2.size() - common));
		fm.writeLine(list2Unique);
	};


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CommonSnps commSnps = new CommonSnps();
		if (args.length == 3) {
			commSnps.go(args[0], args[1], args[2]);
		} else {
			commSnps.printHelp();
		}
	}
	
	public void printHelp() {
		System.out.println(
				"Compare two snp list files with the following format:\n" +
				"chr\tstart_pos\t...\n" +
				"Usage: java -jar commonSnps.jar <inFile1> <inFile2> <commonSnpOutFile>\n\n" +
				"Arang Rhie, Oct. 12, 2011"
				);
	}

}
