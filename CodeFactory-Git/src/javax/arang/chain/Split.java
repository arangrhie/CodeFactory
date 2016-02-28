package javax.arang.chain;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class Split extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		int outCnt = 1;
		int chainCnt = 0;
		FileMaker fm = new FileMaker(outPrefix + "." + outCnt);
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("chain")) {
				chainCnt++;
				if (chainCnt == numChainsPerFile) {
					System.out.println(":: [DEBUG] :: " + chainCnt + " chains are written to " + fm.getFileName());
					outCnt++;
					fm = new FileMaker(outPrefix + "." + outCnt);
					chainCnt = 0;
				}
			}
			fm.writeLine(line);
		}
		System.out.println(":: [DEBUG] :: " + chainCnt + " chains are written to " + fm.getFileName());
		System.out.println(outCnt + " files are generated.");
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar chainSplit.jar <in.chain> <numChainsPerFile> <out_prefix>");
		System.out.println("Split chain files containing at maximum of <numChainsPerFile> chains per out files");
		System.out.println("\t<in.chain>: chain or overchain");
		System.out.println("\t<numChainsPerFile>: number of chains per file");
		System.out.println("\t<out_prefix>: <out_prefix>.1 .2 .3 ... will be made");
		System.out.println("Arang Rhie, 2016-02-06. arrhie@gmail.com");
	}

	private static int numChainsPerFile;
	private static String outPrefix;
	public static void main(String[] args) {
		if (args.length == 3) {
			numChainsPerFile = Integer.parseInt(args[1]);
			outPrefix = args[2];
			new Split().go(args[0]);
		} else {
			new Split().printHelp();
		}
	}

}
