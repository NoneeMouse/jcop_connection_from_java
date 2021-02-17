/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qtttool;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

/**
 * This class extends from OutputStream to redirect output to a JTextArrea
 *
 * @author www.codejava.net
 *
 */
public class CustomOutputStream extends OutputStream {

    private JTextArea textArea;
    private QTToolGUI gui;
    int useOfTextArea = 1;

    public int getUseOfTextArea() {
        return useOfTextArea;
    }

    public void setUseOfTextArea(int useOfTextArea) {
        this.useOfTextArea = useOfTextArea;
    }

    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    public CustomOutputStream(QTToolGUI gui) {
        this.gui = gui;
    }

    @Override
    public void write(int b) throws IOException {
        switch (useOfTextArea) {
            case 1:
                gui.appendText(String.valueOf((char) b));
                break;
            case 2:
                gui.appendTextSocket(String.valueOf((char) b));
                break;

            case 3:
                gui.appendlog(String.valueOf((char) b));
                break;
        }

        // redirects data to the text area
        //textArea.append(String.valueOf((char)b));
        // scrolls the text area to the end of data
        //textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
