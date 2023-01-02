/*
 * Copyright (C) 2023 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.javac.handlers;

import static lombok.javac.handlers.HandleDelegate.HANDLE_DELEGATE_PRIORITY;
import static lombok.javac.handlers.JavacHandlerUtil.deleteAnnotationIfNeccessary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.util.List;

import lombok.core.AST;
import lombok.core.HandlerPriority;
import lombok.experimental.EasyComparable;
import lombok.experimental.FieldDefaults;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.javac.JavacTreeMaker;
import lombok.javac.JavacTreeMaker.TypeTag;
import lombok.javac.ResolutionResetNeeded;
import lombok.spi.Provides;

/**
 * Handles the {@code @Compareble} annotation for javac.
 */
@HandlerPriority(1024) @Provides(JavacASTVisitor.class) @ResolutionResetNeeded public class HandleEasyComparable extends JavacASTAdapter {
	
	@Override public void visitStatement(JavacNode statementNode, JCTree statement) {
		if (statement instanceof JCBinary) {
			JCBinary binary = (JCBinary) statement;
			if (isCompare(binary.getKind())) {
				if (binary.lhs instanceof JCTree.JCLiteral || binary.rhs instanceof JCTree.JCLiteral) {
					return;
				}
				if (checkForAutoUnboxingTypes(statementNode, (JCBinary) statement)) {
					return;
				}
				// Convert comparisons to ".compareTo()".
				JavacNode parentNode = statementNode.directUp();
				JavacTreeMaker maker = parentNode.getTreeMaker();
				JCFieldAccess fieldAccess = maker.Select(binary.lhs, parentNode.toName("compareTo"));
				JCMethodInvocation compareToCall = maker.Apply(List.<JCExpression>nil(), fieldAccess, List.<JCExpression>of(binary.rhs));
				binary.lhs = compareToCall;
				binary.rhs = maker.Literal(TypeTag.typeTag("INT"), 0);
//				parentNode.rebuild();
			}
		}
		super.visitStatement(statementNode, statement);
	}
	
	private boolean checkForAutoUnboxingTypes(JavacNode statementNode, JCBinary statement) {
		JavacResolution resolver = new JavacResolution(statementNode.getContext());
		Map<JCTree, JCTree> resolveMethodMember = resolver.resolveMethodMember(statementNode);
		if (resolveMethodMember != null) {
			JCTree lhs = resolveMethodMember.get(statement.lhs);
			JCTree rhs = resolveMethodMember.get(statement.rhs);
			// Check if it's one of the auto-unboxing types.
			// Those will be handled by the compiler more
			// efficiently.
			return isAutoUnboxingType(lhs.type) || isAutoUnboxingType(rhs.type);
		}
		return false;
	}
	
	@Override public void endVisitType(JavacNode typeNode, JCClassDecl type) {
		JavacNode annotationNode = null;
		for (JavacNode javacNode : typeNode.down()) {
			if (javacNode.getKind() == AST.Kind.ANNOTATION) {
				annotationNode = javacNode;
				break;
			}
		}
		if (annotationNode != null) deleteAnnotationIfNeccessary(annotationNode, EasyComparable.class);
		super.endVisitType(typeNode, type);
	}
	
	private static final java.util.List<String> autoUnboxingTypes = Arrays.asList("java.lang.Boolean", "java.lang.Byte", "java.lang.Character", "java.lang.Float", "java.lang.Integer", "java.lang.Long", "java.lang.Short", "java.lang.Double");
	
	private boolean isAutoUnboxingType(Type type) {
		String typeName = type.toString();
		return autoUnboxingTypes.contains(typeName);
	}
	
	private boolean isCompare(Kind kind) {
		switch (kind) {
		case LESS_THAN:
		case GREATER_THAN:
		case LESS_THAN_EQUAL:
		case GREATER_THAN_EQUAL:
		case EQUAL_TO:
			return true;
		}
		return false;
	}
}
