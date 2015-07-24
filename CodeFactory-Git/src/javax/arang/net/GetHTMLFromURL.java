package javax.arang.net;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class GetHTMLFromURL{
	public static void main(String args[])   {
		try{
			URL url = new URL("http://147.46.154.130:50030/jobtracker.jsp");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while(in.ready() && !in.readLine().startsWith("<h2 id=\"running_jobs\">")){
			}
			System.out.println("<h2 id=\"running_jobs\">Running Jobs</h2>");
			int skip = 0;
			while(in.ready() && (skip < 2)) {
				if ((line = in.readLine()).contains("<hr>")) {
					skip++;
				} else if (line.startsWith("<tr><td id=\"job_") && !line.contains("\">hadoop</td><td id=\"name_")){
					// skip
					continue;
				} else {
					System.out.println(line);
				}
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
}