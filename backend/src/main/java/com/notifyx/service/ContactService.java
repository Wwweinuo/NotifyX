package com.notifyx.service;

import com.notifyx.dto.ContactDTO;
import com.notifyx.entity.Contact;
import com.notifyx.enums.BirthdayType;
import com.notifyx.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    /**
     * 分页查询联系人，支持按姓名关键字搜索
     */
    public Page<Contact> getContacts(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return contactRepository.findByNameContaining(keyword.trim(), pageable);
        }
        return contactRepository.findAll(pageable);
    }

    /**
     * 根据 ID 查询
     */
    public Contact getContactById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("联系人不存在，ID: " + id));
    }

    /**
     * 新增联系人（从 DTO 转换）
     */
    public Contact createContact(ContactDTO dto) {
        Contact contact = new Contact();
        mapDtoToEntity(dto, contact);
        return contactRepository.save(contact);
    }

    /**
     * 修改联系人
     */
    public Contact updateContact(Long id, ContactDTO dto) {
        Contact contact = getContactById(id);
        mapDtoToEntity(dto, contact);
        return contactRepository.save(contact);
    }

    /**
     * 删除联系人
     */
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }

    /**
     * 查询所有启用的联系人
     */
    public List<Contact> getEnabledContacts() {
        return contactRepository.findByEnabledTrue();
    }

    private void mapDtoToEntity(ContactDTO dto, Contact contact) {
        contact.setName(dto.getName());
        contact.setRelationship(dto.getRelationship());
        contact.setBirthdayType(BirthdayType.valueOf(dto.getBirthdayType()));
        contact.setBirthdayDate(dto.getBirthdayDate());
        contact.setLunarMonth(dto.getLunarMonth());
        contact.setLunarDay(dto.getLunarDay());
        contact.setIsLeapMonth(dto.getIsLeapMonth() != null ? dto.getIsLeapMonth() : false);
        contact.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        contact.setNotifyEmail(dto.getNotifyEmail());
        contact.setRemark(dto.getRemark());
    }
}
