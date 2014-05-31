package converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import prolog.PrologPredicate;
import prolog.PrologQuery;
import rcl.AbstractRCLExpression;
import rcl.RCLExpression;
import rcl.RCLLiteral;

public class RCLPrologConverter {
	
	static final String variables[] = {"A", "B", "C", "D", "E", "F"};
	
	Map<Integer, RCLExpression> expressionsMap = new HashMap<Integer, RCLExpression>();
	
	public RCLPrologConverter() {
		
	}
	
	public PrologQuery convert(RCLExpression expr) {
		if (!expr.getType().equals("entity")) {
			return null;
		}
		
		List<PrologPredicate> predicates = convert(expr, 0);
		if (predicates == null) {
			return null;
		} else {
			return new PrologQuery(predicates);
		}
	}
	
	private List<PrologPredicate> convert(RCLExpression expr, int currentVar) {
		List<PrologPredicate> predicates = new ArrayList<PrologPredicate>();
		for (AbstractRCLExpression e : expr.getSubExpressions()) {
			RCLExpression expression = (RCLExpression) e;
			if (expression.getType().equals("type")) {
				String value = ((RCLLiteral) expression.getSubExpressions().get(0)).getValue();
				if (value.equals("reference")) {
					int id = Integer.parseInt(((RCLLiteral) expr.getSubExpressionWithType("reference-id").getSubExpressions().get(0)).getValue());
					predicates.addAll(convert(expressionsMap.get(id), currentVar));
				} else if (value.equals("type-reference")) {
					int id = Integer.parseInt(((RCLLiteral) expr.getSubExpressionWithType("reference-id").getSubExpressions().get(0)).getValue());
					List<String> temp = new ArrayList<String>();
					temp.add(variables[currentVar]);
					temp.add(((RCLLiteral) expressionsMap.get(id).getSubExpressionWithType("type").getSubExpressions().get(0)).getValue());
					predicates.add(new PrologPredicate("type", temp));
				} else if (value.equals("type-reference-group")) {
					int id = Integer.parseInt(((RCLLiteral) expr.getSubExpressionWithType("reference-id").getSubExpressions().get(0)).getValue());
					List<String> temp = new ArrayList<String>();
					temp.add(variables[currentVar]);
					temp.add(((RCLLiteral) expressionsMap.get(id).getSubExpressionWithType("type").getSubExpressions().get(0)).getValue()+"-group");
					predicates.add(new PrologPredicate("type", temp));
				} else if (value.equals("tile") || value.equals("region")) {
					return null;
				} else {
					List<String> temp = new ArrayList<String>();
					temp.add(variables[currentVar]);
					temp.add(value);
					predicates.add(new PrologPredicate("type", temp));
				}
			} else if (expression.getType().equals("color")) {
				List<String> temp = new ArrayList<String>();
				temp.add(variables[currentVar]);
				temp.add(((RCLLiteral) expression.getSubExpressions().get(0)).getValue());
				predicates.add(new PrologPredicate("color", temp));
			} else if (expression.getType().equals("indicator")) {
				List<String> temp = new ArrayList<String>();
				temp.add(variables[currentVar]);
				temp.add(((RCLLiteral) expression.getSubExpressions().get(0)).getValue());
				predicates.add(new PrologPredicate("indicator", temp));
			} else if (expression.getType().equals("spatial-relation")) {
				if (expression.getSubExpressionWithType("entity") != null && ((RCLLiteral) expression.getSubExpressionWithType("entity").getSubExpressionWithType("type").getSubExpressions().get(0)).getValue().equals("region")) {
					List<String> temp = new ArrayList<String>();
					temp.add(variables[currentVar]);
					temp.add(((RCLLiteral) expression.getSubExpressionWithType("entity").getSubExpressionWithType("indicator").getSubExpressions().get(0)).getValue());
					predicates.add(new PrologPredicate("region", temp));
				} else if (expression.getSubExpressionWithType("entity") != null) {
					List<String> temp = new ArrayList<String>();
					temp.add(variables[currentVar]);
					temp.add(((RCLLiteral) expression.getSubExpressionWithType("relation").getSubExpressions().get(0)).getValue());
					temp.add(variables[currentVar+1]);
					predicates.add(new PrologPredicate("relation", temp));
					predicates.addAll(convert(expression.getSubExpressionWithType("entity"), currentVar+1));
				} else {
					return null;
				}
			} else if (expression.getType().equals("id")) {
				int id = Integer.parseInt(((RCLLiteral) expression.getSubExpressions().get(0)).getValue());
				expressionsMap.put(id, expr);
			}
		}
		
		return predicates;
	}

}
