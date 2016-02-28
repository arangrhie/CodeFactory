package javax.arang.bed;

import java.util.ArrayList;

import javax.arang.IO.I2Owrapper;
import javax.arang.IO.basic.FileMaker;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.bed.util.Bed;
import javax.arang.bed.util.Region;

public class GetConnecters extends I2Owrapper {

	@Override
	public void hooker(FileReader fr1, FileReader fr2, FileMaker fm) {
		String line;
		String[] tokens;
		ArrayList<Region> regions = new ArrayList<Region>();
		
		while (fr1.hasMoreLines()) {
			line = fr1.readLine();
			tokens = line.split(RegExp.TAB);
			regions.add(new Region(Integer.parseInt(tokens[Bed.START]), Integer.parseInt(tokens[Bed.END]), line));
		}
		
		int start;
		int end;
		Region regionOverlapped;
		String out;
		
		while (fr2.hasMoreLines()) {
			line = fr2.readLine();
			tokens = line.split(RegExp.TAB);
			start = Integer.parseInt(tokens[Bed.START]);
			end = Integer.parseInt(tokens[Bed.END]);
			regionOverlapped = null;
			for (Region region : regions) {
				if (region.isInRegion(start)) {
					if (region.isInRegion(end)) {
						// If region is containing both start, end
						continue;
					} else {
						// If only start is in region
						regionOverlapped = region;
						break;
					}
				}
			}
			// start is in region
			if (regionOverlapped != null) {
				out = (line + "\t" + regionOverlapped.getName());
				for (Region region : regions) {
					if (region.isInRegion(end)) {
						fm.writeLine(out + "\t" + region.getName());
						break;
					}
				}
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar bedGetConnecters.jar <in1.bed> <in2.bed> <out.bed>");
		System.out.println("\tGet connecting regions (bridges) for <in1.bed> from <in2.bed>. Works only per chr.");
		System.out.println("\t<in1.bed>:\t------------         ------       ------------");
		System.out.println("\t<in2.bed>:\t  ----    ------------               ----------");
		System.out.println("\t<out.bed>:\t          ------------");
		System.out.println("Arang Rhie, 2015-08-25. arrhie@gmail.com");
	}

	public static void main(String[] args) {
		if (args.length == 3) {
			new GetConnecters().go(args[0], args[1], args[2]);
		} else {
			new GetConnecters().printHelp();
		}
	}

}
