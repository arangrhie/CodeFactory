package javax.arang.test;

import java.util.Calendar;
import java.util.Random;

public class MyThread extends Thread {

	private int seq;
	private int d = 0;
	
	public MyThread(int seq) {
		this.seq = seq;
		Random randomGen = new Random();
		setD(randomGen.nextInt(100));
	}
	
	public int getD() {
		return d;
	}
	
	public void setD(int d) {
		this.d = d;
	}
	
	@Override
	public void run() {
        System.out.println(this.seq+" thread start.");
        try {
        	System.out.println(Calendar.getInstance().getTime());
            Thread.sleep(1000);
        }catch(Exception e) {
        }
        System.out.println(this.seq+" thread end.");
    }
	
}
