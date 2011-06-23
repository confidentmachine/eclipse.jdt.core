/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ASTConverter17Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST.JLS4);
	}

	public ASTConverter17Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 15 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"test0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter17Test.class);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	/*
	 * Binary literals
	 */
	public void test0001() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public static final int VAR = 0b001;\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		final List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		final Expression initializer = fragment.getInitializer();
		assertEquals("Not a number literal", ASTNode.NUMBER_LITERAL, initializer.getNodeType());
		checkSourceRange(initializer, "0b001", contents);
		assertEquals("Wrong token", "0b001", ((NumberLiteral) initializer).getToken());
	}
	/*
	 * Binary literals with underscores
	 */
	public void test0002() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public static final int VAR = 0b0_0__1;\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		final List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		final Expression initializer = fragment.getInitializer();
		assertEquals("Not a number literal", ASTNode.NUMBER_LITERAL, initializer.getNodeType());
		checkSourceRange(initializer, "0b0_0__1", contents);
		assertEquals("Wrong token", "0b0_0__1", ((NumberLiteral) initializer).getToken());
	}

	/*
	 * Integer literals with underscores
	 */
	public void test0003() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public static final int VAR = 1_2_3_4;\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		final List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		final Expression initializer = fragment.getInitializer();
		assertEquals("Not a number literal", ASTNode.NUMBER_LITERAL, initializer.getNodeType());
		checkSourceRange(initializer, "1_2_3_4", contents);
		assertEquals("Wrong token", "1_2_3_4", ((NumberLiteral) initializer).getToken());
		IVariableBinding variableBinding = fragment.resolveBinding();
		Integer constantValue = (Integer) variableBinding.getConstantValue();
		assertEquals("Wrong value", 1234, constantValue.intValue());
	}
	/*
	 * Switch on strings
	 */
	public void test0004() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		switch(s) {\n" +
			"			case \"Hello\" :\n" +
			"				System.out.println(s);\n" +
			"				break;\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a switch statement", ASTNode.SWITCH_STATEMENT, node.getNodeType());
		SwitchStatement switchStatement = (SwitchStatement) node;
		Expression expression = switchStatement.getExpression();
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		assertEquals("Wrong type", "java.lang.String", typeBinding.getQualifiedName());
	}
	/*
	 * Union types (update for bug 340608)
	 */
	public void test0005() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException | ArithmeticException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not an union type", ASTNode.UNION_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException | ArithmeticException", contents);
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No binding", typeBinding);
	}
	/*
	 * Check that catch type with union type as a simple type is converted to a simple type
	 */
	public void test0006() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException", contents);
	}
	/*
	 * Check that catch type with union type as a simple type is converted to a simple type
	 */
	public void test0007() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try (Reader r = new FileReader(s)) {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException", contents);
		List resources = tryStatement.resources();
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s)", contents);
	}
	/*
	 * Check that catch type with union type as a simple type is converted to a simple type
	 */
	public void test0008() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try (Reader r = new FileReader(s);) {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException", contents);
		List resources = tryStatement.resources();
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s);", contents);
	}
	/*
	 * Check that catch type with union type as a simple type is converted to a simple type
	 */
	public void test0009() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try (Reader r = new FileReader(s);Reader r2 = new FileReader(s);) {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement TryStatement = (TryStatement) node;
		List catchClauses = TryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException", contents);
		List resources = TryStatement.resources();
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s);", contents);
		checkSourceRange((ASTNode) resources.get(1), "Reader r2 = new FileReader(s);", contents);
	}
	/*
	 * Check that catch type with union type as a simple type is converted to a simple type
	 */
	public void test0010() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try (Reader r = new FileReader(s);Reader r2 = new FileReader(s)) {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException", contents);
		List resources = tryStatement.resources();
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s);", contents);
		checkSourceRange((ASTNode) resources.get(1), "Reader r2 = new FileReader(s)", contents);
	}
	/*
	 * Union types (update for bug 340608)
	 */
	public void test0011() throws JavaModelException {
		String contents =
			"public class X {\n" + 
			"    public static void main(String[] args) {\n" + 
			"        try {\n" + 
			"            int option= 1;\n" + 
			"            throw option == 1 ? new ExceptionA() : new ExceptionB();\n" + 
			"        } catch (/*final*/ ExceptionA | ExceptionB ex) {\n" + 
			"            System.out.println(\"type of ex: \" + ex.getClass());\n" + 
			"            // next 2 methods on 'ex' use different parts of lub:\n" + 
			"            ex.myMethod();\n" + 
			"            throw ex;\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n" + 
			"interface Mix {\n" + 
			"    public void myMethod();\n" + 
			"}\n" + 
			"class ExceptionA extends RuntimeException implements Mix {\n" + 
			"    private static final long serialVersionUID = 1L;\n" + 
			"    public void myMethod() {\n" + 
			"        System.out.println(\"ExceptionA.myMethod()\");\n" + 
			"    }\n" + 
			"    public void onlyA() {\n" + 
			"        System.out.println(\"ExceptionA.onlyA()\");\n" + 
			"    }\n" + 
			"}\n" + 
			"class ExceptionB extends RuntimeException implements Mix {\n" + 
			"    private static final long serialVersionUID = 1L;\n" + 
			"    public void myMethod() {\n" + 
			"        System.out.println(\"ExceptionB.myMethod()\");\n" + 
			"    }\n" + 
			"    public void onlyB() {\n" + 
			"        System.out.println(\"ExceptionA.onlyB()\");\n" + 
			"    }\n" + 
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not an union type", ASTNode.UNION_TYPE, type.getNodeType());
		checkSourceRange(type, "ExceptionA | ExceptionB", contents);
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No binding", typeBinding);
		typeBinding = typeBinding.getGenericTypeOfWildcardType();
		assertNull("This should be null for intersection type", typeBinding);
	}
	/*
	 * Binary literals with underscores
	 */
	public void test0012() throws JavaModelException {
		AST localAst= AST.newAST(AST.JLS4);
		NumberLiteral literal= localAst.newNumberLiteral();
		try {
			literal.setToken("0b1010");
			literal.setToken("0xCAFE_BABE");
			literal.setToken("01_234");
			literal.setToken("1_234");
			literal.setToken("0b1_01_0");
		} catch(IllegalArgumentException e) {
			assertTrue("Should not happen", false);
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=344522
	 */
	public void test0013() throws JavaModelException {
		String contents =
				"import java.util.*;\n" +
				"public class X {\n" + 
				"	public static Object foo() {\n" + 
				"		List<String> l = new ArrayList<>();\n" +
				"		return l;\n" +
				"	}\n" + 
				"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) ((VariableDeclarationFragment) statement.fragments().get(0)).getInitializer();
		Type type = classInstanceCreation.getType();
		assertTrue("Should be Parameterized type", type.isParameterizedType());
		checkSourceRange(type, "ArrayList<>", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=349862
	 */
	public void test0014() throws JavaModelException {
		String contents =
				"public class X {\n" + 
				"	void foo() {\n" + 
				"		try (Object | Integer res= null) {\n" + 
				"		} catch (Exception e) {\n" + 
				"		}\n" + 
				"	}\n" + 
				"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, true, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertTrue("The method declaration is not malformed", isMalformed(node));
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=344522
	 */
	public void test0015() throws JavaModelException {
		String contents =
				"import java.lang.invoke.MethodHandle;\n" + 
				"import java.lang.invoke.MethodHandles;\n" + 
				"import java.lang.invoke.MethodType;\n" + 
				"\n" + 
				"public class X {\n" + 
				"	public static void main(String[] args) throws Throwable {\n" + 
				"		Object x;\n" + 
				"		String s;\n" + 
				"		int i;\n" + 
				"		MethodType mt;\n" + 
				"		MethodHandle mh;\n" + 
				"		MethodHandles.Lookup lookup = MethodHandles.lookup();\n" + 
				"		// mt is (char,char)String\n" + 
				"		mt = MethodType.methodType(String.class, char.class, char.class);\n" + 
				"		mh = lookup.findVirtual(String.class, \"replace\", mt);\n" + 
				"		s = (String) mh.invokeExact(\"daddy\", 'd', 'n');\n" + 
				"		// invokeExact(Ljava/lang/String;CC)Ljava/lang/String;\n" + 
				"		assert s.equals(\"nanny\");\n" + 
				"		// weakly typed invocation (using MHs.invoke)\n" + 
				"		s = (String) mh.invokeWithArguments(\"sappy\", 'p', 'v');\n" + 
				"		assert s.equals(\"nanny\");\n" + 
				"		// mt is (Object[])List\n" + 
				"		mt = MethodType.methodType(java.util.List.class, Object[].class);\n" + 
				"		mh = lookup.findStatic(java.util.Arrays.class, \"asList\", mt);\n" + 
				"		assert (mh.isVarargsCollector());\n" + 
				"		x = mh.invoke(\"one\", \"two\");\n" + 
				"		// invoke(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;\n" + 
				"		System.out.println(x);\n" + 
				"		// mt is (Object,Object,Object)Object\n" + 
				"		mt = MethodType.genericMethodType(3);\n" + 
				"		mh = mh.asType(mt);\n" + 
				"		x = mh.invokeExact((Object) 1, (Object) 2, (Object) 3);\n" + 
				"		// invokeExact(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" + 
				"		System.out.println(x);\n" + 
				"		// mt is ()int\n" + 
				"		mt = MethodType.methodType(int.class);\n" + 
				"		mh = lookup.findVirtual(java.util.List.class, \"size\", mt);\n" + 
				"		i = (int) mh.invokeExact(java.util.Arrays.asList(1, 2, 3));\n" + 
				"		// invokeExact(Ljava/util/List;)I\n" + 
				"		assert (i == 3);\n" + 
				"		mt = MethodType.methodType(void.class, String.class);\n" + 
				"		mh = lookup.findVirtual(java.io.PrintStream.class, \"println\", mt);\n" + 
				"		mh.invokeExact(System.out, \"Hello, world.\");\n" + 
				"		// invokeExact(Ljava/io/PrintStream;Ljava/lang/String;)V\n" + 
				"	}\n" + 
				"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		IProblem[] problems = unit.getProblems();
		for (int i = 0; i < problems.length; i++) {
			IProblem iProblem = problems[i];
			System.err.println(iProblem);
		}
		final List invokeExactMethods = new ArrayList();
		unit.accept(new ASTVisitor() {
			public boolean visit(MethodInvocation methodInvocation) {
				IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
				if (methodBinding != null) {
					IJavaElement javaElement = methodBinding.getJavaElement();
					assertNotNull("No java element for : " + methodBinding, javaElement);
					String elementName = javaElement.getElementName();
					if ("invokeExact".equals(elementName)) {
						invokeExactMethods.add(methodBinding);
					}
				}
				return true;
			}
		});
		assertEquals("Wrong size", 4, invokeExactMethods.size());
		IMethodBinding first = (IMethodBinding) invokeExactMethods.get(0);
		IMethodBinding second = (IMethodBinding) invokeExactMethods.get(1);
		assertEquals(first.getMethodDeclaration(), second.getMethodDeclaration());
		String firstKey = first.getKey();
		String secondKey = second.getKey();
		assertEquals("Wrong key", "Ljava/lang/invoke/MethodHandle;.invokeExact(Ljava/lang/String;CC)Ljava/lang/String;|Ljava/lang/Throwable;", firstKey);
		assertEquals("Wrong key", "Ljava/lang/invoke/MethodHandle;.invokeExact(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;|Ljava/lang/Throwable;", secondKey);
		assertFalse("Not the same key", firstKey.equals(secondKey));
		// check that all are resolved
		for (Iterator iterator = invokeExactMethods.iterator(); iterator.hasNext();) {
			IMethodBinding methodBinding = (IMethodBinding) iterator.next();
			assertTrue("Not resolved", ((IMethod) methodBinding.getJavaElement()).isResolved());
			assertTrue("Not a varargs method", methodBinding.getMethodDeclaration().isVarargs());
			assertFalse("Is a varargs method", methodBinding.isVarargs());
		}
	}
}