import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import parsing.AbstractParser;
import parsing.CompleteParser;
import rcl.AbstractRCLExpression;
import rcl.RCLCreator;

import com.trainrobots.core.corpus.Command;
import com.trainrobots.core.nodes.Node;
import com.trainrobots.nlp.scenes.SceneManager;
import com.trainrobots.nlp.scenes.WorldModel;

import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.parser.IParse;


public class CustomTester {
	
	private AbstractParser parser;
	private List<TestCommand> testCommands;
	
	public CustomTester(AbstractParser parser, File file) throws IOException {
		this.parser = parser;
		this.testCommands = new ArrayList<CustomTester.TestCommand>();
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		String currentSentence = null;
		int sceneNumber = -1;
		while ((line = in.readLine()) != null) {
			if (line.startsWith("//") || line.equals("")) {
				// Case comment or empty line, skip
				continue;
			}
			line = line.trim();
			if (currentSentence == null) {
				currentSentence = line;
			} else if (sceneNumber == -1) {
				sceneNumber = Integer.parseInt(line);
			} else {
				AbstractRCLExpression expression = RCLCreator.createFromString(line);
				this.testCommands.add(new TestCommand(currentSentence, expression, sceneNumber));
				currentSentence = null;
				sceneNumber = -1;
			}
		}
		in.close();
	}
	
	public CustomTester(CompleteParser parser, List<Command> testCommands) {
		this.parser = parser;
		this.testCommands = new ArrayList<CustomTester.TestCommand>();
		for (Command command : testCommands) {
			Node node = command.rcl.toNode();
			removeAlignment(node);
			AbstractRCLExpression expression = RCLCreator.createFromString(node.toString());
			
			this.testCommands.add(new TestCommand(command.text, expression, command.sceneNumber));
		}
	}
	
	public void test(boolean expressive) {
		System.out.println("Testing:");
		int counter = 1;
		
		int correct = 0;
		float partialCorrect = 0;
		int wrongHas = 0;
		int wrongNo = 0;
		int noParseHas = 0;
		int noParseNo = 0;
		
		for (TestCommand command : testCommands) {
			/*if (!command.text.equals("place the yellow pyramid on top of the light grey brick")) {
				continue;
			} else {
				System.out.println("bla");
			}*/
			WorldModel world = SceneManager.getScene(command.sceneNumber).before;
			AbstractRCLExpression resultExpr = parser.parse(command.text, world);
			AbstractRCLExpression expression = command.rcl;
			
			if (resultExpr == null) {
				System.out.println(counter + " : =============");
				System.out.println(command.text);
				System.out.println(expression);
				System.out.println("NO PARSE");
				
				List<AbstractRCLExpression> allParses = parser.getAllParses(command.text);
				boolean found = false;
				for (AbstractRCLExpression parse : allParses) {
					if (parse.equals(expression)) {
						System.out.println("HAS CORRECT");
						noParseHas++;
						
						found = true;
						break;
					}
				}
				if (!found) {
					System.out.println("NO CORRECT");
					noParseNo++;
					
					for (AbstractRCLExpression parse : allParses) {
						System.out.println(parse);
					}
				}
			} else {
				if (resultExpr.equals(expression)) {
					correct++;
					partialCorrect++;
					if (expressive) {
						System.out.println(counter + " : =============");
						System.out.println(command.text);
						System.out.println(expression);
						System.out.println("CORRECT");
						System.out.println(resultExpr);
					}
				} else {
					float correctness = (float) resultExpr.numberOfCorrect(expression) / (float) resultExpr.size();
					partialCorrect += correctness;
					System.out.println(counter + " : =============");
					System.out.println(command.text);
					System.out.println(expression);
					System.out.println("WRONG");
					System.out.println(resultExpr);
					
					List<AbstractRCLExpression> allParses = parser.getAllParses(command.text);
					boolean found = false;
					for (AbstractRCLExpression parse : allParses) {
						if (parse.equals(expression)) {
							System.out.println("HAS CORRECT");
							wrongHas++;
							
							found = true;
							break;
						}
					}
					if (!found) {
						System.out.println("NO CORRECT");
						wrongNo++;
						
						for (AbstractRCLExpression parse : allParses) {
							System.out.println(parse);
						}
					}
				}
			}
			
			counter++;
		}
		
		System.out.println("\n\n\n");
		System.out.println("CORRECT: " + correct);
		System.out.println("PARTIAL CORRECT: " + partialCorrect);
		System.out.println("WRONG: " + (wrongHas + wrongNo));
		System.out.println("\t- HAS CORRECT: " + wrongHas);
		System.out.println("\t- NO CORRECT: " + wrongNo);
		System.out.println("NO PARSE: " + (noParseHas + noParseNo));
		System.out.println("\t- HAS CORRECT: " + noParseHas);
		System.out.println("\t- NO CORRECT: " + noParseNo);
		
	}
	
	public void testPartial(boolean expressive) {
		System.out.println("Testing:");
		int counter = 1;
		
		float correct = 0;
		int wrongHas = 0;
		int wrongNo = 0;
		int noParseHas = 0;
		int noParseNo = 0;
		
		for (TestCommand command : testCommands) {
			/*if (!command.text.equals("place the yellow pyramid on top of the light grey brick")) {
				continue;
			} else {
				System.out.println("bla");
			}*/
			WorldModel world = SceneManager.getScene(command.sceneNumber).before;
			AbstractRCLExpression resultExpr = parser.parse(command.text, world);
			AbstractRCLExpression expression = command.rcl;
			
			if (resultExpr == null) {
				System.out.println(counter + " : =============");
				System.out.println(command.text);
				System.out.println(expression);
				System.out.println("NO PARSE");
				
				List<AbstractRCLExpression> allParses = parser.getAllParses(command.text);
				boolean found = false;
				for (AbstractRCLExpression parse : allParses) {
					if (parse.equals(expression)) {
						System.out.println("HAS CORRECT");
						noParseHas++;
						
						found = true;
						break;
					}
				}
				if (!found) {
					System.out.println("NO CORRECT");
					noParseNo++;
					
					for (AbstractRCLExpression parse : allParses) {
						System.out.println(parse);
					}
				}
			} else {
				if (resultExpr.equals(expression)) {
					correct++;
					if (expressive) {
						System.out.println(counter + " : =============");
						System.out.println(command.text);
						System.out.println(expression);
						System.out.println("CORRECT");
						System.out.println(resultExpr);
					}
				} else {
					float correctness = (float) resultExpr.numberOfCorrect(expression) / (float) resultExpr.size();
					correct += correctness;
					System.out.println(counter + " : =============");
					System.out.println(command.text);
					System.out.println(expression);
					System.out.println("WRONG (" + correctness + ")");
					System.out.println(resultExpr);
					
					List<AbstractRCLExpression> allParses = parser.getAllParses(command.text);
					boolean found = false;
					for (AbstractRCLExpression parse : allParses) {
						if (parse.equals(expression)) {
							System.out.println("HAS CORRECT");
							wrongHas++;
							
							found = true;
							break;
						}
					}
					if (!found) {
						System.out.println("NO CORRECT");
						wrongNo++;
						
						for (AbstractRCLExpression parse : allParses) {
							System.out.println(parse);
						}
					}
				}
			}
			
			counter++;
		}
		
		System.out.println("\n\n\n");
		System.out.println("CORRECT: " + correct);
		System.out.println("WRONG: " + (wrongHas + wrongNo));
		System.out.println("\t- HAS CORRECT: " + wrongHas);
		System.out.println("\t- NO CORRECT: " + wrongNo);
		System.out.println("NO PARSE: " + (noParseHas + noParseNo));
		System.out.println("\t- HAS CORRECT: " + noParseHas);
		System.out.println("\t- NO CORRECT: " + noParseNo);
		
	}
	
	private static void removeAlignment(Node node) {
		if (node.children != null) {
			for (int i = node.children.size() - 1; i >= 0; i--) {
				Node child = node.children.get(i);
				if (child.tag.equals("token:")) {
					node.children.remove(i);
				} else {
					removeAlignment(child);
				}
			}
		}
	}
	
	
	private class TestCommand {
		
		public String text;
		public AbstractRCLExpression rcl;
		public int sceneNumber;
		
		public TestCommand(String text, AbstractRCLExpression rcl, int sceneNumber) {
			this.text = text;
			this.rcl = rcl;
			this.sceneNumber = sceneNumber;
		}
		
	}

}
