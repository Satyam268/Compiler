//satyam

package cop5556sp17;

import static cop5556sp17.Scanner.Kind.KW_IMAGE;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	boolean barArrow = true;

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.slotNumber = 1;
		this.iterator = 0;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slotNumber;
	int length;
	int iterator;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;

		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();

		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.

		ArrayList<ParamDec> params = program.getParams();
		length = params.size();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, mv); //changed
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, startRun, endRun, 1);

		// TODO visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method

		cw.visitEnd();// end of class

		// generate classfile and return it
		return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// modified
		assignStatement.getE().visit(this, arg);

		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());

		if(assignStatement.getVar().getFirstToken().isKind(KW_IMAGE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",PLPRuntimeImageOps.copyImageSig, false);
		} ///commented for now

		assignStatement.getVar().visit(this, arg);
		return null;

		/*
		 * 	assignStatement.getE().visit(this, arg);
			CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
			CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
			assignStatement.getVar().visit(this, arg);
			return null;
				 AssignmentStatement = IdentLValue Expression
				store value of Expression into location indicated by IdentLValue

				if the type of elements is image, this should copy the image.
				use PLPRuntimeImageOps.copyImage //invoke static method
		 */
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		if(binaryChain.getE1().getFirstToken().kind.equals(Kind.OP_GRAY)){

			if(binaryChain.getArrow().kind.equals(Kind.BARARROW))
				barArrow=true;
			else
				barArrow=false;

		}

		binaryChain.getE0().visit(this, 1);
		binaryChain.getE1().visit(this, 2);
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		/*

		 Visit children to generate code to leave values of arguments on stack.
		Generate code to perform operation, leaving result on top of the stack.
		New in Assignment 6:  methods to add two images, subtract two images, etc.
		Routines are provided in PLPRuntimeImageOps.
		New in assignment 6:  implement &, |, and %.
		Expressions should be evaluated from left to write consistent with the structure of the AST.
		You may need to modify your TypeCheckVisitor t
		 */

		/*CodeGenUtils.genPrint(DEVEL, mv, "\nentered binary exp");

		if (binaryExpression.getE1().getTypeName().equals(TypeName.IMAGE)
				&& binaryExpression.getE0().getTypeName().equals(TypeName.INTEGER)) {
			binaryExpression.getE1().visit(this, arg);
			binaryExpression.getE0().visit(this, arg);
		} else {
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
		}

		Label SET_TRUE = new Label();
		Label EXIT = new Label();

		binaryExpression.getE0().visit(this, arg);//leaves something on top of stack
		binaryExpression.getE1().visit(this, arg);//leaves something on top of stack



		switch (binaryExpression.getOp().getText()) {
		case "==":
			mv.visitJumpInsn(IF_ICMPEQ, SET_TRUE);
			mv.visitLdcInsn(false);
			break;
		case "!=":
			mv.visitJumpInsn(IF_ICMPNE, SET_TRUE);
			mv.visitLdcInsn(false);
			break;
		case "<":
			mv.visitJumpInsn(IF_ICMPLT, SET_TRUE);
			mv.visitLdcInsn(false);
			break;
		case ">":
			mv.visitJumpInsn(IF_ICMPGT, SET_TRUE);
			mv.visitLdcInsn(false);
			break;
		case ">=":
			mv.visitJumpInsn(IF_ICMPGE, SET_TRUE);
			mv.visitLdcInsn(false);
			break;
		case "<=":
			mv.visitJumpInsn(IF_ICMPLE, SET_TRUE);
			mv.visitLdcInsn(false);
			break;
		case "+":
			if ((binaryExpression.getE0().getTypeName().equals(TypeName.IMAGE)) || (binaryExpression.getE1

().getTypeName().equals(TypeName.IMAGE))) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,

"add",PLPRuntimeImageOps.addSig,false);
			}
			else
			{
				mv.visitInsn(IADD);
			}
			break;
		case "-":
			if (binaryExpression.getE0().getTypeName().equals(TypeName.IMAGE) || (binaryExpression.getE0

().getTypeName().equals(TypeName.INTEGER))) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,

"add",PLPRuntimeImageOps.addSig,false);
			}
			else {
				mv.visitInsn(ISUB);
			}
			break;
		case "&":
			mv.visitInsn(IAND);
			break;
		case "|":
			mv.visitInsn(IOR);
			break;
		case "*":
			if (binaryExpression.getE0().getTypeName().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,

"add",PLPRuntimeImageOps.addSig,false);
			}
			else {
				mv.visitInsn(IMUL);
			}
			break;
		case "/":
			if (binaryExpression.getE0().getTypeName().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div",

PLPRuntimeImageOps.divSig, false);
			}
			else {
				mv.visitInsn(IDIV);
			}
			break;
		case "%":
			if (binaryExpression.getE0().getTypeName().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,

"mod",PLPRuntimeImageOps.modSig, false);
			} else {
				mv.visitInsn(IREM);
			}
			break;

		default:
			break;

		}
		mv.visitJumpInsn(GOTO, EXIT);

		mv.visitLabel(SET_TRUE);
		mv.visitLdcInsn(true);

		mv.visitLabel(EXIT);
		return null;
		 */

		if (binaryExpression.getE1().getTypeName().equals(TypeName.IMAGE)
				&&binaryExpression.getE0().getTypeName().equals(TypeName.INTEGER)) {
			// this tiny bit of hack ensures that multiplication is now
			// commutative
			binaryExpression.getE1().visit(this, arg);
			binaryExpression.getE0().visit(this, arg);
		} else {
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
		}
		switch (binaryExpression.getOp().kind) {
		case PLUS:
			if (binaryExpression.getE0().getTypeName().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "add",
						"(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",
						false);
			} else {
				mv.visitInsn(IADD);
			}
			break;
		case MINUS:
			if (binaryExpression.getE0().getTypeName().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "sub",
						"(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",
						false);
			} else {
				mv.visitInsn(ISUB);
			}
			break;
		case TIMES:
			if (binaryExpression.getE0().getTypeName().equals(TypeName.IMAGE)||binaryExpression.getE1().getTypeName().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul",
						"(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;",
						false);
			} else {
				mv.visitInsn(IMUL);
			}
			break;
		case DIV:
			if (binaryExpression.getE0().getTypeName().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "div", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
			} else {
				mv.visitInsn(IDIV);
			}
			break;
		case LT:
			Label l1 = new Label();
			Label exit1 = new Label();
			mv.visitJumpInsn(IF_ICMPLT, l1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, exit1);
			mv.visitLabel(l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(exit1);
			break;
		case LE:
			Label l2 = new Label();
			Label exit2 = new Label();
			mv.visitJumpInsn(IF_ICMPLE, l2);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, exit2);
			mv.visitLabel(l2);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(exit2);
			break;
		case GT:
			Label l3 = new Label();
			Label exit3 = new Label();
			mv.visitJumpInsn(IF_ICMPGT, l3);
			//System.out.println("about to set 0");
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, exit3);
			mv.visitLabel(l3);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(exit3);
			break;
		case GE:
			Label l4 = new Label();
			Label exit4 = new Label();
			mv.visitJumpInsn(IF_ICMPGE, l4);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, exit4);
			mv.visitLabel(l4);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(exit4);
			break;
		case EQUAL:
			Label l5 = new Label();
			Label exit5 = new Label();
			mv.visitJumpInsn(IF_ICMPEQ, l5);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, exit5);
			mv.visitLabel(l5);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(exit5);
			break;
		case NOTEQUAL:
			Label l6 = new Label();
			Label exit6 = new Label();
			mv.visitJumpInsn(IF_ICMPEQ, l6);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, exit6);
			mv.visitLabel(l6);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(exit6);
			break;
		case AND:
			mv.visitInsn(IAND);
			break;
		case OR:
			mv.visitInsn(IOR);
			break;
		case MOD:
			if (binaryExpression.getE0().getTypeName().equals(TypeName.IMAGE)) {
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mod",
						"(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
			} else {
				mv.visitInsn(IREM);
			}
			break;
		default:
			break;
		}
		return null;

	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Implement this
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering block");
		// local variables dec
		// statements executed in run method.

		// Create label at start of code
		Label blockStart = new Label();
		Label blockEnd = new Label();
		mv.visitLabel(blockStart);

		ArrayList<Dec> decs = block.getDecs();
		for (Dec dec : decs) {
			dec.visit(this, mv);
		}

		ArrayList<Statement> statements = block.getStatements();
		for (Statement statement : statements) {
			if (statement instanceof AssignmentStatement) {
				if (((AssignmentStatement) statement).getVar().getDeclaration() instanceof ParamDec) {
					mv.visitVarInsn(ALOAD, 0);
				}
			}
			statement.visit(this, mv);

			if(statement instanceof BinaryChain){
				mv.visitInsn(POP);
			}
		}

		// create label at end of code
		//If a statement was a BinaryChain, it will have left a value on top of the stack.
		//Check for this and pop it if necessary.
		mv.visitLabel(blockEnd);

		for (Dec dec : decs) {
			mv.visitLocalVariable(dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc(), null,blockStart, blockEnd,dec.getSlot());
		}

		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		CodeGenUtils.genPrint(DEVEL, mv, "\n entering bool lit");
		mv.visitLdcInsn(booleanLitExpression.getValue());
		CodeGenUtils.genPrint(DEVEL, mv, "\n exiting bool lit");
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		//added
		//assert false : "not yet implemented";
		if(constantExpression.getFirstToken().isKind(Kind.KW_SCREENHEIGHT)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",PLPRuntimeFrame.getScreenHeightSig, false);
		}
		else if(constantExpression.getFirstToken().isKind(Kind.KW_SCREENWIDTH)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",PLPRuntimeFrame.getScreenWidthSig, false);
		}

		//PLPRuntimeFrame
		/*ConstantExpression = screenWidth | screenHeight
			Generate code to invoke PLPRuntimeFrame.getScreenWidth
			or PLPRuntimeFrame.getScreenHeight.
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "parseLong", "(Ljava/lang/String;)J", false);
		 * */
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Implement this

		CodeGenUtils.genPrint(DEVEL, mv, "\nentering dec");

		// assign to local variable. using slotNumber++;
		declaration.setSlot(slotNumber++);
		if(declaration.getType().isKind(Kind.KW_FRAME)){
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSlot());
		}
		else if(declaration.getType().isKind(Kind.KW_IMAGE)){
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSlot());
		}

		CodeGenUtils.genPrint(DEVEL, mv, "\nexit visit dec");
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		//mv.visitInsn(DUP);
		if(filterOpChain.getFirstToken().isKind(Kind.OP_BLUR)){
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		else if(filterOpChain.getFirstToken().isKind(Kind.OP_CONVOLVE)){
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolveOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		if(filterOpChain.getFirstToken().isKind(Kind.OP_GRAY)){
			if(barArrow)
				mv.visitInsn(DUP);
			else
				mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		return null;
		/*
	FilterOpChain = filterOp Tuple
	Assume that a reference to a BufferedImage is on top of
	the stack.

	Generate code to invoke the appropriate method from
	PLPRuntimeFilterOps.

	filterOp ::= OP_BLUR |OP_GRAY | OP_CONVOLVE
		 */
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		//added

		frameOpChain.getArg().visit(this, arg);
		Kind frameType = frameOpChain.getFirstToken().kind;

		if(frameType == Kind.KW_SHOW){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage",PLPRuntimeFrame.showImageDesc, false);
		}
		else if(frameType == Kind.KW_HIDE){
			mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"hideImage",PLPRuntimeFrame.hideImageDesc,false);
		}
		else if(frameType == Kind.KW_MOVE){
			mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"moveFrame",PLPRuntimeFrame.moveFrameDesc,false);
		}
		else if(frameType == Kind.KW_XLOC){
			mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"getXVal",PLPRuntimeFrame.getXValDesc,false);
		}
		else if(frameType == Kind.KW_YLOC){
			mv.visitMethodInsn(INVOKEVIRTUAL,PLPRuntimeFrame.JVMClassName,"getYVal",PLPRuntimeFrame.getYValDesc,false);
		}

		/*
			FrameOpChain = frameOp Tuple
			Assume that a reference to a PLPRuntimeFrame is on top
			of the stack.

			Visit the tuple elements to generate code to
			leave their values on top of the stack.

			Generate code to invoke the appropriate method from
			PLPRuntimeFrame.

			frameOp ::= KW_SHOW | KW_HIDE | KW_MOVE | KW_XLOC |KW_YLOC
		 */

		return null;
	}

	//clean this
	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Integer part = (Integer) arg;
		TypeName type = identChain.getTypeName();

		// TODO: pass a value of 1 for left
		if(part==1){
			// if it is in the left side of the chain
			if(identChain.getDec() instanceof ParamDec){
				// class variable
				mv.visitVarInsn(ALOAD, 0);

				mv.visitFieldInsn(GETFIELD, className,
						identChain.getFirstToken().getText(),
						identChain.getTypeName().getJVMTypeDesc());

				switch(type) {
				case URL:
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className,
							"readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
					break;
				case FILE:
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className,
							"readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
					break;
				default:
					//fall through
				}
			} else {
				// local variable
				if (identChain.getTypeName().equals(TypeName.INTEGER)
						|| identChain.getTypeName().equals(TypeName.BOOLEAN)) {
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
				} else {
					switch(type) {
					case URL:
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className,
								"readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
						break;
					case FILE:
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className,
								"readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
						break;
					case FRAME:
					case IMAGE:
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
						break;
					default:
						//fall through
					}
				}
			}
		}
		else{
			// TODO: check if the image really needs to be copied

			if(identChain.getTypeName().equals(TypeName.FILE)){
				// first load the file object in which the image has to be copied
				// if a class variable
				if(identChain.getDec() instanceof ParamDec){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className,identChain.getDec().getIdent().getText(), type.getJVMTypeDesc());
				}
				else {
					// local variable
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				}
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",PLPRuntimeImageIO.writeImageDesc, false);
			}

			else if(identChain.getTypeName().equals(TypeName.FRAME)){
				// local variable
				mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName,
						"createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
			}
			mv.visitInsn(DUP);
			if (identChain.getTypeName().equals(TypeName.IMAGE)
					|| identChain.getTypeName().equals(TypeName.INTEGER)
					|| identChain.getTypeName().equals(TypeName.BOOLEAN)) {
				if (identChain.getDec() instanceof ParamDec) {
					// class variable
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText(),
							identChain.getTypeName().getJVMTypeDesc());
				} else {
					if (identChain.getTypeName().equals(TypeName.IMAGE)) {
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
					} else if (identChain.getTypeName().equals(TypeName.INTEGER)
							|| identChain.getTypeName().equals(TypeName.BOOLEAN)) {
						mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
					}
				}
			}
		}


		/*
		 * If this IdentChain is the right side of a binary expression,
		 *
		 * store the item on top of the stack into a variable (if
		 * INTEGER or IMAGE), or write to file (if FILE), or set as the
		 * image in the frame (if FRAME).
		 */

		/*
		 if(identChain.getTypename().equals(TypeName.FILE)){
				// first load the file object in which the image has to be copied
				// if a class variable
				if(identChain.getDc() instanceof ParamDec){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className,
							identChain.getDc().getIdent().getText(), type.getJVMTypeDesc());
				}
				else {
					// local variable
					mv.visitVarInsn(ALOAD, identChain.getDc().getSlotNumber());
				}

				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write",
						"(Ljava/awt/image/BufferedImage;Ljava/io/File;)

Ljava/awt/image/BufferedImage;", false);
			}
			else if(identChain.getTypename().equals(TypeName.FRAME)){
				// local variable
				mv.visitVarInsn(ALOAD, identChain.getDc().getSlotNumber());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName,
						"createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, identChain.getDc().getSlotNumber());
			}
			mv.visitInsn(DUP);
			if (identChain.getTypename().equals(TypeName.IMAGE)
			 || identChain.getTypename().equals(TypeName.INTEGER)
					|| identChain.getTypename().equals(TypeName.BOOLEAN)) {
				if (identChain.getDc() instanceof ParamDec) {
					// class variable
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getFirstToken().getText(),
							identChain.getTypename().getJVMTypeDesc());
				} else {
					if (identChain.getTypename().equals(TypeName.IMAGE)) {
						mv.visitVarInsn(ASTORE, identChain.getDc().getSlotNumber());
					} else if (identChain.getTypename().equals(TypeName.INTEGER)
							|| identChain.getTypename().equals(TypeName.BOOLEAN)) {
						mv.visitVarInsn(ISTORE, identChain.getDc().getSlotNumber());
					}
				}
			}
		 */

		/*else if(side.equals("right")){
			if(identChain.getTypeName().equals(TypeName.FILE)){
				// first load the file object in which the image has to be copied
				// if a class variable
				if(identChain.getDec() instanceof ParamDec){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className,identChain.getDec().getIdent().getText(),

Type.getJVMTypeDesc());
				}
				else {
					// local variable
					mv.visitVarInsn(ALOAD, identChain.getDc().getSlotNumber());
				}

				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write",
						"(Ljava/awt/image/BufferedImage;Ljava/io/File;)

Ljava/awt/image/BufferedImage;", false);
			} //till gere
		 */

		/*if(identType==TypeName.INTEGER){

				if(dec instanceof ParamDec)
				{
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(),

identType.getJVMTypeDesc());
				}
				else
				{
					mv.visitVarInsn(ISTORE, dec.getSlot());
				}
			}

			else if(identType==TypeName.IMAGE){
				mv.visitVarInsn(ASTORE, dec.getSlot());
			}
			else if(identType==TypeName.FILE){
				mv.visitVarInsn(ALOAD, dec.getSlot());
				mv.visitMethodInsn

(INVOKESTATIC,PLPRuntimeImageIO.className,"write",PLPRuntimeImageIO.writeImageDesc,false);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), identType.getJVMTypeDesc

());
			}
			else if(identType==TypeName.FRAME){
				mv.visitVarInsn(ALOAD, dec.getSlot());
				mv.visitMethodInsn

(INVOKESTATIC,PLPRuntimeFrame.JVMClassName,"createOrSetFrame",PLPRuntimeFrame.createOrSetFrameSig,false);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), identType.getJVMTypeDesc

());
			}
		 */		//}
		/* CONFUSION**
				Chain ?= ChainElem | BinaryChain
				ChainElem ::= IdentChain | FilterOpChain | FrameOpChain | ImageOpChain

				IdentChain ?= ident
				Handle the ident appropriately depending on its type and
				whether it is on the left or right side of binary chain.

				If on the left side, load its value or reference onto the stack.

				If this IdentChain is the right side of a binary expression,
				store the item on top of the stack into a variable (if INTEGER or IMAGE),
				or write to file (if FILE),
				or set as the image in the frame (if FRAME).
		 */
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//load value of variable (this could be a field or a local var)
		//take care of both types on basis of slots
		//if instance variable always push this first
		//IdentExpression ?= ident
		//load value of variable (this could be a field or a local var)

		CodeGenUtils.genPrint(DEVEL, mv, "\n entering ident exp");
		if (identExpression.getDec() instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, identExpression.firstToken.getText(), identExpression.getDec().getTypeName().getJVMTypeDesc());
			}
		else{
			if (identExpression.getTypeName().equals(TypeName.INTEGER) || identExpression.getTypeName().equals(TypeName.BOOLEAN)) {
				mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
			}
			else if(identExpression.getTypeName().equals(TypeName.IMAGE)
					||identExpression.getTypeName().equals(TypeName.FRAME)
					||identExpression.getTypeName().equals(TypeName.FILE)
					||identExpression.getTypeName().equals(TypeName.URL)){
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
			}
		}
		/*
		 if(identExpression.getDec() instanceof ParamDec){
			// class variable
			mv.visitVarInsn(ALOAD, 0);

			mv.visitFieldInsn(GETFIELD, className,
			identExpression.getFirstToken().getText(),
			 identExpression.getType().getJVMTypeDesc());
		} else {
			// local variable
			if (identExpression.getType().equals(TypeName.INTEGER)
			|| identExpression.getType().equals(TypeName.BOOLEAN)) {
				mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
			}else if(identExpression.getType().equals(TypeName.IMAGE)
			||identExpression.getType().equals(TypeName.FRAME)
			||identExpression.getType().equals(TypeName.FILE)
			||identExpression.getType().equals(TypeName.URL)){
				mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
			}
		}
		 */


		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		/*
	 	  IdentLValue = ident
        store value on top of stack to this variable
        (which could be a field or local var)
		 */



		if (identX.getDeclaration() instanceof ParamDec) {
			mv.visitFieldInsn(PUTFIELD, className, identX.getFirstToken().getText(),identX.getDeclaration().getTypeName().getJVMTypeDesc());
		}
		else{
			//frame ?
			if(identX.getDeclaration().getTypeName().equals(TypeName.IMAGE)){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, identX.getDeclaration().getSlot());
			}
			else if (identX.getDeclaration().getTypeName().equals(TypeName.INTEGER)
					|| identX.getDeclaration().getTypeName().equals(TypeName.BOOLEAN)){
				mv.visitVarInsn(ISTORE, identX.getDeclaration().getSlot());
			}
		}

		return null;


	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		Label AFTER = new Label();

		ifStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFEQ, AFTER);//jumps to after
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(AFTER);

		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		/*
		 ImageOpChain ?= imageOp Tuple
		Assume that a reference to a BufferedImage  is on top of
		the stack. Visit the tuple elements to generate code to leave
		their values on top of the stack. Generate code to invoke the
		appropriate method from PLPRuntimeImageOps or
		PLPRuntimeImageIO .
		 */
		Tuple tuple = imageOpChain.getArg();
		tuple.visit(this, mv);

		Kind imageKind = imageOpChain.getFirstToken().kind;

		if(imageKind == Kind.OP_WIDTH){
			mv.visitMethodInsn(INVOKESTATIC,"java/awt/Image","getWidth",PLPRuntimeImageOps.getWidthSig,false);
		}
		else if(imageKind == Kind.OP_HEIGHT){
			mv.visitMethodInsn(INVOKESTATIC,"java/awt/Image","getHeight",PLPRuntimeImageOps.getHeightSig,false);
		}
		else if(imageKind == Kind.KW_SCALE){
			mv.visitMethodInsn(INVOKESTATIC,PLPRuntimeImageOps.JVMName,"scale",PLPRuntimeImageOps.scaleSig,false);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		CodeGenUtils.genPrint(DEVEL, mv, "\n entering intLit Expression");
		mv.visitLdcInsn(new Integer(intLitExpression.value));
		CodeGenUtils.genPrint(DEVEL, mv, "\n exiting intLit Expression");
		return null;
	}

	//param dec - fix
	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {

		TypeName type  = paramDec.getTypeName();

		FieldVisitor fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), type.getJVMTypeDesc(), null, null);
		fv.visitEnd();

		if(type == TypeName.INTEGER)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}
		else if(type == TypeName.BOOLEAN)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}
		else if(type == TypeName.FILE)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), type.getJVMTypeDesc());
		}
		else if(type == TypeName.URL)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(iterator++);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className,"getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), type.getJVMTypeDesc());
		}





/*		// paramDec.setSlot(slotNumber++);
		//TypeName type = TypeName.getTypeName(paramDec.getFirstToken());//fix

		TypeName type = paramDec.getTypeName();

		//get the typeName and visit
		cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), type.getJVMTypeDesc(), null, null);
		Label param1 = new Label();
		mv.visitLabel(param1);

		mv.visitVarInsn(ALOAD, 0);

		if(type == TypeName.INTEGER || type == TypeName.BOOLEAN){
			mv.visitVarInsn(ALOAD, 1); // arg string ref added on top of stack
			mv.visitLdcInsn(iterator++); // push index of arg to map
			mv.visitInsn(AALOAD); // index pop, array ref pop, pushes ref[index]

			if (paramDec.getTypeName() == TypeName.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",
						false);
				mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
			}
			else if (paramDec.getTypeName() == TypeName.BOOLEAN) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean",
						"(Ljava/lang/String;)Z", false);
				mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
			}
		}
		else{
			if(type == TypeName.FILE){
				mv.visitTypeInsn(NEW, "java/io/File");	//new file object
				mv.visitInsn(DUP);						//duplicate top of stack
				mv.visitVarInsn(ALOAD, 1);				//this
				mv.visitInsn(iterator++);			//push string args [param _ dec _ index]
				mv.visitInsn(AALOAD);					//top
				mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V",false);
				mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), type.getJVMTypeDesc());
			}

			else if(type == TypeName.URL){
				mv.visitVarInsn(ALOAD, 1);				//this
				mv.visitInsn(iterator++);				//push string args [param _ dec _index]
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL",PLPRuntimeImageIO.getURLSig, false);
				mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), type.getJVMTypeDesc());
			}
		}*/

		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		//added
		sleepStatement.getE().visit(this, arg);//why mv?
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC,"java/lang/Thread","sleep","(J)V",false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		//assert false : "not yet implemented";
		List<Expression> expressions = tuple.getExprList();
		for(Expression e: expressions){
			e.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label GUARD = new Label();
		Label BODY = new Label();

		mv.visitJumpInsn(GOTO,GUARD);

		mv.visitLabel(BODY);
		whileStatement.getB().visit(this, arg);

		mv.visitLabel(GUARD);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE,BODY);

		return null;
	}

}
