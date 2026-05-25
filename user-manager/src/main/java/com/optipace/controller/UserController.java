package com.optipace.controller;


import com.optipace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private  final UserService userService;



}
