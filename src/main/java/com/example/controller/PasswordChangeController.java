package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.domain.model.AppUserDetails;
import com.example.domain.service.UserDetailsServiceImpl;
import com.example.form.PasswordForm;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class PasswordChangeController {

	@Autowired
	UserDetailsServiceImpl service;
	
	//パスワードの変更画面の表示
	@GetMapping("/password/change")
	public String getPasswordChange(Model model, @ModelAttribute PasswordForm form) {
		System.out.println("あああああああああ");
		return "password_change";
	}
	
	//パスワード変更
	@PostMapping("/password/change")
	public String postPasswordChange(Model model, @ModelAttribute PasswordForm form,
			@AuthenticationPrincipal AppUserDetails user) throws java.text.ParseException {
		
		service.updatePasswordDate(user.getUserId(), form.getPassword());
		
		return "home";
	}
}
