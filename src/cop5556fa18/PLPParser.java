package cop5556fa18;

import cop5556fa18.PLPScanner.Token;
import javafx.scene.control.TabPane.TabClosingPolicy;
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
	Kind[] firstFunction = {Kind.KW_sin, Kind.KW_cos, Kind.KW_atan, Kind.KW_abs, Kind.KW_log, Kind.KW_int, Kind.KW_float};
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
	
	public void declaration() throws SyntaxException {
		match(t.kind);
		// type identifier
		match(Kind.IDENTIFIER);
			
		// type identifier = expression
		if (checkKind(Kind.OP_ASSIGN)) {
			match(Kind.OP_ASSIGN);
			expression();
		} else if (checkKind(Kind.COMMA)) {
			// Identifier (, Identifier)*
			while(checkKind(Kind.COMMA)) {
				match(Kind.COMMA);
				match(Kind.IDENTIFIER);
			}
		}
		
	}
	
	public void statement() throws SyntaxException {
		//TODO
		switch (t.kind) {
			case KW_if: {
				match(Kind.KW_if);
				match(Kind.LPAREN);
				expression();
				match(Kind.RPAREN);
				block();
			}
			break;
			case KW_while: {
				match(Kind.KW_while);
				match(Kind.LPAREN);
				expression();
				match(Kind.RPAREN);
				block();
			}
			break;
			case IDENTIFIER: {
				match(Kind.IDENTIFIER);
				match(Kind.OP_ASSIGN);
				expression();
			}
			break;
			case KW_sleep: {
				match(Kind.KW_sleep);
				expression();	
			}
			break;
			case KW_print: {
				match(Kind.KW_print);
				expression();
			}
			break;
			default:{
				//
			}
		}
			
		//throw new UnsupportedOperationException();
	}
	
	public void expression() throws SyntaxException {
		orExpression();
		if (checkKind(Kind.OP_QUESTION)) {
			match(Kind.OP_QUESTION);
			expression();
			match(Kind.OP_COLON);
			expression();
		}
	}
	
	public void orExpression() throws SyntaxException {
		andExpression();
		while(checkKind(Kind.OP_OR)) {
			match(Kind.OP_OR);
			andExpression();
		}
	}
	
	public void andExpression() throws SyntaxException {
		eqExpression();
		while(checkKind(Kind.OP_AND)) {
			match(Kind.OP_AND);
			eqExpression();
		}	
	}
	
	public void eqExpression() throws SyntaxException {
		relExpression();
		while(checkKind(Kind.OP_EQ) || checkKind(Kind.OP_NEQ)) {
			match(t.kind);
			relExpression();
		}
		
	}
	
	public void relExpression() throws SyntaxException {
		addExpression();
		while(checkKind(Kind.OP_LT) || checkKind(Kind.OP_GT) || checkKind(Kind.OP_GE) || checkKind(Kind.OP_LE)) {
			match(t.kind);
			addExpression();
		}
	}
	
	public void addExpression() throws SyntaxException {
		multExpression();
		while(checkKind(Kind.OP_PLUS) || checkKind(Kind.OP_MINUS)) {
			match(t.kind);
			multExpression();
		}
	}
	
	public void multExpression() throws SyntaxException {
		powerExpression();
		while(checkKind(Kind.OP_TIMES) || checkKind(Kind.OP_DIV) || checkKind(Kind.OP_MOD)) {
			match(t.kind);
			powerExpression();
		}
		
	}
	public void powerExpression() throws SyntaxException {
		unaryExpression();
		if (checkKind(Kind.OP_POWER)) {
			match(Kind.OP_POWER);
			powerExpression();
		}
	}
	public void unaryExpression() throws SyntaxException {
		if (checkKind(Kind.OP_PLUS) || checkKind(Kind.OP_MINUS) || checkKind(Kind.OP_EXCLAMATION)) {
			match(t.kind);
			unaryExpression();
		} else {
			primary();
		}
	}
	
	public void primary() throws SyntaxException {
		switch (t.kind) {
			case INTEGER_LITERAL: {
				match(Kind.INTEGER_LITERAL);
			}
			break;
			case BOOLEAN_LITERAL: {
				match(Kind.BOOLEAN_LITERAL);
			}
			break;
			case FLOAT_LITERAL: {
				match(Kind.FLOAT_LITERAL);
			}
			break;
			case CHAR_LITERAL: {
				match(Kind.CHAR_LITERAL);
			}
			break;
			case STRING_LITERAL: {
				match(Kind.STRING_LITERAL);
			}
			break;
			case IDENTIFIER : {
				match(Kind.IDENTIFIER);
			}
			break;
			default: {
				if (checkKind(firstFunction)) {
					function();
				}
				else if (checkKind(Kind.LPAREN)) {
					match(Kind.LPAREN);
					expression();
					match(Kind.RPAREN);
				}
				else {
					throw new SyntaxException(t,"Syntax Error");
				}
//				/
			}
		}
	}
	public void function() throws SyntaxException {
		match(t.kind);
		match(Kind.LPAREN);
		expression();
		match(Kind.RPAREN);
		
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
