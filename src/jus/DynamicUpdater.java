/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Aero
 */
public class DynamicUpdater {
    
    GSONHelper helper = new GSONHelper();
    ArrayList<File> tracked = new ArrayList<File>();
    ArrayList<String> nowrite = new ArrayList<String>();
    
    String root = "";
    
    Progress progress = new Progress();
    Downloader downloader = new Downloader();
    
    public static final String UPDATE_REMOTE_HASHLIST = "update.json";
    public static final String UPDATE_REMOTE_DIR = "data/";
    
    public void setUpdateDirectory(File f){
        root = f.toString();
        char c = root.charAt(root.length()-1);
        if(!(c == '/' || c == '\\' || c == File.separatorChar)){
            root += File.separator;
        }
        addAll(f);
    }
    
    private void addAll(File f){
        for(File file : f.listFiles()){
            if(file.isDirectory()) 
                addAll(file);
            else{
                tracked.add(file);
            }
        }
    }
    
    // add file to list of non-writable files, update will be written to <name>.update
    public void addNonWritable(String name){
        nowrite.add(name);
    }

    private UpdateData getCurrentData() throws NoSuchAlgorithmException, IOException {
        UpdateData data = new UpdateData();
        for(File f : tracked){
            data.hashes.put(f.toString().substring(root.length()), getHash(f));
        }
        return data;
    }
    
    class Progress {
        
        Downloader.Progress current;
        String currentFile;
        int count = 0;
        int at = 0;
        
        public float getProgress(){ // does not account for file size 
            return at / (float) count;
        }
        public String getCurrentFile(){
            return currentFile;
        }
        public Downloader.Progress getCurrentFileProgress(){
            return current;
        }
    }
    
    class UpdateData {
        HashMap<String, String> hashes = new HashMap<String, String>();
    }
    
    // apache commons has a better function for this
    public String getHash(File f) throws NoSuchAlgorithmException, FileNotFoundException, IOException{
        FileInputStream in = new FileInputStream(f);
        MessageDigest md = MessageDigest.getInstance("MD5");
        
        int r = 0;
        byte[] b = new byte[0x7FFF]; // 32kb
        
        while((r=in.read(b))>-1) 
            md.update(b,0,r);        
        
        return new BigInteger(1, md.digest()).toString(16).toUpperCase();
    }
    
    // create current version json based on the current files
    public void rebase(File hashlist) throws IOException, NoSuchAlgorithmException{
        UpdateData data = getCurrentData();
        helper.saveClass(new FileOutputStream(hashlist), data, UpdateData.class);
    }
    
    final ArrayList<String> needUpdate = new ArrayList<String>();
    final DownloadResult updater = new DownloadResult() {

        int index = 0;
        
        @Override
        public void finished(long time) {
            if(index < total){
                try {
                    String file = needUpdate.get(index);
                    File to = nowrite.contains(file)
                            ? new File(root + file + ".update")
                            : new File(root + file);
                    progress.current = downloader.download(new URL(baseURL + UPDATE_REMOTE_DIR + file.replace("\\", "/")), to, updater);
                    progress.currentFile = file;
                    progress.at = index;
                } catch (MalformedURLException ex) {
                    result.failed(time, ex);
                }
                index++;
            }else{
                result.finished(System.currentTimeMillis()-startTime);
            }
        }

        @Override
        public void failed(long time, IOException exception, int responseCode) {
            System.out.println("Update failed!");
            result.failed(time, exception);
        }

    };
    
    int total = 0;
    String baseURL;
    UpdateResult result;
    long startTime = 0;
    
    public DynamicUpdater.Progress update(final String baseURL, final UpdateResult result) {
        try {
            startTime = System.currentTimeMillis();
            UpdateData remote = (UpdateData) helper.loadClass(new URL(baseURL+UPDATE_REMOTE_HASHLIST).openConnection().getInputStream(), UpdateData.class);
            UpdateData current = getCurrentData();
            this.baseURL = baseURL;
            this.result = result;
            needUpdate.clear();
            for(String file : remote.hashes.keySet()){
                if(!remote.hashes.get(file).equals(current.hashes.get(file))){
                    needUpdate.add(file);
                }
            }
            total = needUpdate.size();
            progress.count = total;
            if(needUpdate.size()>0){
                String file = needUpdate.get(0);
                File to = nowrite.contains(file)
                        ? new File(root + file + ".update")
                        : new File(root + file);
                progress.current = downloader.download(new URL(baseURL + UPDATE_REMOTE_DIR + file.replace("\\", "/")), to, updater);
            }else{
                result.finished(0);
            }
            
        } catch (IOException ex) {
            result.failed(0, ex);
        } catch (NoSuchAlgorithmException ex) {
            result.failed(0, ex);
        }
        return progress;
    }
}
