package parsing;
import java.util.ArrayList;
import java.util.List;

import rcl.AbstractRCLExpression;
import rcl.RCLExpression;

import com.trainrobots.nlp.scenes.WorldModel;

import converter.LambdaRCLConverter;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.parser.IParse;
import edu.uw.cs.lil.tiny.parser.ccg.model.Model;
import edu.uw.cs.lil.tiny.parser.graph.IGraphParser;



public abstract class AbstractParser {
	
	protected IGraphParser<Sentence, LogicalExpression> parser;
	protected Model<Sentence, LogicalExpression> model;
	
	protected String lastString;
	protected List<AbstractRCLExpression> lastParses;
	
	public AbstractParser(IGraphParser<Sentence, LogicalExpression> parser, Model<Sentence, LogicalExpression> model) {
		this.parser = parser;
		this.model = model;
	}

	public abstract AbstractRCLExpression parse(String string, WorldModel world);
	
	public List<AbstractRCLExpression> getAllParses(String string) {
		if (string.equals(lastString)) {
			return lastParses;
		} else {
			Sentence sentence = new Sentence(string);
			List<AbstractRCLExpression> expressions = new ArrayList<AbstractRCLExpression>();
			List<IParse<LogicalExpression>> temp = (List<IParse<LogicalExpression>>) parser.parse(sentence, model.createDataItemModel(sentence)).getAllParses();
			for (IParse<LogicalExpression> expr : temp) {
				LambdaRCLConverter converter = new LambdaRCLConverter(expr.getSemantics());
				try {
					AbstractRCLExpression expression = converter.convert();
					expression.cleanup();
					((RCLExpression) expression).removeUnusedIds();
					expressions.add(expression);
				} catch (Exception e) {
					
				}
			}
			return expressions;
		}
	}

}