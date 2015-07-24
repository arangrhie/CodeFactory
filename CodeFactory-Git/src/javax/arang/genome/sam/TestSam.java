package javax.arang.genome.sam;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class TestSam extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("@"))	continue;
			if (line.equals(""))	continue;
			tokens = line.split("\t");
			int flag = Integer.parseInt(tokens[Sam.FLAG]);
			String key = tokens[Sam.QNAME];
			if (SAMUtil.isPair1(flag)) {
				System.out.println(key + " : pair 1");
			} else {
				System.out.println(key + " : pair 2");
			}
			if (SAMUtil.isSecondaryAlignment(flag)) {
				System.out.println(key + " : secondary alignment");
			}
			
		}
		
	}

	@Override
	public void printHelp() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TestSam().go(args[0]);
	}

}
