/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inequalizer.petriNet;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Furbach
 */
public class PetriNet {

    //we should only have one PN:
    private static PetriNet net=new PetriNet();
    //private static Marking m0, mF;
    public boolean expected;

    public static PetriNet getNet() {
        return net;
    }

    public static void setNet(PetriNet net) {
        PetriNet.net = net;
    }

    //nr of places(length of vectors)
    private List<Place> places;
    private List<PNTransition> transitions = new ArrayList<>();
    private boolean locked = false;

    /**
     * Creates a petri net with the given number of places P1...Psize
     *
     * @param size
     */
    public PetriNet(int size) {
        places = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            places.add(new Place("P" + i));
        }
    }

    /**
     * Constructs an empty PN.
     */
    public PetriNet() {
        places = new ArrayList<>();
    }

    /**
     * Adds a new place to the petri net.
     *
     * @param name
     */
    public void addPlace(String name) {
        places.add(new Place(name));
        if (locked) {
            System.err.println("Cannot add places after transitions to the PN! " + name);
        }
    }

    public void addTransition(PNTransition t) {
        transitions.add(t);
        locked = true;
    }

    public List<PNTransition> getTransitions() {
        return transitions;
    }

    public int getSize() {
        return places.size();
    }

    public Place getPlace(String name) throws Exception {
        for (Place place : places) {
            if (place.getName().equals(name)) {
                return place;
            }
        }
        throw new Exception("The place " + name + "does not exist.");
    }

    public int getPlaceNr(String name) {
        for (int i = 0; i < places.size(); i++) {
            if (places.get(i).getName().equals(name)) {
                return i;
            }
        }
        System.err.println("The place " + name + "does not exist.");
        return 0;
    }

    @Override
    public String toString() {
        String temp="Places: ";
        for (Place place : places) {
            temp=temp+place.getName()+" ";
        }
        temp=temp+"\n Transitions:\n";
        for (PNTransition transition : transitions) {
            temp=temp+transition.toString();
        }
        return temp;
    }
    

}
