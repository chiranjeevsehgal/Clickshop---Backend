package com.clickshop.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clickshop.entity.User;
import com.clickshop.entity.User.Role;
import com.clickshop.repository.AdminRepository;
import com.clickshop.repository.OrderRepository;
import com.clickshop.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	AdminRepository adminRepository;

	Scanner sc = new Scanner(System.in);

	@Override
	public boolean addUserService(User user) {
		// TODO Auto-generated method stub

		if (userRepository.existsById((user.getId()))) {
//			System.out.println("Employee with id " + emp.getEno() + " already exists");
			return false;
		}
		if (user.getRole() == null) {
	        user.setRole(Role.USER);  // Assign default role if not set
	    }
		User user1 = userRepository.save(user);
		if (user1 != null)
			return true;
		else {
			return false;
		}
	}
	
//	Update profile except password
	public boolean updateUserProfile(int userId, Map<String, Object> profileData) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                return false;
            }
            
            User user = userOptional.get();
            
            // Update fields if provided
            if (profileData.containsKey("name")) {
                user.setName((String) profileData.get("name"));
            }
            
            if (profileData.containsKey("phone")) {
                user.setContact((String) profileData.get("phone"));
            }
            
            if (profileData.containsKey("address")) {
                user.setAddress((String) profileData.get("address"));
            }
            
            // Email updates might need additional verification
            if (profileData.containsKey("email")) {
                String newEmail = (String) profileData.get("email");
                // Check if email is already in use by another user
                User existingUser = userRepository.findByEmail(newEmail);
                if (existingUser != null && existingUser.getId() != userId) {
                    return false; // Email already in use
                }
                user.setEmail(newEmail);
            }
            
            // Save updated user
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


	@Override
	public boolean updateUser(int userId, String oldPassword, String newPassword) {
		Optional<User> optionalUser = userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();

			// Verify the old password (redundant check, but added for safety)
			if (!user.getPassword().equals(oldPassword)) {
				throw new IllegalArgumentException("Old password is incorrect.");
			}

			// Update the password
			user.setPassword(newPassword);
			userRepository.save(user);
			return true;
		}
		return false; // User not found
	}

	@Override
	public boolean deleteUser(int uno) {
		// TODO Auto-generated method stub
		if (userRepository.existsById(uno)) {
			userRepository.deleteById(uno);
			if (userRepository.existsById(uno)) {
				System.out.println("User could not be deleted");
				return false;
			} else {
				return true;
			}
		} else {
			System.out.println("No user with user id " + uno);
			return false;
		}
	}

	@Override
	public User loginUser(String uname, String password) {
		// TODO Auto-generated method stub
		User user = userRepository.findByUname(uname);
		if (user != null && user.getPassword().equals(password)) {
			if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_ADMIN)) {
				user.setAdmin(true);
			} else {
				user.setAdmin(false);
			}
			return user;
		}
		return null;
	}

	public boolean verifyOldPassword(int userId, String oldPassword) {
		Optional<User> optionalUser = userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			// Compare the old password with the stored password
			return user.getPassword().equals(oldPassword);
		}
		return false;
	}

	@Override
	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	@Override
	public User getUserById(int userId) {
	        Optional<User> userOptional = userRepository.findById(userId);
	        return userOptional.orElse(null);
	    }

	@Override
	public ArrayList<User> getAllUsers() {
		// TODO Auto-generated method stub
		ArrayList<User> userList = new ArrayList<User>();
		userRepository.findAll().forEach(user -> userList.add(user));
		return userList;
	}

	@Override
	public boolean isAdmin(int userId) {
		// TODO Auto-generated method stub
		return adminRepository.isAdmin(userId);
	}
	
	@Override
	public void updateUserStatus(int id, User.Status status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
    }

	public List<User> getAllAdmins() {
		List<User> admins = userRepository.findByRoleIn(Arrays.asList(User.Role.ADMIN, User.Role.SUPER_ADMIN));
		return admins;
	}

	@Transactional
	public boolean promoteToAdmin(String username) {
		User user = userRepository.findByUname(username);
		if (user != null) {
			user.setRole(User.Role.ADMIN); // Setting the new role
			userRepository.save(user);
			return true;
		}
		return false;
	}

	public boolean demoteAdmin(String userName) {
		User user = userRepository.findByUname(userName);
		if (user != null && user.getRole().equals(User.Role.ADMIN)) {
			user.setRole(User.Role.USER);
			userRepository.save(user);
			return true;
		}
		return false; // If user is already normal or not found
	}

}