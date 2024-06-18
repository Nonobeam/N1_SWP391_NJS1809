package com.example.DentistryManagement.controller;

import com.example.DentistryManagement.DTO.UserAppointDTO;
import com.example.DentistryManagement.DTO.UserDTO;
import com.example.DentistryManagement.DTO.AppointmentDTO;
import com.example.DentistryManagement.core.dentistry.Appointment;
import com.example.DentistryManagement.core.mail.Notification;
import com.example.DentistryManagement.core.user.Client;
import com.example.DentistryManagement.core.user.Dentist;
import com.example.DentistryManagement.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/dentist")
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "Dentist API")
public class DentistController {
    private final UserService userService;
    private final DentistService dentistService;
    private final AppointmentService appointmentService;
    private final NotificationService notificationService;


    @Operation(summary = "Dentist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully"),
            @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/appointment-today")
    public ResponseEntity<Optional<List<AppointmentDTO>>> appointmentList() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String mail= authentication.getName();
            Optional<List<Appointment>> appointlist = appointmentService.findAppointByDentist(mail);
            if (appointlist.isPresent() && !appointlist.get().isEmpty()) {
                List<AppointmentDTO> applist = appointlist.get().stream()
                        .map(appointmentriu -> {
                            AppointmentDTO appointment = new AppointmentDTO();
                            appointment.setServices(appointmentriu.getServices());
                            appointment.setStatus(appointmentriu.getStatus());
                            appointment.setTimeSlot(appointmentriu.getTimeSlot().getStartTime());
                            if (appointmentriu.getStaff()!=null){
                                if(appointmentriu.getUser() != null){
                                    appointment.setUser(appointmentriu.getUser());
                                } else {
                                    appointment.setDependent(appointmentriu.getDependent());
                                }
                            }else {
                                if(appointmentriu.getDependent()!=null){
                                    appointment.setDependent(appointmentriu.getDependent());
                                }else appointment.setUser(appointmentriu.getUser());
                            }

                            return appointment;
                        })
                        .collect(Collectors.toList());

                return ResponseEntity.ok(Optional.of(applist));
            } else {
                // lỗi 403
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Optional.empty());
        }
    }


    @Operation(summary = "Dentist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully"),
            @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/reminder")
    public ResponseEntity<Notification> reminderNotice(@RequestBody Notification notification) {
        Notification insertedNotification;
        try {
            if (notification != null){
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String mail= authentication.getName();
                Client client = userService.findClientByMail(mail);
                Dentist dentist = dentistService.findDentistByID(client.getUserID());
                notification.setDentist(dentist);
                LocalDate currentDate = LocalDate.now();
                LocalTime currentTime = LocalTime.now();
                notification.setDate(Date.valueOf(currentDate));
                notification.setTime(Time.valueOf(currentTime));
                notification.setStatus(0);

                insertedNotification = notificationService.insertNotification(notification);
                return ResponseEntity.ok(insertedNotification);
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    @Operation(summary = "Dentist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully"),
            @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/appointment-list")
    public ResponseEntity<Optional<List<Appointment>>> appointmentHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String mail= authentication.getName();
            Optional<List<Appointment>> appointmentlist = appointmentService.findAllAppointByDentist(mail);
            return ResponseEntity.ok(appointmentlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    @Operation(summary = "Dentist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully"),
            @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Error")

    })
    @GetMapping("/customer/{id}")
    public ResponseEntity<?> findAllCustomerByDentist(@PathVariable("id") String id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String mail= authentication.getName();
                UserDTO userDTO =new UserDTO();
                Client client= userService.userInfo(id);
                userDTO.setFirstName(client.getFirstName());
                userDTO.setPhone(client.getPhone());
                userDTO.setMail(client.getMail());
                userDTO.setLastName(client.getLastName());
                userDTO.setBirthday(client.getBirthday());
                Optional<List<Appointment>> appointment=appointmentService.customerAppointfollowdentist(id,mail);
                UserAppointDTO userAppointDTO = new UserAppointDTO();
                userAppointDTO.setUserDTO(userDTO);
                userAppointDTO.setAppointment(appointment);
                return ResponseEntity.ok(userAppointDTO);
             } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


//    @Operation(summary = "Dentist")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successfully"),
//            @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
//            @ApiResponse(responseCode = "404", description = "Not found"),
//            @ApiResponse(responseCode = "500", description = "Error")
//
//    })
//    @GetMapping
//    public ResponseEntity<List<Appointment>> getAppointmentsForDate(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
//    ) {
//        List<Appointment> appointments = appointmentService.getAppointmentsForDate(date);
//        return ResponseEntity.ok(appointments);
//    }


    @Operation(summary = "Dentist")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully"),
        @ApiResponse(responseCode = "403", description = "Don't have permission to do this"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/{status}")
    public ResponseEntity<Appointment> setAppointmentStatus(@PathVariable("status") int status, Appointment appointment) {
        try {
            appointment.setStatus(status);
            return ResponseEntity.ok(appointmentService.AppointmentUpdate(appointment));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
