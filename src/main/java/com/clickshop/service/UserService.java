package com.clickshop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.clickshop.entity.User;

public interface UserService {
	boolean addUserService(User user);

	boolean updateUser(int id, String oldPassword, String newPassword);

	boolean deleteUser(int uno);

	User loginUser(String uname, String password);

	User getUserByEmail(String email);
	
	User getUserById(int userId);

	public ArrayList<User> getAllUsers();

	public boolean isAdmin(int userId);

	public List<User> getAllAdmins();

	public boolean promoteToAdmin(String username);

	public boolean demoteAdmin(String userName);
	
	public boolean updateUserProfile(int userId, Map<String, Object> profileData);
	
	public void updateUserStatus(int id, User.Status status);

	public boolean existsByEmail(String email);
	public boolean existsByUsername(String username); 

}
