package cop5556sp17;

import static cop5556sp17.Scanner.Kind.SEMI;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Kind;

public class ScannerTest {

	@Rule
    public ExpectedException thrown = ExpectedException.none();

	@Test
	public void test0001() throws IllegalCharException, IllegalNumberException {
		String input = "p {\ninteger y \ny <- 6 + 7;}";
		//String input = "p {\ninteger y \ny <- 6 + 7;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
	}


	@Test
	public void testEmpty() throws IllegalCharException, IllegalNumberException {
		String input = "";
		Scanner scanner = new Scanner(input);
		scanner.scan();
	}

	@Test
	public void testSemiConcat() throws IllegalCharException, IllegalNumberException {
		//input string
		String input = ";;;";
		//create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		//get the first token and check its kind, position, and contents
		Scanner.Token token = scanner.nextToken();
		assertEquals(Kind.SEMI, token.kind);
		assertEquals(0, token.pos);
		String text = SEMI.getText();
		assertEquals(text.length(), token.length);
		assertEquals(text, token.getText());
		//get the next token and check its kind, position, and contents
		Scanner.Token token1 = scanner.nextToken();
		assertEquals(SEMI, token1.kind);
		assertEquals(1, token1.pos);
		assertEquals(text.length(), token1.length);
		assertEquals(text, token1.getText());
		Scanner.Token token2 = scanner.nextToken();
		assertEquals(SEMI, token2.kind);
		assertEquals(2, token2.pos);
		assertEquals(text.length(), token2.length);
		assertEquals(text, token2.getText());
		//check that the scanner has inserted an EOF token at the end
		Scanner.Token token3 = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF,token3.kind);
	}


	/**
	 * This test illustrates how to check that the Scanner detects errors properly.
	 * In this test, the input contains an int literal with a value that exceeds the range of an int.
	 * The scanner should detect this and throw and IllegalNumberException.
	 *
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 **/
	@Test
	public void testIntOverflowError() throws IllegalCharException, IllegalNumberException{
		String input = "99999999999999999";
		Scanner scanner = new Scanner(input);

		thrown.expect(IllegalNumberException.class);
		scanner.scan();
		//Scanner.Token token = scanner.nextToken();
		//assertEquals(Kind.INT_LIT, token.kind);
	}

	/**
	 * This test illustrates how to check that the Scanner detects errors properly.
	 * In this test, the input contains an int literal with a value that exceeds the range of an int.
	 * The scanner should detect this and throw and IllegalNumberException.
	 *
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	/*@Test
	public void testShobhit() throws IllegalCharException, IllegalNumberException{

		Scanner scanner = new Scanner(input);
		scanner.scan();


		//Scanner.Token token = scanner.nextToken();
		//assertEquals(Kind.INT_LIT, token.kind);
	}*/

//TODO  more tests
	@Test
	public void testFailed() throws IllegalCharException, IllegalNumberException{
		String input = "screenwidth screenheight blur gray convolve scale";
		Scanner scanner = new Scanner(input);
		scanner.scan();

		Scanner.Token token = scanner.nextToken();
		assertEquals(Kind.KW_SCREENWIDTH, token.kind);

		token = scanner.nextToken();
		assertEquals(Kind.KW_SCREENHEIGHT, token.kind);

		token = scanner.nextToken();
		assertEquals(Kind.OP_BLUR, token.kind);

		//Scanner.Token token = scanner.nextToken();
		//assertEquals(Kind.INT_LIT, token.kind);
	}

	//TODO  more tests
	@Test
	public void testFailed2() throws IllegalCharException, IllegalNumberException{
			String input = "0 0 0 \r\n 123";
			Scanner scanner = new Scanner(input);
			scanner.scan();


			Scanner.Token token = scanner.nextToken();
			assertEquals(Kind.INT_LIT, token.kind);

			token = scanner.nextToken();
			assertEquals(Kind.INT_LIT, token.kind);

			token = scanner.nextToken();
			assertEquals(Kind.INT_LIT, token.kind);
			assertEquals(4, token.pos);

			token = scanner.nextToken();
			assertEquals(Kind.INT_LIT, token.kind);
			assertEquals(9, token.pos);
		}


}
