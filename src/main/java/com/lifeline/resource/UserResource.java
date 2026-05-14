package com.lifeline.resource;

import com.lifeline.entity.User;
import com.lifeline.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UserResource {

    @Inject
    private UserService userService;

    /** GET /api/users — list all users */
    @GET
    public Response getAllUsers() {
        List<User> users = userService.findAll();
        return Response.ok(users).build();
    }

    /** GET /api/users/{email} — get a single user by email */
    @GET
    @Path("/{email}")
    public Response getUser(@PathParam("email") String email) {
        User user = userService.findByEmail(email);
        if (user == null) return notFound("User not found: " + email);
        return Response.ok(user).build();
    }

    /** POST /api/users — create a new user (admin-side call from Users.jsx) */
    @POST
    public Response createUser(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return badRequest("Email is required");
        }
        if (userService.findByEmail(user.getEmail()) != null) {
            return conflict("User already exists: " + user.getEmail());
        }
        User created = userService.createUser(user);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /** PUT /api/users/{email} — update name/role/password */
    @PUT
    @Path("/{email}")
    public Response updateUser(@PathParam("email") String email, User body) {
        if (body == null) return badRequest("Request body is required");
        body.setEmail(email);
        User updated = userService.updateUser(body);
        if (updated == null) return notFound("User not found: " + email);
        return Response.ok(updated).build();
    }

    /** PATCH /api/users/{email}/suspend — toggle suspended status */
    @PATCH
    @Path("/{email}/suspend")
    public Response suspendUser(@PathParam("email") String email) {
        User updated = userService.toggleSuspend(email);
        if (updated == null) return notFound("User not found: " + email);
        return Response.ok(updated).build();
    }

    /** DELETE /api/users/{email} — delete a user permanently */
    @DELETE
    @Path("/{email}")
    public Response deleteUser(@PathParam("email") String email) {
        boolean deleted = userService.deleteUser(email);
        if (!deleted) return notFound("User not found: " + email);
        Map<String, String> msg = new HashMap<>();
        msg.put("detail", "User deleted successfully");
        return Response.ok(msg).build();
    }

    /** DELETE /api/users/{email}/delete_user — alternate delete path called by Users.jsx */
    @DELETE
    @Path("/{email}/delete_user")
    public Response deleteUserAlt(@PathParam("email") String email) {
        return deleteUser(email);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private Response notFound(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("detail", msg);
        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }

    private Response badRequest(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("detail", msg);
        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }

    private Response conflict(String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("detail", msg);
        return Response.status(Response.Status.CONFLICT).entity(body).build();
    }
}
