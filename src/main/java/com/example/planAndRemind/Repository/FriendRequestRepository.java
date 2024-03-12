package com.example.planAndRemind.Repository;


import com.example.planAndRemind.model.FriendRequestEntity;
import com.example.planAndRemind.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequestEntity, Long> {

    List<FriendRequestEntity> findAll();
    FriendRequestEntity save(FriendRequestEntity friendRequestEntity);
    void deleteById(Long id);
    Optional<FriendRequestEntity> findById(Long id);

    Optional<FriendRequestEntity> findByFirstUserAndSecondUser(UserEntity firstUser, UserEntity secondUser);

    List<FriendRequestEntity> findByFirstUserOrSecondUser(UserEntity firstUser, UserEntity receiver);


    @Query("SELECT f FROM FriendRequestEntity f WHERE (f.firstUser = :user OR f.secondUser = :user) AND f.status = :status")
    List<FriendRequestEntity> findByFirstUserOrSecondUserAndStatus(@Param("user") UserEntity user, @Param("status") String status);


    @Query("SELECT fr FROM FriendRequestEntity fr WHERE (fr.firstUser = :user OR fr.secondUser = :user) AND fr.status LIKE %:status%")
    List<FriendRequestEntity> findByFirstUserOrSecondUserAndStatusContaining(@Param("user") UserEntity user, @Param("status") String status);
//    List<FriendRequestEntity> findByFirstUserOrSecondUserAndStatusContaining(UserEntity firstUser,UserEntity secondUser, String status);
}
