/**
 * 
 */
package com.sambram.monitor.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author jben
 *
 */
@Component
public class HttpRequestUtil {
    
    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestUtil.class);
    
    RestTemplate restTemplate = new RestTemplate(); 
    
    /**
     * @param host
     * @param port
     * @param uri
     * @return JSON Response
     */
    public String doGet(String host, String port, String uri) {
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("host", host);
        vars.put("port", port);
        vars.put("uri", uri);
        String response = restTemplate.getForObject("http://{host}:{port}/{uri}", String.class, vars);
        return response;
    }
}
