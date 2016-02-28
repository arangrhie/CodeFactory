package javax.arang.bac;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class PlateToBacIDlist extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		String line;
		String[] tokens;
		
		String poolName = "";
		String pool;
		FileMaker fm = null;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("\t\t")) {
				continue;
			}
			if (line.startsWith("pool")) {
				tokens = line.split(RegExp.TAB);
				pool = tokens[0].substring(0, tokens[0].indexOf("-"));
				if (pool.equals(poolName))	continue;
				poolName = pool;
				fm = new FileMaker(fr.getDirectory() + "/" + lane, pool + ".list");
				fm.writeLine(pool);
				continue;
			}
			tokens = line.trim().split(RegExp.TAB);
			for (int i = 1; i < tokens.length; i++) {
				fm.writeLine(tokens[i]);
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("usage: java -jar bacPlateToBacIDlist.jar <in.excel.plate.to.txt> <lane>");
		System.out.println("\t<in.excel.plate.to.txt>: pool starting with poolN-M,");
		System.out.println("\t\twhich is a tab delimited file.");
		System.out.println("\tFiles will be generated PER POOL, with a list of BAC IDs");
	}
	
	private static String lane;

	public static void main(String[] args) {
		if (args.length == 2) {
			lane = args[1];
			new PlateToBacIDlist().go(args[0]);
		} else {
			new PlateToBacIDlist().printHelp();
		}
	}

}
