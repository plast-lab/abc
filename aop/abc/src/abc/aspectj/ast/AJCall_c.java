
package abc.aspectj.ast;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import polyglot.util.Position;

import polyglot.ast.Call;
import polyglot.ast.Receiver;
import polyglot.ast.Node;
import polyglot.ast.Special;
import polyglot.ast.TypeNode;
import polyglot.ast.Expr;

import polyglot.visit.TypeChecker;

import polyglot.types.SemanticException;
import polyglot.types.MethodInstance;
import polyglot.types.ClassType;
import polyglot.types.ReferenceType;

import polyglot.ext.jl.ast.Call_c;

import abc.aspectj.ast.AspectJNodeFactory;
import abc.aspectj.ast.HostSpecial_c;
import abc.aspectj.ast.MakesAspectMethods;

import abc.aspectj.types.AspectJTypeSystem;
import abc.aspectj.types.AJContext;
import abc.aspectj.types.InterTypeMethodInstance_c;

import abc.aspectj.visit.AspectMethods;

/**
 * Override the typechecking of method calls, to delegate to the host in certain
 * cases when the call occurs from within an intertype declaration.
 * 
 * @author oege
 *
 */
public class AJCall_c extends Call_c implements Call, MakesAspectMethods {
	
	public AJCall_c(Position pos, Receiver target, String name,
				  List arguments) {
	  	super(pos,target,name,arguments);
	}

	/**
   	* Typecheck the Call when the target is null. This method finds
   	* an appropriate target, and then type checks accordingly.
   	* 
   	* @param argTypes list of <code>Type</code>s of the arguments
   	*/
  	protected Node typeCheckNullTarget(TypeChecker tc, List argTypes) throws SemanticException {
	  AspectJTypeSystem ts = (AspectJTypeSystem) tc.typeSystem();
	  AspectJNodeFactory nf = (AspectJNodeFactory) tc.nodeFactory();
	  AJContext c = (AJContext) tc.context();

	  // the target is null, and thus implicit
	  // let's find the target, using the context, and
	  // set the target appropriately, and then type check
	  // the result
	  MethodInstance mi =  c.findMethod(this.name, argTypes);
    
	  Receiver r;
	  
	  if (mi.flags().isStatic()) {
	  	r = nf.CanonicalTypeNode(position(), mi.container()).type(mi.container());
	  } else // test whether this a call to an instance method of an ITHost 
	  		if (ts.refHostOfITD(c,mi)) {
	  			AJContext ajc = (AJContext) c;
	  			ClassType scope = ajc.findMethodScopeInHost(name);
	  			if (! ts.equals(scope,ajc.hostClass())) {
	  				TypeNode tn = nf.CanonicalTypeNode(position(),scope).type(scope);
	  				r = nf.hostSpecial(position(),Special.THIS,tn,c.hostClass()).type(scope);
	  			} else {
	  				r = nf.hostSpecial(position(),Special.THIS,null,c.hostClass()).type(c.hostClass());
	  			}
	  		} else {
			  // The method is non-static, so we must prepend with "this", but we
			  // need to determine if the "this" should be qualified.  Get the
			  // enclosing class which brought the method into scope.  This is
			  // different from mi.container().  mi.container() returns a super type
			  // of the class we want.
			  ClassType scope = c.findMethodScope(name);
	
			  if (! ts.equals(scope, c.currentClass())) {
				  r = nf.This(position(),
							  nf.CanonicalTypeNode(position(), scope)).type(scope);
			  }
			  else {
				  r = nf.This(position()).type(scope);
			  }
	  }

	  // we call typeCheck on the receiver too.
	  r = (Receiver)r.typeCheck(tc);
	  return this.target(r).typeCheck(tc);
  }
  
  
  /** in intertype declarations with an interface host, one can
   *  make calls of the form "super.foo()" - these then have to
   *  be resolved in the super-interfaces of the host. It is an
   *  error when there is more than one candidate found in the
   *  set of interfaces.
   */
  public Node typeCheck(TypeChecker tc) throws SemanticException {
  	AJContext ajc = (AJContext) tc.context();
  	AspectJTypeSystem ts = (AspectJTypeSystem) tc.typeSystem();
  	if (ajc.inInterType() && 
  	    ajc.hostClass().flags().isInterface() &&
  	    target instanceof HostSpecial_c) {
  		HostSpecial_c hs = (HostSpecial_c) target;
  		if (hs.kind() == Special.SUPER) {
			List argTypes = new ArrayList(this.arguments.size());
			for (Iterator i = this.arguments.iterator(); i.hasNext(); ) {
				Expr e = (Expr) i.next();
				argTypes.add(e.type());
			}
			List candidates = new ArrayList(); // perhaps this should be a set
			ClassType itf = null;
  			for (Iterator itfs = ajc.hostClass().interfaces().iterator(); itfs.hasNext(); ) {
  				itf = (ClassType) itfs.next();
  				MethodInstance mi;
  				try { mi = ts.findMethod(itf,name(),argTypes,ajc.currentClass());
  				} catch (SemanticException e) { continue; }
  				candidates.add(mi);
  			}
  			if (candidates.size() > 1)
  				throw new SemanticException("Ambiguous use of super in intertype declaration",position());
  			if (candidates.size() == 0)
  				return super.typeCheck(tc);
  			MethodInstance mi = (MethodInstance) candidates.get(0);
			
			Call_c call = (Call_c)this.target(hs.type(itf)).methodInstance(mi).type(mi.returnType());
		
			System.out.println("target ="+call.target()+" of type "+call.target().type());
		    return call;
  		}
  	    }
  	return super.typeCheck(tc);
  }

        public void aspectMethodsEnter(AspectMethods visitor)
        {
                // do nothing       
        }

        public Node aspectMethodsLeave(AspectMethods visitor, AspectJNodeFactory nf,
                                       AspectJTypeSystem ts)
        {
                Call c = this;
                if (c.methodInstance() instanceof InterTypeMethodInstance_c) {
                        InterTypeMethodInstance_c itmi = (InterTypeMethodInstance_c) c.methodInstance();
                        c = c.methodInstance(itmi.mangled()).name(itmi.mangled().name());
                }
                if (c.target() instanceof HostSpecial_c) {
                        HostSpecial_c hs = (HostSpecial_c) c.target();
						IntertypeDecl id = visitor.intertypeDecl();
                        if (hs.kind() == Special.SUPER) {
                        	if (methodInstance() instanceof InterTypeMethodInstance_c) {
                        		InterTypeMethodInstance_c itmi = (InterTypeMethodInstance_c) methodInstance();
                        		List newArgs = new ArrayList(arguments());
                        		Expr thisref = id.thisReference(nf,ts);
                        		newArgs.add(0,thisref);
                        		MethodInstance impl = c.methodInstance();
                        		List formalTypes = new ArrayList(methodInstance().formalTypes());
                        		formalTypes.add(0,target.type());
                        		impl = impl.container(itmi.origin()).formalTypes(formalTypes).flags(itmi.flags().Static());
                        		TypeNode aspct = nf.CanonicalTypeNode(position,itmi.origin()).type(itmi.origin());                     
                        		c = (Call) c.target(aspct).methodInstance(impl).arguments(newArgs); 
                        	} else {
                                c = id.getSupers().superCall(nf, ts, c, id.host().type().toClass(),
                                                                id.thisReference(nf, ts));
                        	}
                        }
                }
                return c;
        }
}
