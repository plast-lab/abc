/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Ganesh Sittampalam
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

package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;

import soot.SootMethod;
import soot.util.Chain;

import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.Weaver;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.LocalGeneratorEx;


/** A <code>declare warning</code> or <code>declare error</code> declaration.
 *  @author Aske Simon Christensen
 *  @author Ganesh Sittampalam
 */
public class DeclareMessage extends AbstractAdviceDecl {
    public static final int WARNING = 0;
    public static final int ERROR = 1;

    private final String[] sev_name = { "warning", "error" };
    private final int[] polyglot_sev = { ErrorInfo.WARNING, ErrorInfo.SEMANTIC_ERROR };

    private int severity;
    private String message;

    public DeclareMessage(int severity, Pointcut pc, String message, Aspect aspct, Position pos) {
        super(aspct,new MessageAdvice(),pc,new ArrayList(),pos);
        this.severity = severity;
        this.message = message;
    }



    /** Get the severity of the message.
     *  @return either {@link WARNING} or {@link ERROR}.
     */
    public int getSeverity() {
        return severity;
    }

    /** Get the name of the severity of the message.
     *  @return either <code>&qout;warning&quot;</code> or <code>&qout;error&quot;</code>.
     */
    public String getSeverityName() {
        return sev_name[severity];
    }

    /** Get the message to give if the pointcut matches anything. */
    public String getMessage() {
        return message;
    }

    public String toString() {
        return "declare "+sev_name[severity]+": "+pc+": \""+message+"\";";
    }

    public void debugInfo(String prefix,StringBuffer sb) {
        sb.append(prefix+" from aspect: "+getAspect().getName()+"\n");
        sb.append(prefix+" pointcut: "+pc+"\n");
        sb.append(prefix+" special: declare "+getSeverityName()+" : "+getMessage());
    }

    public WeavingEnv getWeavingEnv() {
        return new EmptyFormals();
    }

    public WeavingContext makeWeavingContext() {
        throw new InternalCompilerError
            ("declare warning/error cannot be woven");
    }

    public static class MessageAdvice implements AdviceSpec {
        public boolean isAfter() { return false; }
        public Residue matchesAt(WeavingEnv we,ShadowMatch sm,AbstractAdviceDecl ad) {
            return AlwaysMatch.v();
        }
        public void weave(SootMethod method,
                          LocalGeneratorEx localgen,
                          AdviceApplication adviceappl) {

        }
    }

    public Residue postResidue(ShadowMatch sm) {
        return AlwaysMatch.v();
    }

    public void generateMessage(ShadowMatch sm) {
        if(abc.main.Main.v()==null) throw new InternalCompilerError("main was null");
        if(abc.main.Main.v().error_queue==null) throw new InternalCompilerError("no error queue");
        abc.main.Main.v().error_queue.enqueue
            (ErrorInfoFactory.newErrorInfo
             (polyglot_sev[severity],
              message,
              sm.getContainer(),
              sm.getHost()));
    }

    public Chain makeAdviceExecutionStmts
        (AdviceApplication aa,LocalGeneratorEx localgen,WeavingContext wc) {
        throw new InternalCompilerError
            ("declare warning/error cannot be woven");
    }
    /** Report any errors or warnings for this advice application. */
    public void reportMessages(AdviceApplication adviceappl) {
        if(!NeverMatch.neverMatches(adviceappl.getResidue())) {
            ((DeclareMessage) adviceappl.advice).generateMessage(adviceappl.shadowmatch);
        }
    }
}
