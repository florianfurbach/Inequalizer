/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inequalizer.petriNet;

import inequalizer.Interval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 * @author Florian Furbach
 */
public class PNTransition {
        public SortedSet<Integer> values=new TreeSet<>();
    private static final Logger log = inequalizer.Inequalizer.log;
    private Interval interval;
    static private int id = 0;
    final private String name;
    private int[] t_delta = new int[PetriNet.getNet().getSize()];
    private int[] t_min = new int[PetriNet.getNet().getSize()];
    private PetriNet net=PetriNet.getNet();
    private int n;

    
        public void mark(int value) {
        if (values.add(value)) {
            log.info("adding " + value + " to " + values.toString());
        }
    }

    public int visitNext() {
        int temp = values.first();
        log.info("visiting "+temp+" from "+values.toString());
        values.remove(temp);
        return temp;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }
    
    
    public PNTransition(int[] t_min, int[] t_delta)  {
        this(t_min, t_delta, "T" + id);
        id++;
    }
    public PNTransition(List<Integer> t_mintemp, List<Integer> t_deltatemp)  {
        this(t_mintemp.stream().mapToInt(i -> i).toArray(), t_deltatemp.stream().mapToInt(i -> i).toArray(), "T" + id);
        id++;
    }
    public PNTransition() {
        this.name = "T" + id;
        id++;
        this.n=t_min.length;
    }

    public void setT_delta(String place, int tdelta)  {
        this.t_delta[net.getPlaceNr(place)]=tdelta;
    }

    public void setT_min(String place, int tmin)  {
        this.t_min[net.getPlaceNr(place)]=tmin;
    }

    
    

    public PNTransition(Interval interval, String name, int n) {
        this.interval = interval;
        this.name = name;
        this.n = n;
    }
    
    
    public PNTransition(int[] t_min, int[] t_delta, String name)  {
        this.name = name;
        this.t_delta = t_delta;
        this.t_min = t_min;
        this.n = t_min.length;
        if (t_min.length != t_delta.length) {
            System.err.println("Not a transition: t_delta and t_min have different dimensions.");
        }
        for (int i = 0; i < n; i++) {
            if (t_min[i] < 0) {
                System.err.println("Not a transition: t_min contains a negative value.");
            }
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        String temp=name+" init: "+Arrays.toString(t_min)+"\n";
        temp=temp+"delta "+Arrays.toString(t_delta)+"\n";
        return temp;
    }
    
    

    /*
    Constructs from manual input
     */
//    Transition() {
//        name = "T" + id;
//        id++;
//        Scanner in = new Scanner(System.in);
//
//        System.out.println("--------------Dimension of the problem--------------");
//        n = in.nextInt();
//
//        t_min = new int[n];
//        t_delta = new int[n];
//
//        System.out.println("-------------- Values of t_min --------------");
//        for (int i = 0; i < n; i++) {
//            System.out.println("Component " + (i + 1) + " of t_min");
//            t_min[i] = in.nextInt();
//        }
//
//        System.out.println("-------------- Values of t_delta --------------");
//        for (int i = 0; i < n; i++) {
//            System.out.println("Component " + (i + 1) + " of t_delta");
//            t_delta[i] = in.nextInt();
//        }
//        //in.close();
//    }

    /**
     * @return the t_min
     */
    public int[] getT_min() {
        return t_min;
    }

    /**
     * @return the t_delta
     */
    public int[] getT_delta() {
        return t_delta;
    }

    /**
     * @return the n
     */
    public int getN() {
        return n;
    }

    public void print() {
        System.out.println("Dimension:" + n);
        int i;
        System.out.println("\nt_min:");
        for (i = 0; i < n; i++) {
            System.out.println(t_min[i]);
        }

        System.out.println("\nt_delta:");
        for (i = 0; i < n; i++) {
            System.out.println(t_delta[i]);
        }
    }
}
