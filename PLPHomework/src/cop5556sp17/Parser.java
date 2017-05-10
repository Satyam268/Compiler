package cop5556sp17;

import static cop5556sp17.Scanner.Kind.AND;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.ASSIGN;
import static cop5556sp17.Scanner.Kind.BARARROW;
import static cop5556sp17.Scanner.Kind.COMMA;
import static cop5556sp17.Scanner.Kind.DIV;
import static cop5556sp17.Scanner.Kind.EOF;
import static cop5556sp17.Scanner.Kind.EQUAL;
import static cop5556sp17.Scanner.Kind.GE;
import static cop5556sp17.Scanner.Kind.GT;
import static cop5556sp17.Scanner.Kind.IDENT;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_IF;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SCALE;
import static cop5556sp17.Scanner.Kind.KW_WHILE;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.LE;
import static cop5556sp17.Scanner.Kind.LPAREN;
import static cop5556sp17.Scanner.Kind.LT;
import static cop5556sp17.Scanner.Kind.MINUS;
import static cop5556sp17.Scanner.Kind.MOD;
import static cop5556sp17.Scanner.Kind.NOTEQUAL;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OR;
import static cop5556sp17.Scanner.Kind.PLUS;
import static cop5556sp17.Scanner.Kind.RPAREN;
import static cop5556sp17.Scanner.Kind.TIMES;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Test;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTNode;
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
import cop5556sp17.AST.WhileStatement;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t; //current token
	HashSet<String> paramDecHash;
	HashSet<String> decHash;
	HashSet<String> chainElemHash;
	HashSet<String> factorHash;
	HashSet<String> realOpHash;
	HashSet<String> statementHash;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();

		paramDecHash = new HashSet<>();
		decHash = new HashSet<>();
		chainElemHash = new HashSet<>();
		factorHash = new HashSet<>();
		realOpHash = new HashSet<>();
		statementHash = new HashSet<>();

		paramDecHash.add("url");
		paramDecHash.add("file");
		paramDecHash.add("integer");
		paramDecHash.add("boolean");

		decHash.add("integer");
		decHash.add("boolean");
		decHash.add("image");
		decHash.add("frame");

		//comprises of filterOp, frameOp, imageOp
		//chainElemHash.add("IDENT");
		chainElemHash.add("blur");
		chainElemHash.add("gray");
		chainElemHash.add("convolve");
		chainElemHash.add("show");
		chainElemHash.add("hide");
		chainElemHash.add("move");
		chainElemHash.add("xloc");
		chainElemHash.add("yloc");
		chainElemHash.add("width");
		chainElemHash.add("height");
		chainElemHash.add("scale");

		//factorHash.add("IDENT");
		//factorHash.add("INT_LIT");
		/*factorHash.add("true");
		factorHash.add("false");
		factorHash.add("screenheight");
		factorHash.add("screenwidth");
		factorHash.add("(");*/

		realOpHash.add("<");
		realOpHash.add("<=");
		realOpHash.add(">");
		realOpHash.add(">=");
		realOpHash.add("==");
		realOpHash.add("!=");

		statementHash.add("sleep");
		statementHash.add("while");
		statementHash.add("if");
		statementHash.addAll(chainElemHash);
		//statementHash.add("IDENT");
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 *
	 * @throws SyntaxException
	 */
	ASTNode parse() throws SyntaxException {
		ASTNode root =  program();
		matchEOF();
		return root;
	}

	Program program() throws SyntaxException {
		ArrayList<ParamDec> paramDecList = new ArrayList<ParamDec>();
		Token firstToken = match(Kind.IDENT);
		Block block;

		if(t.isKind(Kind.LBRACE)){
			block =block();
		}
		else{
			paramDecList.add(paramDec());
			while(t.isKind(COMMA)){
				match(COMMA);
				paramDecList.add(paramDec());
			}
			block = block();
		}
		return new Program(firstToken, paramDecList, block);
	}

	ParamDec paramDec() throws SyntaxException {

		Token firstToken = match(Kind.KW_URL, Kind.KW_FILE, Kind.KW_INTEGER, Kind.KW_BOOLEAN);
		Token current = match(Kind.IDENT);

		return new ParamDec(firstToken, current);
	}

	Block block() throws SyntaxException {
		ArrayList<Dec> decList=new ArrayList<>();
		ArrayList<Statement> statementList=new ArrayList<>();

		Token firstToken = match(Kind.LBRACE);

		while(decHash.contains(t.kind.getText()) || statementHash.contains(t.kind.getText()) || t.isKind(IDENT)){
			if(decHash.contains(t.kind.getText()))
				decList.add(dec());
			else
				statementList.add(statement());
		}

		match(Kind.RBRACE);
		return new Block(firstToken, decList ,statementList);
	}

	Dec dec() throws SyntaxException {
		Token firstToken = match(Kind.KW_INTEGER, Kind.KW_BOOLEAN, Kind.KW_IMAGE, Kind.KW_FRAME);
		Token current = match(Kind.IDENT);

		return new Dec(firstToken, current);
	}

	Statement statement() throws SyntaxException {
		Statement s;

		if(t.isKind(Kind.OP_SLEEP))
		{
			s= sleepStatement();
		}

		else if(t.isKind(KW_WHILE)){
			s = whileStatement();
		}

		else if(t.isKind(KW_IF)){
			s = ifStatement();
		}

		else
		{
			Token nextToken = scanner.peek();

			if (nextToken.isKind(Kind.ASSIGN))
			{
				s=assign();
			}
			else
			{
				s=chain();
			}

			match(Kind.SEMI);
		}

		return s;
	}

	AssignmentStatement assign() throws SyntaxException{
		Token firstToken = match(IDENT);
		match(ASSIGN);
		Expression exp = expression();
		return new AssignmentStatement(firstToken, new IdentLValue(firstToken), exp);
	}

	Chain chain() throws SyntaxException {
		Token first=t;
		ChainElem c1;
		ChainElem c2;

		c1=chainElem();
		Token temp=match(ARROW, BARARROW);
		c2=chainElem();

		BinaryChain bc=new BinaryChain(first, c1, temp, c2);
		while (t.isKind(Kind.ARROW) || t.isKind(Kind.BARARROW))
		{
			temp=match(ARROW, BARARROW);
			bc=new BinaryChain(first, bc, temp, chainElem());
		}
		return bc;
	}

	SleepStatement sleepStatement() throws SyntaxException
	{
		Token firstToken = match(Kind.OP_SLEEP);
		Expression e = expression();
		match(Kind.SEMI);
		return new SleepStatement(firstToken,e);
	}

	WhileStatement whileStatement() throws SyntaxException{
		Token firstToken = t;
		match(Kind.KW_WHILE);
		match(Kind.LPAREN);
		Expression exp = expression();
		match(Kind.RPAREN);
		Block block = block();
		return new WhileStatement(firstToken, exp, block);
	}

	IfStatement ifStatement() throws SyntaxException{
		Token firstToken = t;
		match(KW_IF);
		match(LPAREN);
		Expression exp = expression();
		match(RPAREN);
		Block block = block();
		return new IfStatement(firstToken, exp, block);
	}

	ChainElem chainElem() throws SyntaxException {
		ChainElem cElem;
		Token firstToken = t;
		if (t.isKind(OP_BLUR) || t.isKind(OP_CONVOLVE) || t.isKind(OP_GRAY))
		{
			cElem=filterOpChain();
		}
		else if (t.isKind(Kind.KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE) || t.isKind(Kind.KW_XLOC) || t.isKind(KW_YLOC))
		{
			cElem=frameOpChain();
		}
		else if (t.isKind(Kind.OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE))
		{
			cElem=imageOpChain();
		}
		else if (t.isKind(IDENT))
		{
			consume();
			cElem=new IdentChain(firstToken);
		}
		else
		{
			throw new SyntaxException("Unexpected "+ t.kind+" encountered");
		}
		return cElem;
	}

	FilterOpChain filterOpChain() throws SyntaxException
	{
		Token firstToken =t;
		consume();

		Tuple tuple=arg();
		return new FilterOpChain(firstToken, tuple);
	}

	FrameOpChain frameOpChain() throws SyntaxException
	{
		Token firstToken=t;
		consume();
		Tuple tuple=arg();
		return new FrameOpChain(firstToken, tuple);
	}

	ImageOpChain imageOpChain() throws SyntaxException
	{
		Token firstToken=t;
		consume();
		Tuple tuple=arg();
		return new ImageOpChain(firstToken, tuple);
	}

	Tuple arg() throws SyntaxException {
		//TODO
		Token first=t;
		ArrayList <Expression> tupleList=new ArrayList<>();
		if (t.isKind(Kind.LPAREN))
		{
			match(LPAREN);
			tupleList.add(expression());

			while (t.isKind(COMMA))
			{
				match(Kind.COMMA);
				tupleList.add(expression());
			}

			match(RPAREN);
		}
		return new Tuple(first, tupleList);
		//throw new UnimplementedFeatureException();
	}

	Expression expression() throws SyntaxException {
		Token firstToken = t;
		Expression exp = term();
		while(realOpHash.contains(t.kind.getText())){
			Token current = match(LT,LE,GT,GE,EQUAL,NOTEQUAL);
			exp = new BinaryExpression(firstToken, exp, current, term());
		}
		return exp;
	}

	Expression term() throws SyntaxException {
		Token firstToken = t;
		Expression exp = elem();
		while(t.isKind(PLUS) || t.isKind(Kind.MINUS) || t.isKind(OR)){
			Token current = match(PLUS,MINUS,OR);
			exp = new BinaryExpression(firstToken, exp, current, elem());
		}
		return exp;
	}

	Expression elem() throws SyntaxException {
		Token firstToken = t;
		Expression exp = factor();
		while(t.isKind(TIMES) || t.isKind(Kind.DIV) || t.isKind(AND) || t.isKind(Kind.MOD)){
			Token current = match(TIMES,DIV,AND,MOD);
			exp = new BinaryExpression(firstToken, exp, current, factor());
		}
		return exp;
	}

	Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			return new IdentExpression(consume());
		}
		case INT_LIT: {
			return new IntLitExpression(consume());
		}
		case KW_TRUE:
		case KW_FALSE: {
			return new BooleanLitExpression(consume());
		}
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			return new ConstantExpression(consume());
		}
		case LPAREN: {
			consume();
			Expression expression = expression();
			match(RPAREN);
			return expression;
		}
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 *
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 *
	 * Precondition: kind != EOF
	 *
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 *
	 * * Precondition: for all given kinds, kind != EOF
	 *
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		for(Kind k : kinds){
			if(t.isKind(k))
				return consume();
		}

		StringBuilder expectedKinds = new StringBuilder();
		for(Kind k : kinds){
			expectedKinds.append(k+"  ");
		}


		throw new SyntaxException("saw " + t.kind + "expected in: " + expectedKinds);
	}

	/**
	 * Gets the next token and returns the consumed token.
	 *
	 * Precondition: t.kind != EOF
	 *
	 * @return
	 *
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}


}
