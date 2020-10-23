package com.shediz.gateway.service;

import com.shediz.gateway.database.User;
import com.shediz.gateway.database.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;


@Service
public class UserAuthService
{
    private final UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //For generate token
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();

    @Autowired
    UserAuthService(UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }

    public String register(String username, String password, String bio, boolean isPrivate)
    {
        final User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsEnabled(true);
        user.setBio(bio);
        user.setIsPrivate(isPrivate);

        return userRepository.save(user).getUsername();
    }

    public boolean matchAllCredential(String token, String username, String password)
    {
        Optional<User> optional = userRepository.findOneByUsername(username);
        if (optional.isPresent())
        {
            User user = optional.get();

            return passwordEncoder.matches(password, user.getPassword()) &&
                    user.getToken() != null && user.getToken().equals(token);
        }

        return false;
    }

    public Optional<String> login(String username, String password)
    {
       Optional<User> optional = userRepository.findOneByUsername(username);
       if (optional.isPresent())
       {
           User user = optional.get();

           if (passwordEncoder.matches(password, user.getPassword()))
           {
               if (user.getToken() == null || user.getToken().isEmpty())
               {
                   String token = generateNewToken();
                   userRepository.setToken(token, user.getUsername());

                   return Optional.of(token);
               }
               else
                   return Optional.of(user.getToken());
            }
       }

        return Optional.empty();
    }

    /*
    Below code will generate random string in Base64 encoding with 64 chars.
    In Base64 encoding every char encodes 6 bits of data. So for 48 bytes from
    below example, you get the 64 chars.
    */
    private static String generateNewToken()
    {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public Optional<User> findByToken(String token)
    {
        return userRepository.findByToken(token);
    }

    public void logout(User user)
    {
        userRepository.setToken(null, user.getUsername());
    }
}
