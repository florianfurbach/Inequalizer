/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inequalizer;

import inequalizer.petriNet.Marking;
import inequalizer.petriNet.PetriNet;
import inequalizer.petriNet.PNTransition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florian Furbach
 */
public class Inequality {

    private static final Logger log = Logger.getLogger(Inequality.class.getName());
    private final int[] k;
    private int c;
    private final int n;
    //true if it is leq, false for geq
    private final boolean leq;
    private int mf;
    private int m0;
    //for algo:
    private int min;
    private int max;
    //private ArrayList<Integer> starts=new ArrayList<>();
    private int coveredArea = 0;
    private Set<PNTransition> transitions = new HashSet<PNTransition>();

    /**
     * Constructs an inequality kM >= 0
     *
     * @param k
     */
    public Inequality(int[] k) {
        this.c = 0;
        this.k = k;
        this.n = k.length;
        this.leq = false;
        mf = multiply(Inequalizer.mF);
        m0 = multiply(Inequalizer.m0);
    }

    /**
     * Constructs an inequality with the given parameters.
     *
     * @param k
     * @param c
     * @param leq
     */
    public Inequality(int[] k, int c, boolean leq) {

        this.c = c;
        this.k = k;
        this.n = k.length;
        this.leq = leq;
        mf = multiply(Inequalizer.mF);
        m0 = multiply(Inequalizer.m0);
    }

    /**
     *
     * @return index i of the first value k_i of k that is not zero.
     */
    public int getnon0idx() {
        for (int i = 0; i < n; i++) {
            if (k[i] != 0) {
                return i;
            }
        }
        System.err.println("Could not find a non-zero index of k.");
        return n;
    }

    /**
     * @return the maximal absolute value of k.
     */
    private int getMaxK() {
        int maxK = 0;
        for (int i = 0; i < k.length; i++) {
            maxK = Math.max(maxK, Math.abs(k[i]));
        }
        return maxK;
    }

    /**
     * @return the minimal absolute value of k that is not 0.
     */
    private int getMinK() {
        int minK = getMaxK();
        for (int i = 0; i < k.length; i++) {
            if (k[i] != 0) {
                minK = Math.min(minK, Math.abs(k[i]));
            }
        }
        return minK;
    }

    /**
     * @return k*t_delta
     */
    private int getTrans(PNTransition t) {
        int trans = 0;
        for (int i = 0; i < k.length; i++) {
            trans = trans + (k[i] * t.getT_delta()[i]);
        }
        return trans;
    }

    /**
     * @return k*x
     */
    private int multiply(Marking m) {
        int trans = 0;
        for (int i = 0; i < k.length; i++) {
            trans = trans + (k[i] * m.getValue()[i]);
        }
        return trans;
    }

    /**
     *
     * @param t
     * @return k*t_min
     */
    public int getInitial(PNTransition t) {
        int initial = 0;
        for (int i = 0; i < k.length; i++) {
            initial = initial + (k[i] * t.getT_min()[i]);
        }
        return initial;
    }


    /*
     * The inequality is trivially stable iff k*t_min is below the target area and k is negative
     * or k*t_min is above the target area and k is positive.
     */
    public boolean isTrivialStable(PNTransition t) {
        if(!(getInitial(t) < Math.min(c, c - getTrans(t) + 1) && isNeg()) || (getInitial(t) > Math.max(c, c - getTrans(t) - 1) && isPos())
)log.info(" k*" + t.getName() + "- =" + getInitial(t));
        return ((getInitial(t) < Math.min(c, c - getTrans(t) + 1) && isNeg()) || (getInitial(t) > Math.max(c, c - getTrans(t) - 1) && isPos()));
    }

    /*
     * The inequality is relevant if the target area is not empty.
     * Either it is of the form >= and t_delta is negativ or <= and it is positiv.
     */
    public boolean isRelevant(PNTransition t) {
        if ((this.getTrans(t) > 0 && leq) || (this.getTrans(t) < 0 && !leq)) {
            log.info("Relevant: k*" + t.getName() + "delta =" + getTrans(t));
            return true;
        } else {
            return false;
        }

    }

    /**
     * @return true if k contains no negative entries.
     */
    public boolean isPos() {
        for (int i = 0; i < k.length; i++) {
            if (k[i] < 0) {
                return false;
            }
        }
        return true;
    }

    /*
     * returns true if k contains no positive entries.
     */
    public boolean isNeg() {
        for (int i = 0; i < k.length; i++) {
            if (k[i] > 0) {
                return false;
            }
        }
        return true;
    }

    /*
     * returns true if k is mixed.
     */
    public boolean isMixed() {
        return (!isPos() && !isNeg());
    }

    /**
     * @return the k
     */
    public int[] getK() {
        return k;
    }

    /**
     * @return the c
     */
    public int getC() {
        return c;
    }

    /**
     * Sets the value c to a correct value if there are no relevant transition.
     */
    public void setCnotrel() {
        if(isPos())c=mf+1;
        else c=m0;
    }

 
    
    /**
     * Gives the form of the inequality (<= or >=)
     *
     * @return true iff (<=)
     */
    public boolean IsLessOrEqual() {
        return leq;
    }

    /**
     * Gives the form of the inequality (<= or >=)
     *
     * @return true iff (>=)
     */
    public boolean IsGreaterOrEqual() {
        return !leq;
    }

    @Override
    public String toString() {
        String temp="\nk: "+Arrays.toString(k)+"\n";
        temp=temp+"c:" + c; 
        return temp;
    }

    
    
    /**
     * Writes the inequality to the console.
     */
    public void print() {

        System.out.print("k:");
        System.out.println(Arrays.toString(k));
        System.out.println("c:" + c);
    }

    /*
     * The inequality is trivially stable iff k*t_min is below the target area and k is negative
     * or k*t_min is above the target area and k is positive. The target area is from k*m0 to k*mF
     */
    public boolean isAlwaysTrivial(PNTransition t) {
        if ((getInitial(t) < Math.min(multiply(Inequalizer.mF), multiply(Inequalizer.mF) - getTrans(t) + 1) && isNeg()) || (getInitial(t) >= Math.max(multiply(Inequalizer.m0), multiply(Inequalizer.m0) - getTrans(t) - 1) && isPos())) {
            log.info(" k*" + t.getName() + "- =" + getInitial(t) + " k*mf=" + multiply(Inequalizer.mF) + " k*m0=" + multiply(Inequalizer.m0));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Uses the algorithm to check whether there is a c s.th. the inequality is
     * stable and separates m0 and mf
     *
     * @return true iff the algorithm found a c
     * @throws java.lang.Exception iff k is mixed and relevant.
     */
    public boolean findC() throws Exception {
        boolean kpos = isPos();
        addNontrivialTransitions();
        for (PNTransition transition : transitions) {
            if (transition.getInterval().isEmpty()) {
                if (!updateInterval(transition)) {
                    return false;
                }
            }
        }
        if(transitions.isEmpty()){
            c=m0;
            log.info("Not relevant wrt. any transitions");
            return true;
        }
        PNTransition t = getmaxIntervalStart();
        while (getmaxIntervalStart().getInterval().a > getminIntervalEnd().getInterval().b) {
            if (kpos) {
                t = getminIntervalEnd();
            } else {
                t = getmaxIntervalStart();
            }
            if (!updateInterval(t)) {
                return false;
            }
            log.fine(t.getName()+": Found new Interval: " + t.getInterval().toString());
        }
        c = t.getInterval().a;
        return true;
    }

    /**
     *
     * @return the transition with the right most interval
     */
    private PNTransition getmaxIntervalStart() {
        if (transitions.isEmpty()) {
            System.err.println("Transition set is empty.");
        }
        PNTransition t = transitions.iterator().next();
        for (PNTransition transition : transitions) {
            if (transition.getInterval().a > t.getInterval().a) {
                t = transition;
            }
        }
        return t;
    }

    /**
     *
     * @return the transition of the left most interval
     */
    private PNTransition getminIntervalEnd() {
        if (transitions.isEmpty()) {
            System.err.println("Transition set is empty.");
        }
        PNTransition t = transitions.iterator().next();
        for (PNTransition transition : transitions) {
            if (transition.getInterval().b < t.getInterval().b) {
                t = transition;
            }
        }
        return t;
    }

    /**
     * Adds a transition to the algorithm and sets its initial interval to left (right for kneg) of the initial value.
     * @param t
     */
    private void addTransition(PNTransition t) {
        log.info("Found non-trivial transition " + t.getName() + " adding initial value " + getInitial(t));
        transitions.add(t);
        if (isPos()) {
            t.values = new TreeSet<>();
        } else {
            t.values = new TreeSet<>(Comparator.reverseOrder());
        }
        t.values.add(getInitial(t));
        //the first interval is set to the area left (or right for neg ks) of k*t^-.
        int minc = mf + 1;
        int maxc = m0;
        if (isPos()) {
            maxc = getInitial(t) + getTrans(t);
        } else {
            minc = getInitial(t) + 1;
        }
        t.setInterval(new Interval(minc, maxc));
    }

    /**
     * Finds the next suitable interval of c values.
     * @param t the transition for which we run the algorithm.
     * @return false if there are no more intervals to be found.
     */
    private boolean updateInterval(PNTransition t) {
        int kmin = getMinK();
        boolean kpos = isPos();
        int kx = t.visitNext();
        int last = kx;
        coveredArea = 0;

        while (((kpos && last < m0) || (!kpos && last > mf + 1)) && coveredArea < kmin) {

            // visit the nodes reachable from kx
            for (int i : k) {
                if (i != 0) {
                    t.mark(kx + i);
                }
            }
            
            Interval area = getcIntervalBetween(last, kx, t);
            if (!area.isEmpty()) {
                t.setInterval(area);
                return true;
            }
            
            last = kx;
            kx = t.visitNext();
        }

        return false;
    }

    /**
     * Checks whether there is enough space between the marked locations that allows for some c values.
     * @param lower the last marked value
     * @param upper the current marked value
     * @param t the transisiton for which the interval is set.
     * @return the interval of possible c values.
     */
    private Interval getcIntervalBetween(int lower, int upper, PNTransition t) {
        if (upper < lower) {
            int temp = lower;
            lower = upper;
            upper = temp;
        }

        int size = upper - lower;

        lower++;
        upper = upper + getTrans(t);

        if (lower > upper) {
            coveredArea = coveredArea + size;
        } else {
            coveredArea = 0;
        }

        lower = Math.max(lower, mf + 1);
        upper = Math.min(upper, m0);

        return new Interval(lower, upper);
    }

    /**
     * Adds all needed transitions to the algorithm.
     * @throws Exception 
     */
    private void addNontrivialTransitions() throws Exception {
        //precheck
        for (PNTransition t : PetriNet.getNet().getTransitions()) {
            if (isAlwaysTrivial(t)) {
                log.log(Level.INFO, "The inequality is trivial wrt " + t.getName());
            } else if (!isRelevant(t)) {
                log.log(Level.INFO, "The inequality is not relevant wrt " + t.getName());
            } else if (isMixed()) {
                throw new Exception("The inequality returned by the formula is mixed and relevant wrt " + t.getName());
            } else {
//                for (int i : k) {
//                    if(i!=0){
//                        if(Math.abs(i)<=Math.abs(getTrans(t)));
//                        
//                        throw new Exception(t.getName()+":k_i="+i+"; k*tdelta="+getTrans(t));
//                    }
//                }
                addTransition(t);
            }      //adding startvalues k*t- 
        }
    }
}
