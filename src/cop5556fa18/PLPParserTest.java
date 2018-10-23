package cop5556fa18;

import static cop5556fa18.PLPScanner.Kind.OP_PLUS;
import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPParser.SyntaxException;
import cop5556fa18.PLPScanner;
import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.Expression;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPAST.*;

public class PLPParserTest {
	
	//set Junit to be able to catch exceptions
		@Rule
		public ExpectedException thrown = ExpectedException.none();

		
		//To make it easy to print objects and turn this output on and off
		static final boolean doPrint = true;
		private void show(Object input) {
			if (doPrint) {
				System.out.println(input.toString());
			}
		}


		//creates and returns a parser for the given input.
		private PLPParser makeParser(String input) throws LexicalException {
			show(input);
			PLPScanner scanner = new PLPScanner(input).scan();
			show(scanner);
			PLPParser parser = new PLPParser(scanner);
			return parser;
		}
		
		
		@Test
		public void testReadFile() throws LexicalException, SyntaxException {
		    Path path = Paths.get(".", "testParser.txt");
		    try {
		        String input = new String(Files.readAllBytes(path));
		        PLPParser parser = makeParser(input);
		        parser.parse();
		    } catch (java.io.IOException e) {
		        System.out.println("IO ERROR:" + e);
		    } catch (LexicalException e) {
		    	show(e);
		    	throw(e);
		    } catch (SyntaxException e) {
		    	show(e);
		    	show(e.t);
		    	throw(e);
		    }
		}
		

		
//-------------------------------------------------------------------------------------------------------
		/**
		 * Test case with an empty program.  This throws an exception 
		 * because it lacks an identifier and a block
		 *   
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testEmpty() throws LexicalException, SyntaxException {
			String input = "";  //The input is the empty string.  
			thrown.expect(SyntaxException.class);
			PLPParser parser = makeParser(input);
			@SuppressWarnings("unused")
			Program p = parser.parse();
		}
		
		/**
		 * Smallest legal program.
		 *   
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testSmallest() throws LexicalException, SyntaxException {
			String input = "b{}";  
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);
			assertEquals("b", p.name);
			assertEquals(0, p.block.declarationsAndStatements.size());
		}	
		
		
		/**
		 * Utility method to check if an element of a block at an index is a declaration with a given type and name.
		 * 
		 * @param block
		 * @param index
		 * @param type
		 * @param name
		 * @return
		 */
		Declaration checkDec(Block block, int index, Kind type, String name) {
			PLPASTNode node = block.declarationsAndStatements(index);
			assertEquals(VariableDeclaration.class, node.getClass());
			VariableDeclaration dec = (VariableDeclaration) node;
			assertEquals(type, dec.type);
			assertEquals(name, dec.name);
			return dec;
		}	
		
		@Test
		public void testDec0() throws LexicalException, SyntaxException {
			String input = "b{int i; char c;}";
			PLPParser parser = makeParser(input);
			Program p = parser.parse();
			show(p);	
			checkDec(p.block, 0, Kind.KW_int, "i");
			checkDec(p.block, 1, Kind.KW_char, "c");
		}
		
		
		/** 
		 * Test a specific grammar element by calling a corresponding parser method rather than parse.
		 * This requires that the methods are visible (not private). 
		 * 
		 * @throws LexicalException
		 * @throws SyntaxException
		 */
		
		@Test
		public void testExpression() throws LexicalException, SyntaxException {
			String input = "x + 2";
			PLPParser parser = makeParser(input);
			Expression e = parser.expression();  //call expression here instead of parse
			show(e);	
			assertEquals(ExpressionBinary.class, e.getClass());
			ExpressionBinary b = (ExpressionBinary)e;
			assertEquals(ExpressionIdentifier.class, b.leftExpression.getClass());
			ExpressionIdentifier left = (ExpressionIdentifier)b.leftExpression;
			assertEquals("x", left.name);
			assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
			ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
			assertEquals(2, right.value);
			assertEquals(OP_PLUS, b.op);
		}
		
		

		
		/**
		 * Utility method to check if an element of a block at an index is a declaration with a given type and several names.
		 * 
		 * @param block
		 * @param index
		 * @param type
		 * @param ArrayList<String> name
		 * @return
		 */
		Declaration checkDecList(Block block, int index, Kind type, ArrayList<String> names) {
			PLPASTNode node = block.declarationsAndStatements(index);
			assertEquals(VariableListDeclaration.class, node.getClass());
			VariableListDeclaration dec = (VariableListDeclaration) node;
			for (int i = 0; i < dec.names.size(); ++i) {
				assertEquals(type, dec.type);
				assertEquals(names.get(i), dec.names.get(i));
			}
			return dec;
		}
		
		/**
		 * Declaration Test
		 */
}
