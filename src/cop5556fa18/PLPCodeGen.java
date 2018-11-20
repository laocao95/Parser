package cop5556fa18;

import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
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
import cop5556fa18.PLPTypes.Type;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class PLPCodeGen implements PLPASTVisitor, Opcodes {
	
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slotCounter = 3;
	
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	

	public PLPCodeGen(String sourceFileName, boolean dEVEL, boolean gRADE) {
		super();
		this.sourceFileName = sourceFileName;
		DEVEL = dEVEL;
		GRADE = gRADE;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		Label startLabel = new Label();
		Label endLable = new Label();
		for (PLPASTNode node : block.declarationsAndStatements) {
			if (node.getClass() == VariableDeclaration.class) {
				VariableDeclaration vd = (VariableDeclaration)node;
				vd.setSlot(slotCounter);
				slotCounter++;
			}
			else if (node.getClass() == VariableListDeclaration.class) {
				VariableListDeclaration vsd = (VariableListDeclaration) node;
				for (String name : vsd.names) {
					vsd.setSlot(name, slotCounter);
					slotCounter++;
				}
			}
		}
		mv.visitLabel(startLabel);
		for (PLPASTNode node : block.declarationsAndStatements) {
			node.visit(this, null);
		}
		mv.visitLabel(endLable);
		for (PLPASTNode node : block.declarationsAndStatements) {
			if (node.getClass() == VariableDeclaration.class) {
				VariableDeclaration vd = (VariableDeclaration)node;
				switch(vd.type) {
					case KW_int:{
						mv.visitLocalVariable(vd.name, "I", null, startLabel, endLable, vd.getSlot());
					}
					break;
					case KW_float:{
						mv.visitLocalVariable(vd.name, "F", null, startLabel, endLable, vd.getSlot());
					}
					break;
					case KW_boolean:{
						mv.visitLocalVariable(vd.name, "Z", null, startLabel, endLable, vd.getSlot());
					}
					break;
					case KW_char:{
						mv.visitLocalVariable(vd.name, "C", null, startLabel, endLable, vd.getSlot());
					}
					break;
					case KW_string:{
						mv.visitLocalVariable(vd.name, "Ljava/lang/String;", null, startLabel, endLable, vd.getSlot());
					}
					break;
					default: {
						throw new Exception("Type error");
					}
				}
			} else if (node.getClass() == VariableListDeclaration.class) {
				VariableListDeclaration vsd = (VariableListDeclaration) node;
				for (String name : vsd.names) {
					switch(vsd.type) {
						case KW_int:{
							mv.visitLocalVariable(name, "I", null, startLabel, endLable, vsd.getSlot(name));
						}
						break;
						case KW_float:{
							mv.visitLocalVariable(name, "F", null, startLabel, endLable, vsd.getSlot(name));
						}
						break;
						case KW_boolean:{
							mv.visitLocalVariable(name, "Z", null, startLabel, endLable, vsd.getSlot(name));
						}
						break;
						case KW_char:{
							mv.visitLocalVariable(name, "C", null, startLabel, endLable, vsd.getSlot(name));
						}
						break;
						case KW_string:{
							mv.visitLocalVariable(name, "Ljava/lang/String;", null, startLabel, endLable, vsd.getSlot(name));
						}
						break;
						default: {
							throw new Exception("Type error");
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); 
		// If the call to mv.visitMaxs(1, 1) crashes, it is sometimes helpful 
		// to temporarily run it without COMPUTE_FRAMES. You probably won't 
		// get a completely correct classfile, but you will be able to see the 
		// code that was generated.
		
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		PLPCodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		PLPCodeGenUtils.genLog(DEVEL, mv, "leaving main");
		
		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor, asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();			
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		//mv.visitLocalVariable(declaration.name, "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		if (declaration.expression != null) {
			declaration.expression.visit(this, null);
			switch(declaration.expression.getType()) {
				case INTEGER:{
					mv.visitVarInsn(ISTORE, declaration.getSlot());
				}
				break;
				case FLOAT:{
					mv.visitVarInsn(FSTORE, declaration.getSlot());
				}
				break;
				case BOOLEAN:{
					mv.visitVarInsn(ISTORE, declaration.getSlot());
				}
				break;
				case CHAR:{
					mv.visitVarInsn(ISTORE, declaration.getSlot());
				}
				break;
				case STRING:{
					mv.visitVarInsn(ASTORE, declaration.getSlot());
				}
				break;
				default: {
					throw new Exception("Type error");
				}
			}
		}
		return null;
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//nothing to do
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBinary.leftExpression.visit(this, null);
		expressionBinary.rightExpression.visit(this, null);
		Type typeLeft = expressionBinary.leftExpression.getType();
		Type typeRight = expressionBinary.rightExpression.getType();
		switch (expressionBinary.op) {
			case OP_PLUS: {
				if (typeLeft == Type.INTEGER && typeRight == Type.INTEGER)
				{
					mv.visitInsn(IADD);
				}
				else if (typeLeft == Type.FLOAT && typeRight == Type.FLOAT)
				{
					mv.visitInsn(FADD);
				}
				else if (typeLeft == Type.FLOAT && typeRight == Type.INTEGER)
				{
					mv.visitInsn(I2F);
					mv.visitInsn(FADD);
				}
				else if (typeLeft == Type.INTEGER && typeRight == Type.FLOAT)
				{
					mv.visitInsn(SWAP);
					mv.visitInsn(I2F);
					mv.visitInsn(FADD);
				}
				else if (typeLeft == Type.STRING && typeRight == Type.STRING) {					
					Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitVarInsn(ASTORE, 2);
					mv.visitVarInsn(ASTORE, 1);
					mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
				}
			}
			break;
			case OP_MINUS: {
				if (typeLeft == Type.INTEGER && typeRight == Type.INTEGER)
				{
					mv.visitInsn(ISUB);
				}
				else if (typeLeft == Type.FLOAT && typeRight == Type.FLOAT)
				{
					mv.visitInsn(FSUB);
				}
				else if (typeLeft == Type.FLOAT && typeRight == Type.INTEGER)
				{
					mv.visitInsn(I2F);
					mv.visitInsn(FSUB);
				}
				else if (typeLeft == Type.INTEGER && typeRight == Type.FLOAT)
				{
					mv.visitInsn(SWAP);
					mv.visitInsn(I2F);
					mv.visitInsn(SWAP);
					mv.visitInsn(FSUB);
				}

			}
			break;
			case OP_TIMES: {
				if (typeLeft == Type.INTEGER && typeRight == Type.INTEGER)
				{
					mv.visitInsn(IMUL);
				}
				else if (typeLeft == Type.FLOAT && typeRight == Type.FLOAT)
				{
					mv.visitInsn(FMUL);
				}
				else if (typeLeft == Type.FLOAT && typeRight == Type.INTEGER)
				{
					mv.visitInsn(I2F);
					mv.visitInsn(FMUL);
				}
				else if (typeLeft == Type.INTEGER && typeRight == Type.FLOAT)
				{
					mv.visitInsn(SWAP);
					mv.visitInsn(I2F);
					mv.visitInsn(FMUL);
				}
			}
			break;
			case OP_DIV: {
				if (typeLeft == Type.INTEGER && typeRight == Type.INTEGER)
				{
					mv.visitInsn(IDIV);
				}
				else if (typeLeft == Type.FLOAT && typeRight == Type.FLOAT)
				{
					mv.visitInsn(FDIV);
				}
				else if (typeLeft == Type.FLOAT && typeRight == Type.INTEGER)
				{
					mv.visitInsn(I2F);
					mv.visitInsn(FDIV);
				}
				else if (typeLeft == Type.INTEGER && typeRight == Type.FLOAT)
				{
					mv.visitInsn(SWAP);
					mv.visitInsn(I2F);
					mv.visitInsn(SWAP);
					mv.visitInsn(FDIV);
				}
			}
			break;
			case OP_MOD: {
				mv.visitInsn(IREM);
			}
			break;
			case OP_POWER: {
				if (typeLeft == Type.INTEGER && typeRight == Type.INTEGER)
				{			
					mv.visitInsn(I2D);
					mv.visitVarInsn(DSTORE, 1);
					mv.visitInsn(I2D);
					mv.visitVarInsn(DLOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2I);
				}
				else if (typeLeft == Type.FLOAT && typeRight == Type.FLOAT)
				{
					mv.visitInsn(F2D);
					mv.visitVarInsn(DSTORE, 1);
					mv.visitInsn(F2D);
					mv.visitVarInsn(DLOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2F);
				}
				else if (typeLeft == Type.FLOAT && typeRight == Type.INTEGER)
				{
					mv.visitInsn(I2D);
					mv.visitVarInsn(DSTORE, 1);
					mv.visitInsn(F2D);
					mv.visitVarInsn(DLOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2F);
				}
				else if (typeLeft == Type.INTEGER && typeRight == Type.FLOAT)
				{
					mv.visitInsn(F2D);
					mv.visitVarInsn(DSTORE, 1);
					mv.visitInsn(I2D);
					mv.visitVarInsn(DLOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2F);
				}

			}
			break;
			case OP_AND: {
				mv.visitInsn(IAND);
			}
			break;
			case OP_OR: {
				mv.visitInsn(IOR);
			}
			break;
			case OP_EQ: {
				Label l1 = new Label();
				Label l2 = new Label();
				if(typeLeft == Type.INTEGER || typeLeft == Type.BOOLEAN)
				{
					mv.visitJumpInsn(IF_ICMPNE, l1);
				}
				else if (typeLeft == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFNE, l1);
				}
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitLdcInsn(false);
				mv.visitLabel(l2);
			}
			break;
			case OP_NEQ: {
				Label l1 = new Label();
				Label l2 = new Label();
				if(typeLeft == Type.INTEGER || typeLeft == Type.BOOLEAN)
				{
					mv.visitJumpInsn(IF_ICMPEQ, l1);
				}
				else if (typeLeft == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFEQ, l1);
				}
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitLdcInsn(false);
				mv.visitLabel(l2);
			}
			break;
			case OP_GT: {
				Label l1 = new Label();
				Label l2 = new Label();
				if(typeLeft == Type.INTEGER || typeLeft == Type.BOOLEAN)
				{
					mv.visitJumpInsn(IF_ICMPLE, l1);
				}
				else if (typeLeft == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFGE, l1);
				}
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitLdcInsn(false);
				mv.visitLabel(l2);
			}
			break;
			case OP_GE: {
				Label l1 = new Label();
				Label l2 = new Label();
				if(typeLeft == Type.INTEGER || typeLeft == Type.BOOLEAN)
				{
					mv.visitJumpInsn(IF_ICMPLT, l1);
				}
				else if (typeLeft == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFGT, l1);
				}
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitLdcInsn(false);
				mv.visitLabel(l2);
			}
			break;
			case OP_LT: {
				Label l1 = new Label();
				Label l2 = new Label();
				if(typeLeft == Type.INTEGER || typeLeft == Type.BOOLEAN)
				{
					mv.visitJumpInsn(IF_ICMPGE, l1);
				}
				else if (typeLeft == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFLE, l1);
				}
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitLdcInsn(false);
				mv.visitLabel(l2);
			}
			break;
			case OP_LE: {
				Label l1 = new Label();
				Label l2 = new Label();
				if(typeLeft == Type.INTEGER || typeLeft == Type.BOOLEAN)
				{
					mv.visitJumpInsn(IF_ICMPGT, l1);
				}
				else if (typeLeft == Type.FLOAT) {
					mv.visitInsn(FCMPL);
					mv.visitJumpInsn(IFLT, l1);
				}
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitLdcInsn(false);
				mv.visitLabel(l2);
			}
			break;
			default: {
				throw new Exception("Type error");
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Label l1 = new Label();
		Label l2 = new Label();
		expressionConditional.condition.visit(this, null);
		mv.visitJumpInsn(IFEQ, l1);
		expressionConditional.trueExpression.visit(this, null);
		mv.visitJumpInsn(GOTO, l2);
		mv.visitLabel(l1);
		expressionConditional.falseExpression.visit(this, null);
		mv.visitLabel(l2);
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		// TODO Auto-generated method stub
		FunctionWithArg.expression.visit(this, null);
		switch (FunctionWithArg.functionName) {
			case KW_sin: {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
				mv.visitInsn(D2F);
			}
			break;
			case KW_cos: {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
				mv.visitInsn(D2F);
			}
			break;
			case KW_atan: {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
				mv.visitInsn(D2F);
			}
			break;
			case KW_log: {
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
				mv.visitInsn(D2F);
			}
			break;
			case KW_abs: {
				if (FunctionWithArg.expression.getType() == Type.INTEGER) {
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
				} else if (FunctionWithArg.expression.getType() == Type.FLOAT) {
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
				}
			}
			break;
			case KW_int: {
				if (FunctionWithArg.expression.getType() == Type.FLOAT) {
					mv.visitInsn(F2I);
				}
			}
			break;
			case KW_float: {
				if (FunctionWithArg.expression.getType() == Type.INTEGER) {
					mv.visitInsn(I2F);
				}
			}
			break;
			default: {
				throw new Exception("Function type error");
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if (expressionIdent.dec.getClass() == VariableDeclaration.class) {
			VariableDeclaration declartion = (VariableDeclaration) expressionIdent.dec;
			switch (declartion.type) {
				case KW_int:{
					mv.visitVarInsn(ILOAD, declartion.getSlot());
				}
				break;
				case KW_float:{
					mv.visitVarInsn(FLOAD, declartion.getSlot());
				}
				break;
				case KW_boolean:{
					mv.visitVarInsn(ILOAD, declartion.getSlot());
				}
				break;
				case KW_char:{
					mv.visitVarInsn(ILOAD, declartion.getSlot());
				}
				break;
				case KW_string:{
					mv.visitVarInsn(ALOAD, declartion.getSlot());
				}
				break;
				default: {
					throw new Exception("Type error");
				}
			}
		}
		else if (expressionIdent.dec.getClass() == VariableListDeclaration.class) {
			VariableListDeclaration declartion = (VariableListDeclaration) expressionIdent.dec;
			switch (declartion.type) {
				case KW_int:{
					mv.visitVarInsn(ILOAD, declartion.getSlot(expressionIdent.name));
				}
				break;
				case KW_float:{
					mv.visitVarInsn(FLOAD, declartion.getSlot(expressionIdent.name));
				}
				break;
				case KW_boolean:{
					mv.visitVarInsn(ILOAD, declartion.getSlot(expressionIdent.name));
				}
				break;
				case KW_char:{
					mv.visitVarInsn(ILOAD, declartion.getSlot(expressionIdent.name));
				}
				break;
				case KW_string:{
					mv.visitVarInsn(ALOAD, declartion.getSlot(expressionIdent.name));
				}
				break;
				default: {
					throw new Exception("Type error");
				}
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionStringLiteral.text);
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(expressionCharLiteral.text);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementAssign.lhs.visit(this, null);
		statementAssign.expression.visit(this, null);
		if (statementAssign.lhs.dec.getClass() == VariableDeclaration.class) {
			VariableDeclaration vd = (VariableDeclaration)statementAssign.lhs.dec;
			switch(vd.type) {
				case KW_int:{
					mv.visitVarInsn(ISTORE, vd.getSlot());
				}
				break;
				case KW_float:{
					mv.visitVarInsn(FSTORE, vd.getSlot());
				}
				break;
				case KW_boolean:{
					mv.visitVarInsn(ISTORE, vd.getSlot());
				}
				break;
				case KW_char:{
					mv.visitVarInsn(ISTORE, vd.getSlot());
				}
				break;
				case KW_string:{
					mv.visitVarInsn(ASTORE, vd.getSlot());
				}
				break;
				default: {
					throw new Exception("Type error");
				}
			}
		} else if (statementAssign.lhs.dec.getClass() == VariableListDeclaration.class) {
			VariableListDeclaration vsd = (VariableListDeclaration)statementAssign.lhs.dec;
			switch(vsd.type) {
				case KW_int:{
					mv.visitVarInsn(ISTORE, vsd.getSlot(statementAssign.lhs.identifier));
				}
				break;
				case KW_float:{
					mv.visitVarInsn(FSTORE, vsd.getSlot(statementAssign.lhs.identifier));
				}
				break;
				case KW_boolean:{
					mv.visitVarInsn(ISTORE, vsd.getSlot(statementAssign.lhs.identifier));
				}
				break;
				case KW_char:{
					mv.visitVarInsn(ISTORE, vsd.getSlot(statementAssign.lhs.identifier));
				}
				break;
				case KW_string:{
					mv.visitVarInsn(ASTORE, vsd.getSlot(statementAssign.lhs.identifier));
				}
				break;
				default: {
					throw new Exception("Type error");
				}
			}
		}
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//nothing to do
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Label l1 = new Label();
		ifStatement.condition.visit(this, null);
		mv.visitJumpInsn(IFEQ, l1);
		ifStatement.block.visit(this, null);
		mv.visitLabel(l1);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitJumpInsn(GOTO, l1);
		mv.visitLabel(l2);
		whileStatement.b.visit(this, arg);
		mv.visitLabel(l1);
		whileStatement.condition.visit(this, arg);
		mv.visitJumpInsn(IFNE, l2);
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		printStatement.expression.visit(this, arg);
		Type type = printStatement.expression.getType();
		switch (type) {
		case INTEGER : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(I)V", false);
		}
		break;
		case BOOLEAN : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Z)V", false);
		}
		break;
		case FLOAT : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(F)V", false);
		}
		break;
		case CHAR : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(C)V", false);
		}
		break;
		case STRING : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Ljava/lang/String;)V", false);
		}
		break;
		}
		return null;
		
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		sleepStatement.time.visit(this, null);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionUnary.expression.visit(this, arg);
		switch(expressionUnary.op) {
			case OP_MINUS:{
				if (expressionUnary.expression.getType() == Type.INTEGER) {
					mv.visitInsn(INEG);
				}
				else if (expressionUnary.expression.getType() == Type.FLOAT) {
					mv.visitInsn(FNEG);
				}
			}
			break;
			case OP_EXCLAMATION:{
				if (expressionUnary.expression.getType() == Type.INTEGER) {
					mv.visitLdcInsn(-1);
					mv.visitInsn(IXOR);
				}
				else if (expressionUnary.expression.getType() == Type.BOOLEAN) {
					Label l1 = new Label();
					Label l2 = new Label();
					mv.visitJumpInsn(IFEQ, l1);
					mv.visitLdcInsn(false);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitLdcInsn(true);
					mv.visitLabel(l2);
				}
			}
			break;
			default: {
				//throw new Exception("Type error");
			}
		}

		return null;
	}

}
