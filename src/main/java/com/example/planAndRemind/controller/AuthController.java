package com.example.planAndRemind.controller;


import com.example.planAndRemind.dto.AuthRequest;
import com.example.planAndRemind.dto.AuthResponse;
import com.example.planAndRemind.exception.InvalidPasswordException;
import com.example.planAndRemind.exception.UserException;
import com.example.planAndRemind.exception.UserNotFoundException;
import com.example.planAndRemind.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/login"})
    public ResponseEntity<?> loginUser(@RequestBody AuthRequest authRequest){

        try{
            AuthResponse result = authService.loginUser(authRequest);
            return new ResponseEntity<>(result,HttpStatus.OK);
        }
        catch (UserNotFoundException e){
            return new ResponseEntity<>("No User found with this email and password combination!",
                    HttpStatus.BAD_REQUEST);
        }
        catch (UserException e){
            return new ResponseEntity<>("The registration wasn't confirmed for this email. Please visit" +
                    " the registration page to confirm your account!",
                    HttpStatus.BAD_REQUEST);
        }
        catch (InvalidPasswordException e){
            return new ResponseEntity<>("Invalid Password for this email!",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
