/*
 * Copyright (C) 2020 The Project Lombok Authors.
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

import java.util.Map;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCExpression;

import lombok.core.HandlerPriority;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacASTVisitor;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.javac.ResolutionResetNeeded;

@ProviderFor(JavacASTVisitor.class) @HandlerPriority(HANDLE_DELEGATE_PRIORITY + 101)
@ResolutionResetNeeded public class HandleComparable extends JavacASTAdapter {
	
	@Override public void visitStatement(JavacNode statementNode, JCTree statement) {
		if (statement instanceof JCBinary) {
			if (isCompare(((JCBinary) statement).type)) {
				try {
					JavacResolution resolver = new JavacResolution(statementNode.getContext());
					Map<JCTree, JCTree> resolveMethodMember = resolver.resolveMethodMember(statementNode);
					if (resolveMethodMember != null) {
						JCTree lhs = resolveMethodMember.get(((JCBinary) statement).lhs);
						JCTree rhs = resolveMethodMember.get(((JCBinary) statement).rhs);
						// Check if it's one of the auto-unboxing types.
						// Those will be handled by the compiler more
						// efficiently.
						System.out.println(lhs.type);
						System.out.println(rhs.type);
					}
				} catch (RuntimeException e) {
					System.err.println("Exception while resolving: " + statementNode + "(" + statementNode.getFileName() + ")");
					throw e;
				}
			}
		}
	}
	
	private boolean isCompare(Type type) {
		// TODO Auto-generated method stub
		return false;
	}
}
