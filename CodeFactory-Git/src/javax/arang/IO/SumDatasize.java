package javax.arang.IO;

import java.util.StringTokenizer;

import javax.arang.IO.basic.FileReader;

public class SumDatasize extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		StringTokenizer st;
		long dataSize = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine().toString();
			st = new StringTokenizer(line);
			st.nextToken();
			st.nextToken();
			dataSize += Long.parseLong(st.nextToken());
		}
		System.out.println(dataSize);
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		System.out.println("Sum the 3rd \'data size column\' and returns the total size.");
		System.out.println("Usage: java -jar sumDatasize.jar <inFile>");
		System.out.println("inFile has the following format:");
		System.out.println("2011-08-09 04:21  56869705   s3://fx-sample/ak6/031.base_sort/FX_chr17.bas");
		System.out.println("...");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SumDatasize sd = new SumDatasize();
		if (args.length == 1) {
			sd.go(args[0]);
		} else {
			sd.printHelp();
		}

	}

}
