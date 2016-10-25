/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echoclient;

import java.io.IOException;
import java.net.InetAddress;
import javax.swing.JOptionPane;

/**
 *
 * @author travishaycock
 */
public class EchoClient {

    public static void main(String[] args) throws IOException {

        //String serverIP = InetAddress.getLocalHost().getHostAddress();
        System.out.println();
        String userA = JOptionPane.showInputDialog(null, "Enter Hosts IP"); //Prompt for
        if (userA.isEmpty()) {
            System.exit(1);
        }
        EchoRun er = new EchoRun(userA);
        er.startRunning();

    }

}
