package cop5556fa18;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPScanner.Kind;

public class PLPParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}
	
	PLPScanner scanner;
	Token t;

	PLPParser(PLPScanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	public void parse() throws SyntaxException {
		program();
		match(Kind.EOF);
	}
	
	/*
	 * Program -> Identifier Block
	 */
	public void program() throws SyntaxException {
		match(Kind.IDENTIFIER);
		block();
	}
	
	/*
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char, Kind.KW_string /* Complete this */ };
	Kind[] firstStatement = {Kind.KW_if, Kind.KW_while, Kind.KW_sleep, Kind.KW_print, Kind.IDENTIFIER/* Complete this */  };

	public void block() throws SyntaxException {
		match(Kind.LBRACE);
		while (checkKind(firstDec) | checkKind(firstStatement)) {
	     if (checkKind(firstDec)) {
			declaration();
		} else if (checkKind(firstStatement)) {
			statement();
		}
			match(Kind.SEMI);
		}
		match(Kind.RBRACE);

	}
	
	public void declaration() throws SyntaxException{
		match(Kind.KW_int);
		//TODO
		
		
		
		throw new UnsupportedOperationException();
	}
	
	public void statement() {
		//TODO
		throw new UnsupportedOperationException();
	}
	
	//TODO Complete all other productions

	protected boolean checkKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean checkKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}
	
	/**
	 * @param kind
	 * @return 
	 * @return
	 * @throws SyntaxException
	 */
	private void match(Kind kind) throws SyntaxException {
		if (kind == Kind.EOF) {
			System.out.println("End of file"); //return t;
		}
		else if (checkKind(kind)) {
			t = scanner.nextToken();
		}
		else {
			//TODO  give a better error message!
			throw new SyntaxException(t,"Syntax Error");
		}
	}

}
