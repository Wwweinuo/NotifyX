package com.notifyx.repository;

import com.notifyx.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Page<Contact> findByNameContaining(String keyword, Pageable pageable);

    List<Contact> findByEnabledTrue();
}
