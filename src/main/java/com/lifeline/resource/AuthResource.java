package com.lifeline.resource;

import com.lifeline.entity.User;
import com.lifeline.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/authentication")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AuthResource {

    private static final Logger LOGGER = Logger.getLogger(AuthResource.class.getName());
    private static final String TOKEN_PREFIX = "dummy-access-token-";

    @Inject
    private UserService userService;

    /** POST /api/authentication/register */
    @POST
    @Path("/register")
    public Response register(User user) {
        LOGGER.log(Level.INFO, "Registration attempt for email: {0}",
                (user != null ? user.getEmail() : "null"));

        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return error(Response.Status.BAD_REQUEST,
                    "Invalid user data: email is required");
        }

        try {
            if (userService.findByEmail(user.getEmail()) != null) {
                return error(Response.Status.CONFLICT,
                        "User with this email already exists");
            }
            User created = userService.createUser(user);
            return Response.status(Response.Status.CREATED).entity(created).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Registration error", e);
            String msg = e.getMessage();
            if (e.getCause() != null) msg += " [Cause: " + e.getCause().getMessage() + "]";
            return error(Response.Status.INTERNAL_SERVER_ERROR,
                    "Registration database error: " + msg);
        }
    }

    /** POST /api/authentication/login */
    @POST
    @Path("/login")
    public Response login(User credentials) {
        if (credentials == null || credentials.getEmail() == null) {
            return error(Response.Status.BAD_REQUEST, "Email and password are required");
        }

        User user = userService.findByEmail(credentials.getEmail());

        if (user == null || !user.getPassword().equals(credentials.getPassword())) {
            return error(Response.Status.UNAUTHORIZED, "Invalid email or password");
        }

        if (user.isSuspended()) {
            return error(Response.Status.FORBIDDEN,
                    "Your account has been suspended. Contact an administrator.");
        }

        Map<String, String> response = new HashMap<>();
        // Encode email in token so /profile can recover it without a session store
        response.put("access",  TOKEN_PREFIX + user.getEmail());
        response.put("refresh", "dummy-refresh-token");
        response.put("role",    user.getRole());
        response.put("name",    user.getName());
        return Response.ok(response).build();
    }

    /**
     * GET /api/authentication/profile
     * Reads the email embedded in the dummy token to return the real user from DB.
     * Token format: "Bearer dummy-access-token-{email}"
     */
    @GET
    @Path("/profile")
    public Response getProfile(@HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return error(Response.Status.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        if (!token.startsWith(TOKEN_PREFIX)) {
            return error(Response.Status.UNAUTHORIZED, "Invalid token format");
        }

        String email = token.substring(TOKEN_PREFIX.length());
        User user = userService.findByEmail(email);

        if (user == null) {
            return error(Response.Status.NOT_FOUND, "User not found for this token");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("email", user.getEmail());
        profile.put("name",  user.getName());
        profile.put("role",  user.getRole());
        profile.put("is_suspended", user.isSuspended());
        profile.put("is_active",    user.isActive());
        profile.put("profile_picture", user.getProfilePicture());
        return Response.ok(profile).build();
    }

    // ── helper ────────────────────────────────────────────────────────
    private Response error(Response.Status status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("detail", message);
        return Response.status(status).entity(body).build();
    }
}
