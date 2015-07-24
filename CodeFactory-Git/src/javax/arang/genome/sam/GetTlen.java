package javax.arang.genome.sam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GetTlen {

	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		String cigar;
		
		if (args.length != 0) {
			cigar = args[0].trim();
		} else {
			while (in.ready()) {
				cigar = in.readLine();
				if (cigar.equals("")) {
					System.out.println("Usage: java -jar samCigarTlen.jar <input_string>");
					System.out.println("\tPrints the number of matched bases, deletions, insertions to reference");
					System.out.println("\tTLEN = matchs + deletions");
				} else {
					parseCigar(cigar);
				}
			}
		}
	}
	
	private static void parseCigar(String cigar) {
		ArrayList<String[]> cigarArr = Sam.parseArr(cigar);
		int matchs = 0;
		int deletions = 0;
		int insertions = 0;
		
		for (int i = 0; i < cigarArr.size(); i++) {
			if (cigarArr.get(i)[Sam.OP] != null) {
				if (cigarArr.get(i)[Sam.OP].equals("M")) {
					matchs += Integer.parseInt(cigarArr.get(i)[Sam.COUNT]);
				} else if (cigarArr.get(i)[Sam.OP].equals("I")) {
					insertions += Integer.parseInt(cigarArr.get(i)[Sam.COUNT]);
				} else if (cigarArr.get(i)[Sam.OP].equals("D")) {
					deletions += Integer.parseInt(cigarArr.get(i)[Sam.COUNT]);
				}
			}
		}
		
		System.out.println("TLEN = " + (matchs + deletions));
		System.out.println("\tM: " + matchs);
		System.out.println("\tD: " + deletions);
		System.out.println("\tI: " + insertions);
	}

}
