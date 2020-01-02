package main;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;

public class Main {
    private static boolean isClicked = false;
    private static int x, y;
    private static String formeFonc = "";
    private static String couleur = "";

    public static void main(String[] args) {
        Ivy busIvy = new Ivy("monBus", "Premier message de monBusJava", null);


        try {
            busIvy.start("127.255.255.255:2010");

            busIvy.bindMsg("^OneDollar Reco=(.*)$", new IvyMessageListener() {
                @Override
                public void receive (IvyClient client, String[] args) {
                        if(args[0].equals("rectangle")){
                            formeFonc = "CreerRectangle";
                        }else if(args[0].equals("ellipse")){
                            formeFonc ="CreerEllipse";
                        }else{
                            formeFonc+="PasReconnus";
                        }
                    //x=" + x + " y=" + y
                    try {
                        busIvy.sendMsg("Palette:"+formeFonc+" x="+x+" y="+y+ couleur);
                        x = 250;
                        y = 250;
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
