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

import abc.weaving.residues.*;
import abc.weaving.matching.*;

/** Handler for <code>initialization</code> shadow pointcut. */
public class InterfaceInitialization extends ShadowPointcut {
    private ClassnamePattern pattern;

    public InterfaceInitialization(ClassnamePattern pattern,Position pos) {
	super(pos);
	this.pattern=pattern;
    }

    public ClassnamePattern getPattern() {
	return pattern;
    }


    protected Residue matchesAt(ShadowMatch sm) {
	if(!(sm instanceof InterfaceInitializationShadowMatch)) return null;
	InterfaceInitializationShadowMatch ism=(InterfaceInitializationShadowMatch) sm;
	if(!(getPattern().matchesClass(ism.getInterface()))) return null;
	return AlwaysMatch.v;
    }

    public String toString() {
	return "interfaceinitialization()";
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.Pointcut#equivalent(abc.weaving.aspectinfo.Pointcut, java.util.Hashtable)
	 */
	public boolean equivalent(Pointcut otherpc, Hashtable renaming) {
		if (otherpc instanceof InterfaceInitialization) {
			return pattern.equivalent(((InterfaceInitialization)otherpc).getPattern());
		} else return false;
	}

}
