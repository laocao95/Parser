package cop5556fa18;

import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.LHS;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;

public class PLPTypeChecker implements PLPASTVisitor {
	private SymbolTable symbolTable;
	
	PLPTypeChecker() {
		symbolTable = new SymbolTable();
	}
	
	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	// Name is only used for naming the output file. 
		// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}
		
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symbolTable.enterScope();
		for (PLPASTNode node : block.declarationsAndStatements) {
			node.visit(this, arg);
		}
		symbolTable.closeScope();
		return null;
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		if (symbolTable.existInCurrentScope(declaration.name)) {
			throw new SemanticException(declaration.firstToken, "Duplicate local variable");
		}
		
		if (declaration.expression != null) {
			declaration.expression.visit(this, arg);
			
			if (PLPTypes.getType(declaration.type) != declaration.expression.type) {
				throw new SemanticException(declaration.firstToken, "VariableDeclaration type mismatch");
			}
		}
		symbolTable.add(declaration.name, declaration);
		
		return null;
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		for (String name : declaration.names) {
			if (symbolTable.existInCurrentScope(name)) {
				throw new SemanticException(declaration.firstToken, "Duplicate local variable");
			}
			symbolTable.add(name, declaration);
		}
		
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		expressionBooleanLiteral.type = PLPTypes.Type.BOOLEAN;
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		PLPTypes.Type type = symbolTable.getBinaryInferedType(expressionBinary.leftExpression.type, expressionBinary.rightExpression.type, expressionBinary.op);
		if (type == PLPTypes.Type.NONE) {
			throw new SemanticException(expressionBinary.firstToken, "Operation on ExpressionBinary is not allowed");
		}
		expressionBinary.type = type;
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.condition.visit(this, arg);
		if (expressionConditional.condition.type != PLPTypes.Type.BOOLEAN) {
			throw new SemanticException(expressionConditional.firstToken, "orExpressionCondition type should be boolean");
		}
		expressionConditional.trueExpression.visit(this, arg);
		expressionConditional.falseExpression.visit(this, arg);
		
		if (expressionConditional.trueExpression.type != expressionConditional.falseExpression.type) {
			throw new SemanticException(expressionConditional.firstToken, "Types of trueCondition and falseCondition mismatch");
		}
		expressionConditional.type = expressionConditional.trueExpression.type;
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		expressionFloatLiteral.type = PLPTypes.Type.FLOAT;
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		FunctionWithArg.expression.visit(this, arg);
		PLPTypes.Type type = symbolTable.getFunctionInferedType(FunctionWithArg.expression.type, FunctionWithArg.functionName);
		if (type == PLPTypes.Type.NONE) {
			throw new SemanticException(FunctionWithArg.firstToken, "Function on FunctionWithArg is not allowed");
		}
		FunctionWithArg.type = type;
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		expressionIdent.dec = symbolTable.lookup(expressionIdent.name);
		if (expressionIdent.dec == null) {
			throw new SemanticException(expressionIdent.firstToken, "Identifier is not declared");
		}
		if (expressionIdent.dec.getClass() == VariableDeclaration.class) {
			VariableDeclaration dec = (VariableDeclaration)expressionIdent.dec;
			expressionIdent.type = PLPTypes.getType(dec.type);
		}
		else if (expressionIdent.dec.getClass() == VariableListDeclaration.class) {
			VariableListDeclaration dec = (VariableListDeclaration)expressionIdent.dec;
			expressionIdent.type = PLPTypes.getType(dec.type);
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		expressionIntegerLiteral.type = PLPTypes.Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		expressionStringLiteral.type = PLPTypes.Type.STRING;
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		expressionCharLiteral.type = PLPTypes.Type.CHAR;
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		statementAssign.lhs.visit(this, arg);
		statementAssign.expression.visit(this, arg);
		if (statementAssign.lhs.type != statementAssign.expression.type) {
			throw new SemanticException(statementAssign.firstToken, "Assignment types mismatch");
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.condition.visit(this, arg);
		if (ifStatement.condition.type != PLPTypes.Type.BOOLEAN) {
			throw new SemanticException(ifStatement.firstToken, "IfStatement condition is not boolean");
		}
		ifStatement.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		whileStatement.condition.visit(this, arg);
		if (whileStatement.condition.type != PLPTypes.Type.BOOLEAN) {
			throw new SemanticException(whileStatement.firstToken, "WhileStatement condition is not boolean");
		}
		whileStatement.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		printStatement.expression.visit(this, arg);
		if (printStatement.expression.type == PLPTypes.Type.NONE) {
			throw new SemanticException(printStatement.firstToken, "Can not print None value");
		}
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.time.visit(this, arg);
		if (sleepStatement.time.type != PLPTypes.Type.INTEGER) {
			throw new SemanticException(sleepStatement.firstToken, "SleepStatement time should be Integer");
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		if (expressionUnary.op == Kind.OP_EXCLAMATION) {
			if (!(expressionUnary.expression.type == PLPTypes.Type.INTEGER || expressionUnary.expression.type == PLPTypes.Type.BOOLEAN)) {
				throw new SemanticException(expressionUnary.firstToken, "Unary operation disallowed");
			}
		}
		
		if (expressionUnary.op == Kind.OP_PLUS || expressionUnary.op == Kind.OP_MINUS) {
			if (!(expressionUnary.expression.type == PLPTypes.Type.INTEGER || expressionUnary.expression.type == PLPTypes.Type.FLOAT)) {
				throw new SemanticException(expressionUnary.firstToken, "Unary operation disallowed");
			}
		}
		
		expressionUnary.type = expressionUnary.expression.type;
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		lhs.dec = symbolTable.lookup(lhs.identifier);
		if (lhs.dec == null) {
			throw new SemanticException(lhs.firstToken, "Identifier is not declared");
		}
		
		if (lhs.dec.getClass() == VariableDeclaration.class) {
			VariableDeclaration dec = (VariableDeclaration)lhs.dec;
			lhs.type = PLPTypes.getType(dec.type);
		}
		else if (lhs.dec.getClass() == VariableListDeclaration.class) {
			VariableListDeclaration dec = (VariableListDeclaration)lhs.dec;
			lhs.type = PLPTypes.getType(dec.type);
		}
		return null;
	}
}
