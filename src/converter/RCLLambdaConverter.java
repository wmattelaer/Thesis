package converter;
import rcl.AbstractRCLExpression;
import rcl.RCLExpression;
import rcl.RCLLiteral;



public class RCLLambdaConverter {

	private boolean combinedAction;
	
	public RCLLambdaConverter() {
		this(false);
	}
	
	public RCLLambdaConverter(boolean combinedAction) {
		this.combinedAction = combinedAction;
	}
	
	public String convert(RCLExpression expr) {
		
		StringBuilder builder = new StringBuilder();
		convert(expr, -1, 0, builder);
		return builder.toString();
	}
	
	private int convert(RCLExpression expr, int currentVar, int nextVar, StringBuilder builder) throws IllegalArgumentException {
		if (expr == null) {
			throw new IllegalArgumentException("unknown RCL element");
		}
		
		if (expr.getType().equals("event")) {
			int var = nextVar++;
			builder.append("(lambda $" + var + ":e ");
			if (expr.getSubExpressions().size() == 3) {
				builder.append("(and:<t*,t> ");
			}
			
			RCLExpression actionExpr = expr.getSubExpressionWithType("action");
			RCLExpression objectExpr = expr.getSubExpressionWithType("entity");
			
			String actionValue = ((RCLLiteral) actionExpr.getSubExpressions().get(0)).getValue();
			if (this.combinedAction && !actionValue.equals("take")) {
				actionValue = "move-drop";
			}
			
			builder.append("(action:<evt,<act,<ent,t>>> $"+var+" " + actionValue + ":act (det:<<e,t>,e> ");
			nextVar = convert(objectExpr, var, nextVar, builder);
			builder.append("))");
			
			if (expr.getSubExpressions().size() == 3) {
				RCLExpression destinationExpr = expr.getSubExpressionWithType("destination").getSubExpressionWithType("spatial-relation");
				RCLExpression relationExpr = destinationExpr.getSubExpressionWithType("relation");
				RCLExpression destObjExpr = destinationExpr.getSubExpressionWithType("entity");
				RCLExpression measureExpr = destinationExpr.getSubExpressionWithType("measure");
				
				String relationValue = ((RCLLiteral) relationExpr.getSubExpressions().get(0)).getValue();
				
				if (destObjExpr != null && measureExpr == null) {
					builder.append(" (destination:<evt,<rel,<ent,t>>> $"+var+" " + relationValue + ":rel (det:<<e,t>,e> ");
					nextVar = convert(destObjExpr, var, nextVar, builder);
					builder.append("))");
					
					builder.append(")");
				} else if (destObjExpr == null && measureExpr != null) {
					builder.append(" (destination:<evt,<rel,<me,t>>> $"+var+" " + relationValue + ":rel (det:<<e,t>,e> ");
					nextVar = convert(measureExpr.getSubExpressionWithType("entity"), var, nextVar, builder);
					builder.append("))");
					
					builder.append(")");
				} else if (destObjExpr != null && measureExpr != null) {
					builder.append(" (destination:<evt,<rel,<ent,<me,t>>>> $"+var+" " + relationValue + ":rel (det:<<e,t>,e> ");
					nextVar = convert(destObjExpr, var, nextVar, builder);
					builder.append(") (det:<<e,t>,e> ");
					nextVar = convert(measureExpr.getSubExpressionWithType("entity"), var, nextVar, builder);
					
					builder.append(")))");
				} else {
					throw new IllegalArgumentException("unknown RCL element");
				}
			}
			
			builder.append(")");
			
			return nextVar;
		} else if (expr.getType().equals("entity")) {
			int var = nextVar++;
			builder.append("(lambda $" + var + ":e");
			if (expr.getSubExpressions().size() > 1) {
				builder.append(" (and:<t*,t>");
			}
			
			for (AbstractRCLExpression e : expr.getSubExpressions()) {
				if (e instanceof RCLExpression) {
					builder.append(" ");
					nextVar = convert((RCLExpression) e, var, nextVar, builder);
				}
			}
			
			if (expr.getSubExpressions().size() > 1) {
				builder.append(")");
			}
			
			builder.append(")");
			
			return nextVar;
		} else if (expr.getType().equals("color")) {
			RCLLiteral literal = (RCLLiteral) expr.getSubExpressions().get(0);
			builder.append("(color:<ent,<co,t>> $" + currentVar + " " + literal.getValue() + ":co)");
			
			return nextVar;
		} else if (expr.getType().equals("indicator")) {
			RCLLiteral literal = (RCLLiteral) expr.getSubExpressions().get(0);
			builder.append("(indicator:<ent,<ind,t>> $" + currentVar + " " + literal.getValue() + ":ind)");
			
			return nextVar;
		} else if (expr.getType().equals("type")) {
			RCLLiteral literal = (RCLLiteral) expr.getSubExpressions().get(0);
			builder.append("(type:<ent,<typ,t>> $" + currentVar + " " + literal.getValue() + ":typ)");
			
			return nextVar;
		} else if (expr.getType().equals("id")) {
			RCLLiteral literal = (RCLLiteral) expr.getSubExpressions().get(0);
			builder.append("(id:<ent,<i,t>> $" + currentVar + " " + literal.getValue() + ":i)");
			
			return nextVar;
		} else if (expr.getType().equals("reference-id")) {
			RCLLiteral literal = (RCLLiteral) expr.getSubExpressions().get(0);
			builder.append("(reference-id:<ent,<i,t>> $" + currentVar + " " + literal.getValue() + ":i)");
			
			return nextVar;
		} else if (expr.getType().equals("cardinal")) {
			RCLLiteral literal = (RCLLiteral) expr.getSubExpressions().get(0);
			builder.append("(cardinal:<ent,<i,t>> $" + currentVar + " " + literal.getValue() + ":i)");
			
			return nextVar;
		} else if (expr.getType().equals("spatial-relation")) {
			RCLExpression relationExpr = expr.getSubExpressionWithType("relation");
			RCLExpression destObjExpr = expr.getSubExpressionWithType("entity");
			RCLExpression measureExpr = expr.getSubExpressionWithType("measure");
			
			String relationValue = ((RCLLiteral) relationExpr.getSubExpressions().get(0)).getValue();
			
			if (destObjExpr != null) {
				builder.append("(relation:<ent,<rel,<ent,t>>> $"+currentVar+" " + relationValue + ":rel (det:<<e,t>,e> ");
				nextVar = convert(destObjExpr, currentVar, nextVar, builder);
				builder.append("))");
			} else if (measureExpr != null) {
				builder.append("(relation:<ent,<rel,<me,t>>> $"+currentVar+" " + relationValue + ":rel (det:<<e,t>,e> ");
				nextVar = convert(measureExpr.getSubExpressionWithType("entity"), currentVar, nextVar, builder);
				builder.append("))");
			} else {
				throw new IllegalArgumentException("unknown RCL element");
			}

			return nextVar;
		} else if (expr.getType().equals("sequence")) {
			RCLExpression expr1 = (RCLExpression) expr.getSubExpressions().get(0);
			RCLExpression expr2 = (RCLExpression) expr.getSubExpressions().get(1);
			
			builder.append("(sequence:<evt,<evt,t>> (det:<<e,t>,e> ");
			nextVar = convert(expr1, currentVar, nextVar, builder);
			builder.append(") (det:<<e,t>,e> ");
			nextVar = convert(expr2, currentVar, nextVar, builder);
			builder.append("))");
			
			return nextVar;
		}
		
		throw new IllegalArgumentException("unknown RCL element");
		
	}

}
