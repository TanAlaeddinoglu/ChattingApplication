package com.chattingapp.chattingapp.chatRoom;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/presence")
public class UserPresenceController {
    private final UserPresenceService userPresenceService;

    public UserPresenceController(UserPresenceService userPresenceService) {
        this.userPresenceService = userPresenceService;
    }

    @GetMapping
    public ResponseEntity<List<UserPresence>> getPresence(
            @RequestParam(value = "ids", required = false) List<String> ids
    ) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(userPresenceService.getAll());
        }
        return ResponseEntity.ok(userPresenceService.getAllByIds(ids));
    }
}
