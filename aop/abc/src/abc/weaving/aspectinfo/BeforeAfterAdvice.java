package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;

import abc.weaving.matching.*;
import abc.weaving.residues.*;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;

/** Advice specification for advice that applies both before and after. */
public class BeforeAfterAdvice extends AbstractAdviceSpec {
    private BeforeAdvice before;
    private AfterAdvice after;

    public BeforeAfterAdvice(Position pos) {
	super(pos);
	before=new BeforeAdvice(pos);
	after=new AfterAdvice(pos);
    }

    public String toString() {
	return "beforeafter";
    }

    public Residue matchesAt(WeavingEnv we,ShadowMatch sm) {
	// BeforeAfterAdvice is just used for internal bookkeeping type stuff,
	// and always matches.
	return AlwaysMatch.v;
    }

    // For use with WeavingContext
    public static interface ChoosePhase {
	public void setBefore();
	public void setAfter();
    }

    public void weave(SootMethod method,LocalGeneratorEx localgen,AdviceApplication adviceappl) {
	WeavingContext wc=adviceappl.advice.makeWeavingContext();
	doWeave(method,localgen,adviceappl,adviceappl.getResidue(),wc);
    }

    void doWeave(SootMethod method,LocalGeneratorEx localgen,
	       AdviceApplication adviceappl,Residue residue,
	       WeavingContext wc) {

	Body b = method.getActiveBody();
        // this non patching chain is needed so that Soot doesn't "Fix" 
        // the traps. 
        Chain units = b.getUnits().getNonPatchingChain();

	ChoosePhase cp=(ChoosePhase) wc;

	Residue beforeResidue,afterResidue;
	if(residue instanceof AlwaysMatch) {
          // Laurie made me do it!
	  beforeResidue=AlwaysMatch.v; 
	  afterResidue=AlwaysMatch.v;
        } else {
	  Local adviceApplied=localgen.generateLocal(BooleanType.v(),"adviceApplied");
	  beforeResidue
	      =AndResidue.construct
	       (new SetResidue(adviceApplied,IntConstant.v(0)),
	        AndResidue.construct(residue,new SetResidue(adviceApplied,IntConstant.v(1))));
	  afterResidue=new TestResidue(adviceApplied,IntConstant.v(1));
	}

	// Weave the after advice first to ensure that the exception range doesn't cover
	// the before advice. Otherwise the signalling variable adviceApplied is not
	// guaranteed to be initialised.
	cp.setAfter();
	after.doWeave(method,localgen,adviceappl,afterResidue,wc);

	cp.setBefore();
	before.doWeave(method,localgen,adviceappl,beforeResidue,wc);

    } // method doWeave 

}

