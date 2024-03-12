package com.example.planAndRemind.Repository;


import com.example.planAndRemind.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    List<UserEntity> findAll();
    UserEntity save(UserEntity userEntity);
    void deleteById(Long id);
    Optional<UserEntity> findById(Long id);
    Optional<UserEntity> findByEmail(String email);

    //JPQL
    @Query("SELECT u FROM UserEntity u WHERE (LOWER(u.firstName) LIKE %:firstName% OR LOWER(u.lastName) LIKE %:lastName%) AND u.accountConfirmation = :accountConfirmation")
    List<UserEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseAndAccountConfirmation(String firstName,
                                                                                                             String lastName,
                                                                                                             String accountConfirmation);
}
