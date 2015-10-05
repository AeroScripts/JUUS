/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jus;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aero
 */
public class JUUS { // Java Userlevel Update System

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DynamicUpdater dup = new DynamicUpdater();
        dup.setUpdateDirectory(new File("/AddOns/"));
        try {
            
            
            // to update hash list file
            //dup.rebase(new File("./update.json"));
            
            
            // register volatile files
            //dup.addNonWritable("aurous.jar");
            
            
            dup.update("http://192.168.1.110:51113/", new UpdateResult() {

                @Override
                public void finished(long time) {
                    System.out.println("Update finished");
                }

                @Override
                public void failed(long time, Exception exception) {
                    System.out.println("Update failed!");
                    exception.printStackTrace();
                }
                
            });
            
        } catch (IOException ex) {
            Logger.getLogger(JUUS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(JUUS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
