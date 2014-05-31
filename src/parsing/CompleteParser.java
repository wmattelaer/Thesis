package parsing;
import rcl.AbstractRCLExpression;

import com.trainrobots.nlp.scenes.WorldModel;

import converter.LambdaRCLConverter;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.parser.IParserOutput;
import edu.uw.cs.lil.tiny.parser.ccg.model.Model;
import edu.uw.cs.lil.tiny.parser.graph.IGraphParser;


public class CompleteParser extends AbstractParser {
	
	public CompleteParser(IGraphParser<Sentence, LogicalExpression> parser, Model<Sentence, LogicalExpression> model) {
		super(parser, model);
	}
	
	@Override
	public AbstractRCLExpression parse(String string, WorldModel world) {
		Sentence sentence = new Sentence(string);
		
		IParserOutput<LogicalExpression> output = parser.parse(sentence, model.createDataItemModel(sentence));
		
		if (output.getBestParses().size() == 0) {
			return null;
		} else {
			AbstractRCLExpression bestExpression = null;
			double bestScore = 0;
			for (int i = 0; i < output.getAllParses().size(); i++) {
				LambdaRCLConverter converter = new LambdaRCLConverter(output.getAllParses().get(i).getSemantics());
				if (bestScore < output.getAllParses().get(i).getScore()) {
					try {
						bestExpression = converter.convert();
						bestScore = output.getAllParses().get(i).getScore();
					} catch (Exception e) {
						
					}
				}
			}
			return bestExpression;
		}
	}

}
