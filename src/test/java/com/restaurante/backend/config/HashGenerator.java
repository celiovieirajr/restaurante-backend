package com.restaurante.backend.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        System.out.println("HASH_START:" + new BCryptPasswordEncoder().encode("password123") + ":HASH_END");
    }
}
