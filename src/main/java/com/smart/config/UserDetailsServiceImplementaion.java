package com.smart.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.model.User;
import com.smart.repository.UserRepository;

public class UserDetailsServiceImplementaion implements UserDetailsService{
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		
		User user =  userRepository.findByEmail(username);
		if(user == null) {
			throw new UsernameNotFoundException("User not found");
		}
		
		CustomUserDetails customUser = new CustomUserDetails(user);
		
		return customUser;
	}

}
