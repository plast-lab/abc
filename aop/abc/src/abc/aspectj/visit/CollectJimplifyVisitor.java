/* Abc - The AspectBench Compiler
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL; 
 * if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.aspectj.visit;

import polyglot.ast.*;
import polyglot.visit.*;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.frontend.Job;
import polyglot.frontend.Source;

import soot.*;
import soot.javaToJimple.*;

import java.util.*;

public class CollectJimplifyVisitor extends ErrorHandlingVisitor {
    private Collection/*<String>*/ source_files;
    private Map class_to_ast;

    private Node current_ast;

    public CollectJimplifyVisitor(Job job, TypeSystem ts, NodeFactory nf,
				  Collection/*<String>*/ source_files,
				  Map class_to_ast) {
	super(job, ts, nf);
	this.source_files = source_files;
	this.class_to_ast = class_to_ast;
    }

    protected NodeVisitor enterCall(Node n) throws SemanticException {
	if (n instanceof SourceFile) {
	    if (!source_files.contains(((SourceFile)n).source().name())) {
		throw new SemanticException("Source file was needed but not given on the commandline", n.position());
	    }
            current_ast = n;
	}
	if (n instanceof ClassDecl) {
	    String cname = ((ClassDecl)n).type().fullName();
	    //System.err.println("Collecting class "+cname);
            class_to_ast.put(cname, current_ast);
	    return bypassChildren(n);
        }
	return this;
    }

}
