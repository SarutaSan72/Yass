// (c) http://forum.java.sun.com/thread.jspa?threadID=490586&messageID=2304192

package yass;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class FileTransferable implements Transferable {

    /**
     * Supported data flavor
     */
    DataFlavor[] dataFlavors = {DataFlavor.javaFileListFlavor};

    /**
     * Instances of the File classes to be transferred
     */
    LinkedList<File> files = new LinkedList<>();

    /**
     * Copies the file to the clipboard.
     *
     * @param file file
     */
    public static void copyToClipboard(File file) {
        FileTransferable ft = new FileTransferable();
        ft.addFile(file);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(ft, null);
    }

    /**
     * Gets the transferDataFlavors attribute of the FileTransferable object
     *
     * @return The transferDataFlavors value
     */
    public DataFlavor[] getTransferDataFlavors() {
        return dataFlavors;
    }

    /**
     * Gets the dataFlavorSupported attribute of the FileTransferable object
     *
     * @param flavor Description of the Parameter
     * @return The dataFlavorSupported value
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return dataFlavors[0].equals(flavor);
    }

    /**
     * Gets the transferData attribute of the FileTransferable object
     *
     * @param flavor Description of the Parameter
     * @return The transferData value
     * @throws UnsupportedFlavorException Description of the Exception
     * @throws IOException                Description of the Exception
     */
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        return files;
    }

    /**
     * Adds a file for the transfer.
     *
     * @param f file
     */
    public void addFile(File f) {
        files.add(f);
    }

}


