/**
 * 
 */
package com.sambram.monitor.util;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * @author jben
 *
 */
@Component
public class RemoteCommandUtil {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteCommandUtil.class);
    
    public void executeCommand(String server, String command) throws JSchException, IOException {
        JSch conn = new JSch();
        Session session = null;
        conn.addIdentity("/home/jben/root.pem");
        session = conn.getSession("root", server, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        
        ChannelExec channel=(ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        InputStream in= channel.getInputStream();

        channel.connect();

        byte[] tmp=new byte[1024];
        while(true){
          while(in.available()>0){
            int i=in.read(tmp, 0, 1024);
            if(i<0)break;
           LOG.info(new String(tmp, 0, i));
          }
          if(channel.isClosed()){
            if(in.available()>0) continue; 
            LOG.info("exit-status: "+channel.getExitStatus());
            break;
          }
          try{Thread.sleep(1000);}catch(Exception ee){}
        }
        
        channel.disconnect();
        session.disconnect();
    }
    
    public InputStream getFile(String server, String filePath) throws SftpException, JSchException {
        JSch conn = new JSch();
        Session session = null;
        conn.addIdentity("/home/jben/root.pem");
        session = conn.getSession("root", server, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
        channel.connect();

        //change folder on the remote server
//        channel.cd("/var/log/realdoc/capture/catalyst");

        InputStream in = channel.get(filePath);
        return in;
    }
}
