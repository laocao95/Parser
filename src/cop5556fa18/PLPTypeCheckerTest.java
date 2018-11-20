package cop5556fa18;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner;
import cop5556fa18.PLPTypeChecker.SemanticException;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPParser.SyntaxException;
import cop5556fa18.PLPScanner.LexicalException;

public class PLPTypeCheckerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scan, parse, and type check an input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		PLPScanner scanner = new PLPScanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new PLPParser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		PLPASTVisitor v = new PLPTypeChecker();
		ast.visit(v, null);
	}
	
	
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}
//
//	@Test
//	public void expression1() throws Exception {
//		String input = "prog {show 1+2;}";
//		typeCheck(input);
//	}
//
//	@Test
//	public void expression2_fail() throws Exception {
//		String input = "prog { show true+4; }"; //should throw an error due to incompatible types in binary expression
//		thrown.expect(SemanticException.class);
//		try {
//			typeCheck(input);
//		} catch (SemanticException e) {
//			show(e);
//			throw e;
//		}
//	}
	
//	@Test
//	public void testReadFile() throws Exception {
//	    Path path = Paths.get(".", "test.txt");
//	    try {
//	        String input = new String(Files.readAllBytes(path));
//	        typeCheck(input);
//	    } catch (Exception e) {
//	    	show(e);
//	    	throw e;
//	    }
//	}

}
