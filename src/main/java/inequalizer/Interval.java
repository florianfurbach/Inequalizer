/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inequalizer;

/**
 *
 * @author Florian Furbach
 */
public class Interval {
protected int a,b;

    public Interval(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "["+a+","+b+"]";
    }

    boolean isEmpty() {
        return (b<a);
    }

}
