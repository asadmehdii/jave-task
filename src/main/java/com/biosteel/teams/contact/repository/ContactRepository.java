package com.biosteel.teams.contact.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.biosteel.teams.contact.model.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findByDeletedAtIsNull();

    Page<Contact> findByDeletedAtIsNull(Pageable pageable);

    @Query("SELECT c FROM Contact c WHERE c.deletedAt IS NULL AND " +
            "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Contact> findBySearchQuery(String query, Pageable pageable);
}