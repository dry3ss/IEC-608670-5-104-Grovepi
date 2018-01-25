/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quick_logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author will
 */
public class LockedLogger {
    private final ReentrantLock lock = new ReentrantLock();
    private final Logger logger = Logger.getLogger(LockedLogger.class.getName());
    private FileHandler fh;  
    
    class MyFormatter extends Formatter {
    // Create a DateFormat to format the logger timestamp.

    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }

    public String getHead(Handler h) {
        return super.getHead(h);
    }

    public String getTail(Handler h) {
        return super.getTail(h);
    }
}
    
    
    
    
    public LockedLogger(String path_to_folder, String name_log)
    {
        fh=null;
        try {  
            String sFileName = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
            sFileName=path_to_folder+"//"+name_log+"__"+sFileName+".txt";
            // This block configure the logger with handler and formatter  
            fh = new FileHandler(sFileName,true);  
            fh.setLevel(Level.ALL);
            logger.addHandler(fh);  
            logger.setLevel(Level.ALL);
            MyFormatter formatter = new MyFormatter();  
            fh.setFormatter(formatter);  
            logger.setUseParentHandlers(false);
            // the following statement is used to log any messages
            logger.info("\n\n########################################################");
            this.log("Start_of_the_log : "+ new SimpleDateFormat("HH:mm:ss").format(new Date()) );             
            logger.info("########################################################\n\n");
        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }
    
    public void log(String s)
    {
        lock.lock();
        try {
            long time=System.currentTimeMillis();
            logger.log(Level.INFO, time+"\t"+s);        
        } finally {
            lock.unlock();
        }
    }
    
    
}
