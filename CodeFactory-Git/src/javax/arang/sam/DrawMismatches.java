package javax.arang.sam;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.R2wrapper;
import javax.arang.IO.basic.FileReader;
import javax.arang.IO.basic.RegExp;
import javax.arang.genome.fasta.Seeker;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class DrawMismatches extends R2wrapper {

	protected static final int Y_REF = 0;
	protected static final int BASE_LEN = 20;
	protected static final int BASE_HEIGHT = 1000;
	protected static final int DEL_HEIGHT = (int) (BASE_HEIGHT * 0.8);	// 80% of base height
	protected static final int Y_READ = BASE_HEIGHT * 2;
	protected static final int WINDOW_WIDTH = 800;
	protected static final int CANVAS_HEIGHT = BASE_HEIGHT + Y_READ;
	// protected static final int CANVAS_HEIGHT = Y_READ * 2
	
	@Override
	public void hooker(FileReader frRef, FileReader frSam) {
		String line;
		String[] tokens;
		
		int pos;
		int posRef;
		int posRead;
		String readName;
		String cigar;
		String seq;
		char refBase;
		char readBase;
		ArrayList<int[]> cigarPosList;
		int[] cigarArr;
		Seeker refFaSeeker; //= new Seeker(frRef);
		int delLen;
		
		DrawPanel panel = new DrawPanel();
		panel.setBaseColors();
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().add(panel);
        window.setVisible(true);
		int matched;
		while (frSam.hasMoreLines()) {
			line = frSam.readLine();
			if (line.startsWith("@")) {
				System.out.println("[DEBUG] :: " + line);
				continue;
			}
			frRef.reset();
			refFaSeeker = new Seeker(frRef);
			System.out.println("Start reading " + refFaSeeker.getFaName());
			System.out.println("[DEBUG] :: " + line);
			tokens = line.split(RegExp.TAB);
			readName = tokens[Sam.QNAME];
			pos = Integer.parseInt(tokens[Sam.POS]);
			posRef = pos;
			posRead = pos;
			cigar = tokens[Sam.CIGAR];
			seq = tokens[Sam.SEQ];
			matched = SAMUtil.getMappedBases(cigar);
			panel.removeBases();
			if (!isRegionGiven) {
				start = pos;
				end = start + matched;
			} else if (isRegionGiven) {
				matched = (end - start);
			}
			System.out.println("Fasta length to draw: " + matched);
			window.setSize(WINDOW_WIDTH, 300);
			panel.setTotalLen(matched, matched + 10);
			
			cigarPosList = Sam.getAllPosition(pos, cigar);
			for (int i = 0; i < cigarPosList.size(); i++) {
				cigarArr = cigarPosList.get(i);
				if (cigarArr[Sam.CIGAR_POS_TYPE] == Sam.S) {
					continue;
				}
//				System.out.println("[DEBUG] :: " + Sam.getCigarType(cigarArr[Sam.CIGAR_POS_TYPE]) + "\t"
//						+ cigarArr[Sam.CIGAR_POS_ALGN_RANGE_START] + "\t"
//						+ cigarArr[Sam.CIGAR_POS_ALGN_RANGE_END] + "\t"
//						+ cigarArr[Sam.CIGAR_POS_REF_START] + "\t"
//						+ cigarArr[Sam.CIGAR_POS_REF_END]);
				if (cigarArr[Sam.CIGAR_POS_TYPE] == Sam.M) {
					posRef = cigarArr[Sam.CIGAR_POS_REF_START];
					for (posRead = cigarArr[Sam.CIGAR_POS_ALGN_RANGE_START]; posRead <= cigarArr[Sam.CIGAR_POS_ALGN_RANGE_END]; posRead++) {
						refBase = refFaSeeker.baseAt(posRef);
						readBase = seq.charAt(posRead);
						if (readBase != refBase) {
							drawMismatch(panel, posRef, "M",  refBase, readBase);
						}
						posRef++;
					}
				}
				if (cigarArr[Sam.CIGAR_POS_TYPE] == Sam.I) {
					posRef = cigarArr[Sam.CIGAR_POS_REF_END];
					drawIndel(panel, posRef, "I", seq.substring(cigarArr[Sam.CIGAR_POS_ALGN_RANGE_START], cigarArr[Sam.CIGAR_POS_ALGN_RANGE_END] + 1));
				}
				if (cigarArr[Sam.CIGAR_POS_TYPE] == Sam.D) {
					posRef = cigarArr[Sam.CIGAR_POS_REF_START];
					delLen = cigarArr[Sam.CIGAR_POS_REF_END] - cigarArr[Sam.CIGAR_POS_REF_START] + 1;
					drawIndel(panel, posRef, "D", refFaSeeker.getBases(posRef, delLen));
				}
			}
			panel.saveImage(readName);
		}
	}
	
	private void drawMismatch(DrawPanel panel, int posRef, String type, char refBase, char readBase) {
		if (start < posRef && posRef <= end) {
			System.out.println(posRef + "\t" + type + "\t" + refBase + "\t" + readBase);
			int x = (posRef - start);
			panel.paint(x, Y_REF, Character.toUpperCase(refBase) + "");
			panel.paint(x, Y_READ, Character.toUpperCase(readBase) + "");
		}
	}
	
	private void drawIndel(DrawPanel panel, int posRef, String type, String bases) {
		if (start < posRef && posRef <= end) {
			System.out.println(posRef + "\t" + type + "\t" + bases);
			int x = (posRef - start);
			for (int i = 0; i < bases.length(); i++) {
				if (type.equals("D")) {
					panel.paint(x, Y_REF, Character.toUpperCase(bases.charAt(i)) + "");
					panel.paint(x, Y_READ, "D");
				} else if (type.equals("I")) {
					panel.paint(x, Y_REF, "D");
					panel.paint(x, Y_READ, Character.toUpperCase(bases.charAt(i)) + "");
				}
				i++;
			}
		}
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar samDrawMismatches.jar <ref.fa> <in.sam> <out_png_prefix> [start end]");
		System.out.println("\t<ref.fa>: reference fasta. Should be only 1 contig.");
		System.out.println("\t<in.sam>: sam file aligned against <ref.fa>");
		System.out.println("\t\t*Should only contain read.");
		System.out.println("\t<out_png_prefix>: output prefix of .png file. <out_png_prefix>.readid.png will be generated.");
		System.out.println("\t\tMatches and Mismatches will be drawn only for the M/D/I region in cigar field.");
		System.out.println("\t[start end]: only report regions from start (0-based) to end (1-based).");
		System.out.println("\t\twhen not given, plot for the matched region will be generated.");
		System.out.println("Arang Rhie, 2016-01-11. arrhie@gmail.com");
	}

	protected static String outFilePrefix;
	private static boolean isRegionGiven = false;
	private static int start = 0;
	private static int end;
	public static void main(String[] args) {
		if (args.length == 3) {
			outFilePrefix = args[2];
			new DrawMismatches().go(args[0], args[1]);
		} else if (args.length == 5) {
			isRegionGiven = true;
			start = Integer.parseInt(args[3].replace(",", ""));
			end = Integer.parseInt(args[4].replace(",", ""));
			outFilePrefix = args[2];
			new DrawMismatches().go(args[0], args[1]);
		} else {
			new DrawMismatches().printHelp();
		}
	}

}


class DrawPanel extends JComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int x;
	int width;

	int totalLen;
	int windowsize;
	int COLOR_LEGEND_OFFSET = 10;
	int COLOR_LEGEND_LEN_X = 20;
	int COLOR_LEGEND_LEN_Y = 30;

	
	ArrayList<Point> baseComponent = new ArrayList<Point>();
	ArrayList<Color> colorList = new ArrayList<Color>();
	HashMap<String, Color> baseColors = new HashMap<String, Color>();
	ArrayList<String> baseList = new ArrayList<String>();
	
	public void setTotalLen(int len, int windowSize) {
		this.totalLen = len;
		this.width = len;
		this.windowsize = windowSize;
	}
	
	public void setPos(int x, int width) {
		this.x = x;
		this.width = width;
	}
	
	public void removeBases() {
		baseComponent = new ArrayList<Point>();
	}
	@Override
    public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect (0, 0, windowsize, DrawMismatches.CANVAS_HEIGHT);
        
		for (int i = 0; i < baseComponent.size(); i++) {
			Point baseComp = baseComponent.get(i);
			g.setColor(colorList.get(i));
			if (colorList.get(i) == baseColors.get("D")) {
				g.fillRect (baseComp.x, baseComp.y + ((DrawMismatches.BASE_HEIGHT - DrawMismatches.DEL_HEIGHT) / 2), DrawMismatches.BASE_LEN, DrawMismatches.DEL_HEIGHT);
			} else {
				g.fillRect (baseComp.x, baseComp.y, DrawMismatches.BASE_LEN, DrawMismatches.BASE_HEIGHT);
			}
		}
		
		// A T G C Color Legend
//		for (int i = 0; i < baseList.size(); i++) {
//			g.setColor(baseColors.get(baseList.get(i)));
//			// g.fillRect(x, y, width, height)
//			g.fillRect (COLOR_LEGEND_OFFSET, DrawMismatches.Y_READ * 2 + ((i + 1) * COLOR_LEGEND_LEN_Y), COLOR_LEGEND_LEN_X, COLOR_LEGEND_LEN_Y - (COLOR_LEGEND_OFFSET / 2));
//			// g.drawString(text, x, y)
//			g.drawString(baseList.get(i), COLOR_LEGEND_OFFSET * 2 + COLOR_LEGEND_LEN_Y, (int) (DrawMismatches.Y_READ * 2 + ((i + 2) * COLOR_LEGEND_LEN_Y) - (COLOR_LEGEND_OFFSET / 2))); //  + 2.8
//		}
    }
	
	public void paint(int x, int width, String base) {
		//baseComponent.add(new Point(((windowsize * x) / totalLen), (windowsize * width)/totalLen));
		baseComponent.add(new Point(x, width));
		//System.out.println("(" + x + " , " + width + ")");
		colorList.add(baseColors.get(base));
		repaint();
	}
	
	public void saveImage(String qname) {
		BufferedImage bi = new BufferedImage(width, DrawMismatches.CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB); 
		Graphics g = bi.createGraphics();
		this.paint(g);  //this == JComponent
		g.dispose();
		try {
			ImageIO.write(bi,"png", new File(DrawMismatches.outFilePrefix + ".png"));
//			if (qname.contains("/")) {
//				ImageIO.write(bi,"png", new File(DrawMismatches.outFilePrefix + "." + qname.substring(0, qname.indexOf("/")) + ".png"));
//			} else if (qname.contains(":")) {
//				ImageIO.write(bi,"png", new File(DrawMismatches.outFilePrefix + "." + qname.substring(0, qname.indexOf(":")) + ".png"));
//			}
//			else {
//				ImageIO.write(bi,"png", new File(DrawMismatches.outFilePrefix + "." + qname + ".png"));
//			}
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	
	public void setBaseColors() {
		baseColors.put("A", Color.RED);
		baseList.add("A");
		baseColors.put("T", Color.ORANGE);
		baseList.add("T");
		baseColors.put("G", Color.BLUE);
		baseList.add("G");
		baseColors.put("C", Color.GREEN);
		baseList.add("C");
		baseColors.put("D", Color.BLACK);	// Violet
		baseList.add("D");
//		baseColors.put("Low_complexity", new Color(0,80,239));	// Cobalt
//		repeatList.add("Low_complexity");
//		baseColors.put("DNA", new Color(135, 121, 78));	// Brown
//		repeatList.add("DNA");
	}
}
