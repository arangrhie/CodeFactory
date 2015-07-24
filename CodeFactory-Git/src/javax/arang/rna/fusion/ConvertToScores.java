package javax.arang.rna.fusion;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class ConvertToScores extends IOwrapper {

	static int startIdx;
	
	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line = fr.readLine();
		String[] tokens = line.split("\t");
		int score = 0;
		
		fm.writeLine(line);
		
		while(fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			fm.write(tokens[0]);
			for (int i = 1; i < tokens.length; i++) {
				if (i < startIdx) {
					fm.write("\t" + tokens[i]);
				} else {
					if (tokens[i].equals("-")) {
						fm.write("\t0");
					} else {
						score = Integer.parseInt(tokens[i].substring(0, tokens[i].indexOf("/")))
							+ Integer.parseInt(tokens[i].substring(tokens[i].indexOf("/") + 1));
						fm.write("\t" + score);
					}
				}
			}
			fm.writeLine();
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar rnaFusionConvertToScores.jar <in.txt> <start_idx>");
		System.out.println("\t<in.txt>: input file");
		System.out.println("\t<start_idx>: idx of sample in <in.txt>");
		System.out.println("Convert pair/span to pair+span score.");
		System.out.println("Arang Rhie, 2014-10-31. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			startIdx = Integer.parseInt(args[1]);
			new ConvertToScores().go(args[0], args[0].replace(".txt", "_score.txt"));
		} else {
			new ConvertToScores().printHelp();
		}
	}

}
