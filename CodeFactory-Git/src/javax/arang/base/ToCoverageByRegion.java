package javax.arang.base;

import javax.arang.IO.IOwrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.base.util.Base;

public class ToCoverageByRegion extends IOwrapper {

	@Override
	public void hooker(FileReader frBase, FileMaker fm) {
		String line;
		String[] tokens;
		String chr = region.substring(0, region.lastIndexOf(":"));
		String interval = region.substring(region.lastIndexOf(":") + 1);
		int start = Integer.parseInt(interval.split("-")[0]);
		int end = Integer.parseInt(interval.split("-")[1]);
		int range = end - start;
		fm.write(chr + "\t" + start + "\t" + end + "\t" + frBase.getFileName() + "\t" + range);
		int covered = 0;
		int depthSum = 0;
		while (frBase.hasMoreLines()) {
			line = frBase.readLine();
			tokens = line.split(RegExp.TAB);
			covered++;
			depthSum += Base.maxLikelyBaseCov(
					tokens[Base.A], tokens[Base.C], tokens[Base.G], tokens[Base.T], tokens[Base.D]);
		}
		float coverage = (float) covered * 100 / range;
		float avgDepthCov = (float) depthSum / covered;
		float avgDepthRange = (float) depthSum / range;
		fm.writeLine("\t" + covered + "\t" + String.format("%,.2f", coverage)+ "\t" +  depthSum + "\t"
				+ String.format("%,.2f", avgDepthCov) + "\t" + String.format("%,.2f", avgDepthRange));
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar baseToCoverageByRegion.jar <in.base> <chr:start-end> <out>");
		System.out.println("\t<in.base>: base by target region");
		System.out.println("\t<out>: CHR\tSTART\tEND\tTARGET_ID(in.base.name)\tTARGET_LEN\tCOVERED_BASES\tCOVERAGE\tTOTAL_DEPTH\tAVG.DEPTH_COVERED\tAVG.DEPTH");
		System.out.println("Arang Rhie, 2015-11-20. arrhie@gmail.com");
	}

	private static String region;
	public static void main(String[] args) {
		if (args.length == 3) {
			region = args[1];
			new ToCoverageByRegion().go(args[0], args[2]);
		} else {
			new ToCoverageByRegion().printHelp();
		}

	}

}
