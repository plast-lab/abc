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

import java.util.Hashtable;

import soot.*;

import polyglot.util.Position;

import abc.weaving.matching.*;
import abc.weaving.residues.*;

/** Handler for <code>handler</code> shadow pointcut. */
public class Handler extends ShadowPointcut {
    private ClassnamePattern pattern;

    public Handler(ClassnamePattern pattern,Position pos) {
	super(pos);
	this.pattern = pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }

    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof HandlerShadowMatch)) return null;
	SootClass exc=((HandlerShadowMatch) sm).getException();

	// FIXME: Hack should be removed when patterns are added
	if(getPattern()==null) return AlwaysMatch.v;

	if(!getPattern().matchesClass(exc)) return null;
	return AlwaysMatch.v;

    }

    public String toString() {
	return "handler("+pattern+")";
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof Handler) {
			return pattern.equivalent(((Handler)otherpc).getPattern());
		} else return false;
	}

}
