package com.lifeline.resource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class HealthResource {

    @PersistenceContext(unitName = "LifelinePU")
    private EntityManager em;

    @Inject
    private com.lifeline.service.UserService userService;

    @GET
    public Response checkHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("api", "OK");
        health.put("cdi", userService != null ? "WORKING" : "INJECTION FAILED");

        try {
            // Test DB connection with a simple query
            Object result = em.createNativeQuery("SELECT 1 FROM DUAL").getSingleResult();
            health.put("database", "CONNECTED (Oracle DUAL check: " + result + ")");
        } catch (Exception e) {
            health.put("database", "CONNECTION FAILED: " + e.getMessage());
            // More detail for debugging
            health.put("error", e.getMessage());
            if (e.getCause() != null) {
                health.put("cause", e.getCause().getMessage());
            }
        }

        return Response.ok(health).build();
    }
}
