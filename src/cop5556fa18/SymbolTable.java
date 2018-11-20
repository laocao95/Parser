package cop5556fa18;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPAST.*;

public class SymbolTable {
	private int current_scope;
	private int next_scope;
	private Stack<Integer> scope_stack;
	private HashMap<String, List<DecItem>> hashmap;
	private List<BinaryInferedModel> binaryInferedList;
	private List<FunctionInferedModel> functionInferedList;
	public SymbolTable() {
		next_scope = 0;
		scope_stack = new Stack<Integer>();
		hashmap = new HashMap<String, List<DecItem>>();
		initialInferedTable();
	}
	public void enterScope() {
		current_scope = next_scope++;
		scope_stack.push(current_scope);
	}
	public void closeScope() {
		scope_stack.pop();
		if (!scope_stack.empty()) {
			current_scope = scope_stack.peek();
		} else {
			current_scope = -1;
		}
	}
	public void add(String name, Declaration dec) {
		if (hashmap.containsKey(name)) {
			List<DecItem> list = hashmap.get(name);
			list.add(new DecItem(current_scope, dec));
		} else {
			List<DecItem> list = new ArrayList<>();
			list.add(new DecItem(current_scope, dec));
			hashmap.put(name, list);
		}
	}
	public Declaration lookup(String name) 
	{   
		if (hashmap.containsKey(name)) {
			List<DecItem> DecList = hashmap.get(name);
			@SuppressWarnings("unchecked")
			Stack<Integer> copy_stack = (Stack<Integer>) scope_stack.clone();
			while (!copy_stack.empty()) {
				int scopeNum = copy_stack.pop();
				for (DecItem item : DecList) {
					if (item.getScopeNum() == scopeNum) {
						return item.getDeclaration();
					}
				}
			}
			return null;
			
		} else {
			return null;
		}
	}
	
	public Boolean existInCurrentScope(String name) {
		if (hashmap.containsKey(name)) {
			List<DecItem> decList = hashmap.get(name);
			for (DecItem item : decList) {
				if (item.getScopeNum() == current_scope) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}
	
	public class DecItem {
		private int scope_num;
		private Declaration dec;
		public DecItem(int num, Declaration d) {
			scope_num = num;
			dec = d;
		}
		public int getScopeNum() {
			return scope_num;
		}
		public Declaration getDeclaration() {
			return dec;
		}
	}
	
	public PLPTypes.Type getBinaryInferedType(PLPTypes.Type e0, PLPTypes.Type e1, Kind opKind) {
		for (BinaryInferedModel model : binaryInferedList) {
			if (model.compare(e0, e1, opKind)) {
				return model.inferedType;
			}
		}
		return PLPTypes.Type.NONE;
	}
	
	public PLPTypes.Type getFunctionInferedType(PLPTypes.Type e, Kind functionKind) {
		for (FunctionInferedModel model : functionInferedList) {
			if (model.compare(e, functionKind)) {
				return model.inferedType;
			}
		}
		return PLPTypes.Type.NONE;
	}
	
	public class BinaryInferedModel {
		public PLPTypes.Type e0;
		public PLPTypes.Type e1;
		public List<Kind> opList;
		public PLPTypes.Type inferedType;
		
		public BinaryInferedModel(PLPTypes.Type e0, PLPTypes.Type e1, List<Kind> opList, PLPTypes.Type inferedType) {
			this.e0 = e0;
			this.e1 = e1;
			this.opList = opList;
			this.inferedType = inferedType;
		}
		public Boolean compare(PLPTypes.Type e0, PLPTypes.Type e1, Kind opKind) {
			if (e0 != this.e0 || e1 != this.e1) {
				return false;
			}
			for (Kind kind : opList) {
				if (kind == opKind) {
					return true;
				}
			}
			return false;
		}
	}
	
	public class FunctionInferedModel {
		public PLPTypes.Type e;
		public Kind functionKind;
		public PLPTypes.Type inferedType;
		
		public FunctionInferedModel(PLPTypes.Type e, Kind functionKind, PLPTypes.Type inferedType) {
			this.e = e;
			this.functionKind = functionKind;
			this.inferedType = inferedType;
		}
		public Boolean compare(PLPTypes.Type e, Kind functionKind) {
			if (e != this.e || functionKind != this.functionKind) {
				return false;
			}
			return true;
		}
	}
	public void initialInferedTable() {
		binaryInferedList = new ArrayList<>();
		List<Kind> opList1 = Arrays.asList(Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_TIMES, Kind.OP_DIV, Kind.OP_MOD, Kind.OP_POWER, Kind.OP_AND, Kind.OP_OR);
		List<Kind> opList2 = Arrays.asList(Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_TIMES, Kind.OP_DIV, Kind.OP_POWER);
		List<Kind> opList3 = Arrays.asList(Kind.OP_PLUS);
		List<Kind> opList4 = Arrays.asList(Kind.OP_AND, Kind.OP_OR);
		List<Kind> opList5 = Arrays.asList(Kind.OP_EQ, Kind.OP_NEQ, Kind.OP_GT, Kind.OP_GE, Kind.OP_LT, Kind.OP_LE);
		binaryInferedList.add(new BinaryInferedModel(PLPTypes.Type.INTEGER, PLPTypes.Type.INTEGER, opList1, PLPTypes.Type.INTEGER));
		binaryInferedList.add(new BinaryInferedModel(PLPTypes.Type.FLOAT, PLPTypes.Type.FLOAT, opList2, PLPTypes.Type.FLOAT));
		binaryInferedList.add(new BinaryInferedModel(PLPTypes.Type.FLOAT, PLPTypes.Type.INTEGER, opList2, PLPTypes.Type.FLOAT));
		binaryInferedList.add(new BinaryInferedModel(PLPTypes.Type.INTEGER, PLPTypes.Type.FLOAT, opList2, PLPTypes.Type.FLOAT));
		binaryInferedList.add(new BinaryInferedModel(PLPTypes.Type.STRING, PLPTypes.Type.STRING, opList3, PLPTypes.Type.STRING));
		binaryInferedList.add(new BinaryInferedModel(PLPTypes.Type.BOOLEAN, PLPTypes.Type.BOOLEAN, opList4, PLPTypes.Type.BOOLEAN));
		binaryInferedList.add(new BinaryInferedModel(PLPTypes.Type.INTEGER, PLPTypes.Type.INTEGER, opList4, PLPTypes.Type.INTEGER));
		binaryInferedList.add(new BinaryInferedModel(PLPTypes.Type.INTEGER, PLPTypes.Type.INTEGER, opList5, PLPTypes.Type.BOOLEAN));
		binaryInferedList.add(new BinaryInferedModel(PLPTypes.Type.FLOAT, PLPTypes.Type.FLOAT, opList5, PLPTypes.Type.BOOLEAN));
		binaryInferedList.add(new BinaryInferedModel(PLPTypes.Type.BOOLEAN, PLPTypes.Type.BOOLEAN, opList5, PLPTypes.Type.BOOLEAN));
		
		functionInferedList = new ArrayList<>();
		functionInferedList.add(new FunctionInferedModel(PLPTypes.Type.INTEGER, Kind.KW_abs, PLPTypes.Type.INTEGER));
		functionInferedList.add(new FunctionInferedModel(PLPTypes.Type.FLOAT, Kind.KW_abs, PLPTypes.Type.FLOAT));
		functionInferedList.add(new FunctionInferedModel(PLPTypes.Type.FLOAT, Kind.KW_sin, PLPTypes.Type.FLOAT));
		functionInferedList.add(new FunctionInferedModel(PLPTypes.Type.FLOAT, Kind.KW_cos, PLPTypes.Type.FLOAT));
		functionInferedList.add(new FunctionInferedModel(PLPTypes.Type.FLOAT, Kind.KW_atan, PLPTypes.Type.FLOAT));
		functionInferedList.add(new FunctionInferedModel(PLPTypes.Type.FLOAT, Kind.KW_log, PLPTypes.Type.FLOAT));
		functionInferedList.add(new FunctionInferedModel(PLPTypes.Type.INTEGER, Kind.KW_float, PLPTypes.Type.FLOAT));
		functionInferedList.add(new FunctionInferedModel(PLPTypes.Type.FLOAT, Kind.KW_float, PLPTypes.Type.FLOAT));
		functionInferedList.add(new FunctionInferedModel(PLPTypes.Type.FLOAT, Kind.KW_int, PLPTypes.Type.INTEGER));
		functionInferedList.add(new FunctionInferedModel(PLPTypes.Type.INTEGER, Kind.KW_int, PLPTypes.Type.INTEGER));
	}
}
