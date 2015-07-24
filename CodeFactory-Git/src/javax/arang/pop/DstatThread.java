package javax.arang.pop;


public class DstatThread extends Thread implements Runnable{

	private int seq;
	private String h1 = null;
	private String h2 = null;
	private String inFile1;
	private String inFile2;
	private float d;
	
	public DstatThread(int seq, String inFile1, String inFile2, String h1, String h2) {
		this.seq = seq;
		this.h1 = h1;
		this.h2 = h2;
		this.inFile1 = inFile1;
		this.inFile2 = inFile2;
	}
	
	@Override
	public void run() {
		Dstat dStat = new Dstat();
		dStat.h1 = this.h1;
		dStat.h2 = this.h2;
		dStat.go(inFile1, inFile2, h1 + "_" + h2 + "_" + String.format("%02d", seq) + ".d");
		d = dStat.getD();
	}
	
	public float getD() {
		return d;
	}
	
	public String getH1() {
		return h1;
	}
	
	public String getH2() {
		return h2;
	}
}
