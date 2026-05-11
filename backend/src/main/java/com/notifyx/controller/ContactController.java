package com.notifyx.controller;

import com.notifyx.dto.ApiResponse;
import com.notifyx.dto.ContactDTO;
import com.notifyx.entity.Contact;
import com.notifyx.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public ApiResponse<Page<Contact>> getContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<Contact> contacts = contactService.getContacts(keyword, PageRequest.of(page, size));
        return ApiResponse.success(contacts);
    }

    @PostMapping
    public ApiResponse<Contact> createContact(@RequestBody ContactDTO dto) {
        Contact contact = contactService.createContact(dto);
        return ApiResponse.success(contact);
    }

    @PutMapping("/{id}")
    public ApiResponse<Contact> updateContact(@PathVariable Long id, @RequestBody ContactDTO dto) {
        Contact contact = contactService.updateContact(id, dto);
        return ApiResponse.success(contact);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ApiResponse.success();
    }
}
