package converter;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import rcl.RCLCreator;
import rcl.RCLExpression;
import edu.uw.cs.lil.tiny.mr.lambda.FlexibleTypeComparator;
import edu.uw.cs.lil.tiny.mr.lambda.LogicLanguageServices;
import edu.uw.cs.lil.tiny.mr.lambda.LogicalExpression;
import edu.uw.cs.lil.tiny.mr.language.type.TypeRepository;


public class RCLLambdaConverterTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final File resourceDir = new File("resources/");
		
		
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
	}
	
	@Test
	public void test1() {
		String lambda = "(lambda $0:e (action:<evt,<act,<ent,t>>> $0 take:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 blue:co) (type:<ent,<typ,t>> $1 cube:typ))))))";
		String rcl = "(event: (action: take) (entity: (type: cube) (color: blue)))";
		RCLLambdaConverter converter = new RCLLambdaConverter();
		String res = converter.convert((RCLExpression) RCLCreator.createFromString(rcl));
		assertEquals(LogicalExpression.parse(lambda), LogicalExpression.parse(res));
	}
	
	@Test
	public void test2() {
		String lambda = "(lambda $0:e (action:<evt,<act,<ent,t>>> $0 take:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 green:co) (type:<ent,<typ,t>> $1 prism:typ))))))";
		String rcl = "(event: (action: take) (entity: (type: prism) (color: green)))";
		RCLLambdaConverter converter = new RCLLambdaConverter();
		String res = converter.convert((RCLExpression) RCLCreator.createFromString(rcl));
		assertEquals(LogicalExpression.parse(lambda), LogicalExpression.parse(res));
	}
	
	@Test
	public void test3() {
		String lambda = "(lambda $0:e (action:<evt,<act,<ent,t>>> $0 drop:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 blue:co) (type:<ent,<typ,t>> $1 cube:typ))))))";
		String rcl = "(event: (action: drop) (entity: (type: cube) (color: blue)))";
		RCLLambdaConverter converter = new RCLLambdaConverter();
		String res = converter.convert((RCLExpression) RCLCreator.createFromString(rcl));
		assertEquals(LogicalExpression.parse(lambda), LogicalExpression.parse(res));
	}
	
	@Test
	public void test4() {
		String lambda = "(lambda $0:e (action:<evt,<act,<ent,t>>> $0 take:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 blue:co) (type:<ent,<typ,t>> $1 cube:typ) (relation:<ent,<rel,<ent,t>>> $1 above:rel (det:<<e,t>,e> (lambda $2:e (and:<t*,t> (color:<ent,<co,t>> $2 red:co) (type:<ent,<typ,t>> $2 cube:typ))))))))))";
		String rcl = "(event: (action: take) (entity: (type: cube) (color: blue) (spatial-relation: (relation: above) (entity: (type: cube) (color: red)))))";
		RCLLambdaConverter converter = new RCLLambdaConverter();
		String res = converter.convert((RCLExpression) RCLCreator.createFromString(rcl));
		assertEquals(LogicalExpression.parse(lambda), LogicalExpression.parse(res));
	}
	
	@Test
	public void test5() {
		String lambda = "(lambda $0:e (and:<t*,t> (action:<evt,<act,<ent,t>>> $0 move:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 blue:co) (type:<ent,<typ,t>> $1 cube:typ))))) (destination:<evt,<rel,<ent,t>>> $0 left:rel (det:<<e,t>,e> (lambda $2:e (and:<t*,t> (color:<ent,<co,t>> $2 red:co) (type:<ent,<typ,t>> $2 cube:typ)))))))";
		String rcl = "(event: (action: move) (entity: (type: cube) (color: blue)) (destination: (spatial-relation: (relation: left) (entity: (type: cube) (color: red)))))";
		RCLLambdaConverter converter = new RCLLambdaConverter();
		String res = converter.convert((RCLExpression) RCLCreator.createFromString(rcl));
		assertEquals(LogicalExpression.parse(lambda), LogicalExpression.parse(res));
	}
	
	@Test
	public void test6() {
		String lambda = "(sequence:<evt,<evt,t>> (det:<<e,t>,e> (lambda $0:e (action:<evt,<act,<ent,t>>> $0 take:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 blue:co) (type:<ent,<typ,t>> $1 cube:typ))))))) (det:<<e,t>,e> (lambda $0:e (action:<evt,<act,<ent,t>>> $0 drop:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 blue:co) (type:<ent,<typ,t>> $1 cube:typ))))))))";
		String rcl = "(sequence: (event: (action: take) (entity: (type: cube) (color: blue))) (event: (action: drop) (entity: (type: cube) (color: blue))))";
		RCLLambdaConverter converter = new RCLLambdaConverter();
		String res = converter.convert((RCLExpression) RCLCreator.createFromString(rcl));
		assertEquals(LogicalExpression.parse(lambda), LogicalExpression.parse(res));
	}
	
	@Test
	public void test7() {
		String lambda = "(sequence:<evt,<evt,t>> (det:<<e,t>,e> (lambda $0:e (action:<evt,<act,<ent,t>>> $0 take:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (id:<ent,<i,t>> $1 1:i) (color:<ent,<co,t>> $1 blue:co) (type:<ent,<typ,t>> $1 cube:typ))))))) (det:<<e,t>,e> (lambda $0:e (action:<evt,<act,<ent,t>>> $0 drop:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (type:<ent,<typ,t>> $1 reference:typ) (reference-id:<ent,<i,t>> $1 1:i))))))))";
		String rcl = "(sequence: (event: (action: take) (entity: (id: 1) (type: cube) (color: blue))) (event: (action: drop) (entity: (type: reference) (reference-id: 1))))";
		RCLLambdaConverter converter = new RCLLambdaConverter();
		String res = converter.convert((RCLExpression) RCLCreator.createFromString(rcl));
		assertEquals(LogicalExpression.parse(lambda), LogicalExpression.parse(res));
	}
	
	@Test
	public void test8() {
		String lambda = "(lambda $0:e (and:<t*,t> (action:<evt,<act,<ent,t>>> $0 move:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (color:<ent,<co,t>> $1 green:co) (type:<ent,<typ,t>> $1 cube:typ)))))(destination:<evt,<rel,<me,t>>> $0 left:rel (det:<<e,t>,e> (lambda $2:e (and:<t*,t> (cardinal:<ent,<i,t>> $2 1:i) (type:<ent,<typ,t>> $2 tile:typ)))))))";
		String rcl = "(event: (action: move) (entity: (color: green) (type: cube)) (destination: (spatial-relation: (measure: (entity: (cardinal: 1) (type: tile))) (relation: left))))";
		RCLLambdaConverter converter = new RCLLambdaConverter();
		String res = converter.convert((RCLExpression) RCLCreator.createFromString(rcl));
		assertEquals(LogicalExpression.parse(lambda), LogicalExpression.parse(res));
	}
	
	@Test
	public void test9() {
		String lambda = "(sequence:<evt,<evt,t>> (det:<<e,t>,e> (lambda $0:e (action:<evt,<act,<ent,t>>> $0 take:act (det:<<e,t>,e> (lambda $1:e (and:<t*,t> (id:<ent,<i,t>> $1 1:i) (color:<ent,<co,t>> $1 blue:co) (type:<ent,<typ,t>> $1 prism:typ))))))) (det:<<e,t>,e> (lambda $2:e (and:<t*,t> (action:<evt,<act,<ent,t>>> $2 drop:act (det:<<e,t>,e> (lambda $3:e (and:<t*,t> (type:<ent,<typ,t>> $3 reference:typ) (reference-id:<ent,<i,t>> $3 1:i)))))(destination:<evt,<rel,<ent,<me,t>>>> $2 forward:rel (det:<<e,t>,e> (lambda $4:e (and:<t*,t> (indicator:<ent,<ind,t>> $4 leftmost:ind) (color:<ent,<co,t>> $4 green:co) (type:<ent,<typ,t>> $4 cube:typ)))) (det:<<e,t>,e> (lambda $5:e (and:<t*,t> (cardinal:<ent,<i,t>> $5 2:i) (type:<ent,<typ,t>> $5 tile:typ)))))))))";
		String rcl = "(sequence: (event: (action: take) (entity: (id: 1) (color: blue) (type: prism))) (event: (action: drop) (entity: (type: reference) (reference-id: 1)) (destination: (spatial-relation: (measure: (entity: (cardinal: 2) (type: tile))) (relation: forward) (entity: (indicator: leftmost) (color: green) (type: cube))))))";
		RCLLambdaConverter converter = new RCLLambdaConverter();
		String res = converter.convert((RCLExpression) RCLCreator.createFromString(rcl));
		assertEquals(LogicalExpression.parse(lambda), LogicalExpression.parse(res));
	}

}
