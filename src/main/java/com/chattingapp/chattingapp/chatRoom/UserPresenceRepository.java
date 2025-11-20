package com.chattingapp.chattingapp.chatRoom;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPresenceRepository extends MongoRepository<UserPresence, String> {

}
