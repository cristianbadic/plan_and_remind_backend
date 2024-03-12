package com.example.planAndRemind.service;

import com.example.planAndRemind.Repository.UserRepository;
import com.example.planAndRemind.dto.AuthRequest;
import com.example.planAndRemind.dto.AuthResponse;
import com.example.planAndRemind.exception.DisabledUserException;
import com.example.planAndRemind.exception.InvalidPasswordException;
import com.example.planAndRemind.exception.UserException;
import com.example.planAndRemind.exception.UserNotFoundException;
import com.example.planAndRemind.mapper.UserMapper;
import com.example.planAndRemind.model.UserEntity;
import com.example.planAndRemind.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AuthService implements UserDetailsService {
    private JwtUtil jwtUtil;
    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;

    private UserMapper userMapper;


    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setUserMapper (UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public AuthResponse loginUser(AuthRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();
        this.checkCredentials(email, password);

        UserDetails userDetails = this.loadUserByUsername(email);
        String newGeneratedToken = jwtUtil.generateToken(userDetails);

        UserEntity user =  userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("No User found with this email and password combination!"));

        return new AuthResponse(userMapper.userToDto(user), newGeneratedToken, jwtUtil.getExpirationTime());
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new UsernameNotFoundException("User with this Email not found!"));

        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }


    private void checkCredentials(String email, String password)  {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("user with this Email not found!"));

        if (!user.getAccountConfirmation().equals("1")){
            throw new UserException("The registration wasn't confirmed for this email. Please visit" +
                    " the registration page to confirm your account!");
        }

        try {

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException ex) {
            throw new DisabledUserException("User is disabled");
        } catch (BadCredentialsException ex) {
            throw new InvalidPasswordException("Invalid Password for this email!");
        }
    }
}
