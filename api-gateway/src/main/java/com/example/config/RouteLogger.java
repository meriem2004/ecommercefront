package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component
public class RouteLogger implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(RouteLogger.class);
    
    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("Starting route diagnostics...");
        
        List<RouteDefinition> routes = new ArrayList<>();
        routeDefinitionLocator.getRouteDefinitions()
            .subscribe(routes::add);
            
        if (routes.isEmpty()) {
            logger.warn("NO ROUTES FOUND! Check your route configuration and ensure services are registered.");
        } else {
            logger.info("Found {} routes:", routes.size());
            routes.forEach(route -> {
                logger.info("Route ID: {}, URI: {}, Predicates: {}", 
                    route.getId(), 
                    route.getUri(), 
                    route.getPredicates());
            });
        }
        
        logger.info("Route diagnostics complete.");
    }
}