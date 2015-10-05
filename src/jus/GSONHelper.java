package jus;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Super simple gson helper class
 * @author Luke
 */
public class GSONHelper { 
    Gson son = new Gson();
    public void saveClass(OutputStream out, Object clas, Type t) throws IOException{
        OutputStreamWriter w = new OutputStreamWriter(out);
        w.write(son.toJson(clas, t));
        w.close();
    }
    public void saveClass(OutputStream out, Object clas) throws IOException{
        OutputStreamWriter w = new OutputStreamWriter(out);
        w.write(son.toJson(clas));
        w.close();
    }
    public Object loadClass(InputStream in, Type t) throws IOException{
        return son.fromJson(new InputStreamReader(in), t);
    }
}
