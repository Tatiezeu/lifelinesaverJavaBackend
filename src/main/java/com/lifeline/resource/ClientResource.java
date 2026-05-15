package com.lifeline.resource;

import com.lifeline.entity.ClientProfile;
import com.lifeline.entity.EmergencyAlert;
import com.lifeline.entity.User;
import com.lifeline.service.AlertService;
import com.lifeline.service.ClientProfileService;
import com.lifeline.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST resource for client-specific operations:
 *  - GET  /api/client/profile          → get own profile + vehicle info
 *  - PUT  /api/client/profile          → update own profile + vehicle info
 *  - GET  /api/client/alerts           → get all alerts sent by this client
 *  - POST /api/client/alerts           → send a new SOS alert
 *  - PATCH /api/client/alerts/{id}/cancel → cancel a pending/active alert
 *
 * Authentication: token in header ("Bearer dummy-access-token-{email}")
 */
@Path("/client")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ClientResource {

    private static final String TOKEN_PREFIX = "dummy-access-token-";

    @Inject private UserService userService;
    @Inject private ClientProfileService clientProfileService;
    @Inject private AlertService alertService;

    // ── Token helper ──────────────────────────────────────────────────

    private String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring("Bearer ".length()).trim();
        if (!token.startsWith(TOKEN_PREFIX)) return null;
        return token.substring(TOKEN_PREFIX.length());
    }

    // ── Profile endpoints ─────────────────────────────────────────────

    /**
     * GET /api/client/profile
     * Returns combined user info + vehicle profile for the authenticated client.
     */
    @GET
    @Path("/profile")
    public Response getClientProfile(@HeaderParam("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        if (email == null) return unauthorized();

        User user = userService.findByEmail(email);
        if (user == null) return error(Response.Status.NOT_FOUND, "User not found");

        ClientProfile cp = clientProfileService.findByUser(user);

        Map<String, Object> result = new HashMap<>();
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("role", user.getRole());
        result.put("profile_picture", user.getProfilePicture());
        result.put("phone",          cp != null ? cp.getPhone()          : "");
        result.put("insurance",      cp != null ? cp.getInsurancePolicy(): "");
        result.put("vehicleMake",    cp != null ? cp.getVehicleMake()    : "");
        result.put("vehicleModel",   cp != null ? cp.getVehicleModel()   : "");
        result.put("vehicleYear",    cp != null ? cp.getVehicleYear()    : "");
        result.put("licensePlate",   cp != null ? cp.getLicensePlate()   : "");

        return Response.ok(result).build();
    }

    /**
     * PUT /api/client/profile
     * Body: { name, profile_picture, phone, insurance, vehicleMake, vehicleModel, vehicleYear, licensePlate }
     */
    @PUT
    @Path("/profile")
    public Response updateClientProfile(@HeaderParam("Authorization") String authHeader,
                                        Map<String, String> body) {
        String email = extractEmail(authHeader);
        if (email == null) return unauthorized();

        User user = userService.findByEmail(email);
        if (user == null) return error(Response.Status.NOT_FOUND, "User not found");

        // Update user fields
        if (body.containsKey("name") && body.get("name") != null)
            user.setName(body.get("name"));
        if (body.containsKey("profile_picture") && body.get("profile_picture") != null)
            user.setProfilePicture(body.get("profile_picture"));
        userService.updateUser(user);

        // Build incoming ClientProfile
        ClientProfile incoming = new ClientProfile();
        if (body.containsKey("phone"))        incoming.setPhone(body.get("phone"));
        if (body.containsKey("insurance"))    incoming.setInsurancePolicy(body.get("insurance"));
        if (body.containsKey("vehicleMake"))  incoming.setVehicleMake(body.get("vehicleMake"));
        if (body.containsKey("vehicleModel")) incoming.setVehicleModel(body.get("vehicleModel"));
        if (body.containsKey("vehicleYear"))  incoming.setVehicleYear(body.get("vehicleYear"));
        if (body.containsKey("licensePlate")) incoming.setLicensePlate(body.get("licensePlate"));

        ClientProfile saved = clientProfileService.upsert(user, incoming);

        Map<String, Object> result = new HashMap<>();
        result.put("name",         user.getName());
        result.put("email",        user.getEmail());
        result.put("profile_picture", user.getProfilePicture());
        result.put("phone",        saved.getPhone());
        result.put("insurance",    saved.getInsurancePolicy());
        result.put("vehicleMake",  saved.getVehicleMake());
        result.put("vehicleModel", saved.getVehicleModel());
        result.put("vehicleYear",  saved.getVehicleYear());
        result.put("licensePlate", saved.getLicensePlate());
        return Response.ok(result).build();
    }

    // ── Alert / SOS endpoints ─────────────────────────────────────────

    /**
     * GET /api/client/alerts
     * Returns all alerts sent by the authenticated client.
     */
    @GET
    @Path("/alerts")
    public Response getMyAlerts(@HeaderParam("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        if (email == null) return unauthorized();

        User user = userService.findByEmail(email);
        if (user == null) return error(Response.Status.NOT_FOUND, "User not found");

        List<EmergencyAlert> all = alertService.findAll();
        List<EmergencyAlert> mine = all.stream()
                .filter(a -> a.getUser() != null
                          && a.getUser().getEmail() != null
                          && a.getUser().getEmail().equalsIgnoreCase(email))
                .collect(Collectors.toList());

        return Response.ok(mine).build();
    }

    /**
     * POST /api/client/alerts
     * Create (send) a new SOS alert on behalf of the authenticated client.
     * Body: { message?, latitude?, longitude?, address? }
     */
    @POST
    @Path("/alerts")
    public Response sendSos(@HeaderParam("Authorization") String authHeader,
                            Map<String, Object> body) {
        String email = extractEmail(authHeader);
        if (email == null) return unauthorized();

        User user = userService.findByEmail(email);
        if (user == null) return error(Response.Status.NOT_FOUND, "User not found");

        EmergencyAlert alert = new EmergencyAlert();
        alert.setUser(user);
        alert.setStatus("pending");

        if (body != null) {
            if (body.get("message") != null)
                alert.setMessage(body.get("message").toString());
            if (body.get("latitude") != null)
                alert.setLatitude(Double.parseDouble(body.get("latitude").toString()));
            if (body.get("longitude") != null)
                alert.setLongitude(Double.parseDouble(body.get("longitude").toString()));
            if (body.get("address") != null)
                alert.setAddress(body.get("address").toString());
        }

        // Default message if none provided
        if (alert.getMessage() == null || alert.getMessage().isBlank()) {
            alert.setMessage("SOS emergency request from " + user.getName());
        }

        EmergencyAlert created = alertService.createAlert(alert);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * PATCH /api/client/alerts/{id}/cancel
     * Cancel a pending alert (only if it belongs to the authenticated client).
     */
    @PATCH
    @Path("/alerts/{id}/cancel")
    public Response cancelSos(@HeaderParam("Authorization") String authHeader,
                              @PathParam("id") Long id) {
        String email = extractEmail(authHeader);
        if (email == null) return unauthorized();

        User user = userService.findByEmail(email);
        if (user == null) return error(Response.Status.NOT_FOUND, "User not found");

        EmergencyAlert alert = alertService.findById(id);
        if (alert == null)
            return error(Response.Status.NOT_FOUND, "Alert not found: " + id);

        // Ownership check
        if (alert.getUser() == null || !alert.getUser().getEmail().equalsIgnoreCase(email))
            return error(Response.Status.FORBIDDEN, "You can only cancel your own alerts");

        EmergencyAlert updated = alertService.updateStatus(id, "cancelled");
        return Response.ok(updated).build();
    }

    // ── helpers ───────────────────────────────────────────────────────

    private Response unauthorized() {
        return error(Response.Status.UNAUTHORIZED, "Missing or invalid Authorization header");
    }

    private Response error(Response.Status status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("detail", message);
        return Response.status(status).entity(body).build();
    }
}
