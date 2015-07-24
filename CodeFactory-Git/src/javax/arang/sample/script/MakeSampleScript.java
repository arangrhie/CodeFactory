package javax.arang.sample.script;

import java.util.StringTokenizer;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;

public class MakeSampleScript extends IOwrapper {

	private static String gsnap = "/scratch/GSNAP/gmap_2011-12-28/bin/gsnap -d hg19" +
				" -D /gmi_comm/home/serena/ref/2011-08-15/hg19 -m 10 -i 3 -n 5 -t 15 -B 1 -O -A sam";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if (args.length > 2) {
				StringTokenizer st = new StringTokenizer(args[2], "\"");
				gsnap = st.nextToken();
			}
			new MakeSampleScript().go(args[0], args[1]);
		} catch (Exception e) {
			System.out.println("Invalid input");
			for (int i = 0; i < args.length; i++) {
				System.out.println(args[i]);
			}
			new MakeSampleScript().printHelp();
		}
	}

	@Override
	public void hooker(FileReader fr, FileMaker fm) {
		
		fm.writeLine("#!bin/bash");
		fm.writeLine("#" + gsnap +
				" rawdata/${i}_sequence_R1.txt rawdata/${i}_sequence_R1.txt > ${i}.sam");
		fm.writeLine("");
		
		String sampleName = "";
		while (fr.hasMoreLines()) {
			sampleName = fr.readLine();
			fm.writeLine(gsnap + " rawdata/" + sampleName + "_sequence_R1.txt rawdata/" + sampleName + "_sequence_R1.txt" +
					" > " + sampleName + ".sam");
		}
	}

	@Override
	public void printHelp() {
		// TODO Auto-generated method stub
		System.out.println("Usage: java -jar makeSampleScript.jar <inFile> <outFile> <gsnap and args>");
		System.out.println("\t<inFile>: sample list");
		System.out.println("\t<outFile>: shell script file to run gsnap");
		System.out.println("\t<gsnap and args>: \"" + gsnap + "\"");
	}

}
