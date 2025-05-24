package com.creche.creche;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.time.LocalDate;

@Controller
@RequestMapping("/cuisine")
public class CuisineController {

    @Autowired
    private MealRepository mealRepo;

    @Autowired
    private ChildRepository childRepo;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "cuisine/dashboard";
    }

    @GetMapping("/meal/manage")
    public String showMealForm(Model model) {
        try {
            model.addAttribute("meal", new Meal());
            model.addAttribute("children", childRepo.findAll());
            model.addAttribute("today", LocalDate.now());
            System.out.println("Loaded meal form with " + childRepo.findAll().size() + " children");
            return "cuisine/meal-manage";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement: " + e.getMessage());
            return "cuisine/meal-manage";
        }
    }

    @PostMapping("/meal/manage")
    @Transactional
    public String manageMeal(@ModelAttribute Meal meal, Model model) {
        try {
            mealRepo.save(meal);
            model.addAttribute("success", "Repas enregistré avec succès");
            System.out.println("Saved meal: " + meal.getMenuDescription());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
        model.addAttribute("meal", new Meal());
        model.addAttribute("children", childRepo.findAll());
        model.addAttribute("today", LocalDate.now());
        return "cuisine/meal-manage";
    }

    @GetMapping("/meal/list")
    public String listMeals(Model model) {
        model.addAttribute("meals", mealRepo.findAll());
        return "cuisine/meal-list";
    }
}