package com.clickshop.utils;


import com.clickshop.entity.User.Role;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
	
	public static boolean isValidSession(HttpSession session) {
        Role role = (Role) session.getAttribute("role");
        return role != null; // returns true if the session is valid
    }

}
