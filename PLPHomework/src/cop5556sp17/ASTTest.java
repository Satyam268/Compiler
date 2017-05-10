package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
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
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;
import cop5556sp17.AST.Statement;

public class ASTTest {

	static final boolean doPrint = true;
	static void show(Object s){
		if(doPrint){System.out.println(s);}
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
	}

	@Test
	public void testFactor2() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "false";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BooleanLitExpression.class, ast.getClass());
	}

	@Test
	public void testFactor3() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "screenwidth";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(ConstantExpression.class, ast.getClass());
	}


	@Test
	public void testBinaryExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}

	@Test
	public void testBinaryExpr1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}

	@Test
	public void testBinaryExpression2() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "true*screenheight";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(BooleanLitExpression.class, be.getE0().getClass());
		assertEquals(ConstantExpression.class, be.getE1().getClass());
		assertEquals(TIMES, be.getOp().kind);
	}

	@Test
	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "void file main,integer main2 {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.parse();
		assertEquals(Program.class, ast.getClass());
		Program p = (Program) ast;
		ArrayList<ParamDec> al = new ArrayList<ParamDec>();
		al = p.getParams();
		for (ParamDec pd : al){
			assertEquals(ParamDec.class, pd.getClass());
		}
		assertEquals(Block.class, p.getB().getClass());

	}

	@Test
	public void testProgram() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "void file main,integer main2 {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.parse();
		assertEquals(Program.class, ast.getClass());
		Program p = (Program) ast;
		ArrayList<ParamDec> al = new ArrayList<ParamDec>();
		al = p.getParams();
		for (ParamDec pd : al){
			assertEquals(ParamDec.class, pd.getClass());
		}
		assertEquals(Block.class, p.getB().getClass());

	}

	@Test
	public void testParamDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "url void";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.paramDec();
		assertEquals(ParamDec.class, ast.getClass());

	}

	@Test
	public void testBlock() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "{frame a image b sleep a<b; while(a>b){integer c} if(b==b){blur->scale;} a<-a<b;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.block();
		assertEquals(Block.class, ast.getClass());
		Block b = (Block) ast;
		ArrayList<Dec> ald = new ArrayList<Dec>();
		ald = b.getDecs();
		for (Dec d : ald){
			assertEquals(Dec.class, d.getClass());
		}
		ArrayList<Statement> als = new ArrayList<Statement>();
		als = b.getStatements();
		assertEquals(SleepStatement.class, als.get(0).getClass());
		assertEquals(WhileStatement.class, als.get(1).getClass());
		assertEquals(IfStatement.class, als.get(2).getClass());
		assertEquals(AssignmentStatement.class, als.get(3).getClass());


	}

	@Test
	public void testDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "image i";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.dec();
		assertEquals(Dec.class, ast.getClass());
	}

	@Test
	public void testChainTupleExpression() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "move->blur false%true,123|456";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.chain();
		assertEquals(BinaryChain.class, ast.getClass());
		BinaryChain bc = (BinaryChain) ast;
		assertEquals(FrameOpChain.class, bc.getE0().getClass());
		assertEquals(FilterOpChain.class, bc.getE1().getClass());
		FilterOpChain foc = (FilterOpChain) bc.getE1();
		assertEquals(Tuple.class, foc.getArg().getClass());
		Tuple tup = (Tuple) foc.getArg();
		ArrayList<Expression> al = (ArrayList<Expression>) tup.getExprList();
		for (Expression e:al) {
			assertEquals(Expression.class, e.getClass());
		}
		assertEquals(ARROW, bc.getArrow().kind);
	}




}
