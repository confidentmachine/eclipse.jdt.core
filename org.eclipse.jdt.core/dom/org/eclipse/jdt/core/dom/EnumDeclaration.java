/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.Iterator;
import java.util.List;

/**
 * Enum declaration AST node type.
 *
 * <pre>
 * EnumDeclaration:
 *      [ Javadoc ] { ExtendedModifier } <b>enum</b> Identifier
 *			[ <b>implements</b> Type { <b>,</b> Type } ]
 *			<b>{</b>
 *               [ EnumConstantDeclaration { <b>,</b> EnumConstantDeclaration } ]
 *               [ <b>;</b> { ClassBodyDeclaration | <b>;</b> } ]
 *          <b>}</b>
 * </pre>
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the first modifier or annotation (if present), or the
 * first character of the "enum" keyword (if no
 * modifiers or annotations). The source range extends through the last
 * character of the "}" token following the body declarations.
 * </p>
 * <p>
 * Note: Enum declarations are an experimental language feature 
 * under discussion in JSR-201 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * 
 * @since 3.0
 */
public class EnumDeclaration extends AbstractTypeDeclaration {
	
	/**
	 * The superinterface types (element type: <code>Type</code>). 
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList superInterfaceTypes =
		new ASTNode.NodeList(false, Type.class);

	/**
	 * Creates a new AST node for an enum declaration owned by the given 
	 * AST. By default, the enum declaration has an unspecified, but legal,
	 * name; no modifiers; no javadoc; no superinterfaces; 
	 * and an empty list of body declarations.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	EnumDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ENUM_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		EnumDeclaration result = new EnumDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.setModifiers(getModifiers());
		result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		result.setName((SimpleName) getName().clone(target));
		result.superInterfaceTypes().addAll(
			ASTNode.copySubtrees(target, superInterfaceTypes()));
		result.bodyDeclarations().addAll(
			ASTNode.copySubtrees(target, bodyDeclarations()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getJavadoc());
			acceptChildren(visitor, this.modifiers);
			acceptChild(visitor, getName());
			acceptChildren(visitor, this.superInterfaceTypes);
			acceptChildren(visitor, this.bodyDeclarations);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live ordered list of superinterfaces of this enum
	 * declaration.
	 * 
	 * @return the live list of super interface types
	 *    (element type: <code>Type</code>)
	 */ 
	public List superInterfaceTypes() {
		return this.superInterfaceTypes;
	}
	
	/**
	 * Returns the ordered list of enum constant declarations of this enum
	 * declaration.
	 * <p>
	 * This convenience method returns this node's enum constant declarations
	 * with non-enum constants filtered out. Unlike <code>bodyDeclarations</code>,
	 * this method does not return a live result.
	 * </p>
	 * 
	 * @return the (possibly empty) list of enum constant declarations
	 */ 
	public EnumConstantDeclaration[] getEnumConstants() {
		List bd = bodyDeclarations();
		int enumCount = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			if (it.next() instanceof EnumConstantDeclaration) {
				enumCount++;
			}
		}
		EnumConstantDeclaration[] enumConstants = new EnumConstantDeclaration[enumCount];
		int next = 0;
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof EnumConstantDeclaration) {
				enumConstants[next++] = (EnumConstantDeclaration) decl;
			}
		}
		return enumConstants;
	}

	/**
	 * Resolves and returns the binding for the enum declared in
	 * this enum declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public ITypeBinding resolveBinding() {
		return getAST().getBindingResolver().resolveType(this);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ this.modifiers.listSize()
			+ (this.typeName == null ? 0 : getName().treeSize())
			+ this.superInterfaceTypes.listSize()
			+ this.bodyDeclarations.listSize();
	}
}

