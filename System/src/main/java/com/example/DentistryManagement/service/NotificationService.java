package com.example.DentistryManagement.service;

import com.example.DentistryManagement.core.mail.Notification;
import com.example.DentistryManagement.core.user.Dentist;
import com.example.DentistryManagement.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    public void sendMail(String mail, Notification notificationStructure) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromMail);
        simpleMailMessage.setTo(mail);
        simpleMailMessage.setText(notificationStructure.getMessage());

        mailSender.send(simpleMailMessage);
    }

    public Optional<List<Notification>> receiveNotice(String staffmail) {
        try {
            return notificationRepository.getNotificationByDentist_StaffUserMail(staffmail);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error occurred while fetching all users: " + e.getMessage(), e);
        }
    }

    public Notification insertNotification(Notification notification) {
        try {
            return notificationRepository.save(notification);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error occurred while create notification: " + e.getMessage(), e);
        }
    }

    public List<Notification> findNotificationsByDentist(Dentist dentist) {
        try {
            return notificationRepository.getNotificationsByDentistAndStatus(dentist, 0);
        } catch (Error e) {
            throw e;
        }
    }

    public Optional<Notification> findNotificationByIDAndStatus(String notificationID, int status) {
        try {
            return notificationRepository.getNotificationByNotificationIDAndStatus(notificationID, status);
        } catch (Error e) {
            throw e;
        }
    }

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

}
