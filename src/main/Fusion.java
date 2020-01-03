package main;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;


import javax.swing.Timer;
import java.util.*;


public class Fusion {
    Ivy busIvy = new Ivy("monBus", "Premier message de monBusJava", null);

    private int xPos, yPos, xMouse, yMouse;
    private String colour = "";
    private boolean posGiven = false, colourGiven = false;
    List<String> listFormNom = new ArrayList<String>();

    /**
     * Constructeur de la classe fusion
     * @throws IvyException
     */
    public Fusion() throws IvyException {
        try {
            busIvy.start("127.255.255.255:2010");
        } catch (IvyException e) {
            e.printStackTrace();
        }
        actionListener();
        FigureNameListListener();
    }

    /**
     * Listener qui déclenche les actions à entreprendre selon le geste de l'utilisateur
     * @throws IvyException
     */
    public void actionListener() throws IvyException {
        busIvy.bindMsg("^OneDollar Reco=(.*)$", new IvyMessageListener() {
            @Override
            public void receive(IvyClient client, String[] args) {
                switch (args[0]) {
                    case "rectangle":
                        creerFigure("CreerRectangle");
                        break;
                    case "ellipse":
                        creerFigure("CreerEllipse");
                        break;
                    case "suppression":
                        //TODO
                        break;
                    case "deplacer":
                        //TODO
                        break;
                    default:
                        System.out.println("Pas de forme reconnue");
                        break;
                }
            }
        });
    }

    /**
     * Stock les coordonnées du curseur sur la palette
     */
    public void CoordBindListener(){
        try {
            busIvy.bindMsg("^Palette:MouseMoved x=(.*) y=(.*)$", new IvyMessageListener() {
                @Override
                public void receive(IvyClient client, String[] args) {
                    xMouse = Integer.parseInt(args[0]);
                    yMouse = Integer.parseInt(args[1]);
                }
            });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Maintient une liste des formes actuellement survolées par le curseur
      */
    public void FigureNameListListener(){
        try {
            busIvy.bindMsg("^Palette:MouseEntered nom=(.*)$", new IvyMessageListener() {
                @Override
                public void receive(IvyClient client, String[] args) {
                    listFormNom.add(args[0]);
                }
            });
        busIvy.bindMsg("^Palette:MouseExited nom=(.*)$", new IvyMessageListener() {
            @Override
            public void receive(IvyClient client, String[] args) {
                listFormNom.remove(args[0]);
            }
        });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Listener du click de la souris sur la palette
     */
    public void clickBindPalette(){
        try {
            busIvy.bindMsg("^Palette:MousePressed x=(.*) y=(.*)$", new IvyMessageListener() {
                @Override
                public void receive (IvyClient client, String[] args) {
                    try {
                        busIvy.sendMsg("Palette:TesterPoint x="+xMouse+" y="+yMouse);
                        busIvy.sendMsg("Palette:DemanderInfo nom=" + listFormNom.get(listFormNom.size()-1));
                    } catch (IvyException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envoie la requête tester point au coordonnées survolées par le curseur
     */
    public void testerPoint(){
        try {
            busIvy.sendMsg("Palette:TesterPoint x="+xMouse+" y="+yMouse);
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listener de la commande vocale de couleur
     */
    public void colourBindListener(){
        try {
            busIvy.bindAsyncMsg("^sra5 Text=(.*) Confidence=(.*)$", new IvyMessageListener() {
                @Override
                public void receive(IvyClient client, String[] args) {
                    String cmd = args[0];
                    switch (cmd){
                        case "rouge":
                            colour = "couleurContour=red";
                            colourGiven = true;
                            break;
                        case "bleu":
                            colour = "couleurContour=blue";
                            colourGiven = true;
                            break;
                        case "jaune":
                            colour = "couleurContour=yellow";
                            colourGiven = true;
                            break;
                        case "vert":
                            colour = "couleurContour=green";
                            colourGiven = true;
                            break;
                        case "rose":
                            colour = "couleurContour=pink";
                            colourGiven = true;
                            break;
                        case "orange":
                            colour = "couleurContour=orange";
                            colourGiven = true;
                            break;
                        case "noir":
                            colour = "couleurContour=black";
                            colourGiven = true;
                            break;
                        case "gris":
                            colour = "couleurContour=grey";
                            colourGiven = true;
                            break;
                        case "de cette couleur":
                            try {
                                testerPoint();
                                if(listFormNom.size() > 0){
                                    busIvy.sendMsg("Palette:DemanderInfo nom="+listFormNom.get(0)+"");
                                }else{
                                    busIvy.sendMsg("pas de forme ici");
                                }
                            } catch (IvyException e) {
                                e.printStackTrace();
                            }
                            colourGiven = true;
                            break;
                        default:
                            break;
                    }
                }
            });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listener des commandes vocales de définition de la position
     * @throws IvyException
     */
    public void positionBindListener() throws IvyException {
        busIvy.bindAsyncMsg("^sra5 Text=(.*) Confidence=(.*)$", new IvyMessageListener() {
            @Override
            public void receive(IvyClient client, String[] args) {
                CoordBindListener();
                String cmd = args[0];
                if (cmd.contains("ici") || cmd.contains("la") || cmd.contains("a cette position")) {
                    xPos = xMouse;
                    yPos = yMouse;
                    posGiven = true;
                }
            }
        });
    }

    /**
     * Fonction de création des nouvelles figures sur la palette
     * @param funName type de figure à créer
     */
    public void creerFigure(String funName) {
        Timer t = new Timer(3000, ae -> {
            String msg = "Palette:" + funName;
            if(posGiven){
                msg += " x=" + xPos + " y=" + yPos;
            }else{
                msg += " x=" + (int) (Math.random() * 400) + " y=" + (int) (Math.random() * 400);
            }
            if(colourGiven){
                msg += colour;
            }
            try {
                busIvy.sendMsg(msg += " couleurContour=red");
            } catch (IvyException e) {
                e.printStackTrace();
            }
            cleanVars();
        });
        t.setRepeats(false);
        t.start();
        try {
            colourBindListener();
            positionBindListener();
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Nettoyage des variables après la réalisation de l'action de création
     */
    public void cleanVars(){
        posGiven = false;
        colourGiven = false;
        colour = "";
        xPos = -1;
        yPos = -1;
    }
}





/*busIvy.bindMsg("^Palette:MousePressed x=(.*) y=(.*)$", new IvyMessageListener() {
@Override
public void receive (IvyClient client, String[] args) {
busIvy.send("Palette:TesterPoint x="+Integer.parseInt(args[0])+" y="+Integer.parseInt(args[1]));
xPos = Integer.parseInt(args[0]);
yPos = Integer.parseInt(args[1]);
System.out.println(xPos + " " + yPos);
}
});*/
