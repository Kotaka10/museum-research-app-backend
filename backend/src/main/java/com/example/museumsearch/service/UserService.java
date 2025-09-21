package com.example.museumsearch.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.museumsearch.dto.ViewedMuseumResponse;
import com.example.museumsearch.model.User;

public interface UserService {
    void registerUser(String email, String password, String displayName);
    User editUser(Long id, User user);
    void updateDisplayName(String email, String newDisplayName);
    void updateEmail(String currentEmail, String newEmail);
    void deleteUserById(Long id);
    void deleteUserByEmail(String email);
    void deleteUserByUserName(String userNmae);
    User findUserById(Long id);
    User findUserByEmail(String email);
    User findUserByUserName(String userNmae);
    boolean existsUserByEmail(String email);
    String login(String userNameOrEmail, String password);
    User getCurrentUser(String token);
    void changePassword(String email, String oldPassword, String newPassword);
    List<ViewedMuseumResponse> getViewedMuseums(String email);
    void saveViewedMuseum(String email, Long museumId);
    String uploadProfileImage(String email, MultipartFile image);
    String getProfileImageUrl(Long userId);
    List<User> getAllUsers();
    List<User> searchUsers(String keyword);
    Page<User> getUsers(Pageable pageable);
    Page<User> searchUsers(String keyword, Pageable pageable);
}
