package com.sambram.monitor.dao.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sambram.monitor.dto.Greeting;

import nl.renarj.jasdb.LocalDBSession;
import nl.renarj.jasdb.api.DBSession;
import nl.renarj.jasdb.api.SimpleEntity;
import nl.renarj.jasdb.api.model.EntityBag;
import nl.renarj.jasdb.api.query.QueryBuilder;
import nl.renarj.jasdb.api.query.QueryExecutor;
import nl.renarj.jasdb.api.query.QueryResult;
import nl.renarj.jasdb.core.SimpleKernel;
import nl.renarj.jasdb.core.exceptions.ConfigurationException;
import nl.renarj.jasdb.core.exceptions.JasDBException;
import nl.renarj.jasdb.core.exceptions.JasDBStorageException;

@Component
public class JasDBService {
    
    private static final Logger LOG = LoggerFactory.getLogger(JasDBService.class);

    private final String bagName = "greetings";
    
    @PostConstruct
    public void init() {
        try {
            SimpleKernel.initializeKernel();
        } catch (ConfigurationException e) {
            LOG.error("Error initializing JASDB Kernel");
            System.exit(1);
        }
    }
    
    @PreDestroy
    public void shutdown() {
        try {
            SimpleKernel.shutdown();
        } catch (JasDBException e) {
            LOG.error("Error shutting down JASDB Kernel");
        }
    }
    
    public DBSession getSession() throws JasDBStorageException {
        DBSession session = null;
        try {
            session = new LocalDBSession();
        } catch (JasDBStorageException e) {
            LOG.error("Error creating session");
            throw e;
        }
        return session;
    }
    
    public void addGreeting(Greeting greeting) {
        try {
            
            EntityBag bag = getBag(bagName);
            SimpleEntity entity = new SimpleEntity();
            entity.addProperty("id", greeting.getId());
            entity.addProperty("content", greeting.getContent());
            bag.addEntity(entity);
        } catch (JasDBStorageException e) {
            LOG.error("Error adding greeting-{}", greeting);
        }
    }

    private EntityBag getBag(String bagName) throws JasDBStorageException {
        DBSession session = getSession();
        EntityBag bag = session.createOrGetBag(bagName);
        return bag;
    }
    
    public Greeting getGreeting(Long id) {
        Greeting greeting = new Greeting(id, null);
        try {
            EntityBag bag = getBag(bagName);
            QueryExecutor executor = bag.find(QueryBuilder.createBuilder().field("id").value(id));
            executor.limit(1);
            QueryResult result = executor.execute();

            SimpleEntity entity = result.next();
            String value = entity.getValue("content");
            
            greeting.setContent(value);

        } catch (JasDBStorageException e) {
            LOG.error("Error retrieving greeting-{}", id);
        }

        return greeting;
    }
}
