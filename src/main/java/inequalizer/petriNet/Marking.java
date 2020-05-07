/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inequalizer.petriNet;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Florian Furbach
 */
public class Marking {
    private int[] value=new int[PetriNet.getNet().getSize()];

    public Marking() {
    }
        
    

    public Marking(List<Integer> val) {
        this(val.stream().mapToInt(i -> i).toArray());
    }
    public Marking(int[] value){
        this.value=value;
    }

    public int[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }
    
}
