package jus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Aero
 */
public class Downloader {
    class Progress {

        private int total = 1;
        private int have = 0;
        private float progress = 0f;
        private int rate = 0, rateincr = 0;
        private long start = 0, lastRateCheck;
        private int responsecode = 0;
        // from 0.0 to 1.0
        public float getProgress(){
            return have / ((float)total);
        }

        // in bytes/s
        public int getDownloadRate(){
            long now = System.currentTimeMillis();
            long t = now - lastRateCheck;
            if(t >= 1000){
                float rmod = t/1000f;
                rate = (int) (rateincr / rmod);
                lastRateCheck = now;
                rateincr = 0;
            }
            return rate;
        }
        public int getTotalSize(){
            return total;
        }
        public long getDownloadStartSystemTime(){
            return start;
        }
    }
    
    public Progress download(final URL source, final File destination, final DownloadResult callback){
        System.out.println("Downloading " + source.toString());
        final Progress progress = new Progress();
        new Thread(new Runnable() { // this should eventually be improved to have either 1 central download thread or a thread pool

            @Override
            public void run() {
                long start = System.currentTimeMillis(); // profiling could be done elsewhere but eh
                File temp = new File(destination.toString() + ".temp");
                try {
                    progress.start = start;
                    HttpURLConnection con = (HttpURLConnection) source.openConnection();
                    progress.responsecode = con.getResponseCode();
                    progress.total = con.getContentLength();
                    if(progress.total == 0) progress.total = 1; // dont devide by zero D:
                    InputStream in = con.getInputStream();
                    
                    // ensure folder is created
                    temp.mkdirs();
                    temp.delete();
                    
                    
                    FileOutputStream out = new FileOutputStream(temp);
                    
                    // oldschool but efficient
                    int r = 0;
                    byte[] b = new byte[0x7FFF]; // 32kb
                    progress.lastRateCheck = System.currentTimeMillis();
                    while((r=in.read(b))>-1){
                        out.write(b,0,r);
                        progress.have += r;
                        progress.rateincr += r;
                    }
                    
                    in.close();
                    out.close();
                    
                    temp.renameTo(destination);
                    
                    callback.finished(System.currentTimeMillis() - start);
                    
                } catch (IOException ex) {
                    temp.delete();
                    callback.failed(System.currentTimeMillis() - start, ex, progress.responsecode);
                }
            }
            
        }).start();
        return progress;
    }
}
