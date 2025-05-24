package com.creche.creche;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/parent")
public class ParentController {

    @Autowired
    private ChildRepository childRepo;

    @Autowired
    private InscriptionRepository inscriptionRepo;

    @Autowired
    private AttendanceRepository attendanceRepo;

    @Autowired
    private MealRepository mealRepo;

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "parent/dashboard";
    }

    @GetMapping("/inscription/submit")
    public String showInscriptionForm(Model model) {
        model.addAttribute("child", new Child());
        return "parent/inscription-submit";
    }

    @PostMapping("/inscription/submit")
    @Transactional
    public String submitInscription(@ModelAttribute Child child, Model model, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                model.addAttribute("error", "Utilisateur non authentifié");
                model.addAttribute("child", new Child());
                return "parent/inscription-submit";
            }
            String username = authentication.getName();
            if (username == null) {
                model.addAttribute("error", "Nom d'utilisateur non trouvé");
                model.addAttribute("child", new Child());
                return "parent/inscription-submit";
            }
            System.out.println("Submitting inscription for user: " + username);
            User parent = userRepo.findByUsername(username);
            if (parent == null) {
                model.addAttribute("error", "Parent non trouvé pour l'utilisateur: " + username);
                model.addAttribute("child", new Child());
                return "parent/inscription-submit";
            }
            // Update parent's name and phone number
            if (child.getParentName() != null && !child.getParentName().isEmpty()) {
                parent.setName(child.getParentName());
            }
            if (child.getParentPhone() != null && !child.getParentPhone().isEmpty()) {
                parent.setPhoneNumber(child.getParentPhone());
            }
            userRepo.save(parent);

            // Create new child
            Child newChild = new Child();
            newChild.setName(child.getName());
            newChild.setBirthDate(child.getBirthDate());
            newChild.setAllergies(child.getAllergies());
            newChild.setSpecialNeeds(child.getSpecialNeeds());
            newChild.setParentId(parent.getId());
            newChild.setParentPhone(child.getParentPhone());
            newChild.setParentEmail(child.getParentEmail());
            newChild.setParentName(parent.getName()); // Set transient parentName
            newChild.setParentPhoneNumber(parent.getPhoneNumber()); // Set transient parentPhoneNumber
            newChild.setApproved(false); // Explicitly set approved to false
            childRepo.save(newChild);

            // Create inscription
            Inscription inscription = new Inscription();
            inscription.setChild(newChild);
            inscription.setStatus("PENDING");
            inscription.setAge(newChild.getBirthDate() != null ?
                java.time.Period.between(newChild.getBirthDate(), LocalDate.now()).getYears() : 0);
            inscriptionRepo.save(inscription);

            model.addAttribute("success", "Inscription soumise avec succès");
            System.out.println("Inscription saved for child: " + newChild.getName());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la soumission: " + e.getMessage());
        }
        model.addAttribute("child", new Child());
        return "parent/inscription-submit";
    }

    @GetMapping("/attendance")
    public String viewAttendance(Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            User parent = userRepo.findByUsername(username);
            if (parent == null) {
                model.addAttribute("error", "Parent non trouvé");
                return "parent/attendance";
            }
            model.addAttribute("attendances", attendanceRepo.findByChildParentId(parent.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement des présences: " + e.getMessage());
        }
        return "parent/attendance";
    }

    @GetMapping("/meal/list")
    public String viewMeals(Model model) {
        try {
            model.addAttribute("meals", mealRepo.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement des repas: " + e.getMessage());
        }
        return "parent/meal-list";
    }
}