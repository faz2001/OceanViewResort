package com.hotel.handlers;

import com.hotel.models.User;
import com.hotel.services.AuthService;
import com.hotel.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/**
 * POST /api/auth/login
 * Body: {"username":"...","password":"..."}
 * Implements Sequence 1 — LoginUI → AuthService.login(username, password)
 */
public class AuthHandler extends BaseHandler {

    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        String method = ex.getRequestMethod().toUpperCase();
        String[] segs = getPathSegments(ex);

        try {
            // POST /api/auth/login
            if ("POST".equals(method) && segs.length > 0 && "login".equals(segs[0])) {
                handleLogin(ex);
            } else {
                notFound(ex, "Unknown auth endpoint.");
            }
        } catch (Exception e) {
            System.err.println("[AuthHandler] " + e.getMessage());
            serverError(ex, "Server error: " + e.getMessage());
        }
    }

    /**
     * Step 2: LoginUI → AuthService.login(username, password)
     * Step 5: AuthService → LoginUI: result(success/fail)
     */
    private void handleLogin(HttpExchange ex) throws Exception {
        String body = readBody(ex);
        Map<String,String> params = JsonUtil.parse(body);

        String username = params.getOrDefault("username","").trim();
        String password = params.getOrDefault("password","").trim();

        if (username.isEmpty() || password.isEmpty()) {
            // [fail] showError("Invalid credentials")
            ok(ex, JsonUtil.obj()
                .add("success", false)
                .add("message", "Username and password are required.")
                .build());
            return;
        }

        // Step 3+4: AuthService → UserDB.findUser(username) → userRecord/null
        User user = authService.login(username, password);

        if (user == null) {
            // [fail] showError("Invalid credentials")
            ok(ex, JsonUtil.obj()
                .add("success", false)
                .add("message", "Invalid credentials. Please check your username and password.")
                .build());
        } else {
            // [success] LoginUI → User: showMainMenu()
            ok(ex, JsonUtil.obj()
                .add("success", true)
                .add("message", "Login successful.")
                .addRaw("user", user.toJson())
                .build());
        }
    }
}
