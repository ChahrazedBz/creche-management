package com.creche.creche;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ChildRepository childRepo;

    @Autowired
    private InscriptionRepository inscriptionRepo;

    @Autowired
    private AttendanceRepository attendanceRepo;

    @Autowired
    private ActivityRepository activityRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/child/register")
    public String showChildRegisterForm(Model model) {
        model.addAttribute("child", new Child());
        return "admin/child-register";
    }

    @PostMapping("/child/register")
    @Transactional
    public String registerChild(@ModelAttribute Child child, @RequestParam String parentEmail, @RequestParam String parentPassword, Model model) {
        try {
            User parent = userRepo.findByUsername(parentEmail);
            if (parent == null) {
                parent = new User();
                parent.setUsername(parentEmail);
                parent.setPassword(passwordEncoder.encode(parentPassword));
                parent.setRole("PARENT");
                parent.setName(child.getParentName());
                parent.setPhoneNumber(child.getParentPhone());
                userRepo.save(parent);
                System.out.println("Created new parent: " + parentEmail);
            }

            Child newChild = new Child();
            newChild.setName(child.getName());
            newChild.setBirthDate(child.getBirthDate());
            newChild.setAllergies(child.getAllergies());
            newChild.setSpecialNeeds(child.getSpecialNeeds());
            newChild.setParentId(parent.getId());
            newChild.setParentPhone(child.getParentPhone());
            newChild.setParentEmail(parentEmail);
            newChild.setParentName(parent.getName()); // Set transient parentName
            newChild.setParentPhoneNumber(parent.getPhoneNumber()); // Set transient parentPhoneNumber
            newChild.setApproved(false); // Ensure new child is not approved
            childRepo.save(newChild);

            model.addAttribute("success", "Enfant enregistré avec succès");
            System.out.println("Registered child: " + newChild.getName());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
        model.addAttribute("child", new Child());
        return "admin/child-register";
    }

    @GetMapping("/child/list")
    public String listChildren(Model model) {
        try {
            List<Child> allChildren = childRepo.findAll();
            List<Child> approvedChildren = new ArrayList<>();
            for (Child child : allChildren) {
                if (child != null && child.getId() != null && child.isApproved()) {
                    // Set transient fields for display
                    if (child.getParentId() != null) {
                        Optional<User> parentOpt = userRepo.findById(child.getParentId());
                        parentOpt.ifPresent(parent -> {
                            child.setParentName(parent.getName());
                            child.setParentPhoneNumber(parent.getPhoneNumber());
                        });
                    }
                    approvedChildren.add(child);
                    System.out.println("Child ID: " + child.getId() + ", Name: " + child.getName() + ", Approved: " + child.isApproved());
                }
            }
            model.addAttribute("children", approvedChildren);
            System.out.println("Loaded " + approvedChildren.size() + " approved children for list");
            return "admin/child-list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement des enfants: " + e.getMessage());
            return "admin/child-list";
        }
    }

    @GetMapping("/child/edit/{id}")
    public String showEditChildForm(@PathVariable Long id, Model model) {
        try {
            Optional<Child> childOpt = childRepo.findById(id);
            if (!childOpt.isPresent()) {
                model.addAttribute("error", "Enfant avec ID " + id + " non trouvé");
                model.addAttribute("children", childRepo.findAll());
                return "admin/child-list";
            }
            Child child = childOpt.get();
            String parentEmail = child.getParentEmail();
            if (child.getParentId() != null) {
                Optional<User> parentOpt = userRepo.findById(child.getParentId());
                if (parentOpt.isPresent()) {
                    parentEmail = parentOpt.get().getUsername();
                    child.setParentName(parentOpt.get().getName());
                    child.setParentPhoneNumber(parentOpt.get().getPhoneNumber());
                }
            }
            model.addAttribute("child", child);
            model.addAttribute("parentEmail", parentEmail != null ? parentEmail : "");
            System.out.println("Loaded child for edit: ID=" + id + ", Name=" + child.getName());
            return "admin/child-edit";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement de l'enfant: " + e.getMessage());
            model.addAttribute("children", childRepo.findAll());
            return "admin/child-list";
        }
    }

    @PostMapping("/child/edit/{id}")
    @Transactional
    public String editChild(@PathVariable Long id, @ModelAttribute Child child, @RequestParam String parentEmail, Model model) {
        try {
            Optional<Child> childOpt = childRepo.findById(id);
            if (!childOpt.isPresent()) {
                model.addAttribute("error", "Enfant avec ID " + id + " non trouvé");
                model.addAttribute("children", childRepo.findAll());
                return "admin/child-list";
            }
            Child existingChild = childOpt.get();
            User parent = userRepo.findByUsername(parentEmail);
            if (parent == null) {
                model.addAttribute("error", "Parent non trouvé avec l'email: " + parentEmail);
                model.addAttribute("child", existingChild);
                model.addAttribute("parentEmail", parentEmail);
                return "admin/child-edit";
            }
            existingChild.setName(child.getName() != null ? child.getName() : existingChild.getName());
            existingChild.setBirthDate(child.getBirthDate() != null ? child.getBirthDate() : existingChild.getBirthDate());
            existingChild.setAllergies(child.getAllergies() != null ? child.getAllergies() : existingChild.getAllergies());
            existingChild.setSpecialNeeds(child.getSpecialNeeds() != null ? child.getSpecialNeeds() : existingChild.getSpecialNeeds());
            existingChild.setParentId(parent.getId());
            existingChild.setParentPhone(child.getParentPhone() != null ? child.getParentPhone() : existingChild.getParentPhone());
            existingChild.setParentEmail(parentEmail);
            existingChild.setParentName(parent.getName()); // Set transient parentName
            existingChild.setParentPhoneNumber(parent.getPhoneNumber()); // Set transient parentPhoneNumber
            existingChild.setApproved(existingChild.isApproved()); // Preserve approved status
            childRepo.save(existingChild);
            model.addAttribute("success", "Enfant mis à jour avec succès");
            System.out.println("Updated child: ID=" + id + ", Name=" + existingChild.getName());
            return "redirect:/admin/child/list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
            model.addAttribute("child", child);
            model.addAttribute("parentEmail", parentEmail);
            return "admin/child-edit";
        }
    }

    @PostMapping("/child/delete/{id}")
    @Transactional
    public String deleteChild(@PathVariable Long id, Model model) {
        try {
            Optional<Child> childOpt = childRepo.findById(id);
            if (!childOpt.isPresent()) {
                model.addAttribute("error", "Enfant avec ID " + id + " non trouvé");
                model.addAttribute("children", childRepo.findAll());
                return "admin/child-list";
            }
            attendanceRepo.deleteByChildId(id);
            inscriptionRepo.deleteByChildId(id);
            childRepo.deleteById(id);
            childRepo.flush();
            System.out.println("Deleted child successfully: ID=" + id);
            return "redirect:/admin/child/list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
            model.addAttribute("children", childRepo.findAll());
            return "admin/child-list";
        }
    }

    @GetMapping("/inscription/list")
    public String listInscriptions(Model model) {
        try {
            List<Inscription> inscriptions = inscriptionRepo.findByStatus("PENDING");
            model.addAttribute("inscriptions", inscriptions);
            System.out.println("Loaded " + inscriptions.size() + " pending inscriptions for list");
            return "admin/inscription-list";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement des inscriptions: " + e.getMessage());
            return "admin/inscription-list";
        }
    }

    @GetMapping("/inscription/validate")
    public String showValidateInscriptions(Model model) {
        try {
            List<Inscription> inscriptions = inscriptionRepo.findAll();
            model.addAttribute("inscriptions", inscriptions);
            System.out.println("Loaded " + inscriptions.size() + " inscriptions for validation");
            return "admin/inscription-validate";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement des inscriptions: " + e.getMessage());
            return "admin/inscription-validate";
        }
    }

    @PostMapping("/inscription/validate/{id}")
    @Transactional
    public String validateInscription(@PathVariable Long id, @RequestParam(value = "status", defaultValue = "") String status, Model model) {
        try {
            if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
                model.addAttribute("error", "Statut invalide: " + status);
                model.addAttribute("inscriptions", inscriptionRepo.findAll());
                return "admin/inscription-validate";
            }
            Optional<Inscription> inscriptionOpt = inscriptionRepo.findById(id);
            if (!inscriptionOpt.isPresent()) {
                model.addAttribute("error", "Inscription avec ID " + id + " non trouvée");
                model.addAttribute("inscriptions", inscriptionRepo.findAll());
                return "admin/inscription-validate";
            }
            Inscription inscription = inscriptionOpt.get();
            inscription.setStatus(status);
            if ("APPROVED".equals(status)) {
                Child child = inscription.getChild();
                if (child != null) {
                    child.setApproved(true);
                    childRepo.save(child);
                    System.out.println("Set child ID " + child.getId() + " as approved");
                } else {
                    System.err.println("Child is null for inscription ID: " + id);
                }
            }
            inscriptionRepo.save(inscription);
            inscriptionRepo.delete(inscription);
            model.addAttribute("success", "Inscription " + status.toLowerCase() + " et supprimée");
            System.out.println("Processed and deleted inscription ID: " + id + " with status: " + status);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        model.addAttribute("inscriptions", inscriptionRepo.findAll());
        return "admin/inscription-validate";
    }

    @GetMapping("/attendance/report")
    public String showAttendanceReportForm(Model model) {
        model.addAttribute("date", LocalDate.now());
        return "admin/attendance-report";
    }

    @PostMapping("/attendance/report")
    @Transactional
    public String generateAttendanceReport(@RequestParam(value = "date", required = false) String dateStr, Model model) {
        try {
            LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
            List<Attendance> presentChildren = attendanceRepo.findByDate(date);
            List<Child> allChildren = childRepo.findAll();
            List<Child> absentChildren = allChildren.stream()
                .filter(child -> child.getId() != null && presentChildren.stream()
                    .noneMatch(attendance -> attendance.getChild() != null && attendance.getChild().getId().equals(child.getId())))
                .toList();
            model.addAttribute("date", date);
            model.addAttribute("presentChildren", presentChildren);
            model.addAttribute("absentChildren", absentChildren);
            System.out.println("Generated attendance report for " + date);
            return "admin/attendance-report";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la génération du rapport: " + e.getMessage());
            model.addAttribute("date", LocalDate.now());
            return "admin/attendance-report";
        }
    }

    @GetMapping("/activity/view")
    public String viewActivities(Model model) {
        try {
            List<Activity> activities = activityRepo.findAll();
            model.addAttribute("activities", activities);
            System.out.println("Loaded " + activities.size() + " activities for view");
            return "admin/activity-view";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement des activités: " + e.getMessage());
            return "admin/activity-view";
        }
    }
}