package abc.weaving.matching;

import soot.*;
import soot.jimple.*;
import soot.util.*;

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.*;
import java.util.*;

/** The data structure the pointcut matcher computes */
/*  @author Ganesh Sittampalam                       */
/*  @date 23-Apr-04                                  */
public abstract class AdviceApplication {
    public AdviceDecl advice;
    public Residue residue;
    public ShadowPoints shadowpoints; // added by LJH to keep track of
                                      // where to weave.  Is initialized
                                      // in first pass of weaver. 

    public AdviceApplication(AdviceDecl advice,Residue residue) {
	this.advice=advice;
	this.residue=residue;
    }

    private static void doStatement(GlobalAspectInfo info,
				    MethodAdviceList mal,
				    SootClass cls,
				    SootMethod method,
				    MethodPosition pos) {
	Iterator adviceIt;
	for(adviceIt=info.getAdviceDecls().iterator();
	    adviceIt.hasNext();) {
	    final AdviceDecl ad = (AdviceDecl) adviceIt.next();
	    
	    Pointcut pc=ad.getPointcut();
	    
	    // remove the null check once everything is properly implemented
	    if(pc!=null) {
		
		Iterator shadowIt;
		for(shadowIt=ShadowPointcut.shadowTypesIterator();
		    shadowIt.hasNext();) {
		    
		    ShadowType st=(ShadowType) shadowIt.next();
		    
		    Residue residue=pc.matchesAt(st,cls,method,pos);

		    if(!NeverMatch.neverMatches(residue)) {
			st.addAdviceApplication(mal,ad,residue,pos);
		    }
		}
	    }
	}
    }

    public static Hashtable computeAdviceLists(GlobalAspectInfo info) {
	Iterator clsIt;

	Hashtable ret=new Hashtable();

	for(clsIt=info.getWeavableClasses().iterator();clsIt.hasNext();) {

	    final AbcClass cls 
		= (AbcClass) clsIt.next();

	    SootClass sootCls = cls.getSootClass();
	    Iterator methodIt;

	    for(methodIt=sootCls.methodIterator();methodIt.hasNext();) {

		final SootMethod method = (SootMethod) methodIt.next();

		// FIXME: Replace this call with one to the partial transformer;
		// Iterate through body to find "new", decide if we have a pointcut 
		// that might match it, and add the class to the list if so
		// Either that or pre-compute the list of all classes that our
		// pointcuts could match

		(new soot.jimple.toolkits.base.JimpleConstructorFolder())
		    .transform(method.getActiveBody(),"jtp.jcf",new HashMap());

		if(method.isAbstract()) continue;
		if(method.isNative()) continue;
		
		MethodAdviceList mal=new MethodAdviceList();

		// Do whole body shadows
		doStatement(info,mal,sootCls,method,new WholeMethodPosition());

		// Do statement shadows
		Chain stmtsChain=method.getActiveBody().getUnits();
		Stmt current,next;

		if(!stmtsChain.isEmpty()) { // I guess this is actually never going to be false
		    for(current=(Stmt) stmtsChain.getFirst();
			current!=null;
			current=next) {
			next=(Stmt) stmtsChain.getSuccOf(current);
			doStatement(info,mal,sootCls,method,new StmtMethodPosition(current));
			doStatement(info,mal,sootCls,method,new NewStmtMethodPosition(current,next));
		    }
		}

		// Do exception handler shadows
		Chain trapsChain=method.getActiveBody().getTraps();
		Trap currentTrap;

		if(!trapsChain.isEmpty()) {
		    for(currentTrap=(Trap) trapsChain.getFirst();
			currentTrap!=null;
			currentTrap=(Trap) trapsChain.getSuccOf(currentTrap))
			
			doStatement(info,mal,sootCls,method,new TrapMethodPosition(currentTrap));
		}

		ret.put(method,mal);

	    }
	}
	return ret;
    }
}
