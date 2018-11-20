package cop5556fa18.PLPAST;

import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes;

public abstract class Expression extends PLPASTNode {
	public PLPTypes.Type type;
	public Expression(Token firstToken) {
		super(firstToken);
	}
	public PLPTypes.Type getType() { 
		return type; 
	} 
}