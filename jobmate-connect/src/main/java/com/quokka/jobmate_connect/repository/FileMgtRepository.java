package com.quokka.jobmate_connect.repository;

import com.quokka.jobmate_connect.constant.FileTypeStatus;
import com.quokka.jobmate_connect.entity.FileMgmt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMgtRepository extends JpaRepository<FileMgmt, UUID> {
    Optional<FileMgmt> findByOwnerIdAndType(UUID ownerId, FileTypeStatus type);

    List<FileMgmt> findAllByOwnerId(UUID ownerId);


}
