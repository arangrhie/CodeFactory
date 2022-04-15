package javax.arang.gaf;

public class ReversePath {

	public static void main(String[] args) {
		if (args.length == 1) {
			Path path = new Path(args[0]);
			path.reversePath();
			System.out.println(path.getPath());
		} else {
			System.err.println("Usage: java -jar pathReverseComplement.jar path");
			System.err.println("Reverse complement input path.");
		}

	}

}
