/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inequalizer;

import com.microsoft.z3.*;
import inequalizer.petriNet.*;

/**
 *
 * @author Florian Furbach
 */
public class Formula {

    final Context ctx = new Context();
    BoolExpr phi = ctx.mkTrue();
    BoolExpr phi2 = ctx.mkTrue();
    private final Solver s = ctx.mkSolver();
    final IntExpr[] k = new IntExpr[PetriNet.getNet().getSize()];
    final IntExpr[] m = new IntExpr[PetriNet.getNet().getSize()];
    final IntExpr c = ctx.mkIntConst("c");
    final IntExpr zero = ctx.mkInt(0);
    private final Optimize opt = ctx.mkOptimize();
    BoolExpr klesslimit = ctx.mkTrue();
    public static boolean isOpt = false;
    ArithExpr addtvals = zero;

    public Inequality quantifiedsolve() {
        for (int ki = 0; ki < k.length; ki++) {
            m[ki] = ctx.mkIntConst("m[" + ki + "]");
        }
        ArithExpr km = zero;
        BoolExpr act = ctx.mkTrue();
        for (int ki = 0; ki < k.length; ki++) {
            km = ctx.mkAdd(km, ctx.mkMul(k[ki], m[ki]));
        }
        BoolExpr phiq = ctx.mkTrue();
        BoolExpr mtmin = ctx.mkTrue();
        BoolExpr mtdel = ctx.mkTrue();
        for (PNTransition t : PetriNet.getNet().getTransitions()) {
            for (int ki = 0; ki < k.length; ki++) {
                act = ctx.mkAnd(act, ctx.mkGe(m[ki], ctx.mkInt(t.getT_min()[ki])));
            }
            mtdel = ctx.mkGe(ctx.mkAdd(km, multiplyk(t.getT_delta())), c);
            phiq = ctx.mkAnd(phiq, ctx.mkImplies(ctx.mkAnd(ctx.mkGe(km, c), act), mtdel));

        }

        phiq = ctx.mkForall(m, phiq, 0, null, null, null, null);
        phiq = ctx.mkAnd(phiq, ctx.mkGe(multiplyk(Inequalizer.m0.getValue()), c), ctx.mkLt(multiplyk(Inequalizer.mF.getValue()), c));
        Solver s2 = ctx.mkSolver();
        s2.add(phiq);
        if (s2.check() == Status.SATISFIABLE) {
            Model m = s2.getModel();
            int[] kvalue = new int[PetriNet.getNet().getSize()];
            for (int i = 0; i < kvalue.length; i++) {
                kvalue[i] = Integer.parseInt(((ArithExpr) m.eval(k[i], true)).toString());
            }
            int cval = Integer.parseInt(((ArithExpr) m.eval(c, true)).toString());
            return new Inequality(kvalue, cval, false);

        }
        return null;
    }

    public Formula(int limit, boolean cover) {
        for (int ki = 0; ki < k.length; ki++) {
            k[ki] = ctx.mkIntConst("k[" + ki + "]");
        }
        for (int ki = 0; ki < k.length; ki++) {
            //restrict results to small numbers:
            klesslimit = ctx.mkAnd(klesslimit, ctx.mkLt(k[ki], ctx.mkInt(limit)));
            klesslimit = ctx.mkAnd(klesslimit, ctx.mkGt(k[ki], ctx.mkInt(-limit)));
        }
        construct(cover);

    }

    public Formula(boolean cover) {
        for (int ki = 0; ki < k.length; ki++) {
            k[ki] = ctx.mkIntConst("k[" + ki + "]");
        }
        construct(cover);
    }

    public void construct(boolean cover) {
        //nonmixed            
        BoolExpr kgreatereq0 = ctx.mkTrue();
        BoolExpr klesseq0 = ctx.mkTrue();
        for (int ki = 0; ki < k.length; ki++) {
            kgreatereq0 = ctx.mkAnd(kgreatereq0, ctx.mkGe(k[ki], zero));
            klesseq0 = ctx.mkAnd(klesseq0, ctx.mkLe(k[ki], zero));
            //restrict results to small numbers:
        }
        BoolExpr nonmixed = ctx.mkOr(kgreatereq0, klesseq0);

        //phi=k*mf<k*m0
        phi = ctx.mkLt(multiplyk(Inequalizer.mF.getValue()), multiplyk(Inequalizer.m0.getValue()));
        phi2 = ctx.mkLt(multiplyk(Inequalizer.mF.getValue()), multiplyk(Inequalizer.m0.getValue()));
        phi = ctx.mkAnd(phi, klesslimit);
        //phi2 = ctx.mkAnd(phi2,klesslimit);
        if (cover) {
            phi = ctx.mkAnd(phi, klesseq0);
            phi2 = ctx.mkAnd(phi2, klesseq0);
        }
        //sum of weights for minimization.
        BoolExpr optcond = ctx.mkTrue();
        BoolExpr optcond2 = ctx.mkTrue();
        for (PNTransition t : PetriNet.getNet().getTransitions()) {
            //every transition gets a value 0 for trivial 1 for nontrivial in the end they are added and minimized.
            if (isOpt) {
                IntExpr tval = ctx.mkIntConst(t.getName());
                addtvals = ctx.mkAdd(addtvals, tval);
                optcond = ctx.mkEq(tval, ctx.mkInt(1));
                optcond2 = ctx.mkOr(ctx.mkEq(tval, zero), ctx.mkEq(tval, ctx.mkInt(1)));
            }
            //Generalized S-Invariant:
            BoolExpr sinv = ctx.mkGe(multiplyk(t.getT_delta()), zero);
            //nontrivial
            BoolExpr nontrivial = ctx.mkLt(multiplyk(t.getT_delta()), zero);

            //TODO: edit ki=0 or absolut value restriction for pos "lor" all 0 or neg value restr
            //absolut value of k can not be smaller than -ktdelta
            BoolExpr kgr0restr = ctx.mkTrue();
            BoolExpr kls0restr = ctx.mkTrue();
            for (int ki = 0; ki < k.length; ki++) {
                //ki>0-> |ki| > -ktdelta iff ki>0-> ki >-ktdelta
                kgr0restr = ctx.mkAnd(kgr0restr, ctx.mkImplies(ctx.mkGt(k[ki], zero), ctx.mkGt(k[ki], ctx.mkMul(ctx.mkInt(-1), multiplyk(t.getT_delta())))));
                //ki<0 -> |ki|> -ktelta iff ki<0-> -ki > -ktdelta iff ki<0-> ki < ktdelta
                kls0restr = ctx.mkAnd(kls0restr, ctx.mkImplies(ctx.mkLt(k[ki], zero), ctx.mkLt(k[ki], multiplyk(t.getT_delta()))));
            }
//            if (isOpt) {
//                kgr0restr = ctx.mkAnd(kgr0restr, optcond);
//                kls0restr = ctx.mkAnd(kls0restr, optcond);
//            }
            //only in the interesting case: k*t- is smaller than possible values of c and k pos and analog: (poss c values are k*m0 ... k*mf)
            kgr0restr = ctx.mkImplies(ctx.mkLe(multiplyk(t.getT_min()), multiplyk(Inequalizer.mF.getValue())), kgr0restr);
            //k*t- is bigger than possible values of c and k neg:
            kls0restr = ctx.mkImplies(ctx.mkGe(multiplyk(t.getT_min()), ctx.mkSub(multiplyk(Inequalizer.m0.getValue()), multiplyk(t.getT_delta()))), kls0restr);

            //t: gen s-inv or nontrivial -> nonmixed and kvalue restrictions
            phi = ctx.mkAnd(phi, optcond2, ctx.mkOr(sinv, ctx.mkAnd(optcond, nonmixed, kgr0restr, kls0restr)));
            BoolExpr nonrelgr = ctx.mkGt(multiplyk(t.getT_min()), ctx.mkSub(multiplyk(Inequalizer.mF.getValue()), multiplyk(t.getT_delta())));
            BoolExpr nonrelle = ctx.mkLt(multiplyk(t.getT_min()), multiplyk(Inequalizer.m0.getValue()));
            phi2 = ctx.mkAnd(phi2, ctx.mkOr(sinv, ctx.mkAnd(nonmixed, ctx.mkImplies(kgreatereq0, nonrelgr),
                    ctx.mkImplies(klesseq0, nonrelle)
            )));

            //phi = ctx.mkAnd(phi, klesslimit, ctx.mkOr(sinv, ctx.mkAnd(nontrivial, nonmixed)));
        };
        if (isOpt) {
            opt.Add(phi);
            opt.MkMinimize(addtvals);
        }
        s.add(phi);
        Inequalizer.log.info(phi2.toString());
    }

    @Override
    public String toString() {
        return phi.toString();
    }

    protected ArithExpr multiplyk(int[] x) {
        //TODO: use intexpr and cast to it?
        ArithExpr temp = ctx.mkInt(0);
        for (int ki = 0; ki < k.length; ki++) {
            temp = ctx.mkAdd(temp, ctx.mkMul(k[ki], ctx.mkInt(x[ki])));
        }
        return temp;
    }

    public Inequality simplesolve() {
        Solver s2 = ctx.mkSolver();
        s2.add(phi2);
        if (s2.check() == Status.SATISFIABLE) {
            Model m = s2.getModel();
            int[] kvalue = new int[PetriNet.getNet().getSize()];
            for (int i = 0; i < kvalue.length; i++) {
                kvalue[i] = Integer.parseInt(((ArithExpr) m.eval(k[i], true)).toString());
            }
            return new Inequality(kvalue);

        }
        return null;
    }

    public Inequality solve() {
        Model m = null;
        boolean sol = false;
        if (isOpt) {
            if (opt.Check() == Status.SATISFIABLE) {
                m = opt.getModel();
                sol = true;
            }
        } else {
            if (s.check() == Status.SATISFIABLE) {
                m = s.getModel();
                sol = true;
            }
        }
        if (sol) {
            int[] kvalue = new int[PetriNet.getNet().getSize()];
            for (int i = 0; i < kvalue.length; i++) {
                kvalue[i] = Integer.parseInt(((ArithExpr) m.eval(k[i], true)).toString());
            }
            if (isOpt) {
                //System.out.println("sumk=" + ((ArithExpr) m.eval(addtvals, true)).toString());
            }
            return new Inequality(kvalue);

        }
        return null;
    }

    void add(Inequality ineq) {
        BoolExpr equals = ctx.mkTrue();
        //k'_i!=0 : div= k_i/k'_i
        ArithExpr div = ctx.mkDiv(k[ineq.getnon0idx()], ctx.mkInt(ineq.getK()[ineq.getnon0idx()]));
        for (int i = 0; i < k.length; i++) {
            if (ineq.getK()[i] != 0) {
                equals = ctx.mkAnd(equals, ctx.mkEq(div, ctx.mkDiv(k[i], ctx.mkInt(ineq.getK()[i]))));
            } else {
                equals = ctx.mkAnd(equals, ctx.mkEq(k[i], zero));
            }
        }
        equals = ctx.mkNot(equals);
        //System.out.println("not a multiple of k: "+ineq.getK().toString());
        //System.out.println(equals.toString());
        s.add(equals);
        if (isOpt) {
            opt.Add(equals);
        }
    }
}
