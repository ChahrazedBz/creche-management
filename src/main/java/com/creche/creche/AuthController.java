package com.creche.creche;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ChildRepository childRepo;

    @Autowired
    private InscriptionRepository inscriptionRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, @RequestParam String role, Model model) {
        try {
            if (userRepo.findByUsername(user.getUsername()) != null) {
                model.addAttribute("error", "Nom d'utilisateur déjà pris");
                return "register";
            }
            if ("ADMIN".equals(role)) {
                model.addAttribute("error", "Rôle ADMIN non autorisé");
                return "register";
            }
            user.setRole(role);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepo.save(user);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'inscription: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst()
                .orElse("ROLE_USER")
                .replace("ROLE_", "");
        switch (role) {
            case "ADMIN":
                return "redirect:/admin/dashboard";
            case "EDUCATEUR":
                return "redirect:/educateur/dashboard";
            case "CUISINE":
                return "redirect:/cuisine/dashboard";
            case "PARENT":
                return "redirect:/parent/dashboard";
            default:
                return "redirect:/login";
        }
    }

    @GetMapping("/inscription/submit")
    public String showInscriptionForm(Model model) {
        model.addAttribute("child", new Child());
        return "inscription-submit";
    }

    @PostMapping("/inscription/submit")
    public String submitInscription(@ModelAttribute Child child, Model model, Authentication auth) {
        try {
            String username = auth.getName();
            User parent = userRepo.findByUsername(username);
            if (parent == null || !"PARENT".equals(parent.getRole())) {
                model.addAttribute("error", "Utilisateur non autorisé");
                return "inscription-submit";
            }
            child.setParentId(parent.getId());
            Child savedChild = childRepo.save(child);
            Inscription inscription = new Inscription();
            inscription.setChild(savedChild);
            inscription.setStatus("PENDING");
            inscriptionRepo.save(inscription);
            model.addAttribute("success", "Inscription soumise avec succès");
            return "inscription-submit";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la soumission: " + e.getMessage());
            return "inscription-submit";
        }
    }

    @GetMapping("/test")
    public String test() {
        return "register";
    }
}