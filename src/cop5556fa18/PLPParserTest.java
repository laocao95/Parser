package cop5556fa18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner;
import cop5556fa18.PLPParser;
import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPParser.SyntaxException;

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

		/**
		 * An empty program.  This throws an exception because it lacks an identifier and a block. 
		 * The test case passes because the unit test expects an exception.
		 *  
		 * @throws LexicalException
		 * @throws SyntaxException 
		 */
		@Test
		public void testEmpty() throws LexicalException, SyntaxException {
			String input = "";  
			PLPParser parser = makeParser(input);
			thrown.expect(SyntaxException.class);
			parser.parse();
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
			parser.parse();
		}	
		
		//This test will fail in the starter code. However, it should pass in a complete parser.
		@Test
		public void testDec0() throws LexicalException, SyntaxException {
			String input = "b{int c, b;}";
			PLPParser parser = makeParser(input);
			parser.parse();
		}
		

}
