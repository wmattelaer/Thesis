package probabilistic;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import prolog.PrologQuery;
import rcl.RCLExpression;
import converter.RCLPrologConverter;


public class ProblogChecker {
	
	public static File directory = new File("/home/willem/Desktop");
	public static String problogLocation = "/home/willem/Downloads/problog2/src/problog.py";
	public static String environmentFile = "environment-prob.pl";
	public static String sceneFile = "scene-prob.pl";
	
	private ProbabilisticBelief belief;
	
	private Map<PrologQuery, Double> cachedProbabilities = new HashMap<PrologQuery, Double>();
	private double cachedGripperObjectProbability = -1;
	
	private RCLPrologConverter converter = new RCLPrologConverter();
	
	public ProblogChecker(ProbabilisticBelief belief) {
		setBelief(belief);
	}

	public ProbabilisticBelief getBelief() {
		return belief;
	}

	private void setBelief(ProbabilisticBelief belief) {
		this.belief = belief;
	}
	
	public double probabilityOfGripperObject() {
		if (cachedGripperObjectProbability != -1) {
			return cachedGripperObjectProbability;
		}
		
		BufferedReader in;
		PrintWriter writer;
		try {
			in = new BufferedReader(new FileReader(new File(directory, environmentFile)));
			writer = new PrintWriter(new File(directory, sceneFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
			return 0;
		}
		
		writer.write(belief.toString());
		
		writer.print("\n");
		
		try {
			String line;
			while ((line = in.readLine()) != null) {
				writer.print(line+"\n");
			}
			
			writer.print("\n");
			writer.print("test :- gripper(X), object(X, _, _, _, _, _).\n");
			writer.print("query(test).");
			
			in.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		
		double prob = runProblog("gripper(X), object(X, _, _, _, _, _).");
		cachedGripperObjectProbability = prob;
		return prob;
	}
	
	public double probabilityOfEntity(RCLExpression expression) {
		PrologQuery query = converter.convert(expression);
		if (query != null) {
			if (cachedProbabilities.containsKey(query)) {
				return cachedProbabilities.get(query);
			} else {
				for (PrologQuery q : cachedProbabilities.keySet()) {
					if (cachedProbabilities.get(q) == 0 && q.moreGeneralThan(query)) {
						return 0;
					}
				}
			}
			
			BufferedReader in;
			PrintWriter writer;
			try {
				in = new BufferedReader(new FileReader(new File(directory, environmentFile)));
				writer = new PrintWriter(new File(directory, sceneFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				
				return 0;
			}
			
			writer.write(belief.toString());
			
			writer.print("\n");
			
			try {
				String line;
				while ((line = in.readLine()) != null) {
					writer.print(line+"\n");
				}
				
				writer.print("\n");
				writer.print("test :- " + query + "\n");
				writer.print("query(test).");
				
				in.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			}
			
			double prob = runProblog(query.toString());
			cachedProbabilities.put(query, prob);
			return prob;
			
		} else {
			return 0;
		}
	}
	
	private double runProblog(String query) {
		ProcessBuilder pb = new ProcessBuilder("python3", problogLocation, sceneFile);
		pb.directory(directory);
		pb.redirectErrorStream(true);
		
		Process process = null;
		
		try {
			process = pb.start();
			ProcessReadThread t = new ProcessReadThread(process);
			Thread thread = new Thread(t);
			thread.start();
			thread.join(50 * 1000);
			String line = t.getOutput();
			double result = 0.0001;
			if (line != null) {
				int start = line.indexOf("'test': ");
				int end = line.indexOf("}");
				if (start == -1 || end == -1) {
					System.err.println("ERROR: " + query);
				} else {
					String temp = line.substring(start + "'test': ".length(), end);
					if (!temp.equals("nan")) {
						result = Double.parseDouble(temp);
					}
				}
			} else {
				System.err.println("TIMEOUT: " + query);
				result = 0.0001;
			}
			t.abort();
			process.destroy();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			process.destroy();
			return 0;
		}
	}

}
