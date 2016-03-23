/**
 * 
 */
package com.sambram.monitor.util;

import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sambram.monitor.exceptions.JsonException;

/**
 * @author jben
 *
 */
public class JsonConverterUtil {
    
    private static final Logger LOG = LoggerFactory.getLogger(JsonConverterUtil.class);
 
    
    /**
     * @param object
     * @return json string
     * @throws JsonException
     */
    public static String objectToJson(Object object) throws JsonException {
        String json = null;
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, object);
            json = sw.getBuffer().toString();
        } catch (Exception e) {
            LOG.error("Error Converting object of class-{} to json", object.getClass(), e);
            throw new JsonException("Error Converting object to json");
        }
        
        return json;
    }
    
    /**
     * @param json
     * @param objectClass
     * @return Object of objectClass
     * @throws JsonException
     */
    public static Object jsonToObject(String json, Class<?> objectClass) throws JsonException{
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object obj = mapper.readValue(json, objectClass);
            return obj;
        } catch (Exception e) {
            LOG.error("Error Converting the json-{} to object of class-{}", json, objectClass, e);
            throw new JsonException("Error Converting json to object");
        }
    }
}
