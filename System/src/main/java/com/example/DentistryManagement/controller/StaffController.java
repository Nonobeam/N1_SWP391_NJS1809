package com.example.DentistryManagement.controller;


import com.example.DentistryManagement.core.dentistry.Clinic;
import com.example.DentistryManagement.core.dentistry.Services;
import com.example.DentistryManagement.core.mail.Mail;
import com.example.DentistryManagement.core.mail.Notification;
import com.example.DentistryManagement.core.user.Client;
import com.example.DentistryManagement.core.user.Dentist;
import com.example.DentistryManagement.core.user.Staff;
import com.example.DentistryManagement.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.weaver.ast.Not;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequestMapping("/api/v1/staff")
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "Staff API")
public class StaffController {
    private final ClinicService clinicService;
    private final MailService emailService;
    private final UserService userService;
    private final StaffService staffService;
    private final ServiceService serviceService;
    private final DentistService dentistService;
    private final AppointmentService appointmentService;
    private final NotificationService notificationService;
    private final DentistScheduleService dentistScheduleService;
    private final Logger LOGGER = LogManager.getLogger(UserController.class);


//---------------------------GET ALL CUSTOMERS, SERVICES, CLINICS, NOTIFICATIONS IN THE WORKING CLINIC---------------------------


//    @Operation(summary = "All Services in System")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successfully"),
//            @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
//            @ApiResponse(responseCode = "404", description = "Not found"),
//            @ApiResponse(responseCode = "500", description = "Error")
//
//    })
//    @GetMapping("/all-services")
//    public ResponseEntity<List<Services>> getAllServices() {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String mail= authentication.getName();
//            Staff staff = staffService.findStaffByMail(mail);
//            Clinic clinic = staff.getClinic();
//
//            return ResponseEntity.ok(serviceService.findServicesByClinic(clinic.getClinicID()));
//        } catch (Error error) {
//            return ResponseEntity.badRequest().build();
//        }
//    }


    @Operation(summary = "All Dentists manage by a Staff")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully"),
            @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Error")

    })
    @GetMapping("/dentist/all")
    public ResponseEntity<List<Dentist>> getAllDentists() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String mail = authentication.getName();

        Staff staff;
        List<Dentist> dentists;
        try {
            staff = staffService.findStaffByMail(mail);
            dentists = dentistService.findDentistByStaff(staff);
            return ResponseEntity.ok(dentists);
        } catch (Error error) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "All Notifications in the clinic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully"),
            @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Error")

    })
    @GetMapping("/all-notification")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String mail = authentication.getName();

        Staff staff;
        List<Dentist> dentists;
        try {
            staff = staffService.findStaffByMail(mail);
            dentists = dentistService.findDentistByStaff(staff);
            for (Dentist dentist : dentists) {
                notifications.addAll(notificationService.findNotificationsByDentist(dentist));
                LOGGER.info("Dentist: {}", dentist);
                LOGGER.info("Notifications{}", notificationService.findNotificationsByDentist(dentist));
            }
            return ResponseEntity.ok(notifications);
        } catch (Error error) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "All Customers in the clinic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully"),
            @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Error")

    })
    @GetMapping("/all-customer")
    public ResponseEntity<List<Client>> getAllCustomers() {
        List<Client> clients;
        try {
            clients = userService.findAllCustomer();
            return ResponseEntity.ok(clients);
        } catch (Error error) {
            return ResponseEntity.badRequest().build();
        }
    }


//---------------------------MANAGE DENTIST---------------------------


    @Operation(summary = "Update service for Dentist in System")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully"),
            @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Error")

    })
    @GetMapping("/set-service/{dentistID}")
    public ResponseEntity<Dentist> updateDentistService(@PathVariable String dentistID, @RequestParam String serviceID) {
        Dentist dentist;
        Services service;
        try {
            dentist = dentistService.findDentistByID(dentistID);
            service = serviceService.findServiceByID(serviceID);
            dentist.getServicesList().add(service);

            return ResponseEntity.ok(dentist);
        } catch (Error error) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "Set Dentist Schedule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully set the schedule"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/set-schedule")
    public ResponseEntity<?> setDentistSchedule(@RequestParam String dentistID,
                                                @RequestParam LocalDate startDate,
                                                @RequestParam LocalDate endDate,
                                                @RequestParam String timeSlotID,
                                                @RequestParam String clinicID,
                                                @RequestParam String serviceID) {
        try {
            dentistScheduleService.setDentistSchedule(dentistID, startDate, endDate, timeSlotID, clinicID, serviceID);
            return ResponseEntity.ok("Schedule set successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @Operation(summary = "Delete Dentist Schedule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the schedule"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/delete-schedule")
    public ResponseEntity<?> deleteDentistSchedule(@RequestParam String dentistID,
                                                   @RequestParam LocalDate workDate) {
        try {
            dentistScheduleService.deleteDentistSchedule(dentistID, workDate);
            return ResponseEntity.ok("Schedule deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Send mail for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the schedule"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{notificationID}/send-email")
    public ResponseEntity<?> sendEmail(@PathVariable String notificationID,
                            @RequestParam String mail,
                            @RequestParam String subject,
                            @RequestParam String text) {
        Optional<Notification> optionalNotification = notificationService.findNotificationByIDAndStatus(notificationID, 0);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();

            emailService.sendSimpleMessage(mail, subject, text);
            notification.setStatus(1);

            notificationService.save(notification);
            return ResponseEntity.ok("Mail send successfully");
        } else {
            throw new IllegalArgumentException("Notification not found with ID: " + notificationID);
        }
    }
}