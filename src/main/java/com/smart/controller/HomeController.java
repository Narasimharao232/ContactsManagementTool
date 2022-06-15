package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.helper.Message;
import com.smart.model.User;
import com.smart.repository.UserRepository;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model model) {
//		System.out.println(userRepo.findByEmail("mike@gmail.com"));
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
		
	}	
	
	@GetMapping("/signup")
	public String singup(Model model) {
		model.addAttribute("title", "Model - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult  results, @RequestParam(value ="agreement",defaultValue="false") boolean agreement, Model model,HttpSession session) {
//		System.out.println(results);
		
		
		try {
			if(!agreement) {
				System.out.println("Terms and conditons not accepted");
				throw new Exception("Terms and conditons not accepted");
			}
			
			if(results.hasErrors()) {
				System.out.println(results);
				model.addAttribute("user", user);
				return "signup";
			}
			
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println(user);
			System.out.println(agreement);
			
			User result = userRepo.save(user);
			model.addAttribute("user", new User());
			
			session.setAttribute("message",new Message("Successfully Registered","alert-success"));
			return "signup";
			
		}catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message",new Message("Something Went Wrong!!!"+ e.getMessage(),"alert-danger"));
			return "signup";
		}
		
		
		
	}
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login - Smart Contact Manager");
		return "login";
	}
}
