package main;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyException;


import javax.swing.Timer;
import java.util.*;


class Fusion {
    private Ivy busIvy = new Ivy("monBus", "Premier message de monBusJava", null);

    private int xPos, yPos, xMouse, yMouse;
    private int idRecognizeColourDelete, idVocRegBind;
    private String colour = "";
    private boolean posGiven = false, colourGiven = false;
    private ArrayList<String> listFormNom = new ArrayList<>();
    private Figure figureObserved;

    /**
     * Constructeur de la classe fusion
     *
     * @throws IvyException IvyErrors
     */
    Fusion() throws IvyException {
        try {
            busIvy.start("127.255.255.255:2010");
        } catch (IvyException e) {
            e.printStackTrace();
        }
        actionListener();
        FigureNameListListener();
        clickBindPalette(); //Fonction de test
    }

    /**
     * Listener qui déclenche les actions à entreprendre selon le geste de l'utilisateur
     *
     * @throws IvyException IvyErrors
     */
    private void actionListener() throws IvyException {
        busIvy.bindMsg("^OneDollar Reco=(.*)$", (client, args) -> {
            switch (args[0]) {
                case "rectangle":
                    creerFigure("CreerRectangle");
                    break;
                case "ellipse":
                    creerFigure("CreerEllipse");
                    break;
                case "supprimer":
                    deleteFig();
                    sendMessageToBus("SUPPRESION");
                    break;
                case "deplacer":
                    //TODO DEPLACER
                    sendMessageToBus("DEPLACEMENT");
                    break;
                default:
                    System.out.println("Pas de forme reconnue");
                    break;
            }
        });
    }

    /**
     * Stock les coordonnées du curseur sur la palette
     */
    private void CoordBindListener() {
        try {
            busIvy.bindMsgOnce("^Palette:MouseMoved x=(.*) y=(.*)$", (client, args) -> {
                xMouse = Integer.parseInt(args[0]);
                yMouse = Integer.parseInt(args[1]);
            });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Maintient une liste des formes actuellement survolées par le curseur
     */
    private void FigureNameListListener() {
        try {
            busIvy.bindMsg("^Palette:MouseEntered nom=(.*)$", (client, args) -> listFormNom.add(args[0]));
            busIvy.bindMsg("^Palette:MouseExited nom=(.*)$", (client, args) -> listFormNom.remove(args[0]));
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    private void getFigureInfo(String nom) {
        try {
            sendMessageToBus("Palette:DemanderInfo nom=" + nom);
            busIvy.bindMsgOnce("^Palette:Info nom=(.*) x=(.*) y=(.*) longueur=(.*) hauteur=(.*) couleurFond=(.*) couleurContour=(.*)$", (client, args) -> {
                figureObserved = new Figure(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5], args[6]);
                sendMessageToBus("figInfoBindListener: nom= " + figureObserved.getNom());
            });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listener du click de la souris sur la palette
     * //////////////////////////////////////////////////////////////////////////
     * A SUPPRIMER
     * /////////////////////////////////////////////////////////////////////////
     */
    private void clickBindPalette() {
        try {
            busIvy.bindMsg("^Palette:MouseClicked x=(.*) y=(.*)$", (client, args) -> {
                System.out.println("Hello3");
            });
            System.out.println("hello");
            busIvy.waitForMsg("^Palette:MouseClicked x=(.*) y=(.*)$", 1000000);
            System.out.println("Hello2");
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envoie la requête tester point au coordonnées survolées par le curseur
     */
    private void testerPoint() {
        sendMessageToBus("Palette:TesterPoint x=" + xMouse + " y=" + yMouse);
    }

    /**
     * Listener de la commande vocale de couleur et position
     */
    private void cmdVocListenerForCreateFigure() {
        try {
            idVocRegBind = busIvy.bindMsg("^sra5 Text=(.*) Confidence=(.*)$", (client, args) -> {
                sendMessageToBus("ColourBindListener: text = " + args[0]);
                String cmd = args[0];
                setColour(cmd);
                setPosition(cmd);
            });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fonction de création des nouvelles figures sur la palette
     *
     * @param funName type de figure à créer
     */
    private void creerFigure(String funName) {
        cmdVocListenerForCreateFigure();
        Timer t = new Timer(3000, ae -> {
            String msg = "Palette:" + funName;
            if (posGiven) {
                msg += " x=" + xPos + " y=" + yPos;
            } else {
                msg += " x=" + (int) (Math.random() * 400) + " y=" + (int) (Math.random() * 400);
            }
            if (colourGiven) {
                msg += "couleurContour=" + colour;
            }
            sendMessageToBus(msg + " couleurContour=red");
            cleanVars();
            try {
                busIvy.unBindMsg(idVocRegBind);
            } catch (IvyException e) {
                e.printStackTrace();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void deleteFig() {
        try {
            recognizeColourDelete();
            int ListenerObjectToDelete = busIvy.bindMsg("^sra5 Text=(.*) Confidence=(.*)$", (client, args) -> {
                String cmd = args[0];
                waitForBusMessage("^ColourOfObjectToDelete:(.*)$");
                ArrayList<String> listFormNomToDelete = (ArrayList<String>) listFormNom.clone();
                switch (cmd) {
                    case "cet objet":
                        break;
                    case "ce rectangle":
                        for (String name : new ArrayList<String>(listFormNomToDelete)) {
                            getFigureInfo(name);
                            waitForBusMessage("^figInfoBindListener^:(.*)$");
                            if (!(figureObserved.getNom().contains("R") && (figureObserved.getCouleurContour().equals(colour) || colour.equals(""))))
                            {
                                listFormNomToDelete.remove(name);
                            }
                        }
                        break;
                    case "cette ellipse":
                        for (String name : new ArrayList<String>(listFormNomToDelete)) {
                            getFigureInfo(name);
                            waitForBusMessage("^figInfoBindListener^:(.*)$");
                            if (!(figureObserved.getNom().contains("E") && (figureObserved.getCouleurContour().equals(colour) || colour.equals(""))))
                            {
                                listFormNomToDelete.remove(name);
                            }
                        }
                        break;
                    default:
                        break;
                }
                // Suppression des éléments ayant la bonne couleur et la bonne forme
                for (String name:listFormNomToDelete) {
                    sendMessageToBus("Palette:SupprimerObjet nom=" + name);
                }
                cleanVars();

            });
            // On attend que le bus envoie un message de suppresion pour unbind les listener
            waitForBusMessage("^Palette:SupprimerObjet nom=(.*)$");
            busIvy.unBindMsg(ListenerObjectToDelete);
            busIvy.unBindMsg(idRecognizeColourDelete);
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    private void setColour(String text) {
        switch (text) {
            case "rouge":
                colour = "red";
                colourGiven = true;
                break;
            case "bleu":
                colour = "blue";
                colourGiven = true;
                break;
            case "jaune":
                colour = "yellow";
                colourGiven = true;
                break;
            case "vert":
                colour = "green";
                colourGiven = true;
                break;
            case "rose":
                colour = "pink";
                colourGiven = true;
                break;
            case "orange":
                colour = "orange";
                colourGiven = true;
                break;
            case "noir":
                colour = "black";
                colourGiven = true;
                break;
            case "gris":
                colour = "grey";
                colourGiven = true;
                break;
            case "de cette couleur":
                //TODO
                testerPoint();
                if (listFormNom.size() > 0) {
                    sendMessageToBus("Palette:DemanderInfo nom=" + listFormNom.get(0) + "");
                } else {
                    sendMessageToBus("pas de forme ici");
                }
                colourGiven = true;
                break;
            default:
                break;
        }
    }

    private void setPosition(String text) {
        sendMessageToBus("positionBindListener: text = " + text);
        CoordBindListener();
        if (text.contains("ici") || text.contains("la") || text.contains("a cette position")) {
            xPos = xMouse;
            yPos = yMouse;
            posGiven = true;
        }
    }

    private void recognizeColourDelete() {
        try {
            idRecognizeColourDelete = busIvy.bindMsg("^sra5 Text=(.*) Confidence=(.*)$", (client, args) -> {
                sendMessageToBus("ColourOfObjectToDelete: colour = " + args[0]);
                String cmd = args[0];
                setColour(cmd);
            });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToBus(String msg) {
        try {
            busIvy.sendMsg(msg);
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    private void waitForBusMessage(String regex) {
        try {
            busIvy.waitForMsg(regex, 10000);
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Nettoyage des variables après la réalisation de l'action de création
     */
    private void cleanVars() {
        posGiven = false;
        colourGiven = false;
        colour = "";
        xPos = -1;
        yPos = -1;
    }
}
