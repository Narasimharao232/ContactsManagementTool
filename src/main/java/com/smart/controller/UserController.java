package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.helper.Message;
import com.smart.model.Contact;
import com.smart.model.User;
import com.smart.repository.ContactRepository;
import com.smart.repository.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ContactRepository contactRepo;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String name= principal.getName();
		User user = userRepo.findByEmail(name);
		System.out.println(user);
		model.addAttribute("user", user);
		System.out.println(name);
	}
	
	@GetMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "User Dashboard - Smart Contact Manager");
		return "normal/user_dashboard";
		
	}	
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact - Smart Contact Manager");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
		try {
			String name=principal.getName();		
			User user= userRepo.findByEmail(name);
			
			if(file.isEmpty()) {
				 System.out.print("File is empty");
				 contact.setImage("contact.png");
			}else {
				contact.setImage(file.getOriginalFilename());
				File saveFile =new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator+ file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
			}
			
			
			
			contact.setUser(user);
			user.getContacts().add(contact);
			userRepo.save(user);
			System.out.println(contact);
			
			
			session.setAttribute("message",new Message("successfully added","success"));
			
		}catch(Exception e) {
			System.out.println("Error "+e.getMessage() );
			e.printStackTrace();
			session.setAttribute("message",new Message("Failed to add","danger"));
		}
		
		return "normal/add_contact_form";
	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model,Principal principal) {
		model.addAttribute("title", "View Contacts - Smart Contact Manager");
		String name=principal.getName();		
		User user= userRepo.findByEmail(name);
//		List<Contact> contacts = user.getContacts();
		
		Pageable pageable= PageRequest.of(page, 5);
		Page<Contact> contacts = contactRepo.findContactsByUser(user.getId(),pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	@GetMapping("/contact/{contactId}")
	public String showOneContact(@PathVariable("contactId") int id,Model model,Principal principal) {
		String name=principal.getName();		
		User user= userRepo.findByEmail(name);
		Optional<Contact> optionalContact=contactRepo.findById(id);
		Contact contact=optionalContact.get();
		if(user.getId() == contact.getUser().getId()) {
			System.out.println(user.getId()+' '+contact.getUser().getId());
			model.addAttribute("contact", contact);
		}
		model.addAttribute("title", "View Contact - Smart Contact Manager");
//		System.out.println(contact.isPresent());
		return "normal/single_contact";
	}
	@GetMapping("/delete/{id}")
	@Transactional
	public String deleteContact(@PathVariable int id, Principal principal,Model model,HttpSession session) {
		model.addAttribute("title", "Delete Contact - Smart Contact Manager");
		String name=principal.getName();		
		User user= userRepo.findByEmail(name);
		Optional<Contact> optionalContact=contactRepo.findById(id);
		Contact contact=optionalContact.get();
		if(user.getId() == contact.getUser().getId()) {
//			System.out.println(user.getId()+' '+contact.getUser().getId());
//			System.out.println("hi");
//			contact.setUser(null);
//			contactRepo.delete(contact);
			user.getContacts().remove(contact);
			userRepo.save(user);
//			model.addAttribute("contact", contact);
			session.setAttribute("message",new Message("contact deleted successfully....","alert-success"));

		}

		
		return "redirect:/user/show-contacts/0";
	}
	
	@PostMapping("/update-contact/{id}")
	public String  updateForm(@PathVariable int id,Model model,HttpSession session) {
		model.addAttribute("title", "Update Contact - Smart Contact Manager");
		Contact contact=contactRepo.findById(id).get();
		model.addAttribute("contact", contact);
//		session.setAttribute("message",new Message("contact updated successfully....","alert-success"));
		return "normal/update_form";
	}
	
	@PostMapping("/process-update/")
	public String processUpdate(@ModelAttribute("contact") Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session,Model model) {
		try {
			Contact existingContact = contactRepo.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				
				File oldImage =new ClassPathResource("static/img").getFile();
				File file1 = new File(oldImage,existingContact.getImage());
				file1.delete();
				
				
				File saveFile =new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator+ file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}else {
				contact.setImage(existingContact.getImage());
				
			}
			
			User user = userRepo.findByEmail(principal.getName());
			contact.setUser(user);
			contactRepo.save(contact);
			session.setAttribute("message",new Message("contact updated successfully....","alert-success"));
		}catch(Exception e) {
			e.printStackTrace();
			session.setAttribute("message",new Message("failed to update....","alert-danger"));
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model,Principal principal) {
		model.addAttribute("title", "Profile - Smart Contact Manager");
		String name=principal.getName();		
		User user= userRepo.findByEmail(name);
		model.addAttribute("user",user);
		return "normal/profile";
		
	}
}
