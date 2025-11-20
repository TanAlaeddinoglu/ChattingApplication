package com.chattingapp.chattingapp.chatRoom;


import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserPresenceService {
    private final UserPresenceRepository userPresenceRepository;

    public UserPresenceService(UserPresenceRepository userPresenceRepository) {
        this.userPresenceRepository = userPresenceRepository;
    }
    public UserPresence setOnline(String userId) {
        UserPresence presence =userPresenceRepository.
                findById(userId).orElse(new UserPresence(userId,
                        PresenceStatus.OFFLINE,
                        Instant.now()));
        UserPresence updated = new UserPresence(presence.getUserId(),
                PresenceStatus.ONLINE,
                Instant.now());

        return userPresenceRepository.save(updated);
    }

    public UserPresence setOffline(String userId) {
        UserPresence Updated =  new UserPresence(userId,
                PresenceStatus.OFFLINE,
                Instant.now());

        return userPresenceRepository.save(Updated);
    }
    public UserPresence setTyping(String userId) {
        UserPresence updated = new UserPresence(
                userId,
                PresenceStatus.TYPING,
                Instant.now()
        );
        return userPresenceRepository.save(updated);
    }

    public UserPresence stopTyping(String userId) {
        UserPresence updated = new UserPresence(
                userId,
                PresenceStatus.ONLINE,
                Instant.now()
        );
        return userPresenceRepository.save(updated);
    }
    public UserPresence getPresence(String userId) {
        return userPresenceRepository.findById(userId).orElseGet(() -> new UserPresence(userId, PresenceStatus.ONLINE, Instant.now()
        ));
    }
}
