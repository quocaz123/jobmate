package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;


@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    void deleteAllByExpiryTimeBefore(Date date);
}
