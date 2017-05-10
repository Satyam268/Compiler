package cop5556sp17;

import java.util.ArrayList;
import java.util.Hashtable;

public class Scanner {
	/**
	 * Kind enum
	 */

	/// [mapper] hashmap (position, obj) ::
	//	position: pos in input array
	//	obj : text + line pos
	Hashtable<Integer, TokenDetails> mapper = new Hashtable<>();

	Hashtable<String, Scanner.Kind> seperators;
	Hashtable<String, Scanner.Kind> keywords;
	Hashtable<String, Scanner.Kind> operators;

	public void fillData() {
		keywords = new Hashtable<>();
		keywords.put("integer", Kind.KW_INTEGER);
		keywords.put("boolean", Kind.KW_BOOLEAN);
		keywords.put("image", Kind.KW_IMAGE);
		keywords.put("url", Kind.KW_URL);
		keywords.put("file", Kind.KW_FILE);
		keywords.put("frame", Kind.KW_FRAME);
		keywords.put("while", Kind.KW_WHILE);
		keywords.put("if", Kind.KW_IF);
		keywords.put("true", Kind.KW_TRUE);
		keywords.put("false", Kind.KW_FALSE);
		keywords.put("blur", Kind.OP_BLUR);
		keywords.put("gray", Kind.OP_GRAY);
		keywords.put("convolve", Kind.OP_CONVOLVE);
		keywords.put("screenheight", Kind.KW_SCREENHEIGHT);
		keywords.put("screenwidth", Kind.KW_SCREENWIDTH);
		keywords.put("width", Kind.OP_WIDTH);
		keywords.put("height", Kind.OP_HEIGHT);
		keywords.put("xloc", Kind.KW_XLOC);
		keywords.put("yloc", Kind.KW_YLOC);
		keywords.put("hide", Kind.KW_HIDE);
		keywords.put("show", Kind.KW_SHOW);
		keywords.put("move", Kind.KW_MOVE);
		keywords.put("sleep", Kind.OP_SLEEP);
		keywords.put("scale", Kind.KW_SCALE);
		keywords.put("eof", Kind.EOF);

		seperators = new Hashtable<>();
		seperators.put(";", Kind.SEMI);
		seperators.put(",", Kind.COMMA);
		seperators.put("(", Kind.LPAREN);
		seperators.put(")", Kind.RPAREN);
		seperators.put("{", Kind.LBRACE);
		seperators.put("}", Kind.RBRACE);

		operators = new Hashtable<>();
		operators.put("->", Kind.ARROW);
		operators.put("|->", Kind.BARARROW);
		operators.put("|", Kind.OR);
		operators.put("&", Kind.AND);
		operators.put("==", Kind.EQUAL);
		operators.put("!=", Kind.NOTEQUAL);
		operators.put("<", Kind.LT);
		operators.put(">", Kind.GT);
		operators.put("<=", Kind.LE);
		operators.put(">=", Kind.GE);
		operators.put("+", Kind.PLUS);
		operators.put("-", Kind.MINUS);
		operators.put("*", Kind.TIMES);
		operators.put("/", Kind.DIV);
		operators.put("%", Kind.MOD);
		operators.put("!", Kind.NOT);
		operators.put("<-", Kind.ASSIGN);

	}

	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), KW_IMAGE("image"), KW_URL("url"), KW_FILE(
				"file"), KW_FRAME("frame"), KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), SEMI(
						";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), RBRACE("}"), ARROW("->"), BARARROW(
								"|->"), OR("|"), AND("&"), EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(
										">="), PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), ASSIGN(
												"<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE(
														"convolve"), KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH(
																"screenwidth"), OP_WIDTH("width"), OP_HEIGHT(
																		"height"), KW_XLOC("xloc"), KW_YLOC(
																				"yloc"), KW_HIDE("hide"), KW_SHOW(
																						"show"), KW_MOVE(
																								"move"), OP_SLEEP(
																										"sleep"), KW_SCALE(
																												"scale"), EOF(
																														"eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}

	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown by Scanner when an int literal is not a value that can be
	 * represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
		public IllegalNumberException(String message) {
			super(message);
		}
	}

	/**
	 * Holds the line and position in the line of a token.
	 * //row and column
	 */
	static class LinePos {
		public final int line;//line number
		public final int posInLine;//position in line

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line= " + line + ", posInLine= " + posInLine + "]";
		}
	}

	public class Token {
		public final Kind kind;
		public final int pos;// position in input array
		public final int length;

		// returns the text of this Token
		public String getText() {
			//System.out.println("position is:"+ pos);
			return mapper.get(pos).text;
		}

		// returns a LinePos object representing the line and column of this
		// Token
		LinePos getLinePos() {
			return mapper.get(pos).linepos;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		public String toString() {
			return mapper.get(pos).text + " " + mapper.get(pos).linepos.toString();
		}

		/**
		 * Precondition: kind = Kind.INT_LIT, the text can be represented with a
		 * Java int. Note that the validity of the input should have been
		 * checked when the Token was created. So the exception should never be
		 * thrown.
		 *
		 * @return int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException {
			return Integer.parseInt(mapper.get(pos).text);
		}

		public boolean isKind(Kind kind) {
			if(kind == this.kind)
				return true;
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Token)) {
				return false;
			}
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (kind != other.kind) {
				return false;
			}
			if (length != other.length) {
				return false;
			}
			if (pos != other.pos) {
				return false;
			}
			return true;
		}
		private Scanner getOuterType() {
			return Scanner.this;
		}
	}

	//contructor of the scanner class
	Scanner(String chars) {
		this.chars = chars + "  ";
		//appends 2 spaces to the end to avoid out of bound for operators
		tokens = new ArrayList<Token>();
		fillData();
	}

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to
	 * tokens list.
	 *
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	//main functionality where stream passed and token created
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int i = 0;//pos
		int row = 0, col = 0;//line=0, posInLine=0

		for (i = 0; i < chars.length() - 2; i++) {//loop until 2 leeser then end

			// line change
			if (isLineChange(chars.charAt(i), chars.charAt(i + 1))) {
				row++;//increments row

				if (chars.charAt(i) == '\r') {
					i++;//ensures that i increments by 2
				}

				col = 0;
				continue;
			}

			// isWhiteSpace
			if (Character.isWhitespace(chars.charAt(i))) {
				col++;
				continue;
			}

			// section for handling comments
			if (chars.charAt(i) == '/' && chars.charAt(i + 1) == '*') {

				//length -1 cause checks for i+1
				while (i < chars.length() - 1) {
					if (chars.charAt(i) == '*' && chars.charAt(i + 1) == '/') {
						col++;
						i++;
						break;
					}

					if (isLineChange(chars.charAt(i), chars.charAt(i + 1))) {
						row++;
						col = 0;

						if (chars.charAt(i) == '\r') {
							i++;
						}
						i++;
						continue;
					}

					col++;
					i++;
				}
				continue;//if program ends at comment
			}

			//no need to increment i as continued
			if (seperators.containsKey(chars.charAt(i) + "")) {
				tokens.add(new Token(seperators.get(chars.charAt(i) + ""), i, 1));
				mapper.put(i, new TokenDetails(row, col, chars.charAt(i) + ""));
				col++;
				continue;
			}

			// operator
			String op1, op2, op3;
			op1 = chars.charAt(i) + "";
			op2 = op1 + chars.charAt(i + 1);
			op3 = op2 + chars.charAt(i + 2);

			if (operators.containsKey(op3)) {
				tokens.add(new Token(operators.get(op3), i, 3));
				mapper.put(i, new TokenDetails(row, col, op3));
				i += 2;
				col += 3;
				continue;
			}

			else if (operators.containsKey(op2)) {
				tokens.add(new Token(operators.get(op2), i, 2));
				mapper.put(i, new TokenDetails(row, col, op2));
				i += 1;
				col += 2;
				continue;
			}

			else if (operators.containsKey(op1)) {
				tokens.add(new Token(operators.get(op1), i, 1));
				mapper.put(i, new TokenDetails(row, col, op1));
				col += 1;
				continue;
			}

			//number literal
			if (chars.charAt(i) == '0') {
				tokens.add(new Token(Kind.INT_LIT, i, 1));
				mapper.put(i, new TokenDetails(row, col, chars.charAt(i) + ""));
				col++;
				continue;
			}

			StringBuilder temporary_token = new StringBuilder();
			int start = i;
			if (Character.isDigit(chars.charAt(i)) && chars.charAt(i) > '0') {

				while (i < chars.length() && Character.isDigit(chars.charAt(i))) {
					temporary_token.append(chars.charAt(i));
					checkInt(temporary_token);//checks that number is integer only
					i++;
					col++;
				}

				tokens.add(new Token(Kind.INT_LIT, i - temporary_token.length(), temporary_token.length()));
				mapper.put(start, new TokenDetails(row, col - temporary_token.length(), temporary_token.toString()));
				i--; // to go one char back ; as for loop will increment i
				continue;
			}

			// ident , keywords
			start = i;
			temporary_token = new StringBuilder();
			if (Character.isJavaIdentifierStart(chars.charAt(i))) {
				temporary_token.append(chars.charAt(i));
				i++;
				col++;
				while (i < chars.length() && (Character.isJavaIdentifierPart(chars.charAt(i)))) {
					temporary_token.append(chars.charAt(i));
					i++;
					col++;
				}
				i--; // coz 1 index ahead
				if (keywords.containsKey(temporary_token + "")) {
					tokens.add(new Token(keywords.get(temporary_token.toString()), start, temporary_token.length()));
					mapper.put(start,
							new TokenDetails(row, col - temporary_token.length(), temporary_token.toString()));
					continue;
				}

				tokens.add(new Token(Kind.IDENT, start, temporary_token.length()));
				mapper.put(start, new TokenDetails(row, col - temporary_token.length(), temporary_token.toString()));
				continue;
			}
			throw new IllegalCharException(chars.charAt(i)+"");
		}
		mapper.put(i, new TokenDetails(row, col, "eof"));
		tokens.add(new Token(Kind.EOF, i, 0));

		/*System.out.println("here");*/
		/*for(Token t : tokens){
			System.out.println("text:  " + t.getText() + "	pos:  " +t.getLinePos());
		}*/

		return this;

	}

	private void checkInt(StringBuilder numberLit) throws IllegalNumberException {
		try {
			Integer.parseInt(numberLit.toString());
		}
		catch (Exception ex) {
			throw new IllegalNumberException("Number out of bound");
		}
	}

	private boolean isLineChange(char curr, char next) {
		if (curr == '\n' || (curr == '\r' && next == '\n'))
			return true;
		return false;
	}

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that the
	 * next call will return the Token..
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	/*
	 * Return the next token in the token list without updating the state. (So
	 * the following call to next will return the same token.)
	 */
	public Token peek() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);
	}

	/**
	 * Returns a LinePos object containing the line and position in line of the
	 * given token.
	 *
	 * Line numbers start counting at 0
	 *
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		return t.getLinePos();
	}

	/// obj : text + line pos
	public class TokenDetails {

		LinePos linepos;
		String text;

		//row, column and text in token
		TokenDetails(int row, int col, String text) {
			this.linepos = new LinePos(row, col);
			this.text = text;
		}

	}
}
