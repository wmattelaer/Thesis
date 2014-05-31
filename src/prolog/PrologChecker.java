package prolog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import rcl.RCLExpression;

import com.trainrobots.nlp.scenes.Shape;
import com.trainrobots.nlp.scenes.WorldModel;

import converter.RCLPrologConverter;


public class PrologChecker {
	
	private static final File directory = new File("/Users/Willem/Desktop");
	
	private Process process;
	private Writer processWriter;
	private BufferedReader processReader;
	
	private RCLPrologConverter converter;
	
	private WorldModel world;

	public PrologChecker(WorldModel world) {
		this.world = world;
	}
	
	public void start() {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new File(directory, "scene.pl"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		int i = 1;
		for (Shape shape : world.shapes()) {
			writer.println("object_l(o"+i+", " + shape.color()+ ", "+shape.type() + ", " + shape.position().x + ", " + shape.position().y + ", " + shape.position().z + ").");
			i++;
		}
		writer.close();
		
		ProcessBuilder pb = new ProcessBuilder("/usr/local/bin/swipl", "-q", "-s", "environment.pl", "scene.pl");
		pb.directory(directory);
		pb.redirectErrorStream(true);
		
		try {
			process = pb.start();
			processWriter = new OutputStreamWriter( process.getOutputStream() );
			processReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		converter = new RCLPrologConverter();
	}
	
	public void stop() {
		try {
			processWriter.write("halt.\n");
			processWriter.flush();
			processWriter.close();
			processReader.close();
			process.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void reset() {
		converter = new RCLPrologConverter();
	}
	
	public boolean verifyEntity(RCLExpression entityExpression) {
		String query = converter.convert(entityExpression).toString();
		if (query != null) {
			try {
				processWriter.write(query + "\n");
				processWriter.write("\n");
				processWriter.flush();
				while (!processReader.ready()) {
				}
				if (processReader.ready()) {
					String line = processReader.readLine();
					if (line.startsWith("failed")) {
						return false;
					}
				}
				
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
}
