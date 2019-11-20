package main;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;

public class Main {
    public static void main (String arg[]) {

        Ivy busIvy = new Ivy("monBus", "Premier message de monBusJava", null);

        try {
            busIvy.start("127.255.255.255:2010");
            busIvy.bindMsg("^Palette:(.*)$", new IvyMessageListener() {
                @Override
                public void receive (IvyClient client, String[] args) {

                    //1. Afficher tous les messages précédés du nom de l’agent qui l’a envoyé
                    System.out.println("Palette : " + args[0]);
                }
            });

            busIvy.bindMsg("^Palette:MouseClicked x=(.*) y=(.*)$", new IvyMessageListener() {
                @Override
                public void receive (IvyClient client, String[] args) {
                    try {
                        //2. Envoyer  un  message sur  le  bus  IVY  de  manière  à  faire  afficher  un  rectangle  sur  la palette
                        busIvy.sendMsg("Palette:CreerRectangle x=" + args[0] + " y=" + args[1]);
                    } catch (IvyException e) {
                        e.printStackTrace();
                    }
                }
            });

            busIvy.bindMsg("^Palette:MouseMoved x=(.*) y=(.*)$", new IvyMessageListener() {
                @Override
                public void receive (IvyClient client, String[] args) {
                    try {
                        //3. Afficher seulement les messages de déplacement du pointeur de la souris sur la palette.Le  message  affiché  devra  être  de  la  forme:  «Le  pointeur  de  la  souris  se  trouve  à  la position (coordX, coordY)» avec coordX et coordYles coordonnées transmises dans le message.
                        busIvy.sendMsg("Le  pointeur  de  la  souris  se  trouve  à  la position (" + args[0] + "," + args[1] + ")");
                    } catch (IvyException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (IvyException e) {
            e.printStackTrace();
        }
    }
}
