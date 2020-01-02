package main;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static boolean isClicked = false;
    private static int xPos, yPos, xMouse, yMouse;
    private static String couleur = "";
    static Ivy busIvy = new Ivy("monBus", "Premier message de monBusJava", null);


    public static void main(String[] args) throws IvyException {
        try {
        busIvy.start("127.255.255.255:2010");
        } catch (IvyException e) {
            e.printStackTrace();
        }
        listenToGeste();
    }

    public static void listenToPalette() throws IvyException {
        /*busIvy.bindMsg("^Palette:MousePressed x=(.*) y=(.*)$", new IvyMessageListener() {
                @Override
                public void receive (IvyClient client, String[] args) {
                        xPos = Integer.parseInt(args[0]);
                        yPos = Integer.parseInt(args[1]);
                        System.out.println(xPos + " " + yPos);
                }
            });*/

        busIvy.bindMsg("^Palette:MouseMoved x=(.*) y=(.*)$", new IvyMessageListener() {
            @Override
            public void receive (IvyClient client, String[] args) {
                xMouse = Integer.parseInt(args[0]);
                yMouse = Integer.parseInt(args[1]);
            }
        });
    }

    public static void listenToGeste() throws IvyException {
        busIvy.bindMsg("^OneDollar Reco=(.*)$", new IvyMessageListener() {
            @Override
            public void receive (IvyClient client, String[] args) {
                if(args[0].equals("rectangle")){
                    creerNouvelleForme("CreerRectangle");
                }else if(args[0].equals("ellipse")){
                    creerNouvelleForme("CreerEllipse");
                }else if(args[0].equals("suppression")){
                    //TODO
                }else if(args[0].equals("deplacer")){
                    //TODO
                }else{
                    System.out.println("Pas de forme reconnue");
                }
            }
        });
    }

    public static void listenToCmd() throws IvyException {
        busIvy.bindAsyncMsg("^sra5 Text=(.*) Confidence=(.*)$", new IvyMessageListener() {
            @Override
            public void receive (IvyClient client, String[] args) {
                // Commande vocal reçue
                String cmd = args[0];
                // Définission de la couleur
                if (cmd.contains("rouge")) {
                    couleur = "couleur=rouge";
                } else if (cmd.contains("bleu")) {
                    couleur = "couleur=bleu";
                } else if (cmd.contains("jaune")) {
                    couleur = "couleur=jaune";
                } else if (cmd.contains("vert")) {
                    couleur = "couleur=vert";
                } else if (cmd.contains("rose")) {
                    couleur = "couleur=rose";
                } else if (cmd.contains("orange")) {
                    couleur = "couleur=orange";
                } else if (cmd.contains("noir")) {
                    couleur = "couleur=noir";
                } else if (cmd.contains("gris")) {
                    couleur = "couleur=gris";
                } else if (cmd.contains("de cette couleur")) {
                    try {
                        busIvy.sendMsg("Devinons le nom de l'objet ensemble yeah!");
                    } catch (IvyException e) {
                        e.printStackTrace();
                    }
                }

                if (cmd.contains("ici") || cmd.contains("la") || cmd.contains("a cette position")) {
                    xPos = xMouse;
                    yPos = yMouse;

                }
            }
        });
    }


    public static void creerNouvelleForme(String funName){
        try {
            // TIMER A METTRE ICI
            listenToCmd();
            listenToPalette();
            busIvy.sendMsg("Palette:"+funName+" x="+xPos+" y="+yPos+ couleur);
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }
}
