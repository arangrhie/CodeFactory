package javax.arang.graph.gfa;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;

public class ColorNodesByPath extends Rwrapper {

	@Override
	public void hooker(FileReader fr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void printHelp() {
		System.err.println("Usage: gfaColorNodesByPath.jar in.gfa");
		
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new ColorNodesByPath().go(args[0]);
		} else {
			new ColorNodesByPath().printHelp();
		}

	}

}
