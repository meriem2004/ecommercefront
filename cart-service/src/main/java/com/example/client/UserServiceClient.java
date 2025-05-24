package com.example.client;

import com.example.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email,
                         @RequestHeader("Authorization") String authorization);
    
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id,
                      @RequestHeader("Authorization") String authorization);
}