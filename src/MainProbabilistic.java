import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import parsing.AbstractParser;
import parsing.BeliefParserProblog;
import probabilistic.ProbabilisticTransformer;
import probabilistic.ProblogChecker;

import com.trainrobots.core.DataContext;

import edu.uw.cs.lil.tiny.ccg.categories.syntax.Syntax;
import edu.uw.cs.lil.tiny.ccg.lexicon.ILexicon;
import edu.uw.cs.lil.tiny.ccg.lexicon.LexicalEntry;
import edu.uw.cs.lil.tiny.ccg.lexicon.LexicalEntry.Origin;
import edu.uw.cs.lil.tiny.ccg.lexicon.Lexicon;
import edu.uw.cs.lil.tiny.ccg.lexicon.factored.lambda.FactoredLexicon;
import edu.uw.cs.lil.tiny.ccg.lexicon.factored.lambda.FactoredLexicon.FactoredLexicalEntry;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.lil.tiny.data.singlesentence.SingleSentence;
import edu.uw.cs.lil.tiny.data.utils.LabeledValidator;
import edu.uw.cs.lil.tiny.genlex.ccg.template.TemplateSupervisedGenlex;
import edu.uw.cs.lil.tiny.mr.lambda.FlexibleTypeComparator;
import edu.uw.cs.lil.tiny.mr.lambda.LogicLanguageServices;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.mr.lambda.ccg.LogicalExpressionCategoryServices;
import edu.uw.cs.lil.tiny.mr.lambda.ccg.SimpleFullParseFilter;
import edu.uw.cs.lil.tiny.mr.language.type.TypeRepository;
import edu.uw.cs.lil.tiny.parser.ccg.cky.CKYBinaryParsingRule;
import edu.uw.cs.lil.tiny.parser.ccg.cky.multi.MultiCKYParser;
import edu.uw.cs.lil.tiny.parser.ccg.model.Model;
import edu.uw.cs.lil.tiny.parser.ccg.rules.RuleSetBuilder;
import edu.uw.cs.lil.tiny.parser.ccg.rules.lambda.typeshifting.basic.AdjectiveTypeShifting;
import edu.uw.cs.lil.tiny.parser.ccg.rules.lambda.typeshifting.basic.PluralTypeShifting;
import edu.uw.cs.lil.tiny.parser.ccg.rules.lambda.typeshifting.templated.ForwardTypeRaisedComposition;
import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.BackwardApplication;
import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.BackwardComposition;
import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.ForwardApplication;
import edu.uw.cs.lil.tiny.parser.ccg.rules.primitivebinary.ForwardComposition;
import edu.uw.cs.lil.tiny.parser.ccg.rules.skipping.BackwardSkippingRule;
import edu.uw.cs.lil.tiny.parser.ccg.rules.skipping.ForwardSkippingRule;
import edu.uw.cs.lil.tiny.parser.graph.IGraphParser;
import edu.uw.cs.lil.tiny.utils.concurrency.ITinyExecutor;
import edu.uw.cs.lil.tiny.utils.concurrency.TinyExecutorService;
import edu.uw.cs.lil.tiny.utils.hashvector.HashVectorFactory;
import edu.uw.cs.lil.tiny.utils.hashvector.HashVectorFactory.Type;
import edu.uw.cs.utils.collections.SetUtils;
import edu.uw.cs.utils.log.ILogger;
import edu.uw.cs.utils.log.Log;
import edu.uw.cs.utils.log.LogLevel;
import edu.uw.cs.utils.log.Logger;
import edu.uw.cs.utils.log.LoggerFactory;
import edu.uw.cs.utils.log.thread.LoggingThreadFactory;

public class MainProbabilistic {
	
	public static final ILogger	LOG	= LoggerFactory.create(Main.class);
	
	
	private static File testRCLFile;
	private static File modelFile;
	private static File resourceDir;
	private static String dataPath = "trial_data";

	public static void main(String[] args) {
		
		resourceDir = new File(args[0]);
		testRCLFile = new File(args[1]);
		modelFile = new File(args[2]);
		dataPath = args[3];
		ProblogChecker.directory = new File(args[4]);
		ProblogChecker.problogLocation = args[5];
		ProblogChecker.environmentFile = args[6];
		ProblogChecker.sceneFile = args[7];
		if (args.length >= 10) {
			BeliefParserProblog.correctMean = Double.parseDouble(args[8]);
			BeliefParserProblog.correctSd = Double.parseDouble(args[9]);
		}
		if (args.length >= 13) {
			ProbabilisticTransformer.probabilityThreshold = Double.parseDouble(args[10]);
			BeliefParserProblog.errorMean = Double.parseDouble(args[11]);
			BeliefParserProblog.errorSd = Double.parseDouble(args[12]);
		}
		
		MainProbabilistic m = new MainProbabilistic();
		m.execute();
		
		
		System.out.println(BeliefParserProblog.correctMean + " " + BeliefParserProblog.correctSd + " " + ProbabilisticTransformer.probabilityThreshold + " " + BeliefParserProblog.errorMean + " " + BeliefParserProblog.errorSd);
		System.out.println();
		
	}
	
	public MainProbabilistic() {

	}
	
	private void execute() {
		DataContext.setDataPath(dataPath);
		
		Logger.DEFAULT_LOG = new Log(System.err);
		Logger.setSkipPrefix(true);
		LogLevel.setLogLevel(LogLevel.INFO);
		
		HashVectorFactory.DEFAULT = Type.TREE;
		
//		final File resourceDir = new File("resources/");
		
		try {
			// Init the logical expression type system
			LogicLanguageServices
					.setInstance(new LogicLanguageServices.Builder(
							new TypeRepository(new File(resourceDir, "robot.types")),
							new FlexibleTypeComparator())
							.addConstantsToOntology(new File(resourceDir, "robot.consts.ont"))
							.addConstantsToOntology(new File(resourceDir, "robot.pred.ont"))
							.closeOntology(true)
							.build());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		
		
		
		
		
		
		// //////////////////////////////////////////////////
		// Category services for logical expressions
		// //////////////////////////////////////////////////
		
		final LogicalExpressionCategoryServices categoryServices = new LogicalExpressionCategoryServices(
				true, true);
		
		
		// //////////////////////////////////////////////////
		// Multi threaded executor
		// //////////////////////////////////////////////////
		
		final TinyExecutorService executor = new TinyExecutorService(Runtime
				.getRuntime().availableProcessors(),
				new LoggingThreadFactory(), ITinyExecutor.DEFAULT_MONITOR_SLEEP);
		
		// //////////////////////////////////////////////////
		// CKY parser
		// //////////////////////////////////////////////////
		
		IGraphParser<Sentence, LogicalExpression> parser = null;
		parser = new MultiCKYParser.Builder<LogicalExpression>(
				categoryServices, executor, new SimpleFullParseFilter(
						SetUtils.createSingleton((Syntax) Syntax.S)))
				.addBinaryParseRule(
						new CKYBinaryParsingRule<LogicalExpression>(
								new RuleSetBuilder<LogicalExpression>()
										.add(new ForwardComposition<LogicalExpression>(
												categoryServices, false))
										.add(new BackwardComposition<LogicalExpression>(
												categoryServices, false))
										.add(new ForwardApplication<LogicalExpression>(
												categoryServices))
										.add(new BackwardApplication<LogicalExpression>(
												categoryServices))
//											.add(new PrepositionTypeShifting())
										.add(new AdjectiveTypeShifting())
										.add(new PluralTypeShifting(categoryServices))
										.build()))
				.setPruneLexicalCells(true)
				.setPreChartPruning(true)
				.setMaxNumberOfCellsInSpan(50)
				.addBinaryParseRule(
						new CKYBinaryParsingRule<LogicalExpression>(
								new ForwardSkippingRule<LogicalExpression>(
										categoryServices)))
				.addBinaryParseRule(
						new CKYBinaryParsingRule<LogicalExpression>(
								new BackwardSkippingRule<LogicalExpression>(
										categoryServices)))
				.addBinaryParseRule(
						new CKYBinaryParsingRule<LogicalExpression>(
								new ForwardTypeRaisedComposition(
										categoryServices, true)))
//					.addBinaryParseRule(
//							new CKYBinaryParsingRule<LogicalExpression>(
//									new ThatlessRelative(categoryServices)))
//					.addBinaryParseRule(
//							new CKYBinaryParsingRule<LogicalExpression>(
//									new PluralExistentialTypeShifting(
//											categoryServices)))
										.build();
		
		
		// //////////////////////////////////////////////////
		// Validation function
		// //////////////////////////////////////////////////
		
		final LabeledValidator<SingleSentence, LogicalExpression> validator = new LabeledValidator<SingleSentence, LogicalExpression>();
		
		
		// //////////////////////////////////////////////////
		// Read initial lexicon
		// //////////////////////////////////////////////////
		
		// Create a static set of lexical entries, which are factored using
		// non-maximal factoring (each lexical entry is factored to multiple
		// entries). This static set is used to init the model with various
		// templates and lexemes.
		
		final File seedLexiconFile = new File(resourceDir, "seed.lex");
		final File npLexiconFile = new File(resourceDir, "np-list.lex");
		
		final Lexicon<LogicalExpression> readLexicon = new Lexicon<LogicalExpression>();
		readLexicon.addEntriesFromFile(seedLexiconFile, categoryServices,
				Origin.FIXED_DOMAIN);
		
		final Lexicon<LogicalExpression> semiFactored = new Lexicon<LogicalExpression>();
		for (final LexicalEntry<LogicalExpression> entry : readLexicon
				.toCollection()) {
			for (final FactoredLexicalEntry factoredEntry : FactoredLexicon
					.factor(entry, true, true, 2)) {
				semiFactored.add(FactoredLexicon.factor(factoredEntry));
			}
		}
		
		// Read NP list
		final ILexicon<LogicalExpression> npLexicon = new FactoredLexicon();
		npLexicon.addEntriesFromFile(npLexiconFile, categoryServices,
				Origin.FIXED_DOMAIN);
		
		
		// //////////////////////////////////////////////////
		// Genlex function
		// //////////////////////////////////////////////////
		
		final TemplateSupervisedGenlex<SingleSentence> genlex = new TemplateSupervisedGenlex.Builder<SingleSentence>(
				4).addTemplatesFromLexicon(semiFactored).build();
		
		
		// //////////////////////////////////////////////////
		// Model
		// //////////////////////////////////////////////////
		
		Model<Sentence, LogicalExpression> model = null;
		
		try {
			FileInputStream fis = new FileInputStream(modelFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			model = (Model<Sentence, LogicalExpression>) ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		AbstractParser cParser = new BeliefParserProblog(parser, model);
		
		try {
			CustomTester cTester = new CustomTester(cParser, testRCLFile);
			cTester.test(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		executor.shutdownNow();
		
		
	}

}
