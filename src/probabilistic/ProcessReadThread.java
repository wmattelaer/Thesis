package probabilistic;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ProcessReadThread implements Runnable {
	
	private Process process;
	private BufferedReader processReader;
	private String output = null;
	private boolean run;
	
	public ProcessReadThread(Process process) {
		this.process = process;
	}
	
	public String getOutput() {
		return this.output;
	}

	@Override
	public void run() {
		run = true;
		try {
			processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while (run && !processReader.ready()) {
				Thread.sleep(10);
			}
			if (!run) {
				return;
			}
			String line = processReader.readLine();
			this.output = line;
			processReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void abort() {
		try {
			run = false;
			processReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
