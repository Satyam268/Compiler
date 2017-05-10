package cop5556sp17;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;


public class ParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void test0001() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "p integer i, boolean b {i<-33; b<-false;}";//"p {\ninteger y \ny <- 6 + 7;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser=new Parser(scanner);
		parser.expression();
	}


	@Test
	public void official_01() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "sleepImg url u {image i frame f \nu -> i -> convolve -> f -> show;sleep 5;integer j j <- 42;\n}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser=new Parser(scanner);
		parser.expression();
	}


	@Test
	public void test00011() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "{ xyza123|->; }";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser=new Parser(scanner);
		parser.parse();
		thrown.expect(Parser.SyntaxException.class);
	}

}
