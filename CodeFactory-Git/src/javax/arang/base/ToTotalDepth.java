package javax.arang.base;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.Base;

public class ToTotalDepth extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		String line;
		String[] tokens;
		int depth;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			if (line.startsWith("#") || line.startsWith("Chromosome"))	continue;
			tokens = line.split(RegExp.TAB);
			depth = Base.getTotalDepth(tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]);
			if (hasWeight) {
				depth = (int) ((float) depth * weight);
			}
			fm.writeLine(tokens[Base.CHR] + "\t" + tokens[Base.POS] + "\t" + depth);
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseToTotalDepth.jar <in.base> <out.depth> [weight]");
		System.out.println("\t<in.base>: generated with samBaseDepth.jar or bamBaseDepth.jar");
		System.out.println("\t<out.depth>: depth sum of A C G T D");
		System.out.println("\t\tCONTIG\tPOS\tDEPTH");
		System.out.println("\t[weight]: when given, (int) ((float) depth * weight) will be reported. Can be float. (0.5 for half depth)");
		System.out.println("Arang Rhie, 2015-12-12. arrhie@gmail.com");
	}

	private static boolean hasWeight = false;
	private static float weight = 1.0f;
	
	public static void main(String[] args) {
		if (args.length == 2) {
			new ToTotalDepth().go(args[0], args[1]);
		} else if (args.length == 3) {
			hasWeight = true;
			weight = Float.parseFloat(args[2]);
			new ToTotalDepth().go(args[0], args[1]);
		} else {
			new ToTotalDepth().printHelp();
		}
	}

}
