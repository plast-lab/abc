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

package abc.weaving.aspectinfo;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.*;

/** A <code>percflowbelow</code> per clause. */
public class PerCflowBelow extends PerPointcut {
    public PerCflowBelow(Pointcut pc, Position pos) {
	super(pc, pos);
    }    

    public String toString() {
	return "percflowbelow("+getPointcut()+")";
    }

    public void registerSetupAdvice(Aspect aspct) {
	GlobalAspectInfo.v().addAdviceDecl
	    (new PerCflowSetup(aspct,getPointcut(),true,getPosition()));
    }

    public Residue matchesAt(Aspect aspct,ShadowMatch sm) {
	return new HasAspect(aspct.getInstanceClass().getSootClass(),null);
    }

    public Residue getAspectInstance(Aspect aspct,ShadowMatch sm) {
	return new AspectOf(aspct.getInstanceClass().getSootClass(),null);
    }
}
