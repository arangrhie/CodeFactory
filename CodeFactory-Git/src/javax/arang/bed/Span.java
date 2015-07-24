package javax.arang.bed;

import java.util.HashMap;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.bed.util.Bed;

public class Span extends IOwrapper {

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		HashMap<String, Integer> startMap = new HashMap<String, Integer>();
		HashMap<String, Integer> endMap = new HashMap<String, Integer>();
		
		String line;
		String[] tokens;
		String key;
		int start;
		int end;
		
		int lineNums = 0;
		
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			lineNums++;
			tokens = line.split("\t");
			key = tokens[Bed.CHROM] + "_" + tokens[Bed.NOTE].substring(0, tokens[Bed.NOTE].indexOf("F") + 1);
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			if (!startMap.containsKey(key)) {
				startMap.put(key, Integer.parseInt(tokens[Bed.START]));
				endMap.put(key, Integer.parseInt(tokens[Bed.END]));
			} else {
				if (start < startMap.get(key))	startMap.put(key, start);
				if (end > endMap.get(key))	endMap.put(key, end);
			}
		}
		
		System.out.println("[DEBUG] :: Total lines : " + lineNums);
		
		for (String k : startMap.keySet()) {
			fm.writeLine(k.substring(0, k.indexOf("_")) + "\t" + startMap.get(k) + "\t" + endMap.get(k) + "\t" + k.substring(k.indexOf("_") + 1));
		}
		
		System.out.println("[DEBUG] :: Spanned lines : " + startMap.size());
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedSpan.jar <in.bed> <out.bed>");
		System.out.println("\t<in.bed>: input bed file to reduce.");
		System.out.println("\t<out.bed>: chr minStart maxEnd");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			new Span().go(args[0], args[1]);
		} else {
			new Span().printHelp();
		}
	}

}
