/**
 * JUunit tests for the Scanner
 */

package cop5556fa18;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner.LexicalException;
import cop5556fa18.PLPScanner.Token;

public class PLPScannerTest {
	
	//set Junit to be able to catch exceptions
		@Rule
		public ExpectedException thrown = ExpectedException.none();

		
		//To make it easy to print objects and turn this output on and off
		static boolean doPrint = true;
		private void show(Object input) {
			if (doPrint) {
				System.out.println(input.toString());
			}
		}

		/**
		 *Retrieves the next token and checks that it is an EOF token. 
		 *Also checks that this was the last token.
		 *
		 * @param scanner
		 * @return the Token that was retrieved
		 */
		
		Token checkNextIsEOF(PLPScanner scanner) {
			PLPScanner.Token token = scanner.nextToken();
			assertEquals(PLPScanner.Kind.EOF, token.kind);
			assertFalse(scanner.hasTokens());
			return token;
		}


		/**
		 * Retrieves the next token and checks that its kind, position, length, line, and position in line
		 * match the given parameters.
		 * 
		 * @param scanner
		 * @param kind
		 * @param pos
		 * @param length
		 * @param line
		 * @param pos_in_line
		 * @return  the Token that was retrieved
		 */
		Token checkNext(PLPScanner scanner, PLPScanner.Kind kind, int pos, int length, int line, int pos_in_line) {
			Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			assertEquals(pos, t.pos);
			assertEquals(length, t.length);
			assertEquals(line, t.line());
			assertEquals(pos_in_line, t.posInLine());
			return t;
		}

		/**
		 * Retrieves the next token and checks that its kind and length match the given
		 * parameters.  The position, line, and position in line are ignored.
		 * 
		 * @param scanner
		 * @param kind
		 * @param length
		 * @return  the Token that was retrieved
		 */
		Token checkNext(PLPScanner scanner, PLPScanner.Kind kind, int length) {
			Token t = scanner.nextToken();
			assertEquals(kind, t.kind);
			assertEquals(length, t.length);
			return t;
		}
		


		/**
		 * Simple test case with an empty program.  The only Token will be the EOF Token.
		 *   
		 * @throws LexicalException
		 */
//		@Test
//		public void testEmpty() throws LexicalException {
//			String input = "";  //The input is the empty string.  This is legal
//			show(input);        //Display the input 
//			PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
//			show(scanner);   //Display the Scanner
//			checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
//		}

		
		/**
		 * This example shows how to test that your scanner is behaving when the
		 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
		 * 
		 * The example shows catching the exception that is thrown by the scanner,
		 * looking at it, and checking its contents before rethrowing it.  If caught
		 * but not rethrown, then JUnit won't get the exception and the test will fail.  
		 * 
		 * The test will work without putting the try-catch block around 
		 * new Scanner(input).scan(); but then you won't be able to check 
		 * or display the thrown exception.
		 * 
		 * @throws LexicalException
		 */
		@Test
		public void failIllegalChar() throws LexicalException {
			String input = ";;~";
			show(input);
			thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
			try {
				new PLPScanner(input).scan();
			} catch (LexicalException e) {  //Catch the exception
				show(e);                    //Display it
				assertEquals(2,e.getPos()); //Check that it occurred in the expected position
				throw e;                    //Rethrow exception so JUnit will see it
			}
		}
		
		
		@Test
		public void testNormal() throws LexicalException {
			try {
				String input = "";  //The input is the empty string.  This is legal
				show(input);        //Display the input 
				PLPScanner scanner = new PLPScanner(input).scan();  //Create a Scanner and initialize it
				show(scanner);   //Display the Scanner
				//checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
			} catch (LexicalException e) {
				show(e);
				//throw e;
			}

		}
		
//		@Test
//		public void testReadFile() throws LexicalException {
//		    Path path = Paths.get(".", "test.txt");
//		    try {
//		        String input = new String(Files.readAllBytes(path));
//		        PLPScanner scanner = new PLPScanner(input).scan();
//		        show(scanner);
//		    } catch (java.io.IOException e) {
//		        System.out.println("IO ERROR:" + e);
//		    }
//		}
//		
		@Test
		public void testSampleInDoc() throws LexicalException {
			String input = "boolean a;\r\n" +
			"int b, x, y;\r\n" +
			"char c;\r\n" + 
			"float d, t;\r\n" +
			"string e;\r\n" + 
			"\r\n" +
			"a = true;\r\n" +
			"B = 10;\r\n" +
			"c = 'a';\r\n" +
			"d = 23.2;\r\n" +
			"e = \"Hello, World!\";\r\n" +
			"\r\n" +
			"a = 1+2;\r\n" +
			"d = 2.12 - 1;\r\n" +
			"a == 3;\r\n" +
			"a = 1 + 2 * 4.5;\r\n" +
			"t = (1+2) * 4.5;\r\n" +
			"t = (((4-2)*5.6)/3)+2;\r\n" +
			"\r\n" +
			"int score = 100;\r\n" +
			"\r\n" +
			"if (a == 100) {\r\n" +
			"    print (\"Value of a is 100\");\r\n" +
			"}\r\n" +
			"if (score > 100) {\r\n" +
			"    print (a);\r\n" +
			"}";
			
		    try {
		        PLPScanner scanner = new PLPScanner(input).scan();
		        show(scanner);
		    } catch (LexicalException e) {
		        show(e);
		    }
		}
		
		@Test
		public void testOverflow() throws LexicalException {
			thrown.expect(LexicalException.class);
		    try {
		        String input = new String("int a = 1000000000000000000000000000001");
		        PLPScanner scanner = new PLPScanner(input).scan();
		        show(scanner);
		    } catch (LexicalException e) {
		    	show(e);
		    	throw e;
		    }
		}
		
		@Test
		public void testNorParent() throws LexicalException {
			thrown.expect(LexicalException.class);
		    try {
		        String input = new String("string a = \"123;");
		        PLPScanner scanner = new PLPScanner(input).scan();
		        show(scanner);
		    } catch (LexicalException e) {
		    	show(e);
		    	throw e;
		    }
		}
		
		
		/**
		 * Using the two previous functions as a template.  You can implement other JUnit test cases.
		 */
}
