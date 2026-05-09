package com.print.module.auth;

import com.print.common.result.Result;
import com.print.module.auth.dto.LoginRequest;
import com.print.module.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return Result.success();
    }

    @GetMapping("/user-info")
    public Result<Map<String, Object>> userInfo() {
        return Result.success(authService.getUserInfo());
    }
}
