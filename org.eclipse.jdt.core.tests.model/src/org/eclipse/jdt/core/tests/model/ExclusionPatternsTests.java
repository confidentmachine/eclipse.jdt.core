/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import junit.framework.Test;

public class ExclusionPatternsTests extends ModifyingResourceTests {
	IJavaProject project;
public ExclusionPatternsTests(String name) {
	super(name);
}
/*
 * Returns a new classpath from the given source folders and their respective exclusion patterns.
 * The given array as the following form:
 * [<source folder>, "<pattern>[|<pattern]*"]*
 * E.g. new String[] {
 *   "src1", "p/A.java",
 *   "src2", "*.txt|com.tests/**"
 * } */
protected IClasspathEntry[] createClasspath(String[] sourceFoldersAndExclusionPatterns) {
	int length = sourceFoldersAndExclusionPatterns.length;
	IClasspathEntry[] classpath = new IClasspathEntry[length/2];
	for (int i = 0; i < length; i+=2) {
		String src = sourceFoldersAndExclusionPatterns[i];
		String patterns = sourceFoldersAndExclusionPatterns[i+1];
		StringTokenizer tokenizer = new StringTokenizer(patterns, "|");
		int patternsCount =  tokenizer.countTokens();
		IPath[] patternPaths = new IPath[patternsCount];
		for (int j = 0; j < patternsCount; j++) {
			patternPaths[j] = new Path(tokenizer.nextToken());
		}
		classpath[i/2] = JavaCore.newSourceEntry(new Path(src), patternPaths); 
	}
	return classpath;
}
/*
 * @see getClasspath for the format of sourceFoldersAndExclusionPatterns */
protected void setClasspath(String[] sourceFoldersAndExclusionPatterns) throws JavaModelException {
	this.project.setRawClasspath(createClasspath(sourceFoldersAndExclusionPatterns), null);
}
protected void setUp() throws Exception {
	super.setUp();
	this.project = this.createJavaProject("P", new String[] {"src"}, "bin");
	startDeltas();
}

public static Test suite() {
	return new Suite(ExclusionPatternsTests.class);
}

protected void tearDown() throws Exception {
	stopDeltas();
	this.deleteProject("P");
	super.tearDown();
}
/*
 * Ensure that creating an excluded compilation unit 
 * doesn't make it appear as a child of its package but it is a non-java resource. */
public void testExcludedCompilationUnit() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	IPackageFragment pkg = getPackage("/P/src/p");

	clearDeltas();
	pkg.createCompilationUnit(
		"A.java",
		"package p;\n" +		"public class A {\n" +		"}",
		false,
		null);
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CHILDREN}\n" + 
		"		p[*]: {CONTENT}\n" + 
		"			ResourceDelta(/P/src/p/A.java)[+]"
	);
	
	assertElementsEqual(
		"Unexpected children",
		"",
		pkg.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"A.java",
		pkg.getNonJavaResources());
}
/*
 * Ensure that creating a file that corresponds to an excluded compilation unit 
 * doesn't make it appear as a child of its package but it is a non-java resource.
 */
public void testCreateResourceExcludedCompilationUnit() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	
	clearDeltas();
	this.createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CHILDREN}\n" + 
		"		p[*]: {CONTENT}\n" + 
		"			ResourceDelta(/P/src/p/A.java)[+]"
	);
	
	IPackageFragment pkg = getPackage("/P/src/p");
	assertElementsEqual(
		"Unexpected children",
		"",
		pkg.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"A.java",
		pkg.getNonJavaResources());
}
/*
 * Ensure that renaming an excluded compilation unit so that it is not excluded any longer
 * makes it appears as a child of its package and it is removed from the non-java resources.
 */
public void testRenameExcludedCompilationUnit() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	IPackageFragment pkg = getPackage("/P/src/p");
	ICompilationUnit cu = pkg.createCompilationUnit(
		"A.java",
		"package p;\n" +
		"public class A {\n" +
		"}",
		false,
		null);

	clearDeltas();
	cu.rename("B.java", false, null);
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CHILDREN}\n" + 
		"		p[*]: {CHILDREN | CONTENT}\n" + 
		"			B.java[+]: {}\n" + 
		"			ResourceDelta(/P/src/p/A.java)[-]"
	);
	
	assertElementsEqual(
		"Unexpected children",
		"B.java",
		pkg.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"",
		pkg.getNonJavaResources());
}
/*
 * Ensure that renaming a file that corresponds to an excluded compilation unit so that it is not excluded any longer
 * makes it appears as a child of its package and it is removed from the non-java resources.
 */
public void testRenameResourceExcludedCompilationUnit() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	IFile file = this.createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	
	clearDeltas();
	file.move(new Path("/P/src/p/B.java"),  false, null);
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CHILDREN}\n" + 
		"		p[*]: {CHILDREN | CONTENT}\n" + 
		"			B.java[+]: {}\n" + 
		"			ResourceDelta(/P/src/p/A.java)[-]"
	);
	
	IPackageFragment pkg = getPackage("/P/src/p");
	assertElementsEqual(
		"Unexpected children",
		"B.java",
		pkg.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"",
		pkg.getNonJavaResources());
}
}
