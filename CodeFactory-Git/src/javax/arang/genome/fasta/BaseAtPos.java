package javax.arang.genome.fasta;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class BaseAtPos extends Rwrapper {

	private static String contig;
	private static int pos;
	
	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		boolean isContigToSearch = false;
		int posIdx = 0;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith(">")) {
				tokens = line.split(RegExp.TAB);
				if (tokens[0].substring(1).equals(contig)) {
					isContigToSearch = true;
				} else {
					isContigToSearch = false;
				}
			} else {
				if (isContigToSearch) {
					line = line.trim();
					posIdx += line.length();
					if (posIdx >= pos) {
						System.out.println(contig + ":" + pos + " = " + line.charAt(line.length() - (posIdx - pos) - 1));
						System.exit(0);
					}
				} else {
					continue;
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastaBaseAtPos.jar <in.fasta> <contig:pos>");
		System.out.println("\t<in.fasta>: Any fasta file");
		System.out.println("\t<contig:pos>: <pos> (1-base) at <contig> will be printed");
		System.out.println("Arang Rhie, 2015-11-16. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			contig = args[1].substring(0, args[1].lastIndexOf(":"));
			pos = Integer.parseInt(args[1].substring(args[1].lastIndexOf(":") + 1));
			new BaseAtPos().go(args[0]);
		} else {
			new BaseAtPos().printHelp();
		}
	}

}
