/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.weaving.residues;

import polyglot.util.InternalCompilerError;
import soot.Value;
import soot.Local;
import soot.Type;
import soot.jimple.Stmt;
import soot.jimple.Jimple;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.AdviceWeavingContext;

/** A weaving variable that represents a formal 
 *  parameter to be passed to an advice body
 *  @author Ganesh Sittampalam
 */ 

public class AdviceFormal extends WeavingVar {
    public int pos;
    public Type type;
    private Local loc=null;

    public AdviceFormal(int pos,Type type) {
	this.pos=pos;
	this.type=type;
    }

    public String toString() {
	return "advicearg("+pos+":"+type+")";
    }

    public Stmt set(LocalGeneratorEx localgen,Chain units,Stmt begin,WeavingContext wc,Value val) {
	if(abc.main.Debug.v().showAdviceFormalSets)
	    System.out.println("Setting argument "+pos+" to "+val+" in "+wc);
	if(loc==null) loc = localgen.generateLocal(type,"adviceformal");
	Stmt assignStmt=Jimple.v().newAssignStmt(loc,val);
	units.insertAfter(assignStmt,begin);
	((AdviceWeavingContext) wc).arglist.setElementAt(loc,pos);
	return assignStmt;
    }

    public Local get() {
	if(loc==null) 
	    throw new InternalCompilerError
		("Someone tried to read from a variable bound "
		 +"to an advice formal before it was written: "+this);

	return loc;
    }

    public boolean hasType() {
	return true;
    }

    public Type getType() {
	return type;
    }

}
