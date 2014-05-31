package parsing;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import probabilistic.ProbabilisticBelief;
import probabilistic.ProbabilisticTransformer;
import probabilistic.ProblogChecker;
import rcl.AbstractRCLExpression;
import rcl.RCLCreator;
import rcl.RCLExpression;
import rcl.RCLLiteral;

import com.trainrobots.nlp.scenes.Shape;
import com.trainrobots.nlp.scenes.WorldModel;

import converter.LambdaRCLConverter;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.parser.IParserOutput;
import edu.uw.cs.lil.tiny.parser.ccg.model.Model;
import edu.uw.cs.lil.tiny.parser.graph.IGraphParser;


public class BeliefParserProblog extends AbstractParser {
	
	public static double correctMean = 0.85;
	public static double correctSd = 0.1;
	public static double errorMean = -1;
	public static double errorSd = 0.1;
	
	public BeliefParserProblog(IGraphParser<Sentence, LogicalExpression> parser, Model<Sentence, LogicalExpression> model) {
		super(parser, model);
	}
	
	public AbstractRCLExpression parse(String string, WorldModel world) {
		AbstractRealDistribution errorDistribution = null;
		if (errorMean >= 0 && errorMean <= 1) {
			errorDistribution = new NormalDistribution(errorMean, errorSd);
		}
		ProbabilisticTransformer transformer = new ProbabilisticTransformer(new NormalDistribution(correctMean, correctSd), errorDistribution);
		ProbabilisticBelief belief = transformer.generateBeliefFromWorld(world);
		
		return parse(string, world, belief);
	}
	
	public AbstractRCLExpression parse(String string, WorldModel world, ProbabilisticBelief belief) {
		Sentence sentence = new Sentence(string);
		
		IParserOutput<LogicalExpression> output = parser.parse(sentence, model.createDataItemModel(sentence));
		
		lastString = string;
		lastParses = new ArrayList<AbstractRCLExpression>();
		
		Map<Integer, RCLExpression> entityMappings = new HashMap<Integer, RCLExpression>();
		
		if (output.getAllParses().size() == 0) {
			return null;
		} else {
			ProblogChecker checker = new ProblogChecker(belief);
			
			double bestScore = 0;
			AbstractRCLExpression bestExpression = null;
			for (int i = 0; i < output.getAllParses().size(); i++) {
				if (output.getAllParses().get(i).getScore() < bestScore) {
					continue;
				}
				
				LambdaRCLConverter converter = new LambdaRCLConverter(output.getAllParses().get(i).getSemantics());
				try {
					AbstractRCLExpression expression = converter.convert();
					expression.cleanup();
					((RCLExpression) expression).removeUnusedIds();
					decideAction(expression, world.getShapeInGripper());
					fixExpression(expression);
					lastParses.add(expression);
					double probability = 1;
					boolean isSequence = false;
					
					if (expression instanceof RCLExpression && ((RCLExpression) expression).getType().equals("sequence")) {
						isSequence = true;
					}
					int nbAction = 0;
					for (AbstractRCLExpression expr : expression) {
						if (expr instanceof RCLExpression && ((RCLExpression) expr).getType().equals("entity")) {
							RCLExpression entityExpr = resolveEntity((RCLExpression) expr, entityMappings);
							RCLExpression typeExpr = entityExpr.getSubExpressionWithType("type"); 
							if (typeExpr != null && (((RCLLiteral)typeExpr.getSubExpressions().get(0)).getValue().equals("reference") || ((RCLLiteral)typeExpr.getSubExpressions().get(0)).getValue().equals("region") || ((RCLLiteral)typeExpr.getSubExpressions().get(0)).getValue().equals("edge") || ((RCLLiteral)typeExpr.getSubExpressions().get(0)).getValue().equals("robot") || ((RCLLiteral)typeExpr.getSubExpressions().get(0)).getValue().equals("corner") || ((RCLLiteral)typeExpr.getSubExpressions().get(0)).getValue().equals("tile"))) {
								continue;
							}
							probability = probability * checker.probabilityOfEntity(entityExpr);
							
							RCLExpression referenceExpr = entityExpr.getSubExpressionWithType("id");
							if (referenceExpr != null) {
								entityMappings.put(Integer.parseInt(((RCLLiteral)referenceExpr.getSubExpressions().get(0)).getValue()), entityExpr);
							}
						} else if (expr instanceof RCLExpression && ((RCLExpression) expr).getType().equals("action") && !isSequence) {
							String action = ((RCLLiteral) ((RCLExpression) expr).getSubExpressions().get(0)).getValue();
							if (action.equals("drop")) {
								probability = probability * checker.probabilityOfGripperObject();
								break;
							} else if ((action.equals("move") || action.equals("take"))) {
								probability = probability * (1 - checker.probabilityOfGripperObject());
								break;
							}
						} else if (expr instanceof RCLExpression && ((RCLExpression) expr).getType().equals("action") && isSequence) {
							String action = ((RCLLiteral) ((RCLExpression) expr).getSubExpressions().get(0)).getValue();
							if (nbAction == 0) {
								if (!action.equals("take")) {
									probability = 0;
									break;
								}
							} else if (nbAction == 1) {
								if (!action.equals("drop")) {
									probability = 0;
									break;
								}
							}
							nbAction++;
						}
						
						if (expr instanceof RCLExpression && !sanityCheck2((RCLExpression) expr)) {
							probability = 0;
							break;
						}
					}
					double score = output.getAllParses().get(i).getScore() * probability;
					
					if (score > bestScore) {
						bestScore = score;
						bestExpression = expression;
					}
				} catch (Exception e) {
					
				}
			}
			
			return bestExpression;
		}
	}
	
	private boolean sanityCheck2(RCLExpression expression) {
		if (expression.getType().equals("entity")) {
			int typeCount = 0;
			for (AbstractRCLExpression exp : expression.getSubExpressions()) {
				if (exp instanceof RCLExpression && ((RCLExpression) exp).getType().equals("type")) {
					typeCount++;
				}
			}
			if (typeCount != 1) {
				return false;
			}
		} else if (expression.getType().equals("sequence")) {
			RCLExpression expr = (RCLExpression) expression.getSubExpressions().get(1);
			RCLExpression entityExpr = expr.getSubExpressionWithType("entity");
			if (entityExpr.getSubExpressionWithType("reference-id") == null) {
				return false;
			}
		} else if (expression.getType().equals("event")) {
			int destinationCount = 0;
			for (AbstractRCLExpression expr : expression.getSubExpressions()) {
				if (expr instanceof RCLExpression && ((RCLExpression) expr).getType().equals("destination")) {
					destinationCount++;
				}
			}
			if (destinationCount > 1) {
				return false;
			}
		}
		
		return true;
	}
	
	private void fixExpression(AbstractRCLExpression expression) {
		if (expression instanceof RCLExpression && ((RCLExpression)expression).getType().equals("event")) {
			RCLExpression expr = (RCLExpression) expression;
			if (((RCLLiteral) expr.getSubExpressionWithType("action").getSubExpressions().get(0)).getValue().equals("take") && expr.getSubExpressionWithType("destination") != null) {
				expr.getSubExpressions().remove(expr.getSubExpressionWithType("destination"));
			}
		} else if (expression instanceof RCLExpression && ((RCLExpression)expression).getType().equals("sequence")) {
			RCLExpression expr = (RCLExpression) expression;
			for (AbstractRCLExpression e : expr.getSubExpressions()) {
				fixExpression(e);
			}
		}
	}
	
	private void decideAction(AbstractRCLExpression expression, Shape gripperShape) {
		if (expression instanceof RCLExpression && ((RCLExpression)expression).getType().equals("event")) {
			RCLExpression expr = (RCLExpression) expression;
			if (((RCLLiteral) expr.getSubExpressionWithType("action").getSubExpressions().get(0)).getValue().equals("move-drop")) {
				if (gripperShape == null) {
					((RCLLiteral) expr.getSubExpressionWithType("action").getSubExpressions().get(0)).setValue("move");
				} else {
					((RCLLiteral) expr.getSubExpressionWithType("action").getSubExpressions().get(0)).setValue("drop");
				}
			}
		}
		
		if (expression instanceof RCLExpression && ((RCLExpression)expression).getType().equals("sequence")) {
			RCLExpression expr = (RCLExpression) ((RCLExpression)expression).getSubExpressions().get(0);
			((RCLLiteral) expr.getSubExpressionWithType("action").getSubExpressions().get(0)).setValue("take");
			expr = (RCLExpression) ((RCLExpression)expression).getSubExpressions().get(1);
			((RCLLiteral) expr.getSubExpressionWithType("action").getSubExpressions().get(0)).setValue("drop");
		}
	}
	
	private RCLExpression resolveEntity(RCLExpression expression, Map<Integer, RCLExpression> entityMappings) {
		RCLExpression result = (RCLExpression) RCLCreator.createFromString(expression.toString());
		
		RCLExpression typeExpr = result.getSubExpressionWithType("type");
		RCLExpression referenceExpr = result.getSubExpressionWithType("reference-id");
		if (referenceExpr != null && typeExpr != null && ((RCLLiteral)typeExpr.getSubExpressions().get(0)).getValue().equals("type-reference")) {
			RCLExpression expr = entityMappings.get(Integer.parseInt(((RCLLiteral)referenceExpr.getSubExpressions().get(0)).getValue()));
			RCLExpression tExpr = expr.getSubExpressionWithType("type");
			((RCLLiteral)typeExpr.getSubExpressions().get(0)).setValue(((RCLLiteral)tExpr.getSubExpressions().get(0)).getValue());
			result.getSubExpressions().remove(referenceExpr);
		} else if (referenceExpr != null && typeExpr != null && ((RCLLiteral)typeExpr.getSubExpressions().get(0)).getValue().equals("type-reference-group")) {
			RCLExpression expr = entityMappings.get(Integer.parseInt(((RCLLiteral)referenceExpr.getSubExpressions().get(0)).getValue()));
			RCLExpression tExpr = expr.getSubExpressionWithType("type");
			((RCLLiteral)typeExpr.getSubExpressions().get(0)).setValue(((RCLLiteral)tExpr.getSubExpressions().get(0)).getValue()+"-group");
			result.getSubExpressions().remove(referenceExpr);
		}
		
		return result;
	}
}
