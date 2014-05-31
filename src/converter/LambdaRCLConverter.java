package converter;
import java.util.HashMap;
import java.util.Map;

import rcl.AbstractRCLExpression;
import rcl.RCLExpression;
import rcl.RCLLiteral;
import edu.uw.cs.lil.tiny.mr.lambda.Lambda;
import edu.uw.cs.lil.tiny.mr.lambda.Literal;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalConstant;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.mr.lambda.Variable;


public class LambdaRCLConverter {
	
	private LogicalExpression expression;
	
	public LambdaRCLConverter(LogicalExpression expression) {
		this.expression = expression;
	}
	
	public AbstractRCLExpression convert() {
		AbstractRCLExpression expr = parse(expression, new HashMap<Variable, RCLExpression>());
		return expr;
	}
	
	public AbstractRCLExpression parse(LogicalExpression expression, Map<Variable, RCLExpression> map) {
		if (expression instanceof Literal) {
			if (((Literal) expression).getPredicate().toString().startsWith("and")) {
				AbstractRCLExpression res = null;
				for (LogicalExpression expr : ((Literal) expression).getArguments()) {
					AbstractRCLExpression t = parse(expr, map);
					if (t != null) {
						res = t;
					}
				}
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("action")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("event");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				AbstractRCLExpression actionExpr = new RCLExpression("action", new RCLLiteral(splits[0]));
				res.getSubExpressions().add(actionExpr);
				
				LogicalExpression expr = ((Literal) expression).getArguments().get(2);
				res.getSubExpressions().add(parse(expr, map));
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("color")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("entity");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				res.getSubExpressions().add(new RCLExpression("color", new RCLLiteral(splits[0])));
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("indicator")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("entity");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				res.getSubExpressions().add(new RCLExpression("indicator", new RCLLiteral(splits[0])));
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("type")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("entity");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				res.getSubExpressions().add(new RCLExpression("type", new RCLLiteral(splits[0])));
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("id")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("entity");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				res.getSubExpressions().add(new RCLExpression("id", new RCLLiteral(splits[0])));
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("cardinal")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("entity");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				res.getSubExpressions().add(new RCLExpression("cardinal", new RCLLiteral(splits[0])));
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("reference-id")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("entity");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				res.getSubExpressions().add(new RCLExpression("reference-id", new RCLLiteral(splits[0])));
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("sequence")) {
				RCLExpression expr = new RCLExpression("sequence");
				
				LogicalExpression e = ((Literal) expression).getArguments().get(0);
				expr.getSubExpressions().add(parse(e, map));
				
				e = ((Literal) expression).getArguments().get(1);
				expr.getSubExpressions().add(parse(e, map));
				
				return expr;
			} else if (((Literal) expression).getPredicate().toString().startsWith("relation:<ent,<rel,<ent,t>>>")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("entity");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				
				RCLExpression expr = new RCLExpression("spatial-relation");
				expr.getSubExpressions().add(new RCLExpression("relation", new RCLLiteral(splits[0])));
				
				LogicalExpression e = ((Literal) expression).getArguments().get(2);
				expr.getSubExpressions().add(parse(e, map));
				
				
				res.getSubExpressions().add(expr);
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("relation:<ent,<rel,<me,t>>>")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("entity");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				
				RCLExpression expr = new RCLExpression("spatial-relation");
				expr.getSubExpressions().add(new RCLExpression("relation", new RCLLiteral(splits[0])));
				
				LogicalExpression e = ((Literal) expression).getArguments().get(2);
				expr.getSubExpressions().add(new RCLExpression("measure", parse(e, map)));
				
				
				res.getSubExpressions().add(expr);
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("destination:<evt,<rel,<ent,t>>>")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("event");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				
				
				RCLExpression expr = new RCLExpression("spatial-relation");
				expr.getSubExpressions().add(new RCLExpression("relation", new RCLLiteral(splits[0])));
				
				LogicalExpression e = ((Literal) expression).getArguments().get(2);
				expr.getSubExpressions().add(parse(e, map));
				
				res.getSubExpressions().add(new RCLExpression("destination", expr));
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("destination:<evt,<rel,<me,t>>>")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("event");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				
				
				RCLExpression expr = new RCLExpression("spatial-relation");
				expr.getSubExpressions().add(new RCLExpression("relation", new RCLLiteral(splits[0])));
				
				LogicalExpression e = ((Literal) expression).getArguments().get(2);
				expr.getSubExpressions().add(new RCLExpression("measure", parse(e, map)));
				
				res.getSubExpressions().add(new RCLExpression("destination", expr));
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("destination:<evt,<rel,<ent,<me,t>>>>")) {
				Variable var = (Variable) ((Literal) expression).getArguments().get(0);
				RCLExpression res = map.get(var);
				if (res == null) {
					res = new RCLExpression("event");
					map.put(var, res);
				}
				
				LogicalConstant con = (LogicalConstant) ((Literal) expression).getArguments().get(1);
				String splits[] = con.getName().toString().split(":");
				
				
				RCLExpression expr = new RCLExpression("spatial-relation");
				expr.getSubExpressions().add(new RCLExpression("relation", new RCLLiteral(splits[0])));
				
				LogicalExpression e = ((Literal) expression).getArguments().get(2);
				expr.getSubExpressions().add(parse(e, map));
				
				e = ((Literal) expression).getArguments().get(3);
				expr.getSubExpressions().add(new RCLExpression("measure", parse(e, map)));
				
				res.getSubExpressions().add(new RCLExpression("destination", expr));
				
				return res;
			} else if (((Literal) expression).getPredicate().toString().startsWith("det")) {
				LogicalExpression expr = ((Literal) expression).getArguments().get(0);
				return parse(expr, map);
			}
		} else if (expression instanceof Lambda) {
			return parse(((Lambda) expression).getBody(), map);
		}
		
		return null;
	}

}
