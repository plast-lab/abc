package abc.eaj.extension;

import java.util.*;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.Position;

import abc.aspectj.ast.*;

import abc.eaj.visit.*;

/**
 * @author Julian Tibble
 */
public class EAJAdviceDecl_c extends AdviceDecl_c implements EAJAdviceDecl
{
    public EAJAdviceDecl_c(Position pos, Flags flags,
                           AdviceSpec spec, List throwTypes,
                           Pointcut pc, Block body)
    {
        super(pos, flags, spec, throwTypes, pc, body);
    }

    public EAJAdviceDecl conjoinPointcutWith(GlobalPointcuts visitor, Pointcut global)
    {
        EAJAdviceDecl_c n = (EAJAdviceDecl_c) this.copy();
        n.pc = visitor.conjoinPointcuts(pc, global);
        return n;
    }
}
