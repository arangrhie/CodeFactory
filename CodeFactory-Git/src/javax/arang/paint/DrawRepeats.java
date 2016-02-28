package javax.arang.paint;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import javax.arang.IO.Rwrapper;
import javax.arang.IO.basic.FileReader;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class DrawRepeats extends Rwrapper {

	static final int WINDOW_SIZE = 1020;
	
	@Override
	public void hooker(FileReader fr) {
		
		DrawPanel panel = new DrawPanel();

		String line;
		String[] tokens;
		
		final int CHR_POS = 4;
		final int BEGIN = 5;
		final int END = 6;
		final int REPEAT_TYPE = 10;
		
		
		HashMap<Integer, Integer>	posMap = new HashMap<Integer, Integer>();
		HashMap<Integer, String>	classMap = new HashMap<Integer, String>();
		ArrayList<Integer>	posList = new ArrayList<Integer>();
		ArrayList<String>	classList = new ArrayList<String>();	
		ArrayList<String>	repeatName = new ArrayList<String>();
		
		int totalLen = 0;
		boolean knowFaPos = false;
		int begin;
		int end;
		String repeat;
		while (fr.hasMoreLines()) {
			line = fr.readLine();
			tokens = line.split("\t");
			if (!knowFaPos) {
				String faPos = tokens[CHR_POS];
				System.out.println(faPos);
				String[] pos = faPos.split("[:-]+");
				int faStart = Integer.parseInt(pos[1]);
				int faEnd = Integer.parseInt(pos[2]);
				totalLen = faEnd - faStart + 1;
				knowFaPos = true;
			}
			begin = Integer.parseInt(tokens[BEGIN]);
			end = Integer.parseInt(tokens[END]);
			repeat = tokens[REPEAT_TYPE];
			if (!repeatName.contains(tokens[REPEAT_TYPE])) {
				repeatName.add(repeat);
				System.out.println(repeat);
			}
			if (repeat.contains("/")) {
				repeat = repeat.substring(0, repeat.indexOf("/"));
			}
			
			
			posList.add(begin);
			posMap.put(begin, end);
			classMap.put(begin, repeat);
			if (!classList.contains(repeat)) {
				classList.add(repeat);
			}
		}
		
		panel.setTotalLen(totalLen, WINDOW_SIZE);
		panel.setRepeatColors();
		
		System.out.println(totalLen);
		for (int i = 0; i < classList.size(); i++) {
			System.out.println(classList.get(i));
		}
		
		
		JFrame window = new JFrame();
        window.setSize(WINDOW_SIZE,100);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().add(panel);
        window.setVisible(true);
        for (int i = 0; i < posList.size(); i++) {
        	int pos = posList.get(i);
        	panel.paint(pos, posMap.get(pos), classMap.get(pos));
        }
		
	}

	@Override
	public void printHelp() {
		System.out.println("Usage: java -jar paintDrawRepeat.jar <repeatmasker.out>");
		System.out.println("\tDraw a pannel drawing the repeats");
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			new DrawRepeats().go(args[0]);
		} else {
			new DrawRepeats().printHelp();
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
	int OFFSET = 10;
	
	ArrayList<Point> repeatComponent = new ArrayList<Point>();
	ArrayList<Color> colorList = new ArrayList<Color>();
	HashMap<String, Color> repeatColors = new HashMap<String, Color>();
	ArrayList<String> repeatList = new ArrayList<String>();
	
	public void setTotalLen(int len, int windowSize) {
		this.totalLen = len;
		this.windowsize = windowSize;
	}
	
	public void setPos(int x, int width) {
		this.x = x;
		this.width = width;
	}
	
	@Override
    public void paint(Graphics g) {
		g.setColor(Color.white);
		g.fillRect (0, 0, windowsize*2, 400); 
        
		for (int i = 0; i < repeatComponent.size(); i++) {
			Point repeatComp = repeatComponent.get(i);
			g.setColor(colorList.get(i));
			g.fillRect (repeatComp.x, 10, repeatComp.y, 30);
		}
		
		for (int i = 0; i < repeatList.size(); i++) {
			g.setColor(repeatColors.get(repeatList.get(i)));
			g.fillRect (10, 50 + (i * 30), 30, 30);
		}
    }
	
	public void paint(int x, int width, String repeat) {
		repeatComponent.add(new Point(((windowsize * x) / totalLen), (windowsize * width)/totalLen));
		colorList.add(repeatColors.get(repeat));
		//System.out.println(x + "\t" + width + "\t" + repeat);
		repaint();
	}
	
	public void setRepeatColors() {
		repeatColors.put("LTR", Color.MAGENTA);
		repeatList.add("LTR");
		repeatColors.put("rRNA", new Color(162,0,37));
		repeatList.add("rRNA");
		repeatColors.put("SINE", new Color(11,72,11));
		repeatList.add("SINE");
		repeatColors.put("Simple_repeat", new Color(236,120,42));
		repeatList.add("Simple_repeat");
		repeatColors.put("LINE", new Color(153,51,255));	// Violet
		repeatList.add("LINE");
		repeatColors.put("Low_complexity", new Color(0,80,239));	// Cobalt
		repeatList.add("Low_complexity");
		repeatColors.put("DNA", new Color(135, 121, 78));	// Brown
		repeatList.add("DNA");
	}
}
