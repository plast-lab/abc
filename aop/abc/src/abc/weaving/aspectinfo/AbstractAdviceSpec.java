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
import polyglot.util.InternalCompilerError;
import soot.*;

/** Skeleton implementation of the {@link abc.weaving.aspectinfo.AdviceSpec} interface.
 *  Useful when implementing advice specifications.
 */
public abstract class AbstractAdviceSpec extends Syntax implements AdviceSpec {

    public AbstractAdviceSpec(Position pos) {
	super(pos);
    }

    // FIXME: delegate this properly once we work out the precise rules
    public boolean isAfter() {
	if(this instanceof AfterAdvice 
	   || this instanceof AfterReturningAdvice 
	   || this instanceof AfterThrowingAdvice) return true;
	return false;
    }
}
