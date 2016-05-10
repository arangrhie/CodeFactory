package javax.arang.sv;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ForPlotting extends IOwrapper {
	
	static private int DISTANT_FROM_INS_0 = 2;
	static private int DISTANT_FROM_DEL_0 = -2;
	private int posToDrawInsA = DISTANT_FROM_INS_0;
	private int posToDrawDelA = DISTANT_FROM_DEL_0;
	private int posToDrawComplexA = DISTANT_FROM_INS_0;
	private int posToDrawInsB = DISTANT_FROM_INS_0;
	private int posToDrawDelB = DISTANT_FROM_DEL_0;
	private int posToDrawComplexB = DISTANT_FROM_INS_0;

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		String line;
		String[] tokens;
		
		int start = 0;
		int position = 0;

		boolean isFirst = true;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split(RegExp.TAB);
			position = Integer.parseInt(tokens[SV.START]);
			if (isFirst) {
				start = position;
				isFirst = false;
			}
			
			if (position - start >= window) {
				start += window;
				posToDrawInsA = DISTANT_FROM_INS_0;
				posToDrawDelA = DISTANT_FROM_DEL_0;
				posToDrawComplexA = DISTANT_FROM_INS_0;
				posToDrawInsB = DISTANT_FROM_INS_0;
				posToDrawDelB = DISTANT_FROM_DEL_0;
				posToDrawComplexB = DISTANT_FROM_INS_0;
			}
			
			writeResult(fm, line, tokens[SV.TYPE], tokens[tokens.length - 1], start);
		}
	}
	
	private void writeResult(FileMaker fm, String line, String type, String hap, int position) {
		int posToDraw;
		if (hap.equals("A") || hap.equals("H")) {
			posToDraw = getPosToDrawByType(type, "A");
			fm.writeLine(line + "\t" + position + "\t" + posToDraw);
		}
		if (hap.equals("B") || hap.equals("H")) {
			posToDraw = getPosToDrawByType(type, "B");
			fm.writeLine(line + "\t" + position + "\t" + posToDraw);
		}
	}

	private int getPosToDrawByType(String type, String hap) {
		if (hap.equals("A")) {
			if (type.startsWith("I")) {
				return ++posToDrawInsA;
			} else if (type.startsWith("D")) {
				if (posToDrawDelA < 0) {
					return --posToDrawDelA;
				} else {
					return ++posToDrawDelA;
				}
			} else if (type.startsWith("C")){
				return ++posToDrawComplexA;
			}
		} else if (hap.equals("B")) {
			if (type.startsWith("I")) {
				return ++posToDrawInsB;
			} else if (type.startsWith("D")) {
				if (posToDrawDelB < 0) {
					return --posToDrawDelB;
				} else {
					return ++posToDrawDelB;
				}
			} else if (type.startsWith("C")){
				return ++posToDrawComplexB;
			}
		}
		return 0;
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar svForPolotting.jar <sv.bed> <out.sv.bed> [WINDOW DISTANT_FROM_0]");
		System.out.println("\tThe plotting position will be as increased positions per [WINDOW] size.");
		System.out.println("\t<sv.bed>: " + SV.svHeader);
		System.out.println("\t<out.sv.bed>: Gives two plotting positions (x and y) at the end of the columns");
		System.out.println("\t[WINDOW]: Window size to be shown on the X-axis. DEFAULT=1000000 (1Mb)");
		System.out.println("\t[DISTANT_FROM_0]: Distance of the dots to be drawn on the Y axis from 0. DEFAULT=2");
		System.out.println("Arang Rhie, 2016-03-29. arrhie@gmail.com");
	}

	private static int window = 1000000;
	public static void main(String[] args) {
		if (args.length == 2) {
			new ForPlotting().go(args[0], args[1]);
		} else if (args.length == 4) {
			window = Integer.parseInt(args[2]);
			DISTANT_FROM_INS_0 = Integer.parseInt(args[3]);
			DISTANT_FROM_DEL_0 = DISTANT_FROM_INS_0 * 1;
			new ForPlotting().go(args[0], args[1]);
		} else {
			new ForPlotting().printHelp();
		}
	}

}
