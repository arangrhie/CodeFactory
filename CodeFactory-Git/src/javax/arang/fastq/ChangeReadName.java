package javax.arang.fastq;

import java.util.HashMap;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;

public class ChangeReadName extends I2Owrapper {

	@Override
	public void hooker(FileReader frFastq, FileReader frMap, FileMaker fm) {
		String line;
		String[] tokens;
		HashMap<String, String> readNameMap = new HashMap<String, String>();
		
		while (frMap.hasMoreLines()) {
			line = frMap.readLine();
			tokens = line.split(RegExp.TAB);
			readNameMap.put(tokens[0], tokens[1]);
		}
		
		System.err.println(readNameMap.size() + " read names will be replaced");
		FileMaker fmNoMap = new FileMaker(fm.getFileName() + ".to_add");
		
		int lineNum = 0;
		String oldName;
		while (frFastq.hasMoreLines()) {
			line = frFastq.readLine();
			// read id
			if (lineNum % 4 == 0) {
				oldName = line.substring(1);
				if (readNameMap.containsKey(oldName)) {
					fm.writeLine("@" + readNameMap.get(oldName));
				} else {
					System.err.println(oldName + " does not exists in the " + frMap.getFileName() + ". " + oldName + " will be used.");
					fmNoMap.writeLine(oldName);
					fm.writeLine(line);
				}
				lineNum = 0;
			} else {
				fm.writeLine(line);
			}
			lineNum++;
		}
		
		fmNoMap.closeMaker();
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar fastqChangeReadName.jar <in.fastq> <in.map> <out.fastq>");
		System.out.println("\t<in.fastq>: input fastq");
		System.out.println("\t<in.map>: original read name to new read name, tab delimited 2 column text file");
		System.out.println("\t<out.fastq>: read name replaced to new read name.");
		System.out.println("Arang Rhie, 2016-07-22. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new ChangeReadName().go(args[0], args[1], args[2]);
		} else {
			new ChangeReadName().printHelp();
		}
	}

}
