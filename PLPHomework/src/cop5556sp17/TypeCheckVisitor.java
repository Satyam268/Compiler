package cop5556sp17;

import static cop5556sp17.AST.Type.*;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.INTEGER;
import static cop5556sp17.AST.Type.TypeName.NONE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.DIV;
import static cop5556sp17.Scanner.Kind.EQUAL;
import static cop5556sp17.Scanner.Kind.GE;
import static cop5556sp17.Scanner.Kind.GT;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_SCALE;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.LE;
import static cop5556sp17.Scanner.Kind.MINUS;
import static cop5556sp17.Scanner.Kind.NOTEQUAL;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.TIMES;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}
	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Chain chain = binaryChain.getE0();
		chain.visit(this, null);

		Token op=binaryChain.getArrow();

		ChainElem chainElement = binaryChain.getE1();
		chainElement.visit(this, null);

		Token t=chainElement.getFirstToken();

		if (chain.getTypeName().isType(TypeName.URL) && op.isKind(ARROW) && chainElement.getTypeName().isType(TypeName.IMAGE))
		{
			binaryChain.setTypeName(TypeName.IMAGE);
		}
		else if (chain.getTypeName().isType(TypeName.FILE) && op.isKind(Kind.ARROW) && chainElement.getTypeName().isType(TypeName.IMAGE))
		{
			binaryChain.setTypeName(TypeName.IMAGE);
		}
		else if (chain.getTypeName().isType(TypeName.FRAME) && op.isKind(Kind.ARROW) && (chainElement  instanceof FrameOpChain && (t.isKind(Kind.KW_XLOC) || t.isKind(Kind.KW_YLOC))))
		{
			binaryChain.setTypeName(TypeName.INTEGER);
		}
		else if (chain.getTypeName().isType(TypeName.FRAME) && op.isKind(Kind.ARROW) && (chainElement  instanceof FrameOpChain && (t.isKind(Kind.KW_SHOW) || t.isKind(Kind.KW_HIDE) || t.isKind(Kind.KW_MOVE))))
		{
			binaryChain.setTypeName(TypeName.FRAME);
		}
		else if (chain.getTypeName().isType(TypeName.IMAGE) && op.isKind(Kind.ARROW) && (chainElement  instanceof ImageOpChain && (t.isKind(Kind.OP_WIDTH) || t.isKind(Kind.OP_HEIGHT))))
		{
			binaryChain.setTypeName(TypeName.INTEGER);
		}
		else if (chain.getTypeName().isType(TypeName.IMAGE) && op.isKind(Kind.ARROW) && chainElement.getTypeName().isType(TypeName.FRAME))
		{
			binaryChain.setTypeName(TypeName.FRAME);
		}
		else if (chain.getTypeName().isType(TypeName.IMAGE) && op.isKind(Kind.ARROW) && chainElement.getTypeName().isType(TypeName.FILE))
		{
			binaryChain.setTypeName(TypeName.NONE);
		}
		else if (chain.getTypeName().isType(TypeName.IMAGE) && (op.isKind(Kind.ARROW) || op.isKind(Kind.BARARROW)) && (chainElement  instanceof FilterOpChain && (t.isKind(Kind.OP_GRAY) || t.isKind(Kind.OP_BLUR) || t.isKind(Kind.OP_CONVOLVE))))
		{
			binaryChain.setTypeName(TypeName.IMAGE);
		}
		else if (chain.getTypeName().isType(TypeName.IMAGE) && op.isKind(Kind.ARROW) && (chainElement  instanceof ImageOpChain && (t.isKind(Kind.KW_SCALE))))
		{
			binaryChain.setTypeName(TypeName.IMAGE);
		}
		else if (chain.getTypeName().isType(TypeName.IMAGE) && op.isKind(Kind.ARROW) && (chainElement  instanceof IdentChain && chainElement.getTypeName().isType(TypeName.IMAGE)))
		{
			binaryChain.setTypeName(TypeName.IMAGE);
		}
		else if (chain.getTypeName().isType(TypeName.INTEGER) && op.isKind(Kind.ARROW) && (chainElement  instanceof IdentChain && chainElement.getTypeName().isType(TypeName.INTEGER)) )
		{
			binaryChain.setTypeName(TypeName.INTEGER);
		}
		else
		{
			throw new TypeCheckException("not allowed");
		}
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		Expression expression0 = binaryExpression.getE0();
		expression0.visit(this, null);
		Expression expression1 = binaryExpression.getE1();
		expression1.visit(this, null);
		Token op= binaryExpression.getOp();

		TypeName typeE0 = binaryExpression.getE0().getTypeName();
		TypeName typeE1 = binaryExpression.getE1().getTypeName();

		switch (op.getText()) {
		case "+":
		case "-":
			if (typeE0 == TypeName.INTEGER && typeE1 == TypeName.INTEGER) {
				binaryExpression.setTypeName(INTEGER);
			}
			else if (typeE0 == TypeName.IMAGE && typeE1 == TypeName.IMAGE) {
				binaryExpression.setTypeName(IMAGE);
			}
			else {
				throw new TypeCheckException("PLUS/MINUS operated on other than INTEGER or IMAGE.");
			}
			break;

		case ">":
		case "<":
		case ">=":
		case "<=":
			if (typeE0 == TypeName.INTEGER && typeE1 == TypeName.INTEGER) {
				binaryExpression.setTypeName(TypeName.BOOLEAN);
			}
			else if (typeE0 == TypeName.BOOLEAN && typeE1 == TypeName.BOOLEAN) {
				binaryExpression.setTypeName(TypeName.BOOLEAN);
			}
			else {
				throw new TypeCheckException("LE/GE/GT/LT operated on other than INTEGER or BOOLEAN.");
			}
			break;
		case "==":
		case "!=":
			if (typeE0 == typeE1) {
				binaryExpression.setTypeName(TypeName.BOOLEAN);
			}
			else {
				throw new TypeCheckException("EQUAL/NOT EQUAL operated on expressions of different types.");
			}
			break;
		case "*":
			if (typeE0 == TypeName.INTEGER && typeE1 == TypeName.INTEGER) {
				binaryExpression.setTypeName(INTEGER);
			}
			else if (typeE0 == TypeName.INTEGER && typeE1 == TypeName.IMAGE) {
				binaryExpression.setTypeName(IMAGE);
			}
			else if (typeE0 == TypeName.IMAGE && typeE1 == TypeName.INTEGER) {
				binaryExpression.setTypeName(IMAGE);
			}
			else {
				throw new TypeCheckException("TIMES operated on invalid combination of INTEGER or IMAGE.");
			}
			break;
		case "/":
			if (typeE0 == TypeName.INTEGER && typeE1 == TypeName.INTEGER) {
				binaryExpression.setTypeName(INTEGER);
			}
			else if (typeE0.equals(IMAGE) && typeE1.equals(INTEGER)) {
				binaryExpression.setTypeName(TypeName.IMAGE);
			}
			else {
				throw new TypeCheckException("DIV operated on other than INTEGER.");
			}
			break;
		case "&":
			if (typeE0 == TypeName.BOOLEAN && typeE1 == TypeName.BOOLEAN) {
				binaryExpression.setTypeName(TypeName.BOOLEAN);
			} else {
				throw new TypeCheckException("AND operated on other than BOOLEAN.");
			}
			break;
		case "|":
			if (typeE0 == TypeName.BOOLEAN && typeE1 == TypeName.BOOLEAN) {
				binaryExpression.setTypeName(TypeName.BOOLEAN);
			} else {
				throw new TypeCheckException("OR operated on other than BOOLEAN.");
			}
			break;
		case "%":
			if (typeE0 == TypeName.INTEGER && typeE1 == TypeName.INTEGER) {
				binaryExpression.setTypeName(INTEGER);
			}
			else if (typeE0 == TypeName.IMAGE && typeE1 == TypeName.INTEGER) {
				binaryExpression.setTypeName(IMAGE);
			}
			else {
				throw new TypeCheckException("MOD operated on other than INTEGER.");
			}
			break;
		default: {
			throw new TypeCheckException("Invalid operand found in Binary Expression.");
		}
		}
		return null;


		/*TypeName e1_Type=expression0.getTypeName();
		TypeName e2_Type=expression1.getTypeName();

		if (e1_Type.isType(TypeName.INTEGER) && e2_Type.isType(TypeName.INTEGER) && (op.isKind(Kind.PLUS) || op.isKind(MINUS)))
		{
			binaryExpression.setTypeName(TypeName.INTEGER);
		}
		else if (e1_Type.isType(TypeName.IMAGE) && e2_Type.isType(TypeName.IMAGE) && (op.isKind(Kind.PLUS) || op.isKind(MINUS)))
		{
			binaryExpression.setTypeName(IMAGE);
		}
		else if (e1_Type.isType(TypeName.INTEGER) && e2_Type.isType(TypeName.INTEGER) && (op.isKind(Kind.TIMES) || op.isKind(DIV)))
		{
			binaryExpression.setTypeName(INTEGER);
		}
		else if (e1_Type.isType(TypeName.INTEGER) && e2_Type.isType(TypeName.IMAGE) && op.isKind(TIMES))
		{
			binaryExpression.setTypeName(TypeName.IMAGE);
		}
		else if (e1_Type.isType(TypeName.IMAGE) && e2_Type.isType(TypeName.INTEGER) && op.isKind(TIMES))
		{
			binaryExpression.setTypeName(TypeName.IMAGE);
		}
		else if (e1_Type.isType(TypeName.INTEGER) && e2_Type.isType(TypeName.INTEGER) && (op.isKind(Kind.LT) || op.isKind(LE) || op.isKind(GT) || op.isKind(GE)))
		{
			binaryExpression.setTypeName(TypeName.BOOLEAN);
		}
		else if (e1_Type.isType(TypeName.BOOLEAN) && e2_Type.isType(TypeName.BOOLEAN) && (op.isKind(Kind.LT) || op.isKind(LE) || op.isKind(GT) || op.isKind(GE)))
		{
			binaryExpression.setTypeName(TypeName.BOOLEAN);
		}
		else if (op.isKind(EQUAL) || op.isKind(NOTEQUAL))
		{
			if (!e1_Type.isType(e2_Type))
			{
				throw new TypeCheckException("type mismatch in binary expression");
			}
			binaryExpression.setTypeName(TypeName.BOOLEAN);
		}
		else
		{
			throw new TypeCheckException("not allowed");
		}

		return null;
		 */
		}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symtab.enterScope();
		ArrayList<Dec> decList = block.getDecs();
		for (Dec dec : decList) {
			dec.visit(this, null);
		}

		ArrayList<Statement> statementList = block.getStatements();
		for (Statement statement : statementList) {

			statement.visit(this, null);
		}

		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		booleanLitExpression.setTypeName(TypeName.BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		List<Expression> expList=filterOpChain.getArg().getExprList();

		if (expList.size()!=0)
		{
			throw new TypeCheckException("tuple has size != 0");
		}

		filterOpChain.setTypeName(IMAGE);
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {

		Token token = frameOpChain.getFirstToken();
		List<Expression> expList= frameOpChain.getArg().getExprList();

		if(token.isKind(Kind.KW_SHOW) || token.isKind(KW_HIDE)){
			if(expList.size() != 0){
				throw new TypeCheckException("");
			}
			frameOpChain.setTypeName(NONE);
		}
		else if(token.isKind(Kind.KW_XLOC)||token.isKind(KW_YLOC)){
			if(expList.size() != 0){
				throw new TypeCheckException("tuple has size != 0");
			}
			frameOpChain.setTypeName(TypeName.INTEGER);
		}
		else if(token.isKind(Kind.KW_MOVE)){
			if(expList.size() != 2){
				throw new TypeCheckException("tuple has size != 2");
			}
			//not sure of
			frameOpChain.getArg().visit(this, null);
			frameOpChain.setTypeName(NONE);
		}
		else{
			new Exception("Bug in Parser");
		}

		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		String ident = identChain.getFirstToken().getText();
		Dec decIdent= symtab.lookup(ident);

		identChain.setDec(decIdent);
		if (decIdent==null)
		{
			throw new TypeCheckException(ident+"is not declared");
		}

		Token identType=decIdent.getType();
		identChain.setTypeName(Type.getTypeName(identType));

		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {

		Dec declaration=symtab.lookup(identExpression.getFirstToken().getText());

		if (declaration == null)
		{
			throw new TypeCheckException("undeclared variable ");
		}

		identExpression.setTypeName(Type.getTypeName(declaration.getType()));
		identExpression.setDec(declaration);

		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {

		ifStatement.getE().visit(this, null);
		ifStatement.getB().visit(this, null);
		TypeName type = ifStatement.getE().getTypeName();

		if(!type.isType(TypeName.BOOLEAN)){
			throw new TypeCheckException("only boolean allowed inside if");
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		intLitExpression.setTypeName(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		//do i need to visit?
		sleepStatement.getE().visit(this, null);//required?
		TypeName expressionType = sleepStatement.getE().getTypeName();

		if(!expressionType.isType(TypeName.INTEGER)){
			throw new TypeCheckException("Only integer values allowed");
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {

		whileStatement.getE().visit(this, null);
		whileStatement.getB().visit(this, null);
		TypeName type = whileStatement.getE().getTypeName();
		if(type != TypeName.BOOLEAN){
			throw new TypeCheckException("only boolean allowed in while");
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//why is setType required? is it required?
		Boolean isInserted = symtab.insert(declaration.getIdent().getText(), declaration);
		if(!isInserted){
			throw new TypeCheckException("");
		}

		declaration.setTypeName(Type.getTypeName(declaration.getFirstToken()));
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		ArrayList<ParamDec> pdecList =  program.getParams();

		for(ParamDec pd : pdecList){
			pd.visit(this, null);
		}

		program.getB().visit(this, null);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {

		IdentLValue identLval = assignStatement.getVar();
		identLval.visit(this, null);

		Expression expression = assignStatement.getE();
		expression.visit(this, null);


		TypeName vartype=Type.getTypeName(identLval.getDeclaration().getType());
		TypeName expressionType=expression.getTypeName();

		if (vartype != expressionType)
		{
			throw new TypeCheckException("identlvalue and expression not of same type");
		}

		/*
		if(!identLval.getDeclaration().getTypeName().isType(expression.getTypeName())){
			throw new TypeCheckException("");
		}*/

		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {

		Dec declaration=symtab.lookup(identX.getFirstToken().getText());

		if (declaration==null)
		{
			throw new TypeCheckException("variable isn't declared");
		}

		identX.setDeclaration(declaration);

		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {

		paramDec.setTypeName(Type.getTypeName(paramDec.getFirstToken()));

		//paramDec.setSlot(-1); ?? is it required

		Boolean isInserted = symtab.insert(paramDec.getIdent().getText(),paramDec);
		if(!isInserted){
			throw new TypeCheckException("variable already declared");
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setTypeName(INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Token token = imageOpChain.getFirstToken();
		Tuple tuple = imageOpChain.getArg();

		if(token.isKind(Kind.OP_HEIGHT) || token.isKind(Kind.OP_WIDTH)){
			if(tuple.getExprList().size() != 0){
				throw new TypeCheckException("tuple size != 0");
			}
			imageOpChain.setTypeName(INTEGER);
		}
		else if(token.isKind(KW_SCALE)){
			if(tuple.getExprList().size() != 1){
				throw new TypeCheckException("tuple size != 1");
			}
			tuple.visit(this,null);
			imageOpChain.setTypeName(IMAGE);
		}
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		List<Expression> expressionList = tuple.getExprList();
		for (Expression expression : expressionList) {
			expression.visit(this, null); //expression here or after if
			if(!expression.getTypeName().isType(INTEGER)){
				throw new TypeCheckException("only integer allowed in tuple");
			}
		}
		return null;
	}

}
