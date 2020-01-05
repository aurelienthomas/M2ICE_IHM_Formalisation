package main;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyException;


import javax.swing.Timer;
import java.util.*;


class Fusion {
    private Ivy busIvy = new Ivy("monBus", "Premier message de monBusJava", null);

    private int xPos, yPos, xMouse, yMouse;
    private int idRecognizeColourDelete, idVocRegBind, idListenerObjectToDelete;
    private String colour = "", nomFigToMove ="";
    private boolean posGiven = false, colourGiven = false, hasClicked = false;
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
                    sendMessageToBus("SUPPRESION");
                    deleteFig();
                    break;
                case "deplacer":
                    sendMessageToBus("DEPLACEMENT");
                    moveFigure();
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
    private void getClickCoord() {
        try {
            busIvy.bindMsgOnce("^Palette:MouseClicked x=(.*) y=(.*)$", (client, args) -> {
                xMouse = Integer.parseInt(args[0]);
                yMouse = Integer.parseInt(args[1]);
                sendMessageToBus("Click: (" +xMouse+","+yMouse+")");
                xPos = xMouse;
                yPos = yMouse;
                hasClicked = true;
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
            busIvy.bindMsgOnce("^Palette:Info nom=(.*) x=(.*) y=(.*) longueur=(.*) hauteur=(.*) couleurFond=(.*) couleurContour=(.*)$", (client, args) -> {
                System.out.println(args[0] + "," +args[1] + "," +args[2] + "," +args[3] + "," +args[4] + "," +args[5] + "," + args[6]);
                figureObserved = new Figure(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5], args[6]);
                sendMessageToBus("figInfoBindListener: nom= " + figureObserved.getNom());
            });
            sendMessageToBus("Palette:DemanderInfo nom=" + nom);
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
     * Fonction de création des nouvelles figures sur la palette
     *
     * @param funName type de figure à créer
     */
    private void creerFigure(String funName) {
        cmdVocListenerForCreateFigure();
        getClickCoord();
        Timer t = new Timer(5000, ae -> {
            String msg = "Palette:" + funName;
            if (posGiven && hasClicked) {
                msg += " x=" + xPos + " y=" + yPos;
            } else {
                msg += " x=" + (int) (Math.random() * 400) + " y=" + (int) (Math.random() * 400);
            }
            if (colourGiven) {
                msg += " couleurFond=" + colour;
            }
                sendMessageToBus(msg);
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

    /**
     * Listener de la commande vocale de couleur et position
     */
    private void cmdVocListenerForCreateFigure() {
        try {
            idVocRegBind = busIvy.bindMsg("^sra5 Text=(.*) Confidence=(.*)$", (client, args) -> {
                String cmd = args[0].replace(".","").replace(" ", "");
                setColour(cmd);
                setPosition(cmd);
            });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    private void deleteFig() {
        try {
            recognizeColourDelete();
            idListenerObjectToDelete = busIvy.bindMsg("^sra5 Text=(.*) Confidence=(.*)$", (client, args) -> {
                String cmd = args[0].replace(".","").replace(" ", "");
                ArrayList<String> listFormNomToDelete = (ArrayList<String>) listFormNom.clone();
                switch (cmd) {
                    case "cetobjet":
                        for (String name : new ArrayList<String>(listFormNomToDelete)) {
                            getFigureInfo(name);
                            waitForBusMessage("^figInfoBindListener:(.*)$");
                            if (!(figureObserved.getCouleurContour().equals(colour) || colour.equals("")))
                            {
                                listFormNomToDelete.remove(name);
                            }

                        }
                        break;
                    case "cerectangle":
                        for (String name : new ArrayList<String>(listFormNomToDelete)) {
                            getFigureInfo(name);
                            waitForBusMessage("^figInfoBindListener:(.*)$");
                            if (!(figureObserved.getNom().contains("R") && (figureObserved.getCouleurContour().equals(colour) || colour.equals(""))))
                            {
                                listFormNomToDelete.remove(name);
                            }

                        }
                        System.out.println(listFormNomToDelete);
                        break;
                    case "cetteellipse":
                        for (String name : new ArrayList<String>(listFormNomToDelete)) {
                            getFigureInfo(name);
                            waitForBusMessage("^figInfoBindListener:(.*)$");
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
                try {
                    busIvy.unBindMsg(idListenerObjectToDelete);
                    busIvy.unBindMsg(idRecognizeColourDelete);
                } catch (IvyException e) {
                    e.printStackTrace();
                }
            });
            // On attend que le bus envoie un message de suppresion pour unbind les listener

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
            case "decettecouleur":
                if (listFormNom.size() > 0) {
                    String nom = listFormNom.get(listFormNom.size()-1);
                    getFigureInfo(nom);
                    waitForBusMessage("^figInfoBindListener:(.*)$");
                    colour = figureObserved.getCouleurFond();
                    System.out.println(figureObserved.getNom());
                    System.out.println(figureObserved.getCouleurFond());
                    colourGiven = true;
                } else {
                    sendMessageToBus("pas de forme ici");
                }
                break;
            default:
                break;
        }
        if(colourGiven) {
            sendMessageToBus("Colour: " + colour);
        }
    }

    private void setPosition(String text) {
        if (text.equals("ici") || text.equals("la") || text.equals("acetteposition")) {
            posGiven = true;
            sendMessageToBus("Position given: " + posGiven);
        }
    }

    private void recognizeColourDelete() {
        try {
            idRecognizeColourDelete = busIvy.bindMsg("^sra5 Text=(.*) Confidence=(.*)$", (client, args) -> {
                String cmd = args[0];
                setColour(cmd);
                if (colourGiven){
                sendMessageToBus("ColourOfObjectToDelete: colour = " + args[0]);
                }
            });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }





    public void moveFigure(){
        getVocalConfirmation();
        getClickCoord();
        designateObjectToMove();
        // C'est pas très propre mais ça marche
        waitForBusMessage("^Click:(.*)$");
        waitForBusMessage("^FigureToMove:(.*)$");
        if(hasClicked && posGiven && !nomFigToMove.equals("")){
            System.out.println("Palette:DeplacerObjetAbsolu nom="+ nomFigToMove +" x="+ xPos +" y="+ yPos);
            sendMessageToBus("Palette:DeplacerObjetAbsolu nom="+ nomFigToMove +" x="+ xPos +" y="+ yPos);
        }

        try {
            busIvy.unBindMsg(idVocRegBind);
            busIvy.unBindMsg(idListenerObjectToDelete);
        } catch (IvyException e) {
            e.printStackTrace();
        }
        cleanVars();
    }

    private void getVocalConfirmation() {
        try {
            idVocRegBind = busIvy.bindMsg("^sra5 Text=(.*) Confidence=(.*)$", (client, args) -> {
                String cmd = args[0].replace(".","").replace(" ", "");
                setPosition(cmd);
            });
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    private void designateObjectToMove() {
        try {
            idListenerObjectToDelete = busIvy.bindMsg("^sra5 Text=(.*) Confidence=(.*)$", (client, args) -> {
                String cmd = args[0].replace(".", "").replace(" ", "");
                String formSearched = "";
                ArrayList<String> listFormNomToMove = (ArrayList<String>) listFormNom.clone();
                int i = listFormNomToMove.size()-1;
                boolean figFound = false;
                switch (cmd) {
                    case "cetobjet":
                        formSearched = "";
                        break;
                    case "cerectangle":
                        formSearched = "R";
                        break;
                    case "cetteellipse":
                        formSearched = "E";
                        break;
                    default:
                        formSearched="";
                        break;
                }
                if(cmd.equals("cetobjet") || cmd.equals("cerectangle") || cmd.equals("cetteellipse")) {
                    while (!figFound && i >= 0) {
                        if (listFormNomToMove.get(i).contains(formSearched)) {
                            figFound = true;
                            nomFigToMove = listFormNomToMove.get(i);
                        }
                    }
                    sendMessageToBus("FigureToMove: " + nomFigToMove);
                }
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
            busIvy.waitForMsg(regex, 5000);
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
        hasClicked = false;
        figureObserved = null;
        nomFigToMove ="";
        colour = "";
        xPos = -1;
        yPos = -1;
    }
}
