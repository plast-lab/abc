/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Sascha Kuzins
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.weaving.weaver.around.soot;

import soot.*;
import soot.jimple.*;
import soot.baf.*;
import soot.jimple.*;
import soot.util.*;
import java.util.*;


import soot.tagkit.*;

public abstract class AbstractInterfaceInvokeExpr extends abc.weaving.weaver.around.soot.AbstractInstanceInvokeExpr 
                             implements InterfaceInvokeExpr, ConvertToBaf
{
    protected AbstractInterfaceInvokeExpr(ValueBox baseBox, SootMethodRef methodRef,
                                  ArrayList argBoxes)
    {
        if( methodRef.isStatic() ) throw new RuntimeException("wrong static-ness");
        this.baseBox = baseBox; this.methodRef = methodRef;
        this.argBoxes = argBoxes;
    }

    public boolean equivTo(Object o)
    {
        if (o instanceof AbstractInterfaceInvokeExpr)
        {
            AbstractInterfaceInvokeExpr ie = (AbstractInterfaceInvokeExpr)o;
            if (!(baseBox.getValue().equivTo(ie.baseBox.getValue()) &&
                    getMethod().equals(ie.getMethod()) && 
                    argBoxes.size() == ie.argBoxes.size()))
                return false;
            for (int i = 0; i < argBoxes.size(); i++)
                if (!(super.getArg(i).equivTo(ie.getArg(i))))
                    return false;
            return true;
        }
        return false;
    }

    /** Returns a hash code for this object, consistent with structural equality. */
    public int equivHashCode() 
    {
        return baseBox.getValue().equivHashCode() * 101 + getMethod().equivHashCode() * 17;
    }

    public abstract Object clone();

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(Jimple.INTERFACEINVOKE + " " + baseBox.getValue().toString() +
            "." + methodRef.getSignature() + "(");

        for(int i = 0; i < argBoxes.size(); i++)
        {
            if(i != 0)
                buffer.append(", ");

            buffer.append(getArg(i).toString());
        }

        buffer.append(")");

        return buffer.toString();
    }

    public void toString(UnitPrinter up)
    {
        up.literal(Jimple.INTERFACEINVOKE);
        up.literal(" ");
        baseBox.toString(up);
        up.literal(".");
        up.methodRef(methodRef);
        up.literal("(");
        
        for(int i = 0; i < argBoxes.size(); i++)
        {
            if(i != 0)
                up.literal(", ");
                
            getArgBox(i).toString(up);
        }

        up.literal(")");
    }

    
    public void apply(Switch sw)
    {
        ((ExprSwitch) sw).caseInterfaceInvokeExpr(this);
    }

    private static int sizeOfType(Type t)
    {
        if(t instanceof DoubleType || t instanceof LongType)
            return 2;
        else if(t instanceof VoidType)
            return 0;
        else
            return 1;
    }

    private static int argCountOf(SootMethodRef m)
    {
        int argCount = 0;
        Iterator typeIt = m.parameterTypes().iterator();

        while(typeIt.hasNext())
        {
            Type t = (Type) typeIt.next();

            argCount += sizeOfType(t);
        }

        return argCount;
    }

    public void convertToBaf(JimpleToBafContext context, List out)
    {
        ((ConvertToBaf)getBase()).convertToBaf(context, out);;

       for(int i = 0; i < argBoxes.size(); i++)
        {
            ((ConvertToBaf)(getArg(i))).convertToBaf(context, out);
        }
       
       Unit u;
       out.add(u = Baf.v().newInterfaceInvokeInst(methodRef, argCountOf(methodRef)));

	Unit currentUnit = context.getCurrentUnit();

	Iterator it = currentUnit.getTags().iterator();	
	while(it.hasNext()) {
	    u.addTag((Tag) it.next());
	}
	
    }
}
