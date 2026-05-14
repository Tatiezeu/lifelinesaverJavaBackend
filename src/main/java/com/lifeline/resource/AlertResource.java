package com.lifeline.resource;

import com.lifeline.entity.EmergencyAlert;
import com.lifeline.entity.User;
import com.lifeline.service.AlertService;
import com.lifeline.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/alerts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AlertResource {

    @Inject
    private UserService userService;

    @Inject
    private AlertService alertService;

    /** GET /api/alerts — list all alerts */
    @GET
    public List<EmergencyAlert> getAllAlerts() {
        return alertService.findAll();
    }

    /** GET /api/alerts/stats — counts for emergency dashboard */
    @GET
    @Path("/stats")
    public Response getStats(@QueryParam("month") Integer month, @QueryParam("year") Integer year) {
        return Response.ok(alertService.getStats(month, year)).build();
    }

    /** GET /api/alerts/{id} — get one alert */
    @GET
    @Path("/{id}")
    public Response getAlert(@PathParam("id") Long id) {
        EmergencyAlert alert = alertService.findById(id);
        if (alert == null) return notFound(id);
        return Response.ok(alert).build();
    }

    /** POST /api/alerts — create new alert */
    @POST
    public Response createAlert(EmergencyAlert alert) {
        if (alert.getUser() != null && alert.getUser().getEmail() != null) {
            User realUser = userService.findByEmail(alert.getUser().getEmail());
            if (realUser != null) {
                alert.setUser(realUser);
            }
        }
        EmergencyAlert created = alertService.createAlert(alert);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * PATCH /api/alerts/{id} — update alert status.
     * Frontend (LiveMap.jsx) sends: { "status": "resolved" }
     */
    @PATCH
    @Path("/{id}")
    public Response updateAlertStatus(@PathParam("id") Long id, Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        if (status == null || status.isBlank()) {
            Map<String, String> err = new HashMap<>();
            err.put("detail", "status field is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        EmergencyAlert updated = alertService.updateStatus(id, status);
        if (updated == null) return notFound(id);
        return Response.ok(updated).build();
    }

    /** DELETE /api/alerts/{id} — delete one alert */
    @DELETE
    @Path("/{id}")
    public Response deleteAlert(@PathParam("id") Long id) {
        boolean deleted = alertService.deleteAlert(id);
        if (!deleted) return notFound(id);
        Map<String, String> msg = new HashMap<>();
        msg.put("detail", "Alert deleted");
        return Response.ok(msg).build();
    }

    /** DELETE /api/alerts — delete all alerts (clear history) */
    @DELETE
    public Response deleteAllAlerts() {
        int count = alertService.deleteAll();
        Map<String, Object> msg = new HashMap<>();
        msg.put("detail", "All alerts deleted");
        msg.put("count", count);
        return Response.ok(msg).build();
    }

    // ── helper ────────────────────────────────────────────────────────
    private Response notFound(Long id) {
        Map<String, String> body = new HashMap<>();
        body.put("detail", "Alert not found: " + id);
        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }
}
