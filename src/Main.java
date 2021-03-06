import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import parsing.AbstractParser;
import parsing.BeliefParserProblog;

import com.trainrobots.core.DataContext;
import com.trainrobots.core.corpus.Corpus;

import converter.DatasetTransformer;
import edu.uw.cs.lil.tiny.ccg.categories.syntax.Syntax;
import edu.uw.cs.lil.tiny.ccg.lexicon.ILexicon;
import edu.uw.cs.lil.tiny.ccg.lexicon.LexicalEntry;
import edu.uw.cs.lil.tiny.ccg.lexicon.LexicalEntry.Origin;
import edu.uw.cs.lil.tiny.ccg.lexicon.Lexicon;
import edu.uw.cs.lil.tiny.ccg.lexicon.factored.lambda.FactoredLexicon;
import edu.uw.cs.lil.tiny.ccg.lexicon.factored.lambda.FactoredLexicon.FactoredLexicalEntry;
import edu.uw.cs.lil.tiny.data.collection.CompositeDataCollection;
import edu.uw.cs.lil.tiny.data.collection.IDataCollection;
import edu.uw.cs.lil.tiny.data.sentence.Sentence;
import edu.uw.cs.lil.tiny.data.sentence.SentenceLengthFilter;
import edu.uw.cs.lil.tiny.data.singlesentence.SingleSentence;
import edu.uw.cs.lil.tiny.data.singlesentence.SingleSentenceDataset;
import edu.uw.cs.lil.tiny.data.utils.LabeledValidator;
import edu.uw.cs.lil.tiny.genlex.ccg.template.TemplateSupervisedGenlex;
import edu.uw.cs.lil.tiny.learn.ILearner;
import edu.uw.cs.lil.tiny.learn.validation.stocgrad.ValidationStocGrad;
import edu.uw.cs.lil.tiny.mr.lambda.FlexibleTypeComparator;
import edu.uw.cs.lil.tiny.mr.lambda.LogicLanguageServices;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.mr.lambda.ccg.LogicalExpressionCategoryServices;
import edu.uw.cs.lil.tiny.mr.lambda.ccg.SimpleFullParseFilter;
import edu.uw.cs.lil.tiny.mr.language.type.TypeRepository;
import edu.uw.cs.lil.tiny.parser.ccg.cky.CKYBinaryParsingRule;
import edu.uw.cs.lil.tiny.parser.ccg.cky.multi.MultiCKYParser;
import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.LexemeFeatureSet;
import edu.uw.cs.lil.tiny.parser.ccg.factoredlex.features.LexicalTemplateFeatureSet;
import edu.uw.cs.lil.tiny.parser.ccg.features.basic.LexicalFeatureSet;
import edu.uw.cs.lil.tiny.parser.ccg.features.basic.LexicalFeaturesInit;
import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.ExpLengthLexicalEntryScorer;
import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.SkippingSensitiveLexicalEntryScorer;
import edu.uw.cs.lil.tiny.parser.ccg.features.basic.scorer.UniformScorer;
import edu.uw.cs.lil.tiny.parser.ccg.features.lambda.LogicalExpressionCoordinationFeatureSet;
import edu.uw.cs.lil.tiny.parser.ccg.model.LexiconModelInit;
import edu.uw.cs.lil.tiny.parser.ccg.model.Model;
import edu.uw.cs.lil.tiny.parser.ccg.model.ModelLogger;
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
import edu.uw.cs.lil.tiny.utils.string.StubStringFilter;
import edu.uw.cs.utils.collections.ISerializableScorer;
import edu.uw.cs.utils.collections.SetUtils;
import edu.uw.cs.utils.log.ILogger;
import edu.uw.cs.utils.log.Log;
import edu.uw.cs.utils.log.LogLevel;
import edu.uw.cs.utils.log.Logger;
import edu.uw.cs.utils.log.LoggerFactory;
import edu.uw.cs.utils.log.thread.LoggingThreadFactory;

public class Main {
	
	public static final ILogger	LOG	= LoggerFactory.create(Main.class);
	
	
	private static String testFile = "test.ccg";
	private static String trainFile = "train.ccg";
	private static boolean trainModel = true;

	public static void main(String[] args) throws NumberFormatException, IOException {
		
		DataContext.setDataPath("train-robots/evaluation_data");
//		DataContext.setDataPath("train-robots/training_data");
//		DataContext.setDataPath("train-robots/trial_data");
		
		
		generateDataset();
//		execute();
	}
	
	private static void generateDataset() {
		DatasetTransformer transformer = new DatasetTransformer(Corpus.getCommands());
		try {
			transformer.transform(new File(trainFile), new File(testFile), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static void execute() {
		Logger.DEFAULT_LOG = new Log(System.err);
		Logger.setSkipPrefix(true);
		LogLevel.setLogLevel(LogLevel.INFO);
		
		HashVectorFactory.DEFAULT = Type.TREE;
		
		final File resourceDir = new File("resources/all-comb-det/");
		
		
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
		
		
		final List<IDataCollection<? extends SingleSentence>> folds = new ArrayList<IDataCollection<? extends SingleSentence>>(
				1);
		folds.add(SingleSentenceDataset.read(
					new File(trainFile),
					new StubStringFilter()));
		final CompositeDataCollection<SingleSentence> train = new CompositeDataCollection<SingleSentence>(
				folds);
		
		
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
		
		final IGraphParser<Sentence, LogicalExpression> parser = new MultiCKYParser.Builder<LogicalExpression>(
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
//										.add(new PrepositionTypeShifting())
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
//				.addBinaryParseRule(
//						new CKYBinaryParsingRule<LogicalExpression>(
//								new ThatlessRelative(categoryServices)))
//				.addBinaryParseRule(
//						new CKYBinaryParsingRule<LogicalExpression>(
//								new PluralExistentialTypeShifting(
//										categoryServices)))
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
		
		
		final ILearner<Sentence, SingleSentence, Model<Sentence, LogicalExpression>> learner = new ValidationStocGrad.Builder<Sentence, SingleSentence, LogicalExpression>(
				train, parser, validator)
				.setGenlex(genlex, categoryServices)
				.setLexiconGenerationBeamSize(100)
				.setNumIterations(1)
				.setProcessingFilter(
						new SentenceLengthFilter<SingleSentence>(50))
				.setConflateGenlexAndPrunedParses(false).build();
		
		
		// //////////////////////////////////////////////////
		// Model
		// //////////////////////////////////////////////////
		
		Model<Sentence, LogicalExpression> model = null;
		
		if (trainModel) {
			final ISerializableScorer<LexicalEntry<LogicalExpression>> uniform0Scorer = new UniformScorer<LexicalEntry<LogicalExpression>>(
					0.0);
			final SkippingSensitiveLexicalEntryScorer<LogicalExpression> skippingScorer = new SkippingSensitiveLexicalEntryScorer<LogicalExpression>(
					categoryServices.getEmptyCategory(), -1.0, uniform0Scorer);
			model = new Model.Builder<Sentence, LogicalExpression>()
					.setLexicon(new FactoredLexicon())
					.addLexicalFeatureSet(
							new LexicalFeatureSet.Builder<Sentence, LogicalExpression>()
									.setInitialScorer(skippingScorer).build())
					.addLexicalFeatureSet(
							new LexemeFeatureSet.Builder<Sentence>().build())
					.addLexicalFeatureSet(
							new LexicalTemplateFeatureSet.Builder<Sentence>()
									.setScale(0.1).build())
					.addParseFeatureSet(
							new LogicalExpressionCoordinationFeatureSet<Sentence>(
									true, true, true))
					.build();
		
		
		// //////////////////////////////////////////////////
		// Init model
		// //////////////////////////////////////////////////
		
			new LexiconModelInit<Sentence, LogicalExpression>(semiFactored)
					.init(model);
			new LexiconModelInit<Sentence, LogicalExpression>(npLexicon)
					.init(model);
			new LexicalFeaturesInit<Sentence, LogicalExpression>(semiFactored,
					"LEX", new ExpLengthLexicalEntryScorer<LogicalExpression>(10.0,
							1.1)).init(model);
			new LexicalFeaturesInit<Sentence, LogicalExpression>(npLexicon, "LEX",
					new ExpLengthLexicalEntryScorer<LogicalExpression>(10.0, 1.1))
					.init(model);
			new LexicalFeaturesInit<Sentence, LogicalExpression>(semiFactored,
					"XEME", 10.0).init(model);
			new LexicalFeaturesInit<Sentence, LogicalExpression>(npLexicon, "XEME",
					10.0).init(model);
		
		
		
		// Model logger
			final ModelLogger modelLogger = new ModelLogger(true);
		
//			modelLogger.log(model, System.out);
		
			learner.train(model);
		
//			modelLogger.log(model, System.out);
		
			try {
				FileOutputStream fout = new FileOutputStream(new File("latestModel"));
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(model);
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		} else {
			try {
				FileInputStream fis = new FileInputStream(new File("modelAll"));
				ObjectInputStream ois = new ObjectInputStream(fis);
				model = (Model<Sentence, LogicalExpression>) ois.readObject();
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		AbstractParser cParser = new BeliefParser(parser, model, true);
		
		try {
			CustomTester cTester = new CustomTester(cParser, new File(testFile));
			cTester.test(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		executor.shutdownNow();
		
		
	}

}
