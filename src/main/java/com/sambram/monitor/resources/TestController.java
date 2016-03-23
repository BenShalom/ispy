package com.sambram.monitor.resources;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sambram.monitor.dao.service.JasDBService;
import com.sambram.monitor.dto.Greeting;
import com.sambram.monitor.exceptions.JsonException;
import com.sambram.monitor.util.HttpRequestUtil;
import com.sambram.monitor.util.JsonConverterUtil;
import com.sambram.monitor.util.RemoteCommandUtil;

@Controller
public class TestController {
    
    private static final Logger LOG = LoggerFactory.getLogger(TestController.class);
    
    @Autowired
    JasDBService jasDBService;
    
    @Autowired
    HttpRequestUtil httpRequestUtil;
    
    @Autowired
    RemoteCommandUtil remoteCommandUtil;
    
    
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    private Map<String, SseEmitter> logEmitterMap = new HashMap<>();

    @RequestMapping("/greeting")
    public @ResponseBody Greeting greeting(@RequestParam(required=false, defaultValue="World") String name) {
        LOG.info("==== in greeting ====");
        Greeting greeting = new Greeting(counter.incrementAndGet(), String.format(template, name));
        jasDBService.addGreeting(greeting);
        return greeting;
    }
    
    @RequestMapping("/greeting/{id}")
    public @ResponseBody Greeting greeting(@PathVariable("id") Long id) {
        LOG.info("==== in greeting for Id ====");
        Greeting greeting = jasDBService.getGreeting(id);
        return greeting;
    }
    
    @RequestMapping("/ping")
    public @ResponseBody String ping(@RequestParam("server") String server, @RequestParam("port") String port) {
        String doGet = httpRequestUtil.doGet(server, port, "ping");
        return doGet;
    }
    
    @RequestMapping("/healthcheck")
    public @ResponseBody String healthCheck(@RequestParam("server") String server, @RequestParam("port") String port) {
        String doGet = httpRequestUtil.doGet(server, port, "healthcheck");
        return doGet;
    }
    
    @RequestMapping("/version")
    public @ResponseBody String version(@RequestParam("server") String server, @RequestParam("port") String port) {
        String doGet = httpRequestUtil.doGet(server, port, "service-version");
        return doGet;
    }
    
    @RequestMapping("/serverstatus")
    public @ResponseBody String serverStatus(@RequestParam("server") String server, @RequestParam("appPort") String appPort, @RequestParam("adminPort") String adminPort) throws JsonException {
        
        String ping = ping(server, adminPort);
        String healthCheck = healthCheck(server, adminPort);
        String version = version(server, appPort);
        
        HashMap<Object, Object> statusMap = new HashMap<>();
        statusMap.put("ping", ping);
        statusMap.put("healthCheck", healthCheck);
        statusMap.put("version", version);
        return JsonConverterUtil.objectToJson(statusMap);       
    }
    
    @RequestMapping("/downloadlogs")
    public void downloadLogs(HttpServletResponse response, @RequestParam("server") String server, @RequestParam("logPath") String logPath) throws SftpException, JSchException, IOException {
       String fileName = "test.log";
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        InputStream fileStream = remoteCommandUtil.getFile(server, logPath);
        InputStream inputStream = new BufferedInputStream(fileStream);
        
        //Copy bytes from source to destination(outputstream in this example), closes both streams.
        FileCopyUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
    }
    
    @RequestMapping("/execute")
    public @ResponseBody String executeCommand(@RequestParam("server") String server, @RequestParam("cmd") String command) throws SftpException, JSchException, IOException {
       remoteCommandUtil.executeCommand(server, command);
       return "success";
    }
    
    @RequestMapping("/taillogs")
    public SseEmitter tailLogs(@RequestParam("server") String server, @RequestParam("logPath") String logPath) throws SftpException, JSchException, IOException {
        LOG.info("Tail logs for {}_{}", server, logPath);
        SseEmitter sseEmitter = new SseEmitter();
        logEmitterMap.put(server+"_"+logPath, sseEmitter);
        emitResponse(sseEmitter);
        return sseEmitter;
    }
    
    @Async
    public void emitResponse(SseEmitter emitter) throws IOException {
        Boolean complete = false;
        Integer runCounter = 0;
        while (!complete) {
            runCounter ++;
            LOG.info("Sending response to emitter");
            try {
                emitter.send("Response-" + counter.incrementAndGet());
            } catch (Exception e) {
                LOG.warn("Emitter not open", e);
                complete = true;
            }
            if(runCounter == 500) {
                emitter.complete();
                complete = true;
            }
        }
    }
    
    @RequestMapping("/taillogs/stop")
    public @ResponseBody String tailLogsStop(@RequestParam("server") String server, @RequestParam("logPath") String logPath) throws SftpException, JSchException, IOException {
        SseEmitter sseEmitter = logEmitterMap.get(server+"_"+logPath);
        sseEmitter.complete();
        return "Stopped";
    }
}
