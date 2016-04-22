/**
 * 
 */
package com.sambram.monitor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    
    public List<String> executeCommand(String server, String command) throws JSchException, IOException {
        
        List<String> output = new ArrayList<>();
        
        Session session = getRemoteSession(server);
        
        /*ChannelExec channel=(ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        InputStream in= channel.getInputStream();*/
        
        ChannelExec channel=(ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        InputStream in= channel.getInputStream();
        
        channel.connect();
        LOG.info("Executing Command:\n{}", command );
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while (true) {
            while ((line = reader.readLine()) != null) {
                    LOG.info("Output-" + line);
                    output.add(line);
            }
            if (channel.isClosed()) {
                if (in.available() > 0)
                    continue;
                LOG.info("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }
        
        /*byte[] tmp=new byte[1024];
        while(true){
          while(in.available()>0){
            int i=in.read(tmp, 0, 1024);
            if(i<0)break;
           LOG.info("Cmd O/P" + new String(tmp, 0, i));
          }
          if(channel.isClosed()){
            if(in.available()>0) continue; 
            LOG.info("exit-status: "+channel.getExitStatus());
            break;
          }
          try{Thread.sleep(1000);}catch(Exception ee){}
        }*/
        
        channel.disconnect();
        session.disconnect();
        return output;
    }

    /**
     * @param server
     * @return Session
     * @throws JSchException
     * @throws IOException
     */
    private Session getRemoteSession(String server) throws JSchException, IOException {
        JSch conn = new JSch();
        conn.addIdentity("aws_root", FileUtils.readFileToByteArray(new File("/home/jben/root.pem")), null, null);
        Session session = conn.getSession("root", server, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }
    
    @Async
    public void tailLog(String server, String logpath, SseEmitter emitter) throws JSchException, IOException {
        Session session = getRemoteSession(server);
        
        ChannelExec channel=(ChannelExec) session.openChannel("exec");
        channel.setCommand("tail -f " + logpath);
        InputStream in= channel.getInputStream();

        channel.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while (true) {
            while ((line = reader.readLine()) != null) {
                try {
                    LOG.debug("Sending line-" + line);
                    emitter.send(line);
                } catch (Exception e) {
                    LOG.warn("Emitter closed", e);
                    channel.disconnect();
                    break;
                }
            }
            if (channel.isClosed()) {
                if (in.available() > 0)
                    continue;
                LOG.info("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }
        channel.disconnect();
        session.disconnect();
    }
    
    public InputStream getFile(String server, String filePath) throws SftpException, JSchException, IOException {
        Session session = getRemoteSession(server);

        ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
        channel.connect();

        //change folder on the remote server
//        channel.cd("/var/log/realdoc/capture/catalyst");

        InputStream in = channel.get(filePath);
        return in;
    }
}
