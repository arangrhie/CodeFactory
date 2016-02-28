package javax.arang.fastq;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class AddReadGroup extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		int lineNum = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			lineNum++;
			if (lineNum % 4 == 1) {
				if (line.startsWith("@")) {
					fm.writeLine(line.replace("@", "@" + readGroup + ":"));
				} else {
					fm.writeLine("@" + readGroup + ":" + line);
				}
			} else {
				fm.writeLine(line);
				if (lineNum % 4 == 0) {
					lineNum = 0;
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar -Xmx1g fastqAddReadGroup.jar <in.fastq> <read-group> <out.fastq>");
		System.out.println("\tAdds <read-group> in fasta sequence read id.");
		System.out.println("\t\t@READ-ID => @<read-group>:READ-ID");
		System.out.println("\t<in.fastq>: Any fastq file");
		System.out.println("\t<read-group>: Any text string, with no spaces, @s.");
		System.out.println("Arang Rhie, 2015-09-21. arrhie@gmail.com");
	}

	private static String readGroup;
	public static void main(String[] args) {
		if (args.length == 3) {
			readGroup = args[1];
			new AddReadGroup().go(args[0], args[2]);
		} else {
			new AddReadGroup().printHelp();
		}
	}

}
