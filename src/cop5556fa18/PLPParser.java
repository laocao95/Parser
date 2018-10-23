package cop5556fa18;

import cop5556fa18.PLPScanner.Token;
import javafx.scene.control.TabPane.TabClosingPolicy;
import cop5556fa18.PLPScanner.Kind;

import java.util.ArrayList;
import java.util.List;

import cop5556fa18.PLPAST.*;
/*
 * Name: Zhiwei Cao    id:50945378   P4
 * */
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
	
	public Program parse() throws SyntaxException {
		Program p = null;
		
		p = program();
		//match(Kind.EOF);
		matchEOF();
		
		return p;
	}
	
	/*
	 * Program -> Identifier Block
	 */
	Program program() throws SyntaxException {
		Program p = null;
		Token firstToken = t;
		match(Kind.IDENTIFIER);
		String name = String.copyValueOf(scanner.chars, firstToken.pos, firstToken.length);
		Block b = block();
		p = new Program(firstToken, name, b);
		return p;
	}
	
	/*
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char, Kind.KW_string /* Complete this */ };
	Kind[] firstStatement = {Kind.KW_if, Kind.KW_while, Kind.KW_sleep, Kind.KW_print, Kind.IDENTIFIER/* Complete this */  };
	Kind[] firstFunction = {Kind.KW_sin, Kind.KW_cos, Kind.KW_atan, Kind.KW_abs, Kind.KW_log, Kind.KW_int, Kind.KW_float};
	
	
	public Block block() throws SyntaxException {
		Block b = null;
		Token firstToken = t;
		List<PLPASTNode> nodeList = new ArrayList<>();
		
		match(Kind.LBRACE);
		
		while (checkKind(firstDec) | checkKind(firstStatement)) {
			
			if (checkKind(firstDec)) {
				Declaration dec = declaration();
				nodeList.add(dec);
			} else if (checkKind(firstStatement)) {
				Statement s = statement();
				nodeList.add(s);
			}
			match(Kind.SEMI);
		}
		match(Kind.RBRACE);
		
		b = new Block(firstToken, nodeList);
		return b;
	}
	
	public Declaration declaration() throws SyntaxException {
		Declaration dec = null;
		Token firstToken = t;
		Kind type = t.kind;
		List<String> names = new ArrayList<>();
		//already check, just consume
		match(type);
		// type identifier
		names.add(String.copyValueOf(scanner.chars, t.pos, t.length));
		match(Kind.IDENTIFIER);
			
		// type identifier = expression
		if (checkKind(Kind.OP_ASSIGN)) {
			match(Kind.OP_ASSIGN);
			Expression e = expression();
			dec = new VariableDeclaration(firstToken, type, names.get(0), e);
		} else if (checkKind(Kind.COMMA)) {
			// Identifier (, Identifier)*
			while(checkKind(Kind.COMMA)) {
				match(Kind.COMMA);
				names.add(String.copyValueOf(scanner.chars, t.pos, t.length));
				match(Kind.IDENTIFIER);
			}
			dec = new VariableListDeclaration(firstToken, type, names);
		}
		if (dec == null) {
			dec = new VariableDeclaration(firstToken, type, names.get(0), null);
		}
		return dec;
	}
	
	public Statement statement() throws SyntaxException {
		//TODO
		Statement s = null;
		Token firstToken = t;
		
		switch (t.kind) {
			case KW_if: {
				match(Kind.KW_if);
				match(Kind.LPAREN);
				Expression e = expression();
				match(Kind.RPAREN);
				Block b = block();
				s = new IfStatement(firstToken, e, b);
			}
			break;
			case KW_while: {
				match(Kind.KW_while);
				match(Kind.LPAREN);
				Expression e = expression();
				match(Kind.RPAREN);
				Block b = block();
				s = new WhileStatement(firstToken, e, b);
			}
			break;
			case IDENTIFIER: {
				String name = String.copyValueOf(scanner.chars, firstToken.pos, firstToken.length);
				match(Kind.IDENTIFIER);
				match(Kind.OP_ASSIGN);
				Expression e = expression();
				s = new AssignmentStatement(firstToken, name, e);
			}
			break;
			case KW_sleep: {
				match(Kind.KW_sleep);
				Expression e = expression();
				s = new SleepStatement(firstToken, e);
			}
			break;
			case KW_print: {
				match(Kind.KW_print);
				Expression e = expression();
				s = new PrintStatement(firstToken, e);
			}
			break;
			default:{
				//
			}
		}
		
		return s;
			
		//throw new UnsupportedOperationException();
	}
	
	public Expression expression() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		Expression orExpr = orExpression();
		if (checkKind(Kind.OP_QUESTION)) {
			match(Kind.OP_QUESTION);
			Expression trueExpr = expression();
			match(Kind.OP_COLON);
			Expression falseExpr = expression();
			e = new ExpressionConditional(firstToken, orExpr, trueExpr, falseExpr);
		} else {
			e = orExpr;
		}
		return e;
	}
	
	public Expression orExpression() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		Expression leftExpr = andExpression();
		Expression rightExpr = null;
		while (checkKind(Kind.OP_OR)) {
			match(Kind.OP_OR);
			if (e != null) {
				leftExpr = e;
			}
			rightExpr = andExpression();
			e = new ExpressionBinary(firstToken, leftExpr, Kind.OP_OR, rightExpr);
		}
		if (e == null) {
			e = leftExpr;
		}
		return e;
	}
	
	public Expression andExpression() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		Expression leftExpr = eqExpression();
		Expression rightExpr = null;
		while(checkKind(Kind.OP_AND)) {
			match(Kind.OP_AND);
			if (e != null) {
				leftExpr = e;
			}
			rightExpr = eqExpression();
			e = new ExpressionBinary(firstToken, leftExpr, Kind.OP_AND, rightExpr);
		}
		if (e == null) {
			e = leftExpr;
		}
		return e;
	}
	
	public Expression eqExpression() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		Expression leftExpr = relExpression();
		Expression rightExpr = null;
		while(checkKind(Kind.OP_EQ) || checkKind(Kind.OP_NEQ)) {
			Kind kind = t.kind;
			match(kind);
			if (e != null) {
				leftExpr = e;
			}
			rightExpr = relExpression();
			e = new ExpressionBinary(firstToken, leftExpr, kind, rightExpr);
		}
		if (e == null) {
			e = leftExpr;
		}
		return e;
	}
	
	public Expression relExpression() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		Expression leftExpr = addExpression();
		Expression rightExpr = null;
		while(checkKind(Kind.OP_LT) || checkKind(Kind.OP_GT) || checkKind(Kind.OP_GE) || checkKind(Kind.OP_LE)) {
			Kind kind = t.kind;
			match(kind);
			if (e != null) {
				leftExpr = e;
			}
			rightExpr = addExpression();
			e = new ExpressionBinary(firstToken, leftExpr, kind, rightExpr);
		}
		if (e == null) {
			e = leftExpr;
		}
		return e;
	}
	
	public Expression addExpression() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		Expression leftExpr = multExpression();
		Expression rightExpr = null;
		while(checkKind(Kind.OP_PLUS) || checkKind(Kind.OP_MINUS)) {
			Kind kind = t.kind;
			match(kind);
			if (e != null) {
				leftExpr = e;
			}
			rightExpr = multExpression();
			e = new ExpressionBinary(firstToken, leftExpr, kind, rightExpr);
		}
		if (e == null) {
			e = leftExpr;
		}
		return e;
	}
	
	public Expression multExpression() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		Expression leftExpr = powerExpression();
		Expression rightExpr = null;
		while(checkKind(Kind.OP_TIMES) || checkKind(Kind.OP_DIV) || checkKind(Kind.OP_MOD)) {
			Kind kind = t.kind;
			match(kind);
			if (e != null) {
				leftExpr = e;
			}
			rightExpr = powerExpression();
			e = new ExpressionBinary(firstToken, leftExpr, kind, rightExpr);
		}
		if (e == null) {
			e = leftExpr;
		}
		return e;
	}
	
	public Expression powerExpression() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		Expression leftExpr = unaryExpression();
		Expression rightExpr = null;
		if (checkKind(Kind.OP_POWER)) {
			match(Kind.OP_POWER);
			rightExpr = powerExpression();
			e = new ExpressionBinary(firstToken, leftExpr, Kind.OP_POWER, rightExpr);
		}
		if (e == null) {
			e = leftExpr;
		}
		return e;
	}
	
	public Expression unaryExpression() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		if (checkKind(Kind.OP_PLUS) || checkKind(Kind.OP_MINUS) || checkKind(Kind.OP_EXCLAMATION)) {
			match(firstToken.kind);
			Expression e0 = unaryExpression();
			e = new ExpressionUnary(firstToken, firstToken.kind, e0);
		} else {
			e = primary();
		}
		return e;
	}
	
	public Expression primary() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		switch (t.kind) {
			case INTEGER_LITERAL: {
				match(Kind.INTEGER_LITERAL);
				String str = String.copyValueOf(scanner.chars, firstToken.pos, firstToken.length);
				int value = Integer.parseInt(str);
				e = new ExpressionIntegerLiteral(firstToken, value);
			}
			break;
			case BOOLEAN_LITERAL: {
				match(Kind.BOOLEAN_LITERAL);
				String str = String.copyValueOf(scanner.chars, firstToken.pos, firstToken.length);
				Boolean value = Boolean.parseBoolean(str);
				e = new ExpressionBooleanLiteral(firstToken, value);
			}
			break;
			case FLOAT_LITERAL: {
				match(Kind.FLOAT_LITERAL);
				String str = String.copyValueOf(scanner.chars, firstToken.pos, firstToken.length);
				float value = Float.parseFloat(str);
				e = new ExpressionFloatLiteral(firstToken, value);
			}
			break;
			case CHAR_LITERAL: {
				match(Kind.CHAR_LITERAL);
				char text = scanner.chars[firstToken.pos + 1];
				e = new ExpressionCharLiteral(firstToken, text);
			}
			break;
			case STRING_LITERAL: {
				match(Kind.STRING_LITERAL);
				String text = String.copyValueOf(scanner.chars, firstToken.pos + 1, firstToken.length - 2);
				e = new ExpressionStringLiteral(firstToken, text);
			}
			break;
			case IDENTIFIER : {
				match(Kind.IDENTIFIER);
				String name = String.copyValueOf(scanner.chars, firstToken.pos, firstToken.length);
				e = new ExpressionIdentifier(firstToken, name);
			}
			break;
			default: {
				if (checkKind(firstFunction)) {
					e = function();
				}
				else if (checkKind(Kind.LPAREN)) {
					match(Kind.LPAREN);
					e = expression();
					match(Kind.RPAREN);
				}
				else {
					throw new SyntaxException(t,"Syntax Error");
				}

			}
		}
		return e;
	}
	public Expression function() throws SyntaxException {
		Expression e = null;
		Token firstToken = t;
		match(firstToken.kind);
		match(Kind.LPAREN);
		e = expression();
		match(Kind.RPAREN);
		e = new FunctionWithArg(firstToken, firstToken.kind, e);
		return e;
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
	
	private Token matchEOF() throws SyntaxException {
		if (checkKind(Kind.EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error"); //TODO  give a better error message!
	}
	
	/**
	 * @param kind
	 * @return 
	 * @return
	 * @throws SyntaxException
	 */
	private void match(Kind kind) throws SyntaxException {
		if (checkKind(kind)) {
			t = scanner.nextToken();
		} else {
			//TODO  give a better error message!
			throw new SyntaxException(t,"Syntax Error");
		}
	}

}
