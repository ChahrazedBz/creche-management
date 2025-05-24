package com.creche.creche;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/educateur")
public class EducateurController {

    @Autowired
    private ChildRepository childRepo;

    @Autowired
    private AttendanceRepository attendanceRepo;

    @Autowired
    private ActivityRepository activityRepo;

    @Autowired
    private DevelopmentLogRepository developmentLogRepo;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "educateur/dashboard";
    }

    @GetMapping("/attendance/record")
    public String showAttendanceForm(Model model) {
        model.addAttribute("children", childRepo.findAll());
        model.addAttribute("attendance", new Attendance());
        model.addAttribute("today", LocalDate.now());
        return "educateur/attendance-record";
    }

    @PostMapping("/attendance/record")
    public String recordAttendance(@RequestParam Long childId, @RequestParam LocalDate date, @RequestParam LocalTime arrivalTime, @RequestParam(required = false) LocalTime departureTime, Model model) {
        try {
            Child child = childRepo.findById(childId).orElseThrow(() -> new IllegalArgumentException("Enfant non trouvé"));
            Attendance attendance = new Attendance();
            attendance.setChild(child);
            attendance.setDate(date);
            attendance.setArrivalTime(arrivalTime);
            attendance.setDepartureTime(departureTime);
            attendanceRepo.save(attendance);
            model.addAttribute("success", "Présence enregistrée avec succès");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
        }
        model.addAttribute("children", childRepo.findAll());
        model.addAttribute("attendance", new Attendance());
        model.addAttribute("today", LocalDate.now());
        return "educateur/attendance-record";
    }

    @GetMapping("/activity/manage")
    public String showActivityForm(Model model) {
        model.addAttribute("activity", new Activity());
        model.addAttribute("activities", activityRepo.findAll());
        return "educateur/activity-manage";
    }

    @PostMapping("/activity/manage")
    public String manageActivity(@ModelAttribute Activity activity, Model model) {
        try {
            activityRepo.save(activity);
            model.addAttribute("success", "Activité enregistrée avec succès");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
        }
        model.addAttribute("activity", new Activity());
        model.addAttribute("activities", activityRepo.findAll());
        return "educateur/activity-manage";
    }

    @GetMapping("/development/log")
    public String showDevelopmentForm(Model model) {
        model.addAttribute("children", childRepo.findAll());
        model.addAttribute("developmentLog", new DevelopmentLog());
        model.addAttribute("today", LocalDate.now());
        return "educateur/development-log";
    }

    @PostMapping("/development/log")
    public String logDevelopment(@ModelAttribute DevelopmentLog developmentLog, @RequestParam Long childId, Model model) {
        try {
            Child child = childRepo.findById(childId).orElseThrow(() -> new IllegalArgumentException("Enfant non trouvé"));
            developmentLog.setChild(child);
            developmentLog.setDate(LocalDate.now());
            developmentLogRepo.save(developmentLog);
            model.addAttribute("success", "Suivi enregistré avec succès");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
        }
        model.addAttribute("children", childRepo.findAll());
        model.addAttribute("developmentLog", new DevelopmentLog());
        model.addAttribute("today", LocalDate.now());
        return "educateur/development-log";
    }
}