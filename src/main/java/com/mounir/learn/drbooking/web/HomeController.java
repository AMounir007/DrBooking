package com.mounir.learn.drbooking.web;

import com.mounir.learn.drbooking.domain.Plan;
import com.mounir.learn.drbooking.service.BookingException;
import com.mounir.learn.drbooking.service.ProviderService;
import com.mounir.learn.drbooking.web.dto.RegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.ZoneId;
import java.util.List;

@Controller
public class HomeController {

    private final ProviderService providerService;

    public HomeController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("plans", Plan.values());
        return "home";
    }

    @GetMapping("/pricing")
    public String pricing(Model model) {
        model.addAttribute("plans", Plan.values());
        return "pricing";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationRequest());
        model.addAttribute("zones", zones());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationRequest form,
                           BindingResult binding, Model model) {
        if (!binding.hasErrors()) {
            try {
                providerService.register(form);
                return "redirect:/login?registered";
            } catch (BookingException ex) {
                binding.reject("registration", ex.getMessage());
            }
        }
        model.addAttribute("zones", zones());
        return "register";
    }

    private List<String> zones() {
        return List.of("UTC", "Africa/Cairo", "Europe/London", "Europe/Paris", "Europe/Berlin",
                        "Asia/Dubai", "Asia/Riyadh", "Asia/Kolkata", "America/New_York", "America/Los_Angeles")
                .stream().filter(z -> ZoneId.getAvailableZoneIds().contains(z)).toList();
    }
}
