package qtttool;

import com.nxp.id.jc.CapFile;
import com.nxp.id.jc.CardManager;
import com.nxp.id.jc.JCApplet;
import com.nxp.id.jc.JCException;
import com.nxp.id.jc.JCInfo;
import com.nxp.id.jc.JCard;
import com.nxp.id.jc.LoadFile;
import com.nxp.id.jc.OPApplet;
import com.nxp.id.jc.OPKey;
import com.nxp.id.jc.terminal.RemoteJCTerminal;
import com.nxp.id.jsbl.apdu.APDU;
import java.io.PrintStream;

public class JcopCommunicator {

    private static QTToolGUI gui;
    private static CustomOutputStream customOutput;
    RemoteJCTerminal remoteJCTerminal;
    JCard card;
    CardManager cardManager;
    int useOfTextArea = 1;
    private static JcopCommunicator jcop;

    private JcopCommunicator() {
        // TODO Auto-generated constructor stub
    }

    public static JcopCommunicator getInstance() {
        if (jcop == null) {
            jcop = new JcopCommunicator();
        }
        return jcop;
    }

    public void connecToJcop(String ipAddress) {
        setUseOfTextArea(1);
        System.out.println("Conneting to Jcop...");
        printLog("Conneting to Jcop...");
        remoteJCTerminal = new RemoteJCTerminal();
        printLog(String.format("connecting to: "+ipAddress+":%d", 8050));
        remoteJCTerminal.init(String.format(ipAddress+":%d", 8050));
        try {
            remoteJCTerminal.open();

            System.out.println("Connection opened");
            printLog("Please Wait...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                printLog("exception");
            }
            byte[] atrbytes = remoteJCTerminal.waitForCard(0);
        } catch (JCException e) {
            System.out.format("caught JCE %s code %d (%s)\n",
                    e.getMessage(),
                    e.errorCode,
                    e.toString());
            e.printStackTrace();
        }

    }

    public void getJCard() {
        System.out.println("Getting Card Instance...");
        printLog("Getting Card Instance...");
        card = new JCard(remoteJCTerminal, null, 0);
        System.out.println("Card Instance Received...");
        printLog("Card Instance Received...");
        System.out.println("Jcop Connection Done.");
        printLog("Jcop Connection Done.");

    }

    public String selectApplet(byte[] aid) {
        //byte[] aid = {(byte)0xA0,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x52,(byte)0x30,(byte)0x10};
        JCApplet applet = new JCApplet(card, aid);
         printLog("Selecting Applet: "+byteArrayToHex(aid));
        byte[] answ = applet.select();//Overall, thanks everyone here gave me help.
        printLog( "OUT: "+byteArrayToHex(answ));
        return byteArrayToHex(answ);
    }

    public void initUpdateExtAuthForApplet() {
        System.out.println("init Update ...");
        cardManager.initializeUpdate(0, 0, OPApplet.SCP_UNDEFINED);
        System.out.println("Authenticate to card manager ...");
        cardManager.externalAuthenticate(OPApplet.APDU_MAC);
    }

    public void sendPersoCommand(byte[] apdu) {
        byte[] response = cardManager.send(apdu, 0, apdu.length);
        System.out.println(byteArrayToHex(response));
    }

    public void selectCardManager() {
        byte[] caid = {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x51, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        cardManager = new CardManager(card, caid);
        byte[] response = cardManager.select();
        System.out.println(byteArrayToHex(response));
        cardManager.initializeUpdate(0, 0);
        System.out.println("Authenticate to card manager ...");
        cardManager.externalAuthenticate(OPApplet.APDU_CLR);
    }

    //private loader(){}
    public void load() throws Exception {
        String capFileName = "E:\\JCOP\\eclipse\\workspace\\discover-dpas_non_hci\\discover-dpas\\bin\\com\\girmiti\\dpas\\javacard\\dpas.cap";
        CapFile capFile = null;
        //try{
        capFile = new CapFile(capFileName, null);
        //}catch(Exception e){
        //     System.out.println("Cap file in trouble...");
        //     System.exit(0);          
        //}

        //capFile.readCapFile(capFileName,null);
        //show information about the CAP file
        System.out.println(capFile.infoString());

        System.out.println("\nPackage name: " + capFile.pkg);
        byte[][] applets = capFile.aids;
        if ((applets == null) || (applets.length == 0)) {
            throw new RuntimeException("no applets in cap file");
        }
        // Get connection to terminal, take note that jcop.exe is required to be activated
        // in simulation mode.
        System.out.println("Open terminal ...");

        // Get the off-card representative for the card manager and use it to
        // select the on-card CardManager
        System.out.println("Select card manager ...");
        byte[] caid = {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x51, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        cardManager = new CardManager(card, caid);
        cardManager.select();
        // For downloading something, we have to be authenticated to card manager.
        // For this, the keys must be set. The keys to use should of course
        // be configurable as well.
        byte[] dfltKey = c2b("404142434445464748494a4b4c4d4e4f");
        cardManager.setKey(new OPKey(255, 1, OPKey.DES_ECB, dfltKey));
        cardManager.setKey(new OPKey(255, 2, OPKey.DES_ECB, dfltKey));
        cardManager.setKey(new OPKey(255, 3, OPKey.DES_ECB, dfltKey));
        //cardManager.setKey(new OPKey(1, 1, OPKey.DES_ECB, c2b("707172737475767778797a7b7c7d7e7f")));
        //cardManager.setKey(new OPKey(1, 2, OPKey.DES_ECB, c2b("606162636465666768696a6b6c6d6e6f")));
        //cardManager.setKey(new OPKey(1, 3, OPKey.DES_ECB, c2b("505152535455565758595a5b5c5d5e5f")));
        System.out.println("init Update ...");
        cardManager.initializeUpdate(0, 0, OPApplet.SCP_UNDEFINED);
        System.out.println("Authenticate to card manager ...");
        cardManager.externalAuthenticate(OPApplet.APDU_CLR);
        // Delete old AID and Package
        System.out.print("Deleting old AppAID ...");
        try {
            cardManager.delete(capFile.aids[0], (byte) 0x00, capFile.aids[0].length, true);
        } catch (Exception e) {
            System.out.print(" Not found. Ignoring ...");
        }
        System.out.println();
        System.out.print("Deleting old PkgAID ...");
        try {
            cardManager.delete(capFile.pkgId, (byte) 0x00, capFile.pkgId.length, true);
        } catch (Exception e) {
            System.out.print(" Not found. Ignoring ...");
        }
        System.out.println();
        // Load the cap-file          
        System.out.print("Loading cap-file ... ");
        byte[] cardManagerAid = cardManager.getAID();

        cardManager.installForLoad(capFile.pkgId, 0, capFile.pkgId.length, cardManagerAid, 0, cardManagerAid.length, null, 0, null, 0, 0, null, 0);
        //  cardManager.load(capFile, null, CardManager.LOAD_COMP, null, cardManager.getMaxBlockLen());

        /*         public byte[] load(CapFile paramCapFile, 
 * int[] paramArrayOfint, 
 * int paramInt1,
 *  PrintWriter paramPrintWriter,
 *   int paramInt2, 
 *   String paramString, 
 *   byte[] paramArrayOfbyte1,
 *    int paramInt3, 
 *    byte[] paramArrayOfbyte2) 
        	 
        	  
        	  public byte[] load(CapFile paramCapFile,
        	   int[] paramArrayOfint,
        	    int paramInt1, 
        	    PrintWriter paramPrintWriter, 
        	    int paramInt2, 
        	    String paramString,
        	     byte[] paramArrayOfbyte1, 
        	     int paramInt3,
        	      byte[] paramArrayOfbyte2, 
        	      boolean paramBoolean) {
         */
        byte[] response = cardManager.load(capFile, null, 0, null, 0, null, null, 0, null);
        if (response != null) {
            System.out.println(byteArrayToHex(response));
        }
        System.out.println("Finished!");
        // Install applet, we try to install the first applet given in the
        // cap file, and try to instantiate it under the same AID as given for its
        // representation in the cap file. No installation data is passed.
        byte[] capaid = capFile.aids[0];
        byte[] instanceAid = {(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x52, (byte) 0x30, (byte) 0x10};
        System.out.print("Installing applet ... ");
        byte defaultInstallParam[] = {-55, 0}; // don't know what it's for

        byte[] response1 = cardManager.installForInstallAndMakeSelectable(capFile.pkgId, 0, capFile.pkgId.length, capaid, 0, capaid.length, instanceAid, 0, instanceAid.length, 0, defaultInstallParam, 0, defaultInstallParam.length, null, 0);
        if (response != null) {
            System.out.println(byteArrayToHex(response));
        }
        System.out.println("Succeeded!");
        // synchronize state with on-card card manager
        System.out.println("Update!");
        cardManager.update();
        // Print information regarding card manager, applets and packages on-card
        JCInfo info = JCInfo.INFO;
        System.out.println("\nCardManager AID : " + JCInfo.dataToString(cardManager.getAID()));
        System.out.println("CardManager state : " + info.toString("card.status", (byte) cardManager.getState()) + "\n");
        Object[] app = cardManager.getApplets(1, 0, true);
        if (app == null) {
            System.out.println("No applets installed on-card");
        } else {
            System.out.println("Applets:");
            for (int i = 0; i < app.length; i++) {

                System.out.println(info.toString("applet.status", (byte) ((OPApplet) app[i]).getState()) + " " + JCInfo.dataToString(((OPApplet) app[i]).getAID()));
            }
        }
        // List packages on card
        Object[] lf = cardManager.getLoadFiles(0, true);
        if (lf == null) {
            System.out.println("No packages installed on-card");
        } else {
            System.out.println("Packages:");
            for (int i = 0; i < lf.length; i++) {
                System.out.println(info.toString("loadfile.status", (byte) ((LoadFile) lf[i]).getState()) + " " + JCInfo.dataToString(((LoadFile) lf[i]).getAID()));
            }
        }

    }
    static String numbers = "0123456789abcdef";

    private byte[] c2b(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() % 2 != 0) {
            throw new RuntimeException("invalid length");
        }
        byte[] result = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            int i1 = numbers.indexOf(s.charAt(i));
            if (i1 == -1) {
                throw new RuntimeException("invalid number");
            }
            int i2 = numbers.indexOf(s.charAt(i + 1));
            if (i2 == -1) {
                throw new RuntimeException("invalid number");
            }
            result[i / 2] = (byte) ((i1 << 4) | i2);
        }
        return result;
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void setCustomOutputText(int textAreaToUse) {
        useOfTextArea = textAreaToUse;
    }

    public void initialize(QTToolGUI qttTool) {
        gui = qttTool;
        // customOutput = new CustomOutputStream(gui);
        // System.setOut(new PrintStream(customOutput, true));
    }

    void performConnection(String ipAddress) {
        connecToJcop(ipAddress);
        getJCard();
    }

    String performCommand(String line) {
        byte[] aid = hexStringToByteArray(line);

        String status = selectApplet(aid);

        return status;
    }

    private static byte[] hexStringToByteArray(String a) {
        byte[] b = new byte[a.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(a.substring(index, index + 2), 16);
            b[i] = (byte) j;
        }
        return b;

    }

    public void printLog(String info) {
        switch (useOfTextArea) {
            case 1:
                gui.appendText(info+"\n");
                break;
            case 2:
                gui.appendTextSocket(info+"\n");
                break;

            case 3:
                gui.appendlog(info+"\n");
                break;
        }
    }

    public void setUseOfTextArea(int useOfTextArea) {
        this.useOfTextArea = useOfTextArea;
    }
}
