package com.example.DentistryManagement.service;

import com.example.DentistryManagement.core.user.Staff;
import com.example.DentistryManagement.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;

    public Staff findStaffByMail(String mail) {
        Staff staff;
        try {
            staff = staffRepository.findStaffByUserMail(mail);
            return staff;
        } catch (Error error) {
            throw error;
        }
    }

    public Staff findStaffById(String staffID) {
        Staff staff;
        try {
            staff = staffRepository.findStaffByStaffID(staffID);
            return staff;
        } catch (Error error) {
            throw error;
        }
    }
}
