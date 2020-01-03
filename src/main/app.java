package main;

import fr.dgac.ivy.IvyException;

public class app {
    public static void main(String[] args){
        try {
            Fusion fusion = new Fusion();
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }
}
