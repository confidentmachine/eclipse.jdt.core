/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
 * Enumeration constant declaration AST node type.
 *
 * <pre>
 * EnumConstantDeclaration:
 *      [ Javadoc ] { ExtendedModifier } Identifier
 *            [ <b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b> ]
 *			  [ <b>{</b> { ClassBodyDeclaration | <b>;</b> } <b>}</b> ]
 * </pre>
 * <p>
 * When a Javadoc comment is present, the source
 * range begins with the first character of the "/**" comment delimiter.
 * When there is no Javadoc comment, the source range begins with the first
 * character of the identifier. If there are class body declarations, the
 * source range extends through the last character of the last character of
 * the "}" token following the body declarations. If there are arguments but
 * no class body declarations, the source range extends through the last
 * character of the ")" token following the arguments. If there are no 
 * arguments and no class body declarations, the source range extends through
 * the last character of the identifier.
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
public class EnumConstantDeclaration extends BodyDeclaration {
	
	/**
	 * The constant name; lazily initialized; defaults to a unspecified,
	 * legal Java class identifier.
	 */
	private SimpleName constantName = null;

	/**
	 * The list of argument expressions (element type: 
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList arguments =
		new ASTNode.NodeList(true, Expression.class);
			
	/**
	 * The body declarations (element type: <code>BodyDeclaration</code>).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList bodyDeclarations = 
		new ASTNode.NodeList(true, BodyDeclaration.class);

	/**
	 * Creates a new AST node for an enumeration constants declaration owned by
	 * the given AST. By default, the enumeration constant has an unspecified,
	 * but legal, name; no javadoc; an empty list of modifiers and annotations;
	 * an empty list of arguments; and an empty list of body declarations.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	EnumConstantDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ENUM_CONSTANT_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		EnumConstantDeclaration result = new EnumConstantDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.setModifiers(getModifiers());
		result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		result.setName((SimpleName) getName().clone(target));
		result.arguments().addAll(ASTNode.copySubtrees(target, arguments()));
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
			acceptChildren(visitor, this.arguments);
			acceptChildren(visitor, this.bodyDeclarations);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the name of the constant declared in this enum declaration.
	 * 
	 * @return the constant name node
	 */ 
	public SimpleName getName() {
		if (this.constantName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return this.constantName;
	}
		
	/**
	 * Sets the name of the constant declared in this enum declaration to the
	 * given name.
	 * 
	 * @param constantName the new constant name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName constantName) {
		if (constantName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.constantName, constantName, false);
		this.constantName = constantName;
	}

	/**
	 * Returns the live ordered list of argument expressions in this enumeration
	 * constant declaration. Note that an empty list of arguments is equivalent
	 * to not explicitly specifying arguments.
	 * 
	 * @return the live list of argument expressions 
	 *    (element type: <code>Expression</code>)
	 */ 
	public List arguments() {
		return this.arguments;
	}

	/**
	 * Returns the live ordered list of body declarations of this enumeration
	 * constant declaration. Note that an empty list is equivalent to not
	 * explicitly specifying any body declarations.
	 * 
	 * @return the live list of body declarations
	 *    (element type: <code>BodyDeclaration</code>)
	 */ 
	public List bodyDeclarations() {
		return this.bodyDeclarations;
	}
	
	/**
	 * Resolves and returns the field binding for this enum constant.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public IVariableBinding resolveVariable() {
		return getAST().getBindingResolver().resolveVariable(this);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void appendDebugString(StringBuffer buffer) {
		buffer.append("EnumConstantDeclaration[");//$NON-NLS-1$
		buffer.append(getName().getIdentifier());
		buffer.append(" ");//$NON-NLS-1$
		if (!arguments().isEmpty()) {
			buffer.append("(");//$NON-NLS-1$
			for (Iterator it = arguments().iterator(); it.hasNext(); ) {
				Expression e = (Expression) it.next();
				e.appendDebugString(buffer);
				if (it.hasNext()) {
					buffer.append(",");//$NON-NLS-1$
				}
			}
			buffer.append(")");//$NON-NLS-1$
		}
		if (!bodyDeclarations().isEmpty()) {
			buffer.append(" {");//$NON-NLS-1$
			for (Iterator it = bodyDeclarations().iterator(); it.hasNext(); ) {
				BodyDeclaration d = (BodyDeclaration) it.next();
				d.appendDebugString(buffer);
				if (it.hasNext()) {
					buffer.append(";");//$NON-NLS-1$
				}
			}
			buffer.append("}");//$NON-NLS-1$
		}
		buffer.append("]");//$NON-NLS-1$
	}
		
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ this.modifiers.listSize()
			+ (this.constantName == null ? 0 : getName().treeSize())
			+ this.arguments.listSize()
			+ this.bodyDeclarations.listSize();
	}
}

