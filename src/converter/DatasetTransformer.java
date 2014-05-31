package converter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import rcl.AbstractRCLExpression;
import rcl.RCLCreator;
import rcl.RCLExpression;

import com.trainrobots.core.corpus.Command;
import com.trainrobots.core.nodes.Node;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;


public class DatasetTransformer {
	
	private List<Command> commands;
	
	private StanfordCoreNLP pipeline;
	
	private static final int TRAIN_SIZE = 2500;
	
	public DatasetTransformer(List<Command> commands) {
		this.commands = commands;
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    props.put("parse.model", "edu/stanford/nlp/models/lexparser/englishRNN.ser.gz");
	    this.pipeline = new StanfordCoreNLP(props);
	}
	
	public void transform(File trainFile, File testFile, boolean debug) throws FileNotFoundException {
		int failedCommands = 0;
		
		PrintWriter trainWriter = new PrintWriter(trainFile);
		PrintWriter testWriter = new PrintWriter(testFile);
		
		
		RCLLambdaConverter converter = new RCLLambdaConverter(true);
		
		int i = 0;
		
		for (Command command : commands) {
			if (i < TRAIN_SIZE) {
				if (command.rcl == null) {
					continue;
				}

				if (debug) {
					System.out.println(command.id);
					System.out.println(command.text);
				}
				Node node = command.rcl.toNode();
				removeAlignment(node);
				if (debug) {
					System.out.println(node);
				}
				
				AbstractRCLExpression expression = RCLCreator.createFromString(node.toString());
				
				try {
					String lambda = converter.convert((RCLExpression) expression);
					String commandText = applyCoreferenceResolution(cleanup(command.text));
					commandText = applyCoreferenceResolution2(commandText);
					commandText = applyCoreferenceResolution3(commandText);
					commandText = applyCoreferenceResolution4(commandText);
					if (debug) {
						System.out.println(lambda);
					}
					trainWriter.println(commandText);
					trainWriter.println(lambda);
					trainWriter.println();
				} catch (IllegalArgumentException exception) {
					failedCommands++;
					if (debug) {
						System.out.println("FAILED!!");
					}
				}
				
				
				if (debug) {
					System.out.println();
				}
			} else {
				if (command.rcl == null) {
					continue;
				}

				if (debug) {
					System.out.println(command.id);
					System.out.println(command.text);
				}
				Node node = command.rcl.toNode();
				removeAlignment(node);
				if (debug) {
					System.out.println(node);
				}
				
				AbstractRCLExpression expression = RCLCreator.createFromString(node.toString());
				
				try {
					String commandText = applyCoreferenceResolution(cleanup(command.text));
					commandText = applyCoreferenceResolution2(commandText);
					commandText = applyCoreferenceResolution3(commandText);
					commandText = applyCoreferenceResolution4(commandText);
					
					testWriter.println(commandText);
					testWriter.println(command.sceneNumber);
					testWriter.println(expression);
					testWriter.println();
				} catch (IllegalArgumentException exception) {
					failedCommands++;
					if (debug) {
						System.out.println("FAILED!!");
					}
				}
				
				
				if (debug) {
					System.out.println();
				}
			}
			
			i++;
		}
		
		trainWriter.close();
		testWriter.close();
		
		System.out.println("Failures: " + failedCommands + " / " + commands.size());
	}
	
	private String applyCoreferenceResolution(String string) {
		Annotation annotation = new Annotation(string);
	    this.pipeline.annotate(annotation);
	    
	    Map<Integer, CorefChain> graph = annotation.get(CorefChainAnnotation.class);
	    List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
	    
	    StringBuilder builder = new StringBuilder(string);
	    
	    for(Map.Entry<Integer, CorefChain> entry : graph.entrySet()) {
	    	CorefChain c = entry.getValue();
	    	
	    	List<CorefMention> cms = c.getMentionsInTextualOrder();
	    	if (cms.size() > 1) {
	    		for (CorefMention it : cms) {
	    			ArrayCoreMap sentence = (ArrayCoreMap) sentences.get(it.sentNum-1);
    		    	List<CoreLabel> senTokens = sentence.get(TokensAnnotation.class);
    		    	int end = senTokens.get(it.endIndex-2).endPosition();
	    			String temp = "";
	    			for (int i = it.startIndex; i < it.endIndex; i++) {
	    				if (i > it.startIndex) {
			    			temp += " ";
			    		}
			    		temp += senTokens.get(i-1).originalText();
			    	}
	    			
	    			if (temp.equals("it") || temp.equals("that") || temp.equals("this")) {
	    				builder.insert(end, " (1)");
	    				String types[] = {"tetrahedron", "brick", "slab", "block", "cub", "parallelipiped", "border", "prism", "corner", "pyramid", "cubes", "robot", "blocks", "tower", "floor", "triangle", "box", "square", "cube", "stack", "step"};
	    				int index = Integer.MAX_VALUE;
	    				int length = 0;
	    				for (int i = 0; i < types.length; i++) {
	    					int j = string.indexOf(" " + types[i] + " ");
	    					if (j != -1 && j < index) {
	    						index = j;
	    						length = types[i].length();
	    					}
	    				}
	    				
	    				if (length != 0) {
	    					builder.insert(index + length + 1, " [1]");
	    				}
	    				
	    				break;
	    			}
	    		}
	    		break;
	    	}
	    }

	    return builder.toString();
	}
	
	private String applyCoreferenceResolution2(String string) {
		if (string.contains(" place that ") && !string.contains(" place that (1)")) {
			StringBuilder builder = new StringBuilder(string);
			
			int index = string.indexOf(" place that ");
			builder.insert(index + " place that ".length(), "(1) ");
			
			String types[] = {"tetrahedron", "brick", "slab", "block", "cub", "parallelipiped", "border", "prism", "corner", "pyramid", "cubes", "robot", "blocks", "tower", "floor", "triangle", "box", "square", "cube", "stack", "step"};
			index = Integer.MAX_VALUE;
			int length = 0;
			for (int i = 0; i < types.length; i++) {
				int j = string.indexOf(" " + types[i] + " ");
				if (j != -1 && j < index) {
					index = j;
					length = types[i].length();
				}
			}
			
			if (length != 0) {
				builder.insert(index + length + 1, " [1]");
			}
			
			
			return builder.toString();
			
			
		} else if (string.contains(" place it ") && !string.contains(" place it (1)")) {
			StringBuilder builder = new StringBuilder(string);
			
			int index = string.indexOf(" place it ");
			builder.insert(index + " place it ".length(), "(1) ");
			
			String types[] = {"tetrahedron", "brick", "slab", "block", "cub", "parallelipiped", "border", "prism", "corner", "pyramid", "cubes", "robot", "blocks", "tower", "floor", "triangle", "box", "square", "cube", "stack", "step"};
			index = Integer.MAX_VALUE;
			int length = 0;
			for (int i = 0; i < types.length; i++) {
				int j = string.indexOf(" " + types[i] + " ");
				if (j != -1 && j < index) {
					index = j;
					length = types[i].length();
				}
			}
			
			if (length != 0) {
				builder.insert(index + length + 1, " [1]");
			}
			
			
			return builder.toString();
			
			
		} else if (string.contains(" this ") && !string.contains(" this (1)")) {
			StringBuilder builder = new StringBuilder(string);
			
			int index = string.indexOf(" this ");
			builder.insert(index + " this ".length(), "(1) ");
			
			String types[] = {"tetrahedron", "brick", "slab", "block", "cub", "parallelipiped", "border", "prism", "corner", "pyramid", "cubes", "robot", "blocks", "tower", "floor", "triangle", "box", "square", "cube", "stack", "step"};
			index = Integer.MAX_VALUE;
			int length = 0;
			for (int i = 0; i < types.length; i++) {
				int j = string.indexOf(" " + types[i] + " ");
				if (j != -1 && j < index) {
					index = j;
					length = types[i].length();
				}
			}
			
			if (length != 0) {
				builder.insert(index + length + 1, " [1]");
			}
			
			
			return builder.toString();
			
			
		} else {
			return string;
		}
	}
	
	private String applyCoreferenceResolution3(String string) {
		if (!string.contains(" one")) {
			return string;
		}
		
		Annotation annotation = new Annotation(string);
	    this.pipeline.annotate(annotation);
	    
	    List<CoreLabel> tokens = annotation.get(TokensAnnotation.class);
	    
	    StringBuilder builder = new StringBuilder(string);
		
		for (int i = 1; i < tokens.size(); i++) {
			if (tokens.get(i).originalText().equals("one")) {
				String colors[] = {"purple", "green", "white", "yellow", "gray", "cyan", "magenta", "red", "sky", "turquoise", "blue", "dark", "grey", "pink", "light"};
				List<String> col = Arrays.asList(colors);
				if (col.contains(tokens.get(i-1).originalText())) {
					builder.insert(tokens.get(i).endPosition(), " {1}");
					String types[] = {"tetrahedron", "brick", "slab", "block", "cub", "parallelipiped", "border", "prism", "corner", "pyramid", "cubes", "robot", "blocks", "tower", "floor", "triangle", "box", "square", "cube", "stack", "step"};
					List<String> typ = Arrays.asList(types);
					for (int k = 0; k < tokens.size(); k++) {
						if (typ.contains(tokens.get(k).originalText())) {
							if (!tokens.get(k+1).originalText().equals("[") && !tokens.get(k+3).originalText().equals("]")) {
								builder.insert(tokens.get(k).endPosition(), " [1]");
							}
							break;
						}
					}
				}
			}
		}
		
		return builder.toString();
	}
	
	private String applyCoreferenceResolution4(String string) {
		if (!string.contains(" ones")) {
			return string;
		}
		
		Annotation annotation = new Annotation(string);
	    this.pipeline.annotate(annotation);
	    
	    List<CoreLabel> tokens = annotation.get(TokensAnnotation.class);
	    
	    StringBuilder builder = new StringBuilder(string);
		
		for (int i = 1; i < tokens.size(); i++) {
			if (tokens.get(i).originalText().equals("ones")) {
				String colors[] = {"purple", "green", "white", "yellow", "gray", "cyan", "magenta", "red", "sky", "turquoise", "blue", "dark", "grey", "pink", "light"};
				List<String> col = Arrays.asList(colors);
				if (col.contains(tokens.get(i-1).originalText())) {
					builder.insert(tokens.get(i).endPosition(), " {1}");
					String types[] = {"tetrahedron", "brick", "slab", "block", "cub", "parallelipiped", "border", "prism", "corner", "pyramid", "cubes", "robot", "blocks", "tower", "floor", "triangle", "box", "square", "cube", "stack", "step"};
					List<String> typ = Arrays.asList(types);
					for (int k = 0; k < tokens.size(); k++) {
						if (typ.contains(tokens.get(k).originalText())) {
							if (!tokens.get(k+1).originalText().equals("[") && !tokens.get(k+3).originalText().equals("]")) {
								builder.insert(tokens.get(k).endPosition(), " [1]");
							}
							break;
						}
					}
				}
			}
		}
		
		return builder.toString();
	}
	
	private String cleanup(String string) {
		StringBuilder builder = new StringBuilder(string.toLowerCase());
		
		for (int i = 0; i < builder.length(); i++) {
			if (builder.charAt(i) == '.' || builder.charAt(i) == ',') {
				builder.deleteCharAt(i);
				i--;
			}
		}
		
		return builder.toString();
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

}
